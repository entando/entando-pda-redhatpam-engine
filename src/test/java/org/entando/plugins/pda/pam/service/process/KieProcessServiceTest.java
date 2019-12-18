package org.entando.plugins.pda.pam.service.process;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.entando.plugins.pda.core.engine.Connection;
import org.entando.plugins.pda.core.exception.ProcessNotFoundException;
import org.entando.plugins.pda.core.model.ProcessDefinition;
import org.entando.plugins.pda.pam.exception.KieInvalidIdException;
import org.entando.plugins.pda.pam.service.KieUtils;
import org.entando.plugins.pda.pam.service.api.KieApiService;
import org.entando.plugins.pda.pam.service.process.model.KieProcessDefinition;
import org.entando.plugins.pda.pam.service.task.model.KieProcessDefinitionsResponse;
import org.entando.plugins.pda.pam.util.KieProcessTestHelper;
import org.entando.web.exception.InternalServerException;
import org.entando.web.request.PagedListRequest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kie.server.api.exception.KieServicesHttpException;
import org.kie.server.client.UIServicesClient;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

public class KieProcessServiceTest {

    private Connection connection;
    private KieProcessService kieProcessService;
    private UIServicesClient uiServicesClient;

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

        connection = Connection.builder().build();

        KieApiService kieApiService = mock(KieApiService.class);
        uiServicesClient = mock(UIServicesClient.class);

        when(kieApiService.getUiServicesClient(connection)).thenReturn(uiServicesClient);

        kieProcessService = new KieProcessService(restTemplateBuilder, kieApiService);
    }

    @Test
    public void shouldListProcessDefinitions() throws Exception {
        // Given
        PagedListRequest pageRequest = new PagedListRequest(1, KieProcessService.MAX_KIE_PAGE_SIZE, null, null);
        mockProcessDefinitionList(pageRequest);

        pageRequest.setPage(2);
        mockProcessDefinitionList(pageRequest);

        // When
        List<ProcessDefinition> processDefinitions = kieProcessService.listDefinitions(dummyConnection());

        // Then
        mockServer.verify();
        assertThat(processDefinitions).isEqualTo(KieProcessTestHelper.createKieProcessDefinitionList());
    }

    @Test
    public void shouldGetProcessDiagram() {
        // Given
        when(uiServicesClient.getProcessInstanceImage(
                KieProcessTestHelper.PROCESS_CONTAINER_ID_1, Long.valueOf(KieProcessTestHelper.PROCESS_ID_1)))
            .thenReturn(KieProcessTestHelper.PROCESS_DIAGRAM__1);

        // When
        String diagram = kieProcessService.getProcessDiagram(connection, KieProcessTestHelper.PROCESS_PDA_ID);

        // Then
        assertThat(diagram).isEqualTo(KieProcessTestHelper.PROCESS_DIAGRAM__1);
    }

    @Test
    public void shouldThrowNotFoundWhenGetProcessDiagram() {
        // Given
        when(uiServicesClient.getProcessInstanceImage(any(), anyLong()))
                .thenThrow(new KieServicesHttpException(null, HttpStatus.NOT_FOUND.value(), null, null));

        // Then
        expectedException.expect(ProcessNotFoundException.class);

        // When
        kieProcessService.getProcessDiagram(connection, "1@invalid");
    }

    @Test
    public void shouldThrowInternalErrorWhenGetProcessDiagram() {
        // Given
        when(uiServicesClient.getProcessInstanceImage(any(), anyLong()))
                .thenThrow(new KieServicesHttpException(null, HttpStatus.INTERNAL_SERVER_ERROR.value(), null, null));

        // Then
        expectedException.expect(InternalServerException.class);

        // When
        kieProcessService.getProcessDiagram(connection, KieProcessTestHelper.PROCESS_PDA_ID);
    }

    @Test
    public void shouldThrowBadRequestWhenGetProcessDiagram1() {
        expectedException.expect(KieInvalidIdException.class);

        kieProcessService.getProcessDiagram(connection, "1-process_1");
    }

    @Test
    public void shouldThrowBadRequestWhenGetProcessDiagram2() {
        expectedException.expect(KieInvalidIdException.class);

        kieProcessService.getProcessDiagram(connection, "not_a_number@process-1");
    }

    private void mockProcessDefinitionList(PagedListRequest pageRequest)
            throws JsonProcessingException {

        List<KieProcessDefinition> expectedResponse = pageRequest.getPage() == 1
                ? KieProcessTestHelper.createKieProcessDefinitionList() : new ArrayList<>();

        mockServer.expect(requestTo(containsString(KieProcessService.PROCESS_DEFINITION_LIST_URL
                    + KieUtils.createFilters(pageRequest, true))))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(new KieProcessDefinitionsResponse(expectedResponse)),
                        MediaType.APPLICATION_JSON));
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
