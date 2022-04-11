package org.entando.plugins.pda.pam.service.task;

import com.google.common.collect.Iterables;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.entando.keycloak.security.AuthenticatedUser;
import org.entando.plugins.pda.core.engine.Connection;
import org.entando.plugins.pda.core.exception.TaskNotFoundException;
import org.entando.plugins.pda.core.model.Task;
import org.entando.plugins.pda.core.request.Filter;
import org.entando.plugins.pda.core.request.PagedListRequest;
import org.entando.plugins.pda.core.response.PagedMetadata;
import org.entando.plugins.pda.core.response.PagedRestResponse;
import org.entando.plugins.pda.core.service.task.TaskService;
import org.entando.plugins.pda.pam.exception.KieInvalidPageStart;
import org.entando.plugins.pda.pam.exception.KieInvalidResponseException;
import org.entando.plugins.pda.pam.exception.NoConnectionWithKieServerException;
import org.entando.plugins.pda.pam.service.api.KieApiService;
import org.entando.plugins.pda.pam.service.task.model.KieTask;
import org.entando.plugins.pda.pam.service.task.model.KieTaskDetails;
import org.entando.plugins.pda.pam.service.task.model.KieTaskListResponse;
import org.entando.plugins.pda.pam.service.task.model.KieTaskSummaryResponse;
import org.entando.plugins.pda.pam.service.util.KieInstanceId;
import org.kie.server.api.exception.KieServicesHttpException;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.client.UserTaskServicesClient;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Slf4j
@RequiredArgsConstructor
public class KieTaskService implements TaskService {

    public static final int PAGE_START = 1;
    public static final int LAST_PAGE_TRUE = 1;
    public static final int LAST_PAGE_FALSE = 0;
    public static final int SIMPLE_NAVIGATION = -1;
    public static final String POT_OWNERS_ENDPOINT = "/queries/tasks/instances/pot-owners";

    public static final String USER_PARAM = "user";
    public static final String SORT_ORDER_PARAM = "sortOrder";
    public static final String PAGE_SIZE_PARAM = "pageSize";
    public static final String PAGE_PARAM = "page";
    public static final String FILTER_PARAM = "filter";
    public static final String SORT_PARAM = "sort";
    public static final String GROUPS_PARAM = "groups";

    private final KieApiService kieApiService;
    private final RestTemplateBuilder restTemplateBuilder;

    @Override
    public PagedRestResponse<Task> list(Connection connection, AuthenticatedUser user, PagedListRequest request,
            String filter, List<String> groups) {
        String searchFilter = filter == null ? "" : filter.replace("*", "%");
        List<Task> result = queryTasks(connection, user, request, searchFilter, groups);
        return createPagedResponse(connection, user, request, result, searchFilter, groups);
    }

    private List<Task> queryTasks(Connection connection, AuthenticatedUser user,
            PagedListRequest request, String filter, List<String> groups) {

        if (request.getPage() < PAGE_START) {
            throw new KieInvalidPageStart();
        }
        RestTemplate restTemplate = restTemplateBuilder
                .basicAuthentication(connection.getUsername(), connection.getPassword())
                .build();
        String username = user == null ? connection.getUsername() : user.getAccessToken().getPreferredUsername();
        String url = getUrl(connection, username, request, filter, groups);

        try {
            KieTaskListResponse response = restTemplate.getForObject(url, KieTaskListResponse.class);
            if (response == null) {
                return Collections.emptyList();
            }
            return Optional.ofNullable(response.getTaskSummaries()).orElse(Collections.emptyList())
                    .stream()
                    .map(KieTaskSummaryResponse::toTaskSummary)
                    .map(KieTask::from)
                    .collect(Collectors.toList());
        } catch (ResourceAccessException e) {
            throw new NoConnectionWithKieServerException(e);
        }
    }

    private String getUrl(Connection connection, String username, PagedListRequest request, String filter,
            List<String> groups) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
                .fromHttpUrl(connection.getUrl() + POT_OWNERS_ENDPOINT)
                .queryParam(PAGE_PARAM, request.getPage() - 1)
                .queryParam(PAGE_SIZE_PARAM, request.getPageSize())
                .queryParam(SORT_ORDER_PARAM, !request.getDirection().equals(Filter.DESC_ORDER))
                .queryParam(USER_PARAM, username);
        if (StringUtils.isNotBlank(request.getSort())) {
            uriComponentsBuilder.queryParam(SORT_PARAM, convertSortProperty(request.getSort()));
        }
        if (!CollectionUtils.isEmpty(groups)) {
            uriComponentsBuilder.queryParam(GROUPS_PARAM, Iterables.toArray(groups, String.class));
        }
        if (StringUtils.isNotBlank(filter)) {
            uriComponentsBuilder.queryParam(FILTER_PARAM, filter);
        }
        return uriComponentsBuilder.build(false).toUriString();
    }

    private PagedRestResponse<Task> createPagedResponse(Connection connection,
            AuthenticatedUser user, PagedListRequest request, List<Task> result, String filter, List<String> groups) {

        PagedListRequest nextPageRequest = new PagedListRequest(request.getPage() + 1, request.getPageSize(),
                request.getSort(), request.getDirection());

        int lastPage = LAST_PAGE_FALSE;
        if (result.size() != request.getPageSize()
                || queryTasks(connection, user, nextPageRequest, filter, groups).isEmpty()) {
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
            if (e.getHttpCode().equals(HttpStatus.NOT_FOUND.value())) {
                throw new TaskNotFoundException(e);
            }

            throw new KieInvalidResponseException(HttpStatus.valueOf(e.getHttpCode()), e.getMessage(), e);
        }
    }
}
