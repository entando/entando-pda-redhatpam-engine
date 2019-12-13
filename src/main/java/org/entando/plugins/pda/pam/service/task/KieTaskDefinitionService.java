package org.entando.plugins.pda.pam.service.task;

import java.util.Collections;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.entando.keycloak.security.AuthenticatedUser;
import org.entando.plugins.pda.core.engine.Connection;
import org.entando.plugins.pda.core.model.Task;
import org.entando.plugins.pda.core.service.task.TaskDefinitionService;
import org.entando.web.request.PagedListRequest;
import org.entando.web.response.PagedRestResponse;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KieTaskDefinitionService implements TaskDefinitionService {

    private final KieTaskService taskService;

    @Override
    public Set<String> listColumns(Connection connection, AuthenticatedUser user) {
        PagedListRequest pagedRequest = new PagedListRequest(1, 1, "taskId", PagedListRequest.DIRECTION_VALUE_DEFAULT);
        PagedRestResponse<Task> list = taskService.list(connection, user, pagedRequest);
        if (list.getPayload().isEmpty()) {
            return Collections.emptySet();
        }
        return list.getPayload().get(0).getData().keySet();
    }
}
