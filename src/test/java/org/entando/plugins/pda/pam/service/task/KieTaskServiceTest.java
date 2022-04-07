package org.entando.plugins.pda.pam.service.task;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.entando.plugins.pda.core.utils.TestUtils.getDummyConnection;
import static org.entando.plugins.pda.core.utils.TestUtils.getDummyUser;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.entando.keycloak.security.AuthenticatedUser;
import org.entando.plugins.pda.core.engine.Connection;
import org.entando.plugins.pda.core.exception.TaskNotFoundException;
import org.entando.plugins.pda.core.model.Task;
import org.entando.plugins.pda.core.request.Filter;
import org.entando.plugins.pda.core.request.PagedListRequest;
import org.entando.plugins.pda.core.response.PagedRestResponse;
import org.entando.plugins.pda.pam.exception.KieInvalidPageStart;
import org.entando.plugins.pda.pam.service.api.KieApiService;
import org.entando.plugins.pda.pam.service.task.model.KieTask;
import org.entando.plugins.pda.pam.service.task.model.KieTaskDetails;
import org.entando.plugins.pda.pam.service.task.model.KieTaskListResponse;
import org.entando.plugins.pda.pam.service.task.model.KieTaskSummaryResponse;
import org.entando.plugins.pda.pam.service.util.KieInstanceId;
import org.entando.plugins.pda.pam.util.KieTaskTestHelper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kie.server.api.exception.KieServicesHttpException;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.client.UserTaskServicesClient;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ResponseActions;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

@SuppressWarnings({"PMD.TooManyMethods", "PMD.ExcessiveImports"})
public class KieTaskServiceTest {

    private KieTaskService kieTaskService;
    private UserTaskServicesClient taskClient;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private MockRestServiceServer mockServer;

    private final ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setUp() {
        KieApiService kieApiService = mock(KieApiService.class);
        taskClient = mock(UserTaskServicesClient.class);

        when(kieApiService.getUserTaskServicesClient(any())).thenReturn(taskClient);

        RestTemplate restTemplate = new RestTemplate();
        RestTemplateBuilder restTemplateBuilder = mock(RestTemplateBuilder.class);
        when(restTemplateBuilder.basicAuthentication(anyString(), anyString())).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        mockServer = MockRestServiceServer.createServer(restTemplate);

        kieTaskService = new KieTaskService(kieApiService, restTemplateBuilder);
    }

    @Test
    public void shouldListTasks() throws Exception {
        // Given
        List<TaskSummary> expected = KieTaskTestHelper.createKieTaskList();
        PagedListRequest request = new PagedListRequest();
        Connection connection = getDummyConnection();

        mockTaskList(expected);

        // When
        PagedRestResponse<Task> response = kieTaskService.list(connection, null, request, null, null);

        // Then
        mockServer.verify();
        assertThat(response.getPayload()).extracting("name")
                .containsExactlyInAnyOrder(expected.get(0).getName(), expected.get(1).getName(),
                        expected.get(2).getName());
        assertThat(response.getMetadata().getLastPage()).isEqualTo(KieTaskService.LAST_PAGE_TRUE);
    }

    @Test
    public void shouldSearchTaskList() throws Exception {
        // Given
        List<TaskSummary> expected = Collections.singletonList(KieTaskTestHelper.createKieTaskList().get(0));
        PagedListRequest request = new PagedListRequest();
        Connection connection = getDummyConnection();
        AuthenticatedUser user = null;
        String filter = "* 1";

        mockTaskList(expected)
                .andExpect(queryParam(KieTaskService.FILTER_PARAM, UriUtils.encode("% 1", StandardCharsets.UTF_8)));

        // When
        PagedRestResponse<Task> response = kieTaskService.list(connection, user, request, filter, null);

        // Then
        mockServer.verify();
        assertThat(response.getPayload().size()).isEqualTo(expected.size());
    }

