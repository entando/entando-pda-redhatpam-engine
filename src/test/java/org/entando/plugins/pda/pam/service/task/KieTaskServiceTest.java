package org.entando.plugins.pda.pam.service.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.entando.keycloak.security.AuthenticatedUser;
import org.entando.plugins.pda.core.engine.Connection;
import org.entando.plugins.pda.core.model.Task;
import org.entando.plugins.pda.pam.service.task.model.KieProcessVariablesResponse;
import org.entando.plugins.pda.pam.service.task.model.KieTasksResponse;
import org.entando.plugins.pda.pam.util.KieTaskTestHelper;
import org.entando.web.request.Filter;
import org.entando.web.request.PagedListRequest;
import org.entando.web.response.PagedRestResponse;
import org.junit.Before;
import org.junit.Test;
import org.keycloak.representations.AccessToken;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

public class KieTaskServiceTest {

    private KieTaskService kieTaskService;

    private MockRestServiceServer mockServer;
    private final ObjectMapper mapper = new ObjectMapper();

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
        mockVariablesRequest(ExpectedCount.times(2));
        mockTasksRequest(ExpectedCount.manyTimes());

        // When
        PagedRestResponse<Task> tasks = kieTaskService.list(dummyConnection(), null, new PagedListRequest());

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
        mockVariablesRequest(ExpectedCount.times(2));
        mockTasksRequest(ExpectedCount.manyTimes());

        // When
        PagedRestResponse<Task> tasks = kieTaskService
                .list(dummyConnection(), null, new PagedListRequest(1, 10, "task-id", Filter.DESC_ORDER));

        // Then
        mockServer.verify();
        assertThat(tasks.getPayload()).isEqualTo(KieTaskTestHelper.createKieTaskListFull());
    }

    @Test
    public void shouldListUsingAuthenticatedUser() throws Exception {
        // Given
        String username = "chuck_norris";
        AuthenticatedUser user = dummyUser(username);
        mockServer.expect(requestTo(containsString(KieTaskService.TASK_LIST_URL)))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("user", username))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(new KieTasksResponse(KieTaskTestHelper.createKieTaskListUser())),
                        MediaType.APPLICATION_JSON));
        mockVariablesRequest(ExpectedCount.once());
        mockTasksRequest(ExpectedCount.once());

        // When
        PagedRestResponse<Task> tasks = kieTaskService.list(dummyConnection(), user, new PagedListRequest());

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
        mockVariablesRequest(ExpectedCount.once());
        mockTasksRequest(ExpectedCount.manyTimes());

        // When
        PagedRestResponse<Task> tasks = kieTaskService.list(dummyConnection(), null, new PagedListRequest());

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
        mockServer.expect(requestTo(
                containsString(KieTaskService.TASK_URL.replace("{tInstanceId}", generatedTask.getId()))))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(generatedTask),
                        MediaType.APPLICATION_JSON));
        mockTasksRequest(ExpectedCount.once());
        mockVariablesRequest(ExpectedCount.once());

        // When
        Task task = kieTaskService.get(dummyConnection(), null, generatedTask.getId());

        // Then
        mockServer.verify();
        assertThat(generatedTask).isEqualTo(task);
    }

    @Test
    public void shouldReturnFlattenDataOnGet() throws Exception {
        // Given
        Task createdTask = KieTaskTestHelper.createKieTaskListWithEmbeddedData().get(0);
        mockServer.expect(requestTo(
                containsString(KieTaskService.TASK_URL.replace("{tInstanceId}", createdTask.getId()))))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(createdTask),
                        MediaType.APPLICATION_JSON));
        mockTasksRequest(ExpectedCount.once());
        mockVariablesRequest(ExpectedCount.once());

        // When
        Task task = kieTaskService.get(dummyConnection(), null, createdTask.getId());

        // Then
        assertThat(task.getData().get(KieTaskTestHelper.FIELD_1 + "." + KieTaskTestHelper.FIELD_2)).isNotNull();
    }

    private void mockVariablesRequest(ExpectedCount count) throws JsonProcessingException {
        mockServer.expect(count, requestTo(containsString("/variables/instances")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mapper.writeValueAsString(
                        new KieProcessVariablesResponse(KieTaskTestHelper.createKieProcessVariables())),
                        MediaType.APPLICATION_JSON));
    }

    private void mockTasksRequest(ExpectedCount count) throws JsonProcessingException {
        mockServer.expect(count, requestTo(containsString("/tasks")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(KieTaskTestHelper.createKieTaskDetails()),
                        MediaType.APPLICATION_JSON));
    }

    private AuthenticatedUser dummyUser(String username) {
        AuthenticatedUser user = mock(AuthenticatedUser.class);
        AccessToken token = mock(AccessToken.class);
        when(user.getAccessToken())
                .thenReturn(token);
        when(token.getPreferredUsername())
                .thenReturn(username);
        return user;
    }

    private Connection dummyConnection() {
        return Connection.builder()
                .username("myUsername")
                .password("myPassword")
                .schema("http")
                .host("myurl")
                .port("8080")
                .build();
    }
}
