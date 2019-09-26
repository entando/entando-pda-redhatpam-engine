package org.entando.plugins.pda.pam.service.task;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.entando.plugins.pda.core.engine.Connection;
import org.entando.plugins.pda.core.model.Task;
import org.entando.plugins.pda.core.service.task.TaskService;
import org.entando.plugins.pda.pam.service.task.model.KieProcessVariable;
import org.entando.plugins.pda.pam.service.task.model.KieProcessVariablesResponse;
import org.entando.plugins.pda.pam.service.task.model.KieTask;
import org.entando.plugins.pda.pam.service.task.model.KieTaskDetails;
import org.entando.plugins.pda.pam.service.task.model.KieTasksResponse;
import org.entando.web.exception.BadResponseException;
import org.entando.web.request.Filter;
import org.entando.web.request.PagedListRequest;
import org.entando.web.response.PagedMetadata;
import org.entando.web.response.PagedRestResponse;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class KieTaskService implements TaskService {

    @NonNull
    private RestTemplateBuilder restTemplateBuilder;

    //CHECKSTYLE:OFF
    public static final String TASK_LIST_URL = "/server/queries/tasks/instances/pot-owners";
    public static final String TASK_URL = "/server/queries/tasks/instances/{tInstanceId}";
    public static final String TASK_DETAILS_URL = "/server/containers/{containerId}/tasks/{tInstanceId}?withInputData=true&withOutputData=true";
    public static final String PROCESS_VARIABLES_URL = "/server/queries/processes/instances/{pInstanceId}/variables/instances";
    //CHECKSTYLE:ON

    @Override
    public PagedRestResponse<Task> list(Connection connection, PagedListRequest request) {
        RestTemplate restTemplate = restTemplateBuilder
                .basicAuthorization(connection.getUsername(), connection.getPassword())
                .build();

        final Map<Integer, List<KieProcessVariable>> cachedVariables = new ConcurrentHashMap<>();

        List<Task> result = getTasks(restTemplate, connection, request).stream() //Get Tasks
                .parallel()
                .peek(t -> { //Get Task Details
                    t.putAll(getTaskDetails(restTemplate, connection, t.getContainerId(), t.getId())
                            .getData().entrySet().stream().filter(e -> e.getValue() != null)
                            .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue()))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                })
                .peek(t -> { //Get Process Instance Variables
                    t.putAll(getProcessVariables(cachedVariables, restTemplate, connection, t.getProcessInstanceId())
                            .stream().filter(e -> e.getValue() != null)
                            .map(v -> new AbstractMap.SimpleEntry<>(v.getName(), v.getValue()))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                })
                .collect(Collectors.toList());

        return new PagedMetadata<>(request, result).toRestResponse(); //TODO how to efficiently query total tasks size?
    }

    @Override
    public Task get(Connection connection, String id) {
        RestTemplate restTemplate = restTemplateBuilder
                .basicAuthorization(connection.getUsername(), connection.getPassword())
                .build();

        //Get Task
        KieTask task = getTask(restTemplate, connection, id);

        // Get TaskDetails
        task.putAll(getTaskDetails(restTemplate, connection, task.getContainerId(), task.getId())
                .getData().entrySet().stream().filter(e -> e.getValue() != null)
                .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

        // Get ProcessInstanceVariables
        task.putAll(getProcessVariables(restTemplate, connection, task.getProcessInstanceId())
                .stream().filter(e -> e.getValue() != null)
                .map(v -> new AbstractMap.SimpleEntry<>(v.getName(), v.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

        return task;
    }

    private List<KieTask> getTasks(RestTemplate restTemplate, Connection connection, PagedListRequest request) {
        KieTasksResponse response = Optional.ofNullable(restTemplate.getForObject(
                connection.getUrl() + TASK_LIST_URL + createFilters(request), KieTasksResponse.class))
                .orElseThrow(BadResponseException::new);

        return Optional.ofNullable(response.getTasks())
                .orElse(Collections.emptyList());
    }

    private KieTask getTask(RestTemplate restTemplate, Connection connection, String id) {
        return Optional.ofNullable(restTemplate.getForObject(
                connection.getUrl() + TASK_URL.replace("{tInstanceId}", id), KieTask.class))
                .orElseThrow(BadResponseException::new);
    }

    private List<KieProcessVariable> getProcessVariables(RestTemplate restTemplate, Connection connection,
            Integer processInstanceId) {
        return getProcessVariables(new HashMap<>(), restTemplate, connection, processInstanceId);
    }

    private List<KieProcessVariable> getProcessVariables(Map<Integer,List<KieProcessVariable>> cachedVariables,
            RestTemplate restTemplate, Connection connection, Integer processInstanceId) {

        if (cachedVariables.containsKey(processInstanceId)) {
            return cachedVariables.get(processInstanceId);
        } else {
            List<KieProcessVariable> processVariables = Optional.ofNullable(restTemplate.getForObject(
                    connection.getUrl() + PROCESS_VARIABLES_URL, KieProcessVariablesResponse.class, processInstanceId))
                    .orElseThrow(BadResponseException::new)
                    .getVariables();

            cachedVariables.put(processInstanceId, processVariables);
            return processVariables;
        }
    }

    private KieTaskDetails getTaskDetails(RestTemplate restTemplate, Connection connection, String containerId,
            Integer taskInstanceId) {

        try {
            return Optional.ofNullable(restTemplate.getForObject(
                    connection.getUrl() + TASK_DETAILS_URL, KieTaskDetails.class, containerId, taskInstanceId))
                    .orElseThrow(BadResponseException::new);
        } catch (HttpServerErrorException e) {
            if (e.getStatusCode().is5xxServerError()) {
                log.warn("Error retrieving TaskDetails, silently skipping: container={}, task={}",
                        containerId, taskInstanceId);
                return new KieTaskDetails();
            }

            log.error("Error retrieving TaskDetails: container={}, task={}", containerId, taskInstanceId);
            throw e;
        }
    }

    private String createFilters(PagedListRequest request) {
        StringBuilder queryUrl = new StringBuilder();

        queryUrl.append(String.format("?page=%d&pageSize=%d",
                request.getPage() - 1, request.getPageSize()));

        if (request.getSort() != null) {
            queryUrl.append(String.format("&sort=%s&sortOrder=%s",
                    request.getSort(), request.getDirection().equals(Filter.ASC_ORDER)));
        }

        return queryUrl.toString();
    }

}
