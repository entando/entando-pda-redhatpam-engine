package org.entando.plugins.pda.pam.service.task;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.entando.keycloak.security.AuthenticatedUser;
import org.entando.plugins.pda.core.engine.Connection;
import org.entando.plugins.pda.core.exception.TaskNotFoundException;
import org.entando.plugins.pda.core.model.Task;
import org.entando.plugins.pda.core.service.task.TaskService;
import org.entando.plugins.pda.pam.service.KieUtils;
import org.entando.plugins.pda.pam.service.process.model.KieInstanceId;
import org.entando.plugins.pda.pam.service.task.model.KieProcessVariable;
import org.entando.plugins.pda.pam.service.task.model.KieProcessVariablesResponse;
import org.entando.plugins.pda.pam.service.task.model.KieTask;
import org.entando.plugins.pda.pam.service.task.model.KieTaskDetails;
import org.entando.plugins.pda.pam.service.task.model.KieTasksResponse;
import org.entando.plugins.pda.pam.service.task.util.TaskUtil;
import org.entando.web.exception.BadResponseException;
import org.entando.web.exception.InternalServerException;
import org.entando.web.request.PagedListRequest;
import org.entando.web.response.PagedMetadata;
import org.entando.web.response.PagedRestResponse;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class KieTaskService implements TaskService {

    public static final int LAST_PAGE_TRUE = 1;
    public static final int LAST_PAGE_FALSE = 0;
    public static final int SIMPLE_NAVIGATION = -1;

    private final RestTemplateBuilder restTemplateBuilder;

    //CHECKSTYLE:OFF
    public static final String TASK_LIST_URL = "/queries/tasks/instances/pot-owners";
    public static final String TASK_URL = "/queries/tasks/instances/{tInstanceId}";
    public static final String TASK_DETAILS_URL = "/containers/{containerId}/tasks/{tInstanceId}?withInputData=true&withOutputData=true";
    public static final String PROCESS_VARIABLES_URL = "/queries/processes/instances/{pInstanceId}/variables/instances";
    //CHECKSTYLE:ON

    @Override
    public PagedRestResponse<Task> list(Connection connection, AuthenticatedUser user, PagedListRequest request) {
        RestTemplate restTemplate = restTemplateBuilder
                .basicAuthorization(connection.getUsername(), connection.getPassword())
                .build();

        final Map<String, List<KieProcessVariable>> cachedVariables = new ConcurrentHashMap<>();

        List<Task> result = getTasks(restTemplate, connection, user, request).stream() //Get Tasks
                .map(t -> { //Get Process Instance Variables
                    t.putAll(getProcessVariables(cachedVariables, restTemplate, connection,
                            t.getProcessInstanceId())
                            .stream().filter(e -> e.getValue() != null)
                            .map(v -> new AbstractMap.SimpleEntry<>(v.getName(), v.getValue()))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

                    return t;
                })
                .collect(Collectors.toList());

        result.stream().parallel()
                .forEach(t -> { //Get Task Details
                    t.putAll(getTaskDetails(restTemplate, connection, t.getContainerId(), t.getId())
                            .getData().entrySet().stream().filter(e -> e.getValue() != null)
                            .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue()))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                });

        TaskUtil.flatProperties(result);

        return getTaskPagedRestResponse(connection, user, request, restTemplate, result);
    }

    private PagedRestResponse<Task> getTaskPagedRestResponse(Connection connection, AuthenticatedUser user,
            PagedListRequest request, RestTemplate restTemplate, List<Task> result) {
        PagedListRequest nextPageRequest = new PagedListRequest(request.getPage() + 1, request.getPageSize(),
                request.getSort(), request.getDirection());
        List<KieTask> tasks = getTasks(restTemplate, connection, user, nextPageRequest);
        PagedMetadata<Task> pagedMetadata = new PagedMetadata<>(request.getPage(), request.getPageSize(),
                tasks.isEmpty() ? LAST_PAGE_TRUE : LAST_PAGE_FALSE, SIMPLE_NAVIGATION);
        pagedMetadata.setBody(result);
        return pagedMetadata.toRestResponse();
    }

    @Override
    public Task get(Connection connection, AuthenticatedUser user, String id) {
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

        TaskUtil.flatProperties(task);
        return task;
    }

    private List<KieTask> getTasks(RestTemplate restTemplate, Connection connection, AuthenticatedUser user,
            PagedListRequest request) {
        String url = connection.getUrl() + TASK_LIST_URL + KieUtils.createUserFilter(connection, user)
                + KieUtils.createFilters(request);

        KieTasksResponse response = Optional.ofNullable(restTemplate.getForObject(url, KieTasksResponse.class))
                .orElseThrow(BadResponseException::new);

        return Optional.ofNullable(response.getTasks())
                .orElse(Collections.emptyList());
    }

    private KieTask getTask(RestTemplate restTemplate, Connection connection, String id) {
        KieInstanceId taskId = new KieInstanceId(id);
        String url = connection.getUrl() + TASK_URL
                .replace("{tInstanceId}", taskId.getInstanceId().toString());

        try {
            return Optional.ofNullable(restTemplate.getForObject(url, KieTask.class))
                    .orElseThrow(BadResponseException::new);
        } catch (HttpClientErrorException e) {
            if (HttpStatus.NOT_FOUND.equals(e.getStatusCode())) {
                throw new TaskNotFoundException(e);
            }

            throw new InternalServerException(e.getMessage(), e);
        }
    }

    private List<KieProcessVariable> getProcessVariables(RestTemplate restTemplate, Connection connection,
            String processInstanceId) {
        return getProcessVariables(new HashMap<>(), restTemplate, connection, processInstanceId);
    }

    private List<KieProcessVariable> getProcessVariables(Map<String, List<KieProcessVariable>> cachedVariables,
            RestTemplate restTemplate, Connection connection, String processInstanceId) {

        if (cachedVariables.containsKey(processInstanceId)) {
            return cachedVariables.get(processInstanceId);
        } else {
            String url = connection.getUrl() + PROCESS_VARIABLES_URL
                    .replace("{pInstanceId}", processInstanceId);

            List<KieProcessVariable> processVariables = Optional.ofNullable(restTemplate.getForObject(url,
                    KieProcessVariablesResponse.class, processInstanceId))
                    .orElseThrow(BadResponseException::new)
                    .getVariables();

            cachedVariables.put(processInstanceId, processVariables);
            return processVariables;
        }
    }

    private KieTaskDetails getTaskDetails(RestTemplate restTemplate, Connection connection,
            String containerId, String taskInstanceId) {
        try {
            String url = connection.getUrl() + TASK_DETAILS_URL
                    .replace("{containerId}", containerId)
                    .replace("{tInstanceId}", taskInstanceId);

            return Optional.ofNullable(restTemplate.getForObject(url, KieTaskDetails.class, containerId,
                    taskInstanceId))
                    .orElseThrow(BadResponseException::new);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().is5xxServerError()) {
                log.warn("Error retrieving TaskDetails, silently skipping: container={}, task={}",
                        containerId, taskInstanceId);
                return new KieTaskDetails();
            }

            log.error("Error retrieving TaskDetails: container={}, task={}", containerId, taskInstanceId);
            throw e;
        }
    }
}
