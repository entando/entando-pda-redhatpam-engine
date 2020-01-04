package org.entando.plugins.pda.pam.service.task;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.entando.plugins.pda.core.utils.TestUtils.getDummyConnection;
import static org.entando.plugins.pda.core.utils.TestUtils.getDummyUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.entando.keycloak.security.AuthenticatedUser;
import org.entando.plugins.pda.core.engine.Connection;
import org.entando.plugins.pda.core.exception.TaskNotFoundException;
import org.entando.plugins.pda.core.model.Task;
import org.entando.plugins.pda.pam.service.api.KieApiService;
import org.entando.plugins.pda.pam.service.task.model.KieTask;
import org.entando.plugins.pda.pam.service.util.KieInstanceId;
import org.entando.plugins.pda.pam.util.KieTaskTestHelper;
import org.entando.web.request.Filter;
import org.entando.web.request.PagedListRequest;
import org.entando.web.response.PagedRestResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kie.server.api.exception.KieServicesHttpException;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.client.UserTaskServicesClient;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.http.HttpStatus;

@SuppressWarnings({"PMD.TooManyMethods", "PMD.ExcessiveImports"})
public class KieTaskServiceTest {

    private KieTaskService kieTaskService;
    private UserTaskServicesClient taskClient;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        KieApiService kieApiService = mock(KieApiService.class);
        taskClient = mock(UserTaskServicesClient.class);

        when(kieApiService.getUserTaskServicesClient(any())).thenReturn(taskClient);

