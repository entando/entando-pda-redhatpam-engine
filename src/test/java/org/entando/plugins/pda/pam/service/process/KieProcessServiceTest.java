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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.drools.core.io.impl.ClassPathResource;
import org.entando.plugins.pda.core.engine.Connection;
import org.entando.plugins.pda.core.exception.ProcessNotFoundException;
import org.entando.plugins.pda.core.model.ProcessDefinition;
import org.entando.plugins.pda.core.model.form.Form;
import org.entando.plugins.pda.core.model.form.FormFieldType;
import org.entando.plugins.pda.pam.exception.KieInvalidIdException;
import org.entando.plugins.pda.pam.service.KieUtils;
import org.entando.plugins.pda.pam.service.api.KieApiService;
import org.entando.plugins.pda.pam.service.process.model.KieProcessDefinition;
import org.entando.plugins.pda.pam.service.task.model.KieProcessDefinitionsResponse;
import org.entando.plugins.pda.pam.util.KieProcessTestHelper;
import org.entando.web.exception.InternalServerException;
import org.entando.web.request.PagedListRequest;
import org.junit.Assert;
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

@SuppressWarnings("PMD.ExcessiveImports")
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

    @Test
    public void deserializeProcessFormJson() {

        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Form.class, new FormDeserializer());
        mapper.registerModule(module);

        List<Form> result = new ArrayList<>();

        String json = new String(new ClassPathResource("process-form.json").getBytes());

        try {
            JsonNode parentNode = mapper.readTree(json);

            for (JsonNode childNode : parentNode) {

                Form form  = mapper.treeToValue(childNode, Form.class);

                if (form.getFields().size() > 0) {
                    result.add(mapper.treeToValue(childNode, Form.class));
                }
            }

            assertThat(result.size()).isEqualTo(3);

            assertThat(result.get(0).getId()).isEqualTo("2aeaf281-71e1-45a5-9ab3-0abd855d924e");
            assertThat(result.get(0).getName()).isEqualTo("Property");
            assertThat(result.get(0).getFields().size()).isEqualTo(4);
            assertThat(result.get(0).getFields().get(0).getId()).isEqualTo("field_815717729253767E11");
            assertThat(result.get(0).getFields().get(0).getName()).isEqualTo("age");
            assertThat(result.get(0).getFields().get(0).getLabel()).isEqualTo("Age of property");
            assertThat(result.get(0).getFields().get(0).getRequired()).isFalse();
            assertThat(result.get(0).getFields().get(0).getReadOnly()).isFalse();
            assertThat(result.get(0).getFields().get(0).getType()).isEqualTo(FormFieldType.INTEGER);
            assertThat(result.get(0).getFields().get(0).getPlaceholder()).isEqualTo("Age of property");

            assertThat(result.get(1).getId()).isEqualTo("b71de860-4d3e-4b0c-95e9-c41e4d06f787");
            assertThat(result.get(1).getName()).isEqualTo("Application");
            assertThat(result.get(1).getFields().size()).isEqualTo(2);
            assertThat(result.get(1).getFields().get(0).getId()).isEqualTo("field_290268943445829E11");
            assertThat(result.get(1).getFields().get(0).getName()).isEqualTo("downpayment");
            assertThat(result.get(1).getFields().get(0).getLabel()).isEqualTo("Down Payment");
            assertThat(result.get(1).getFields().get(0).getRequired()).isFalse();
            assertThat(result.get(1).getFields().get(0).getReadOnly()).isFalse();
            assertThat(result.get(1).getFields().get(0).getType()).isEqualTo(FormFieldType.INTEGER);
            assertThat(result.get(1).getFields().get(0).getPlaceholder()).isEqualTo("Down Payment");

            assertThat(result.get(2).getId()).isEqualTo("0cb94115-b991-4dbe-a342-00d99a1cdd2d");
            assertThat(result.get(2).getName()).isEqualTo("Applicant");
            assertThat(result.get(2).getFields().size()).isEqualTo(3);
            assertThat(result.get(2).getFields().get(0).getId()).isEqualTo("field_922175737010885E11");
            assertThat(result.get(2).getFields().get(0).getName()).isEqualTo("name");
            assertThat(result.get(2).getFields().get(0).getLabel()).isEqualTo("Name");
            assertThat(result.get(2).getFields().get(0).getRequired()).isFalse();
            assertThat(result.get(2).getFields().get(0).getReadOnly()).isFalse();
            assertThat(result.get(2).getFields().get(0).getType()).isEqualTo(FormFieldType.STRING);
            assertThat(result.get(2).getFields().get(0).getPlaceholder()).isEqualTo("Name");

        } catch (IOException e) {
            Assert.fail();
        }
    }
}
