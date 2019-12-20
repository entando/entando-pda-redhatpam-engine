package org.entando.plugins.pda.pam.service.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.entando.plugins.pda.core.utils.TestUtils.getDummyConnection;
import static org.entando.plugins.pda.core.utils.TestUtils.getDummyUser;
import static org.entando.plugins.pda.pam.util.KieTaskTestHelper.mockTasksRequest;
import static org.entando.plugins.pda.pam.util.KieTaskTestHelper.mockVariablesRequest;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.ExpectedCount.manyTimes;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import org.entando.keycloak.security.AuthenticatedUser;
import org.entando.plugins.pda.core.model.Task;
import org.entando.plugins.pda.pam.service.process.model.KieInstanceId;
import org.entando.plugins.pda.pam.service.task.model.KieTasksResponse;
import org.entando.plugins.pda.pam.util.KieTaskTestHelper;
import org.entando.web.request.Filter;
import org.entando.web.request.PagedListRequest;
import org.entando.web.response.PagedRestResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

public class KieTaskServiceTest {

    private KieTaskService kieTaskService;

    private MockRestServiceServer mockServer;
    private final ObjectMapper mapper = new ObjectMapper();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        RestTemplateBuilder restTemplateBuilder = mock(RestTemplateBuilder.class);
        when(restTemplateBuilder.basicAuthorization(anyString(), anyString())).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        mockServer = MockRestServiceServer.createServer(restTemplate);

