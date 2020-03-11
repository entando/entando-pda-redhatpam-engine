package org.entando.plugins.pda.pam.service.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.entando.keycloak.security.AuthenticatedUser;
import org.entando.plugins.pda.core.engine.Connection;
import org.entando.plugins.pda.core.service.task.TaskLifecycleBulkService;
import org.entando.plugins.pda.core.service.task.response.TaskBulkActionResponse;
import org.entando.plugins.pda.pam.service.api.KieApiService;
import org.entando.plugins.pda.pam.service.task.util.TaskExceptionUtil;
import org.entando.plugins.pda.pam.service.util.KieInstanceId;
import org.kie.server.api.exception.KieServicesHttpException;
import org.kie.server.client.UserTaskServicesClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KieTaskLifecycleBulkService implements TaskLifecycleBulkService {

    private static final String EXCEPTION_MESSAGE = "Exception on bulk operation. Task id {}";

    private final KieApiService kieApiService;

    @Override
    public List<TaskBulkActionResponse> bulkClaim(Connection connection, AuthenticatedUser user, List<String> ids) {
        UserTaskServicesClient taskServicesClient = kieApiService.getUserTaskServicesClient(connection);
        String username = user == null ? connection.getUsername() : user.getAccessToken().getPreferredUsername();
        List<TaskBulkActionResponse> result = new ArrayList<>();
        ids.forEach(id -> {
            try {
                KieInstanceId taskId = new KieInstanceId(id);
                taskServicesClient.claimTask(taskId.getContainerId(), taskId.getInstanceId(), username);
                result.add(getTaskBulkActionSuccess(id));
            } catch (KieServicesHttpException e) {
                if (e.getHttpCode() == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                    log.error(EXCEPTION_MESSAGE, id, e);
                } else {
                    log.debug(EXCEPTION_MESSAGE, id, e);
                }
                result.add(TaskExceptionUtil.convertToTaskBulkActionResponse(e, id));
            }
        });
        return result;
    }

    @Override
    public List<TaskBulkActionResponse> bulkUnclaim(Connection connection, AuthenticatedUser user, List<String> ids) {
        UserTaskServicesClient taskServicesClient = kieApiService.getUserTaskServicesClient(connection);
        String username = user == null ? connection.getUsername() : user.getAccessToken().getPreferredUsername();
        List<TaskBulkActionResponse> result = new ArrayList<>();
        ids.forEach(id -> {
            try {
                KieInstanceId taskId = new KieInstanceId(id);
                taskServicesClient.releaseTask(taskId.getContainerId(), taskId.getInstanceId(), username);
                result.add(getTaskBulkActionSuccess(id));
            } catch (KieServicesHttpException e) {
                if (e.getHttpCode() == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                    log.error(EXCEPTION_MESSAGE, id, e);
                } else {
                    log.debug(EXCEPTION_MESSAGE, id, e);
                }
                result.add(TaskExceptionUtil.convertToTaskBulkActionResponse(e, id));
            }
        });
        return result;
    }

    @Override
    public List<TaskBulkActionResponse> bulkAssign(Connection connection, AuthenticatedUser user, List<String> ids,
            String assignee) {
        UserTaskServicesClient taskServicesClient = kieApiService.getUserTaskServicesClient(connection);
        String username = user == null ? connection.getUsername() : user.getAccessToken().getPreferredUsername();
        List<TaskBulkActionResponse> result = new ArrayList<>();
        ids.forEach(id -> {
            try {
                KieInstanceId taskId = new KieInstanceId(id);
                taskServicesClient.forwardTask(taskId.getContainerId(), taskId.getInstanceId(), username, assignee);
                result.add(getTaskBulkActionSuccess(id));
            } catch (KieServicesHttpException e) {
                if (e.getHttpCode() == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                    log.error(EXCEPTION_MESSAGE, id, e);
                } else {
                    log.debug(EXCEPTION_MESSAGE, id, e);
                }
                result.add(TaskExceptionUtil.convertToTaskBulkActionResponse(e, id));
            }
        });
        return result;
    }

    @Override
    public List<TaskBulkActionResponse> bulkStart(Connection connection, AuthenticatedUser user, List<String> ids) {
        UserTaskServicesClient taskServicesClient = kieApiService.getUserTaskServicesClient(connection);
        String username = user == null ? connection.getUsername() : user.getAccessToken().getPreferredUsername();
        List<TaskBulkActionResponse> result = new ArrayList<>();
        ids.forEach(id -> {
            try {
                KieInstanceId taskId = new KieInstanceId(id);
                taskServicesClient.startTask(taskId.getContainerId(), taskId.getInstanceId(), username);
                result.add(getTaskBulkActionSuccess(id));
            } catch (KieServicesHttpException e) {
                if (e.getHttpCode() == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                    log.error(EXCEPTION_MESSAGE, id, e);
                } else {
                    log.debug(EXCEPTION_MESSAGE, id, e);
                }
                result.add(TaskExceptionUtil.convertToTaskBulkActionResponse(e, id));
            }
        });
        return result;
    }

    @Override
    public List<TaskBulkActionResponse> bulkPause(Connection connection, AuthenticatedUser user, List<String> ids) {
        UserTaskServicesClient taskServicesClient = kieApiService.getUserTaskServicesClient(connection);
        String username = user == null ? connection.getUsername() : user.getAccessToken().getPreferredUsername();
        List<TaskBulkActionResponse> result = new ArrayList<>();
        ids.forEach(id -> {
            try {
                KieInstanceId taskId = new KieInstanceId(id);
                taskServicesClient.stopTask(taskId.getContainerId(), taskId.getInstanceId(), username);
                result.add(getTaskBulkActionSuccess(id));
            } catch (KieServicesHttpException e) {
                if (e.getHttpCode() == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                    log.error(EXCEPTION_MESSAGE, id, e);
                } else {
                    log.debug(EXCEPTION_MESSAGE, id, e);
                }
                result.add(TaskExceptionUtil.convertToTaskBulkActionResponse(e, id));
            }
        });
        return result;
    }

    @Override
    public List<TaskBulkActionResponse> bulkComplete(Connection connection, AuthenticatedUser user, List<String> ids) {
        UserTaskServicesClient taskServicesClient = kieApiService.getUserTaskServicesClient(connection);
        String username = user == null ? connection.getUsername() : user.getAccessToken().getPreferredUsername();
        List<TaskBulkActionResponse> result = new ArrayList<>();
        ids.forEach(id -> {
            try {
                KieInstanceId taskId = new KieInstanceId(id);
                taskServicesClient.completeTask(taskId.getContainerId(), taskId.getInstanceId(), username,
                        Collections.emptyMap());
                result.add(getTaskBulkActionSuccess(id));
            } catch (KieServicesHttpException e) {
                if (e.getHttpCode() == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                    log.error(EXCEPTION_MESSAGE, id, e);
                } else {
                    log.debug(EXCEPTION_MESSAGE, id, e);
                }
                result.add(TaskExceptionUtil.convertToTaskBulkActionResponse(e, id));
            }
        });
        return result;
    }

    private TaskBulkActionResponse getTaskBulkActionSuccess(String id) {
        return TaskBulkActionResponse.builder()
                .id(id)
                .statusCode(HttpStatus.OK.value())
                .build();
    }
}
