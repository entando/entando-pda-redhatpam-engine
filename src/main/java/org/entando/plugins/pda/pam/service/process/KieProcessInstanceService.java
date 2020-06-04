package org.entando.plugins.pda.pam.service.process;

import static org.entando.plugins.pda.pam.service.process.KieProcessFormService.INITIATOR_VAR;
import static org.entando.plugins.pda.pam.service.task.model.KieTask.KIE_STATUS_COMPLETED;
import static org.entando.plugins.pda.pam.service.task.model.KieTask.KIE_STATUS_CREATED;
import static org.entando.plugins.pda.pam.service.task.model.KieTask.KIE_STATUS_IN_PROGRESS;
import static org.entando.plugins.pda.pam.service.task.model.KieTask.KIE_STATUS_READY;
import static org.entando.plugins.pda.pam.service.task.model.KieTask.KIE_STATUS_RESERVED;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.entando.keycloak.security.AuthenticatedUser;
import org.entando.plugins.pda.core.engine.Connection;
import org.entando.plugins.pda.core.model.ProcessInstance;
import org.entando.plugins.pda.core.service.process.ProcessInstanceService;
import org.entando.plugins.pda.pam.service.api.KieApiService;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.client.UserTaskServicesClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KieProcessInstanceService implements ProcessInstanceService {

    public static final int ALL_ITEMS = -1;
    public static final List<String> ACTIVE_STATUSES = Collections.unmodifiableList(
            Arrays.asList(KIE_STATUS_CREATED, KIE_STATUS_READY, KIE_STATUS_RESERVED, KIE_STATUS_IN_PROGRESS,
                    KIE_STATUS_COMPLETED));
    public static final int PROCESS_INSTANCE_ACTIVE = 1;
    public static final int PROCESS_INSTANCE_COMPLETED = 2;

    private final KieApiService kieApiService;

    @Override
    public List<ProcessInstance> list(Connection connection, String processDefinitionId,
            AuthenticatedUser user) {
        String username = user == null ? connection.getUsername() : user.getAccessToken().getPreferredUsername();

        QueryServicesClient queryServicesClient = kieApiService.getQueryServicesClient(connection);
        UserTaskServicesClient userTaskServicesClient = kieApiService.getUserTaskServicesClient(connection);
        return queryServicesClient.findProcessInstancesByVariableAndValue(INITIATOR_VAR, username,
                Arrays.asList(PROCESS_INSTANCE_ACTIVE, PROCESS_INSTANCE_COMPLETED), 0, ALL_ITEMS)
                .stream()
                .filter(e -> e.getProcessId().equals(processDefinitionId))
                .map((org.kie.server.api.model.instance.ProcessInstance pi) -> toProcessInstance(username,
                        userTaskServicesClient, pi))
                .collect(Collectors.toList());
    }

    private ProcessInstance toProcessInstance(String initiator, UserTaskServicesClient userTaskServicesClient,
            org.kie.server.api.model.instance.ProcessInstance processInstance) {
        List<TaskSummary> taskSummaries = userTaskServicesClient
                .findTasksByStatusByProcessInstanceId(processInstance.getId(), ACTIVE_STATUSES, 0, ALL_ITEMS);
        LocalDateTime date = processInstance.getDate() == null ? null
                : processInstance.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        return ProcessInstance.builder()
                .id(String.valueOf(processInstance.getId()))
                .date(date)
                .processDefinitionId(processInstance.getProcessId())
                .initiator(initiator)
                .processName(processInstance.getProcessName())
                .processVersion(processInstance.getProcessVersion())
                .state(String.valueOf(processInstance.getState()))
                .userTasks(taskSummaries == null ? Collections.emptyList()
                        : taskSummaries.stream().map(TaskSummary::getName).collect(Collectors.toList()))
                .build();
    }
}
