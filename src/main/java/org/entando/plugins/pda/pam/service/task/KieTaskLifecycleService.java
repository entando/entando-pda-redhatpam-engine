package org.entando.plugins.pda.pam.service.task;

import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.entando.keycloak.security.AuthenticatedUser;
import org.entando.plugins.pda.core.engine.Connection;
import org.entando.plugins.pda.core.exception.TaskNotFoundException;
import org.entando.plugins.pda.core.model.Task;
import org.entando.plugins.pda.core.service.task.TaskLifecycleService;
import org.entando.plugins.pda.pam.exception.KieInvalidResponseException;
import org.entando.plugins.pda.pam.service.api.KieApiService;
import org.entando.plugins.pda.pam.service.util.KieInstanceId;
import org.kie.server.api.exception.KieServicesHttpException;
import org.kie.server.client.UserTaskServicesClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KieTaskLifecycleService implements TaskLifecycleService {

    private final KieApiService kieApiService;

    @Override
    public Task claim(Connection connection, AuthenticatedUser user, String id) {
        try {
            UserTaskServicesClient taskServicesClient = kieApiService.getUserTaskServicesClient(connection);
            KieInstanceId taskId = new KieInstanceId(id);

            String username = user == null ? connection.getUsername() : user.getAccessToken().getPreferredUsername();
            taskServicesClient.claimTask(taskId.getContainerId(), taskId.getInstanceId(), username);
            Task result = new Task();
            result.setId(taskId.toString());
            return result;
        } catch (KieServicesHttpException e) {
            if (HttpStatus.NOT_FOUND.value() == e.getHttpCode()) {
                throw new TaskNotFoundException(e);
            }
            throw new KieInvalidResponseException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @Override
    public Task unclaim(Connection connection, AuthenticatedUser user, String id) {
        UserTaskServicesClient taskServicesClient = kieApiService.getUserTaskServicesClient(connection);
        KieInstanceId taskId = new KieInstanceId(id);

        String username = user == null ? connection.getUsername() : user.getAccessToken().getPreferredUsername();
        taskServicesClient.releaseTask(taskId.getContainerId(), taskId.getInstanceId(), username);
        Task result = new Task();
        result.setId(taskId.toString());
        return result;
    }

    @Override
    public Task assign(Connection connection, AuthenticatedUser user, String id, String assignee) {
        UserTaskServicesClient taskServicesClient = kieApiService.getUserTaskServicesClient(connection);
        KieInstanceId taskId = new KieInstanceId(id);

        String username = user == null ? connection.getUsername() : user.getAccessToken().getPreferredUsername();
        taskServicesClient.nominateTask(taskId.getContainerId(), taskId.getInstanceId(), username,
                Collections.singletonList(assignee));
        Task result = new Task();
        result.setId(taskId.toString());
        return result;
    }

    @Override
    public Task start(Connection connection, AuthenticatedUser user, String id) {
        UserTaskServicesClient taskServicesClient = kieApiService.getUserTaskServicesClient(connection);
        KieInstanceId taskId = new KieInstanceId(id);

        String username = user == null ? connection.getUsername() : user.getAccessToken().getPreferredUsername();
        taskServicesClient.startTask(taskId.getContainerId(), taskId.getInstanceId(), username);
        Task result = new Task();
        result.setId(taskId.toString());
        return result;
    }

    @Override
    public Task pause(Connection connection, AuthenticatedUser user, String id) {
        UserTaskServicesClient taskServicesClient = kieApiService.getUserTaskServicesClient(connection);
        KieInstanceId taskId = new KieInstanceId(id);

        String username = user == null ? connection.getUsername() : user.getAccessToken().getPreferredUsername();
        taskServicesClient.suspendTask(taskId.getContainerId(), taskId.getInstanceId(), username);
        Task result = new Task();
        result.setId(taskId.toString());
        return result;
    }

    @Override
    public Task complete(Connection connection, AuthenticatedUser user, String id) {
        UserTaskServicesClient taskServicesClient = kieApiService.getUserTaskServicesClient(connection);
        KieInstanceId taskId = new KieInstanceId(id);

        String username = user == null ? connection.getUsername() : user.getAccessToken().getPreferredUsername();
        taskServicesClient.completeTask(taskId.getContainerId(), taskId.getInstanceId(), username, null);
        Task result = new Task();
        result.setId(taskId.toString());
        return result;
    }
}
