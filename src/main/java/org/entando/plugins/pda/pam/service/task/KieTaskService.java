package org.entando.plugins.pda.pam.service.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.entando.keycloak.security.AuthenticatedUser;
import org.entando.plugins.pda.core.engine.Connection;
import org.entando.plugins.pda.core.exception.TaskNotFoundException;
import org.entando.plugins.pda.core.model.Task;
import org.entando.plugins.pda.core.service.task.TaskService;
import org.entando.plugins.pda.pam.exception.KieInvalidPageStart;
import org.entando.plugins.pda.pam.exception.KieInvalidResponseException;
import org.entando.plugins.pda.pam.service.api.KieApiService;
import org.entando.plugins.pda.pam.service.task.model.KieTask;
import org.entando.plugins.pda.pam.service.task.model.KieTaskDetails;
import org.entando.plugins.pda.pam.service.util.KieInstanceId;
import org.entando.web.request.Filter;
import org.entando.web.request.PagedListRequest;
import org.entando.web.response.PagedMetadata;
import org.entando.web.response.PagedRestResponse;
import org.kie.server.api.exception.KieServicesHttpException;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.client.UserTaskServicesClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KieTaskService implements TaskService {

    public static final int PAGE_START = 1;
    public static final int LAST_PAGE_TRUE = 1;
    public static final int LAST_PAGE_FALSE = 0;
    public static final int SIMPLE_NAVIGATION = -1;

    private final KieApiService kieApiService;

    @Override
    public PagedRestResponse<Task> list(Connection connection, AuthenticatedUser user, PagedListRequest request,
            String filter) {
        UserTaskServicesClient client = kieApiService.getUserTaskServicesClient(connection);

        List<Task> result = queryTasks(client, connection, user, request, filter);
        return createPagedResponse(client, connection, user, request, result);
    }

    private List<Task> queryTasks(UserTaskServicesClient client, Connection connection, AuthenticatedUser user,
            PagedListRequest request, String filter) {

        if (request.getPage() < PAGE_START) {
            throw new KieInvalidPageStart();
        }

        String searchFilter = filter == null ? "" : filter.replace("*", "%");

        String username = user == null ? connection.getUsername() : user.getAccessToken().getPreferredUsername();
        return client.findTasksAssignedAsPotentialOwner(username, searchFilter, new ArrayList<>(),
                request.getPage() - 1, request.getPageSize(),
                convertSortProperty(request.getSort()), !request.getDirection().equals(Filter.DESC_ORDER))
                .stream()
                .map(KieTask::from)
                .collect(Collectors.toList());
    }

    private PagedRestResponse<Task> createPagedResponse(UserTaskServicesClient client, Connection connection,
            AuthenticatedUser user, PagedListRequest request, List<Task> result) {

        PagedListRequest nextPageRequest = new PagedListRequest(request.getPage() + 1, request.getPageSize(),
                request.getSort(), request.getDirection());

        int lastPage = LAST_PAGE_FALSE;
        if (result.size() != request.getPageSize()
                || queryTasks(client, connection, user, nextPageRequest, null).isEmpty()) {
            lastPage = LAST_PAGE_TRUE;
        }

        PagedMetadata<Task> pagedMetadata = new PagedMetadata<>(request.getPage(), request.getPageSize(),
                lastPage, SIMPLE_NAVIGATION);
        pagedMetadata.setBody(result);
        return pagedMetadata.toRestResponse();
    }

    private String convertSortProperty(String sort) {
        return Optional.ofNullable(KieTask.SORT_PROPERTIES.get(sort))
                .orElse(KieTask.SORT_PROPERTIES.get(PagedListRequest.SORT_VALUE_DEFAULT));
    }

    @Override
    public Task get(Connection connection, AuthenticatedUser user, String id) {
        UserTaskServicesClient client = kieApiService.getUserTaskServicesClient(connection);
        KieInstanceId taskId = new KieInstanceId(id);

        try {
            TaskInstance task = client.getTaskInstance(taskId.getContainerId(), taskId.getInstanceId(),
                    true, true, true);

            return KieTaskDetails.from(task);
        } catch (KieServicesHttpException e) {
            if (e.getHttpCode().equals(HttpStatus.NOT_FOUND.value())
                    //Some endpoints return 500 instead of 404
                    || e.getHttpCode().equals(HttpStatus.INTERNAL_SERVER_ERROR.value())) {
                throw new TaskNotFoundException(e);
            }

            throw new KieInvalidResponseException(HttpStatus.valueOf(e.getHttpCode()), e.getMessage(), e);
        }
    }
}