        kieTaskService = new KieTaskService(kieApiService);
    }

    @Test
    public void shouldListTasks() {
        // Given
        List<TaskSummary> expected = KieTaskTestHelper.createKieTaskList();
        PagedListRequest request = new PagedListRequest(1, 10, "id", Filter.DESC_ORDER);
        Connection connection = getDummyConnection();
        AuthenticatedUser user = null;

        mockTaskList(anyString(), request.getPageSize(), expected);

        // When
        PagedRestResponse<Task> response = kieTaskService.list(connection, user, request);

        // Then //TODO validate sort?
        verifyTaskListResult(response, 1, expected, KieTaskService.LAST_PAGE_TRUE,request.getPage() - 1, request.getPageSize());
    }

    @Test
    public void shouldListTasksUsingConnectionUsername() {
        // Given
        List<TaskSummary> expected = KieTaskTestHelper.createKieTaskList();
        PagedListRequest request = new PagedListRequest();
        Connection connection = getDummyConnection();
        AuthenticatedUser user = null;
        String username = connection.getUsername();

        mockTaskList(eq(username), request.getPageSize(), expected);

        // When
        PagedRestResponse<Task> response = kieTaskService.list(connection, user, request);

        // Then
        verifyTaskListResult(response, 1, expected, KieTaskService.LAST_PAGE_TRUE, username);
    }

    @Test
    public void shouldListTasksUsingAuthenticatedUser() {
        // Given
        List<TaskSummary> expected = KieTaskTestHelper.createKieTaskList();
        PagedListRequest request = new PagedListRequest();
        Connection connection = getDummyConnection();
        AuthenticatedUser user = getDummyUser();
        String username = user.getAccessToken().getPreferredUsername();

        mockTaskList(eq(username), request.getPageSize(), expected);

        // When
        PagedRestResponse<Task> response = kieTaskService.list(connection, user, request);

        // Then
        verifyTaskListResult(response, 1, expected, KieTaskService.LAST_PAGE_TRUE, username, null, null);
    }

    @Test
    public void shouldReturnFalseForLastPageOnTaskList() {
        PagedListRequest request = new PagedListRequest(1, 2, "id", Filter.ASC_ORDER);
        Connection connection = getDummyConnection();
        List<TaskSummary> fullList = KieTaskTestHelper.createKieTaskList();
        List<TaskSummary> firstPage = fullList.subList(0, 2);
        List<TaskSummary> lastPage = fullList.subList(2, 3);

        mockTaskList(anyString(), request.getPageSize(), firstPage, lastPage);

        // When
        PagedRestResponse<Task> response = kieTaskService.list(connection, null, request);

        // Then
        verifyTaskListResult(response, 2, firstPage, KieTaskService.LAST_PAGE_FALSE);
    }

    @Test
    public void shouldReturnTrueForLastPageOnTaskList1() {
        // Given
        PagedListRequest request = new PagedListRequest(1, 2, "id", Filter.ASC_ORDER);
        Connection connection = getDummyConnection();
        List<TaskSummary> firstPage = KieTaskTestHelper.createKieTaskList().subList(0, 2);
        List<TaskSummary> lastPage = new ArrayList<>();

        mockTaskList(anyString(), request.getPageSize(), firstPage, lastPage);

        // When
        PagedRestResponse<Task> response = kieTaskService.list(connection, null, request);

        // Then
        verifyTaskListResult(response, 2, firstPage, KieTaskService.LAST_PAGE_TRUE);
    }

    @Test
    public void shouldReturnTrueForLastPageOnTaskList2() {
        // Given
        PagedListRequest request = new PagedListRequest(2, 2, "id", Filter.ASC_ORDER);
        Connection connection = getDummyConnection();
        List<TaskSummary> lastPage = KieTaskTestHelper.createKieTaskList().subList(2, 3);

        mockTaskList(anyString(), request.getPageSize(), lastPage);

        // When
        PagedRestResponse<Task> response = kieTaskService.list(connection, null, request);

        // Then
        verifyTaskListResult(response, 1, lastPage, KieTaskService.LAST_PAGE_TRUE);
    }

    @Test
    public void shouldGetTaskWithFlatVariables() {
        // Given
        TaskInstance expected = KieTaskTestHelper.generateKieTask();
        KieInstanceId taskId = new KieInstanceId(expected.getContainerId(), expected.getId());
        Connection connection = getDummyConnection();
        AuthenticatedUser user = getDummyUser();

        when(taskClient.getTaskInstance(eq(taskId.getContainerId()), eq(taskId.getInstanceId()),
                anyBoolean(), anyBoolean(), anyBoolean()))
                .thenReturn(expected);

        // When
        Task task = kieTaskService.get(connection, user, taskId.toString());

        // Then
        verify(taskClient, times(1))
                .getTaskInstance(eq(taskId.getContainerId()), eq(taskId.getInstanceId()), eq(true),
                        eq(true), eq(true));

        assertThat(task).isEqualTo(KieTask.from(expected));
        assertThat(task.getVariables().get(KieTaskTestHelper.EXTRA_VARS_FLAT_ATTRIBUTE_2))
                .isEqualTo(KieTaskTestHelper.EXTRA_VARS_VALUE_2);
        assertThat(task.getVariables().get(KieTaskTestHelper.EXTRA_VARS_ATTRIBUTE_2))
                .isNull();
    }

    @Test
    public void shouldThrowNotFoundWhenGetTask() {
        // Given
        expectedException.expect(TaskNotFoundException.class);
        KieInstanceId taskId = new KieInstanceId(randomAlphabetic(10), randomNumeric(10));
        Connection connection = getDummyConnection();
        AuthenticatedUser user = getDummyUser();

        when(taskClient.getTaskInstance(anyString(), anyLong(), anyBoolean(), anyBoolean(), anyBoolean()))
                .thenThrow(new KieServicesHttpException(null, HttpStatus.NOT_FOUND.value(), null, null));

        // When
        kieTaskService.get(connection, user, taskId.toString());
    }

    @SafeVarargs
    private final void mockTaskList(String username, Integer pageSize, List<TaskSummary>... pages) {
        OngoingStubbing<List<TaskSummary>> stub = when(
                taskClient.findTasksAssignedAsPotentialOwner(username, isNull(), anyInt(), eq(pageSize)));

        for (List<TaskSummary> taskSummaries : pages) {
            stub = stub.thenReturn(taskSummaries);
        }
    }

    private void verifyTaskListResult(PagedRestResponse<Task> response, int calls, List<TaskSummary> expected,
            int lastPage) {
        verifyTaskListResult(response, calls, expected, lastPage, null, null, null);
    }

    private void verifyTaskListResult(PagedRestResponse<Task> response, int calls, List<TaskSummary> expected,
            int lastPage, Integer page, Integer pageSize) {
        verifyTaskListResult(response, calls, expected, lastPage, null, page, pageSize);
    }

    private void verifyTaskListResult(PagedRestResponse<Task> response, int calls, List<TaskSummary> expected,
            int lastPage, String username) {
        verifyTaskListResult(response, calls, expected, lastPage, username, null, null);
    }

    private void verifyTaskListResult(PagedRestResponse<Task> response, int calls, List<TaskSummary> expected,
            int lastPage, String username, Integer page, Integer pageSize) {

        verify(taskClient, times(calls))
                .findTasksAssignedAsPotentialOwner(username == null ? anyString() : eq(username), isNull(),
                        page == null ? anyInt() : eq(page), pageSize == null ? anyInt() : eq(pageSize));

        assertThat(response.getPayload()).isEqualTo(expected.stream()
                .map(KieTask::from)
                .collect(Collectors.toList()));

        assertThat(response.getMetadata().getTotalItems()).isEqualTo(KieTaskService.SIMPLE_NAVIGATION);
        assertThat(response.getMetadata().getLastPage()).isEqualTo(lastPage);
    }
}
