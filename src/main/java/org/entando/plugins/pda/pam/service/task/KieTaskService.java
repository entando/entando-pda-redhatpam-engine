package org.entando.plugins.pda.pam.service.task;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.entando.plugins.pda.core.engine.Connection;
import org.entando.plugins.pda.core.model.Task;
import org.entando.plugins.pda.core.service.task.TaskService;
import org.entando.plugins.pda.pam.service.task.model.KieProcessVariablesResponse;
import org.entando.plugins.pda.pam.service.task.model.KieTask;
import org.entando.plugins.pda.pam.service.task.model.KieTasksResponse;
import org.entando.web.request.PagedListRequest;
import org.entando.web.response.PagedMetadata;
import org.entando.web.response.PagedRestResponse;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KieTaskService implements TaskService {

    @NonNull
    RestTemplateBuilder restTemplateBuilder;

    public static final String TASK_LIST_URL = "/services/rest/server/queries/tasks/instances/pot-owners";
    public static final String TASK_PROPERTIES_URL = "/services/rest/server/queries/processes/instances/{pInstanceId}/variables/instances";

    public PagedRestResponse<Task> list(Connection connection, PagedListRequest request) {
        RestTemplate restTemplate = restTemplateBuilder
                .basicAuthorization(connection.getUsername(), connection.getPassword())
                .build();

        KieTasksResponse response = restTemplate.getForObject(
                connection.getUrl() + TASK_LIST_URL, KieTasksResponse.class);

        List<Task> result = Optional.ofNullable(response.getTasks())
                .orElse(Collections.emptyList())
                .stream()
                .map(this::fromDto)
                .peek(t -> {
                    KieProcessVariablesResponse variablesResponse = restTemplate.getForObject(
                            connection.getUrl() + TASK_PROPERTIES_URL,
                            KieProcessVariablesResponse.class, t.getProcessInstanceId());

                    t.setProperties(Optional.ofNullable(variablesResponse.getVariables())
                                        .orElse(Collections.emptyList())
                            .stream().map(v -> new AbstractMap.SimpleEntry<>(v.getName(), v.getValue()))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                })
                .collect(Collectors.toList());

        return new PagedMetadata<>(request, result).toRestResponse();
    }

    private Task fromDto(KieTask dto) {
        return Task.builder()
                .id(String.valueOf(dto.getTaskId()))
                .name(dto.getTaskName())
                .description(dto.getTaskDescription())
                .priority(dto.getTaskPriority())
                .processId(dto.getTaskProcDefId())
                .processInstanceId(dto.getTaskProcInstId())
                .skipable(dto.getTaskIsSkipable())
                .status(dto.getTaskStatus())
                .subject(dto.getTaskSubject())
                .build();
    }

}
