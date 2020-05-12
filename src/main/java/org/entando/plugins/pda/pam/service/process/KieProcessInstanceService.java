package org.entando.plugins.pda.pam.service.process;

import java.time.LocalDateTime;
import java.time.ZoneId;
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

    private final KieApiService kieApiService;

    @Override
    public List<ProcessInstance> list(Connection connection, String processDefinitionId,
            AuthenticatedUser user) {
        String username = user == null ? connection.getUsername() : user.getAccessToken().getPreferredUsername();

        QueryServicesClient queryServicesClient = kieApiService.getQueryServicesClient(connection);
        UserTaskServicesClient userTaskServicesClient = kieApiService.getUserTaskServicesClient(connection);
        return queryServicesClient
                .findProcessInstancesByProcessIdAndInitiator(processDefinitionId, username, null, 0, -1)
                .stream()
                .map((org.kie.server.api.model.instance.ProcessInstance pi) -> toProcessInstance(userTaskServicesClient,
                        pi))
                .collect(Collectors.toList());
    }

    private ProcessInstance toProcessInstance(UserTaskServicesClient userTaskServicesClient,
            org.kie.server.api.model.instance.ProcessInstance processInstance) {
        List<TaskSummary> taskSummaries = userTaskServicesClient
                .findTasksByStatusByProcessInstanceId(processInstance.getId(), null, 0, -1);
        LocalDateTime date = processInstance.getDate() == null ? null
                : processInstance.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        return ProcessInstance.builder()
                .id(String.valueOf(processInstance.getId()))
                .date(date)
                .processDefinitionId(processInstance.getProcessId())
                .initiator(processInstance.getInitiator())
                .processName(processInstance.getProcessName())
                .processVersion(processInstance.getProcessVersion())
                .state(String.valueOf(processInstance.getState()))
                .activeUserTasks(taskSummaries == null ? Collections.emptyList()
                        : taskSummaries.stream().map(TaskSummary::getName).collect(Collectors.toList()))
                .build();
    }
}