    @Test
    public void shouldListTasksUsingConnectionUsername() throws Exception {
        // Given
        List<TaskSummary> expected = KieTaskTestHelper.createKieTaskList();
        PagedListRequest request = new PagedListRequest();
        Connection connection = getDummyConnection();
        String username = connection.getUsername();

        mockTaskList(expected).andExpect(queryParam(KieTaskService.USER_PARAM, username));

        // When
        PagedRestResponse<Task> response = kieTaskService.list(connection, null, request, null, null);

        // Then
        mockServer.verify();
        assertThat(response.getPayload().size()).isEqualTo(expected.size());
    }

    @Test
    public void shouldListTasksUsingAuthenticatedUser() throws Exception {
        // Given
        List<TaskSummary> expected = KieTaskTestHelper.createKieTaskList();
        PagedListRequest request = new PagedListRequest();
        Connection connection = getDummyConnection();
        AuthenticatedUser user = getDummyUser();
        String username = user.getAccessToken().getPreferredUsername();

        mockTaskList(expected).andExpect(queryParam(KieTaskService.USER_PARAM, username));

        // When
        PagedRestResponse<Task> response = kieTaskService.list(connection, user, request, null, null);

        // Then
        mockServer.verify();
        assertThat(response.getPayload().size()).isEqualTo(expected.size());
    }

    @Test
    public void shouldReturnFalseForLastPageOnTaskList() throws Exception {
        PagedListRequest request = new PagedListRequest(1, 2, "id", Filter.ASC_ORDER);
        Connection connection = getDummyConnection();
        List<TaskSummary> fullList = KieTaskTestHelper.createKieTaskList();
        List<TaskSummary> firstPage = fullList.subList(0, 2);
        List<TaskSummary> lastPage = fullList.subList(2, 3);

        mockTaskList(firstPage).andExpect(queryParam(KieTaskService.PAGE_PARAM, "0"));
        mockTaskList(lastPage).andExpect(queryParam(KieTaskService.PAGE_PARAM, "1"));

        // When
        PagedRestResponse<Task> response = kieTaskService.list(connection, null, request, null, null);

        // Then
        mockServer.verify();
        assertThat(response.getMetadata().getLastPage()).isEqualTo(KieTaskService.LAST_PAGE_FALSE);
    }

    @Test
    public void shouldReturnTrueForLastPageOnTaskList1() throws Exception {
        // Given
        PagedListRequest request = new PagedListRequest(1, 2, "id", Filter.ASC_ORDER);
        Connection connection = getDummyConnection();
        List<TaskSummary> firstPage = KieTaskTestHelper.createKieTaskList().subList(0, 2);

        mockTaskList(firstPage).andExpect(queryParam(KieTaskService.PAGE_PARAM, "0"));
        mockTaskList(new ArrayList<>()).andExpect(queryParam(KieTaskService.PAGE_PARAM, "1"));

        // When
        PagedRestResponse<Task> response = kieTaskService.list(connection, null, request, null, null);

        // Then
        mockServer.verify();
        assertThat(response.getMetadata().getLastPage()).isEqualTo(KieTaskService.LAST_PAGE_TRUE);
    }

    @Test
    public void shouldReturnTrueForLastPageOnTaskList2() throws Exception {
        // Given
        PagedListRequest request = new PagedListRequest(2, 2, "id", Filter.ASC_ORDER);
        Connection connection = getDummyConnection();
        List<TaskSummary> lastPage = KieTaskTestHelper.createKieTaskList().subList(2, 3);

        mockTaskList(lastPage);

        // When
        PagedRestResponse<Task> response = kieTaskService.list(connection, null, request, null, null);

        // Then
        mockServer.verify();
        assertThat(response.getMetadata().getLastPage()).isEqualTo(KieTaskService.LAST_PAGE_TRUE);
    }

