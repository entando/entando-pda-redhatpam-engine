package org.entando.plugins.pda.pam.service.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.matches;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.regex.Pattern;
import org.entando.keycloak.security.AuthenticatedUser;
import org.entando.plugins.pda.core.engine.Connection;
import org.entando.plugins.pda.core.model.Task;
import org.entando.plugins.pda.pam.service.task.model.KieProcessVariablesResponse;
import org.entando.plugins.pda.pam.service.task.model.KieTaskDetails;
import org.entando.plugins.pda.pam.service.task.model.KieTasksResponse;
import org.entando.plugins.pda.pam.util.KieTaskTestHelper;
import org.entando.web.request.Filter;
import org.entando.web.request.PagedListRequest;
import org.entando.web.response.PagedRestResponse;
import org.junit.Before;
import org.junit.Test;
import org.keycloak.representations.AccessToken;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

public class KieTaskServiceTest {

    private KieTaskService kieTaskService;

    private RestTemplate restTemplate;

    @Before
    public void setup() {
        restTemplate = mock(RestTemplate.class);
        RestTemplateBuilder restTemplateBuilder = mock(RestTemplateBuilder.class);
        when(restTemplateBuilder.basicAuthorization(anyString(), anyString())).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);

        kieTaskService = new KieTaskService(restTemplateBuilder);
    }

    @Test
    public void shouldListTasksFromApi() {
        Connection connection = Connection.builder()
                .username("myUsername")
                .password("myPassword")
                .schema("http")
                .host("myurl")
                .port("8080")
                .build();

        AuthenticatedUser user = null;

        Pattern pTaskListPattern = Pattern.compile((connection.getUrl() + KieTaskService.TASK_LIST_URL
                    + "\\?user=.+"
                    + "&page=%d&pageSize=%d")
                .replace("%d", ".+")
                .replace("%s", ".+"));

        Pattern pInstanceVariablesPattern = Pattern.compile((connection.getUrl() + KieTaskService.PROCESS_VARIABLES_URL
                    + "\\?user=.+")
                .replace("{pInstanceId}", ".+"));

        Pattern tDetailsPattern = Pattern.compile((connection.getUrl() + KieTaskService.TASK_DETAILS_URL
                    + "\\?user=.+")
                .replace("{tInstanceId}", ".+")
                .replace("{containerId}", ".+"));

        when(restTemplate.getForObject(matches(pTaskListPattern), eq(KieTasksResponse.class))).thenReturn(
                new KieTasksResponse(KieTaskTestHelper.createKieTaskList()));

        when(restTemplate.getForObject(matches(pInstanceVariablesPattern), eq(KieProcessVariablesResponse.class),
                eq(KieTaskTestHelper.PROCESS_INSTANCE_ID_1))).thenReturn(
                new KieProcessVariablesResponse(KieTaskTestHelper.createKieProcessVariables()));

        when(restTemplate.getForObject(matches(pInstanceVariablesPattern), eq(KieProcessVariablesResponse.class),
                eq(KieTaskTestHelper.PROCESS_INSTANCE_ID_2))).thenReturn(
                new KieProcessVariablesResponse(KieTaskTestHelper.createKieProcessVariables()));

        when(restTemplate.getForObject(matches(tDetailsPattern), eq(KieTaskDetails.class), any(), any())).thenReturn(
                KieTaskTestHelper.createKieTaskDetails());

        PagedRestResponse<Task> tasks = kieTaskService.list(connection, user, new PagedListRequest());

        verify(restTemplate).getForObject(eq(connection.getUrl() + KieTaskService.TASK_LIST_URL
                + String.format("?user=%s&page=%d&pageSize=%d", connection.getUsername(), 0, 100)), eq(KieTasksResponse.class));

        verify(restTemplate, times(1)).getForObject(matches(pInstanceVariablesPattern),
                eq(KieProcessVariablesResponse.class), eq(KieTaskTestHelper.PROCESS_INSTANCE_ID_1));

        verify(restTemplate, times(1)).getForObject(matches(pInstanceVariablesPattern),
                eq(KieProcessVariablesResponse.class), eq(KieTaskTestHelper.PROCESS_INSTANCE_ID_2));

        verify(restTemplate, times(tasks.getPayload().size())).getForObject(matches(tDetailsPattern), eq(
                KieTaskDetails.class), any(), any());

        assertThat(tasks.getPayload()).isEqualTo(KieTaskTestHelper.createKieTaskListFull());
    }

    @Test
    public void shouldListTasksFromApiWithFilterAndSort() {
        Connection connection = Connection.builder()
                .username("myUsername")
                .password("myPassword")
                .schema("http")
                .host("myurl")
                .port("8080")
                .build();

        AuthenticatedUser user = null;

        Pattern pTaskListPattern = Pattern.compile((connection.getUrl() + KieTaskService.TASK_LIST_URL
                + "\\?user=.+"
                + "&page=%d&pageSize=%d")
                .replace("%d", ".+")
                .replace("%s", ".+"));

        Pattern pInstanceVariablesPattern = Pattern.compile((connection.getUrl() + KieTaskService.PROCESS_VARIABLES_URL
                + "\\?user=.+")
                .replace("{pInstanceId}", ".+"));

        Pattern tDetailsPattern = Pattern.compile((connection.getUrl() + KieTaskService.TASK_DETAILS_URL
                + "\\?user=.+")
                .replace("{tInstanceId}", ".+")
                .replace("{containerId}", ".+"));

        when(restTemplate.getForObject(matches(pTaskListPattern), eq(KieTasksResponse.class))).thenReturn(
                new KieTasksResponse(KieTaskTestHelper.createKieTaskList()));

        when(restTemplate.getForObject(matches(pInstanceVariablesPattern), eq(KieProcessVariablesResponse.class),
                eq(KieTaskTestHelper.PROCESS_INSTANCE_ID_1))).thenReturn(
                new KieProcessVariablesResponse(KieTaskTestHelper.createKieProcessVariables()));

        when(restTemplate.getForObject(matches(pInstanceVariablesPattern), eq(KieProcessVariablesResponse.class),
                eq(KieTaskTestHelper.PROCESS_INSTANCE_ID_2))).thenReturn(
                new KieProcessVariablesResponse(KieTaskTestHelper.createKieProcessVariables()));

        when(restTemplate.getForObject(matches(tDetailsPattern), eq(KieTaskDetails.class), any(), any())).thenReturn(
                KieTaskTestHelper.createKieTaskDetails());

        PagedRestResponse<Task> tasks = kieTaskService.list(connection, user, new PagedListRequest(1, 10, "task-id", Filter.DESC_ORDER));

        verify(restTemplate).getForObject(eq(connection.getUrl() + KieTaskService.TASK_LIST_URL
                    + String.format("?user=%s&page=%d&pageSize=%d&sort=%s&sortOrder=%s", connection.getUsername(), 0, 10, "task-id", false)),
                eq(KieTasksResponse.class));

        verify(restTemplate, times(1)).getForObject(matches(pInstanceVariablesPattern),
                eq(KieProcessVariablesResponse.class), eq(KieTaskTestHelper.PROCESS_INSTANCE_ID_1));

        verify(restTemplate, times(1)).getForObject(matches(pInstanceVariablesPattern),
                eq(KieProcessVariablesResponse.class), eq(KieTaskTestHelper.PROCESS_INSTANCE_ID_2));

        verify(restTemplate, times(tasks.getPayload().size())).getForObject(matches(tDetailsPattern), eq(
                KieTaskDetails.class), any(), any());

        assertThat(tasks.getPayload()).isEqualTo(KieTaskTestHelper.createKieTaskListFull());
    }

    @Test
    public void shouldListUsingAuthenticatedUser() {
        Connection connection = Connection.builder()
                .username("myUsername")
                .password("myPassword")
                .schema("http")
                .host("myurl")
                .port("8080")
                .build();

        AuthenticatedUser user = mock(AuthenticatedUser.class);
        AccessToken token = mock(AccessToken.class);
        String username = "chuck_norris";

        when(user.getAccessToken())
            .thenReturn(token);

        when(token.getPreferredUsername())
            .thenReturn(username);

        Pattern pTaskListPatternWithUser = Pattern.compile((connection.getUrl() + KieTaskService.TASK_LIST_URL
                + "\\?user=" + username
                + "&page=%d&pageSize=%d")
                .replace("%d", ".+")
                .replace("%s", ".+"));

        Pattern pTaskListPatternWithConnection = Pattern.compile((connection.getUrl() + KieTaskService.TASK_LIST_URL
                + "\\?user=" + connection.getUsername()
                + "&page=%d&pageSize=%d")
                .replace("%d", ".+")
                .replace("%s", ".+"));

        Pattern pInstanceVariablesPattern = Pattern.compile((connection.getUrl() + KieTaskService.PROCESS_VARIABLES_URL
                + "\\?user=" + username)
                .replace("{pInstanceId}", ".+"));

        Pattern tDetailsPattern = Pattern.compile((connection.getUrl() + KieTaskService.TASK_DETAILS_URL
                + "\\?user=" + username)
                .replace("{tInstanceId}", ".+")
                .replace("{containerId}", ".+"));

        when(restTemplate.getForObject(matches(pTaskListPatternWithUser), eq(KieTasksResponse.class))).thenReturn(
                new KieTasksResponse(KieTaskTestHelper.createKieTaskListUser()));

        when(restTemplate.getForObject(matches(pTaskListPatternWithConnection), eq(KieTasksResponse.class))).thenReturn(
                new KieTasksResponse(KieTaskTestHelper.createKieTaskListFull()));

        when(restTemplate.getForObject(matches(pInstanceVariablesPattern), eq(KieProcessVariablesResponse.class),
                eq(KieTaskTestHelper.PROCESS_INSTANCE_ID_1))).thenReturn(
                new KieProcessVariablesResponse(KieTaskTestHelper.createKieProcessVariables()));

        when(restTemplate.getForObject(matches(pInstanceVariablesPattern), eq(KieProcessVariablesResponse.class),
                eq(KieTaskTestHelper.PROCESS_INSTANCE_ID_2))).thenReturn(
                new KieProcessVariablesResponse(KieTaskTestHelper.createKieProcessVariables()));

        when(restTemplate.getForObject(matches(tDetailsPattern), eq(KieTaskDetails.class), any(), any())).thenReturn(
                KieTaskTestHelper.createKieTaskDetails());

        PagedRestResponse<Task> tasks = kieTaskService.list(connection, user, new PagedListRequest());

        verify(restTemplate).getForObject(eq(connection.getUrl() + KieTaskService.TASK_LIST_URL
                + String.format("?user=%s&page=%d&pageSize=%d", username, 0, 100)), eq(KieTasksResponse.class));

        verify(restTemplate, times(1)).getForObject(matches(pInstanceVariablesPattern),
                eq(KieProcessVariablesResponse.class), eq(KieTaskTestHelper.PROCESS_INSTANCE_ID_1));

        verify(restTemplate, times(tasks.getPayload().size())).getForObject(matches(tDetailsPattern), eq(
                KieTaskDetails.class), any(), any());

        assertThat(tasks.getPayload()).isEqualTo(KieTaskTestHelper.createKieTaskListUser());
    }
}

