package org.entando.plugins.pda.pam.service.task;

import org.assertj.core.groups.Tuple;
import org.entando.plugins.pda.core.engine.Connection;
import org.entando.plugins.pda.core.model.Task;
import org.entando.plugins.pda.pam.service.task.model.KieProcessVariablesResponse;
import org.entando.plugins.pda.pam.service.task.model.KieTasksResponse;
import org.entando.plugins.pda.pam.util.KieTaskTestHelper;
import org.entando.web.request.PagedListRequest;
import org.entando.web.response.PagedRestResponse;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

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

        Pattern taskPropertiesUrlPattern = Pattern.compile(connection.getUrl() + KieTaskService.TASK_PROPERTIES_URL.replace("{pInstanceId}", ".+"));

        when(restTemplate.getForObject(anyString(), any())).thenReturn(
                new KieTasksResponse(KieTaskTestHelper.createKieTaskList()));

        when(restTemplate.getForObject(matches(taskPropertiesUrlPattern), eq(KieProcessVariablesResponse.class), anyString())).thenReturn(
                new KieProcessVariablesResponse(KieTaskTestHelper.createKieProcessVariables(1L)));

        PagedRestResponse<Task> tasks = kieTaskService.list(connection, new PagedListRequest());

        verify(restTemplate).getForObject(matches(connection.getUrl() + KieTaskService.TASK_LIST_URL), any());
        verify(restTemplate, times(tasks.getPayload().size())).getForObject(matches(taskPropertiesUrlPattern), eq(KieProcessVariablesResponse.class), anyString());

        assertThat(tasks.getPayload()).extracting(Task::getId, Task::getName)
                .containsExactlyInAnyOrder(
                        new Tuple(KieTaskTestHelper.TASK_ID_1, KieTaskTestHelper.TASK_NAME_1),
                        new Tuple(KieTaskTestHelper.TASK_ID_2, KieTaskTestHelper.TASK_NAME_2));

        Map<String, String> extraVars = new HashMap<>();
        extraVars.put(KieTaskTestHelper.EXTRA_VARS_ATTRIBUTE_1, KieTaskTestHelper.EXTRA_VARS_VALUE_1);
        extraVars.put(KieTaskTestHelper.EXTRA_VARS_ATTRIBUTE_2, KieTaskTestHelper.EXTRA_VARS_VALUE_2);

        for (Task task : tasks.getPayload()) {
            assertThat(task.getProperties()).isEqualTo(extraVars);
        }
    }
}