    @Test
    public void shouldListTasksSortedUsingDefaultParamsWhenInvalidRequest() throws Exception {
        // Given
        List<TaskSummary> expected = KieTaskTestHelper.createKieTaskList();
        PagedListRequest request = new PagedListRequest(1, 10, "--INVALID--", "--INVALID--");
        Connection connection = getDummyConnection();

        mockTaskList(expected)
                .andExpect(queryParam(KieTaskService.SORT_PARAM,
                        KieTask.SORT_PROPERTIES.get(PagedListRequest.SORT_VALUE_DEFAULT)))
                .andExpect(queryParam(KieTaskService.SORT_ORDER_PARAM, "true"));

        // When
        PagedRestResponse<Task> response = kieTaskService.list(connection, null, request, null, null);

        // Then
        mockServer.verify();
        assertThat(response.getMetadata().getPage()).isEqualTo(request.getPage());
        assertThat(response.getMetadata().getPageSize()).isEqualTo(request.getPageSize());
        assertThat(response.getMetadata().getLastPage()).isEqualTo(KieTaskService.LAST_PAGE_TRUE);
    }

    @Test
    public void shouldThrowInvalidPageWhenPageLowerThan1() {
        // Given
        expectedException.expect(KieInvalidPageStart.class);
        PagedListRequest request = new PagedListRequest(0, 10, "id", Filter.ASC_ORDER);
        Connection connection = getDummyConnection();
        AuthenticatedUser user = getDummyUser();

        // When
        kieTaskService.list(connection, user, request, null, null);
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

        assertThat(task).isEqualTo(KieTaskDetails.from(expected));

        assertThat(task.getInputData().get(KieTaskTestHelper.EXTRA_VARS_ATTRIBUTE_1))
                .isEqualTo(KieTaskTestHelper.EXTRA_VARS_VALUE_1);
        assertThat(task.getInputData().get(KieTaskTestHelper.EXTRA_VARS_FLAT_ATTRIBUTE_2))
                .isEqualTo(KieTaskTestHelper.EXTRA_VARS_VALUE_2);
        assertThat(task.getInputData().get(KieTaskTestHelper.EXTRA_VARS_ATTRIBUTE_2))
                .isNull();

        assertThat(task.getOutputData().get(KieTaskTestHelper.EXTRA_VARS_ATTRIBUTE_1))
                .isEqualTo(KieTaskTestHelper.EXTRA_VARS_VALUE_1);
        assertThat(task.getOutputData().get(KieTaskTestHelper.EXTRA_VARS_FLAT_ATTRIBUTE_3))
                .isEqualTo(KieTaskTestHelper.EXTRA_VARS_VALUE_3);
        assertThat(task.getOutputData().get(KieTaskTestHelper.EXTRA_VARS_ATTRIBUTE_3))
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

    @Test
    public void shouldPassGroupsWhenSearching() throws Exception {
        // Given
        List<TaskSummary> expected = KieTaskTestHelper.createKieTaskList();
        PagedListRequest request = new PagedListRequest();
        Connection connection = getDummyConnection();

        String[] groups = {"group1", "group2"};
        mockTaskList(expected).andExpect(queryParam(KieTaskService.GROUPS_PARAM, groups));

        // When
        PagedRestResponse<Task> response = kieTaskService.list(connection, null, request, null, Arrays.asList(groups));

        // Then
        mockServer.verify();
        assertThat(response.getPayload().size()).isEqualTo(expected.size());
    }

    private ResponseActions mockTaskList(List<TaskSummary> taskSummaries) throws JsonProcessingException {
        KieTaskListResponse response = new KieTaskListResponse();
        response.setTaskSummaries(taskSummaries.stream()
                .map(KieTaskSummaryResponse::fromTaskSummary)
                .collect(Collectors.toList()));
        ResponseActions responseActions = mockServer
                .expect(ExpectedCount.manyTimes(), requestTo(containsString(KieTaskService.POT_OWNERS_ENDPOINT)))
                .andExpect(method(HttpMethod.GET));
        responseActions.andRespond(withSuccess(mapper.writeValueAsString(response), MediaType.APPLICATION_JSON));
        return responseActions;
    }
}
