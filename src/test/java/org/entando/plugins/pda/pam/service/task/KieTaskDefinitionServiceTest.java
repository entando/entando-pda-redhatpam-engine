package org.entando.plugins.pda.pam.service.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.entando.plugins.pda.core.utils.TestUtils.getDummyConnection;
import static org.entando.plugins.pda.pam.service.task.KieTaskService.TASK_LIST_URL;
import static org.entando.plugins.pda.pam.util.KieTaskTestHelper.mockTasksRequest;
import static org.entando.plugins.pda.pam.util.KieTaskTestHelper.mockVariablesRequest;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.ExpectedCount.manyTimes;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import org.entando.plugins.pda.pam.service.task.model.KieTask;
import org.entando.plugins.pda.pam.service.task.model.KieTasksResponse;
import org.entando.plugins.pda.pam.util.KieTaskTestHelper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

public class KieTaskDefinitionServiceTest {

    private KieTaskDefinitionService taskDefinitionService;

    private MockRestServiceServer mockServer;
    private final ObjectMapper mapper = new ObjectMapper();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.sssZ", Locale.getDefault());
        mapper.setDateFormat(df);

        RestTemplate restTemplate = new RestTemplate();
        RestTemplateBuilder restTemplateBuilder = mock(RestTemplateBuilder.class);
        when(restTemplateBuilder.basicAuthorization(anyString(), anyString())).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        mockServer = MockRestServiceServer.createServer(restTemplate);

        KieTaskService taskService = new KieTaskService(restTemplateBuilder);
        taskDefinitionService = new KieTaskDefinitionService(taskService);
    }

    @Test
    public void shouldReturnTaskColumns() throws Exception {
        // Given
        mockServer.expect(requestTo(containsString(TASK_LIST_URL)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(
                                new KieTasksResponse(KieTaskTestHelper.createKieTaskListWithEmbeddedData())),
                        MediaType.APPLICATION_JSON));
        mockVariablesRequest(mockServer, mapper, once());
        mockTasksRequest(mockServer, mapper, manyTimes());

        // When
        Set<String> response = taskDefinitionService.listColumns(getDummyConnection(), null);

        // Then
        assertThat(response)
                .contains(KieTask.ID, KieTask.NAME, KieTask.PROCESS_ID, KieTask.CONTAINER_ID,
                        KieTaskTestHelper.FIELD_1 + "." + KieTaskTestHelper.FIELD_2);
    }

    @Test
    public void shouldReturnEmptyTaskColumnsWhenThereIsNoTaskInTheList() throws Exception {
        // Given
        mockServer.expect(requestTo(containsString(TASK_LIST_URL)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(new KieTasksResponse(Collections.emptyList())),
                        MediaType.APPLICATION_JSON));
        mockVariablesRequest(mockServer, mapper, once());
        mockTasksRequest(mockServer, mapper, manyTimes());

        // When
        Set<String> response = taskDefinitionService.listColumns(getDummyConnection(), null);

        // Then
        assertThat(response).isEmpty();
    }
}