        kieTaskService = new KieTaskService(restTemplateBuilder);
    }

    @Test
    public void shouldListTasksFromApi() throws Exception {
        // Given
        mockServer.expect(requestTo(containsString(KieTaskService.TASK_LIST_URL)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(new KieTasksResponse(KieTaskTestHelper.createKieTaskList())),
                        MediaType.APPLICATION_JSON));
        mockVariablesRequest(mockServer, mapper, times(2));
        mockTasksRequest(mockServer, mapper, manyTimes());

        // When
        PagedRestResponse<Task> tasks = kieTaskService.list(getDummyConnection(), null, new PagedListRequest());

        // Then
        mockServer.verify();
        assertThat(tasks.getPayload()).isEqualTo(KieTaskTestHelper.createKieTaskListFull());
    }

    @Test
    public void shouldListTasksFromApiWithFilterAndSort() throws Exception {
        // Given
        mockServer.expect(requestTo(containsString(KieTaskService.TASK_LIST_URL)))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("page", "0"))
                .andExpect(queryParam("pageSize", "10"))
                .andExpect(queryParam("sort", "task-id"))
                .andExpect(queryParam("sortOrder", "false"))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(new KieTasksResponse(KieTaskTestHelper.createKieTaskList())),
                        MediaType.APPLICATION_JSON));
        mockVariablesRequest(mockServer, mapper, times(2));
        mockTasksRequest(mockServer, mapper, manyTimes());

        // When
        PagedRestResponse<Task> tasks = kieTaskService
                .list(getDummyConnection(), null, new PagedListRequest(1, 10, "task-id", Filter.DESC_ORDER));

        // Then
        mockServer.verify();
        assertThat(tasks.getPayload()).isEqualTo(KieTaskTestHelper.createKieTaskListFull());
    }

    @Test
    public void shouldListUsingAuthenticatedUser() throws Exception {
        // Given
        String username = "chuck_norris";
        AuthenticatedUser user = getDummyUser(username);
        mockServer.expect(times(2), requestTo(containsString(KieTaskService.TASK_LIST_URL)))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("user", username))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(new KieTasksResponse(KieTaskTestHelper.createKieTaskListUser())),
                        MediaType.APPLICATION_JSON));
        mockVariablesRequest(mockServer, mapper, once());
        mockTasksRequest(mockServer, mapper, once());

        // When
        PagedRestResponse<Task> tasks = kieTaskService.list(getDummyConnection(), user, new PagedListRequest());

        // Then
        mockServer.verify();
        assertThat(tasks.getPayload()).isEqualTo(KieTaskTestHelper.createKieTaskListUser());
    }

    @Test
    public void shouldReturnFlattenData() throws Exception {
        // Given
        mockServer.expect(requestTo(containsString(KieTaskService.TASK_LIST_URL)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(
                                new KieTasksResponse(KieTaskTestHelper.createKieTaskListWithEmbeddedData())),
                        MediaType.APPLICATION_JSON));
        mockVariablesRequest(mockServer, mapper, once());
        mockTasksRequest(mockServer, mapper, manyTimes());

        // When
        PagedRestResponse<Task> tasks = kieTaskService.list(getDummyConnection(), null, new PagedListRequest());

        // Then
        mockServer.verify();
        assertThat(tasks.getPayload().get(0).getData().get(KieTaskTestHelper.FIELD_1 + "." + KieTaskTestHelper.FIELD_2))
                .isNotNull();
        assertThat(tasks.getPayload().get(1).getData().get(KieTaskTestHelper.FIELD_1 + "." + KieTaskTestHelper.FIELD_2))
                .isNotNull();
    }

    @Test
    public void shouldGetTask() throws Exception {
        // Given
        Task generatedTask = KieTaskTestHelper.generateKieTask();
        KieInstanceId taskId = new KieInstanceId(generatedTask.getContainerId(), generatedTask.getId());

        mockServer.expect(requestTo(
                containsString(KieTaskService.TASK_URL
                        .replace("{tInstanceId}", taskId.getInstanceId().toString()))))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(generatedTask),
                        MediaType.APPLICATION_JSON));
        mockTasksRequest(mockServer, mapper, once());
        mockVariablesRequest(mockServer, mapper, once());

        // When
        Task task = kieTaskService.get(getDummyConnection(), null, taskId.toString());

        // Then
        mockServer.verify();
        assertThat(generatedTask).isEqualTo(task);
    }

    @Test
    public void shouldReturnFlattenDataOnGet() throws Exception {
        // Given
        Task createdTask = KieTaskTestHelper.createKieTaskListWithEmbeddedData().get(0);
        KieInstanceId taskId = new KieInstanceId(createdTask.getContainerId(), createdTask.getId());

        mockServer.expect(requestTo(
                containsString(KieTaskService.TASK_URL
                        .replace("{tInstanceId}", taskId.getInstanceId().toString()))))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(createdTask),
                        MediaType.APPLICATION_JSON));
        mockTasksRequest(mockServer, mapper, once());
        mockVariablesRequest(mockServer, mapper, once());

        // When
        Task task = kieTaskService.get(getDummyConnection(), null, taskId.toString());

        // Then
        assertThat(task.getData().get(KieTaskTestHelper.FIELD_1 + "." + KieTaskTestHelper.FIELD_2)).isNotNull();
    }

    @Test
    public void shouldReturnFalseForLastPageOnTaskList() throws Exception {
        // Given
        mockServer.expect(times(2), requestTo(containsString(KieTaskService.TASK_LIST_URL)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(new KieTasksResponse(KieTaskTestHelper.createKieTaskList())),
                        MediaType.APPLICATION_JSON));
        mockVariablesRequest(mockServer, mapper, times(2));
        mockTasksRequest(mockServer, mapper, manyTimes());

        // When
        PagedRestResponse<Task> tasks = kieTaskService.list(getDummyConnection(), null, new PagedListRequest());

        // Then
        assertThat(tasks.getMetadata().getTotalItems()).isEqualTo(KieTaskService.SIMPLE_NAVIGATION);
        assertThat(tasks.getMetadata().getLastPage()).isEqualTo(KieTaskService.LAST_PAGE_FALSE);
    }

    @Test
    public void shouldReturnTrueForLastPageOnTaskList() throws Exception {
        // Given
        mockServer.expect(requestTo(containsString(KieTaskService.TASK_LIST_URL)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(new KieTasksResponse(KieTaskTestHelper.createKieTaskList())),
                        MediaType.APPLICATION_JSON));
        mockVariablesRequest(mockServer, mapper, times(2));
        mockTasksRequest(mockServer, mapper, manyTimes());
        mockServer.expect(requestTo(containsString(KieTaskService.TASK_LIST_URL)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(new KieTasksResponse(Collections.emptyList())),
                        MediaType.APPLICATION_JSON));

        // When
        PagedRestResponse<Task> tasks = kieTaskService.list(getDummyConnection(), null, new PagedListRequest());

        // Then
        assertThat(tasks.getMetadata().getTotalItems()).isEqualTo(-1);
        assertThat(tasks.getMetadata().getLastPage()).isEqualTo(KieTaskService.LAST_PAGE_TRUE);
    }
}
