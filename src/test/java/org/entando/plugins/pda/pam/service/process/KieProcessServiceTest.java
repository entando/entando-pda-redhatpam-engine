package org.entando.plugins.pda.pam.service.process;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.entando.plugins.pda.core.engine.Connection;
import org.entando.plugins.pda.core.model.ProcessDefinition;
import org.entando.plugins.pda.pam.service.KieUtils;
import org.entando.plugins.pda.pam.service.process.model.KieProcessDefinition;
import org.entando.plugins.pda.pam.service.task.model.KieProcessDefinitionsResponse;
import org.entando.plugins.pda.pam.util.KieProcessTestHelper;
import org.entando.web.request.Filter;
import org.entando.web.request.PagedListRequest;
import org.junit.Before;
import org.junit.Test;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesFactory;
import org.mockito.Mockito;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

public class KieProcessServiceTest {

    private KieProcessService kieProcessService;

    private MockRestServiceServer mockServer;
    private final ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        RestTemplateBuilder restTemplateBuilder = Mockito.mock(RestTemplateBuilder.class);
        when(restTemplateBuilder.basicAuthorization(anyString(), anyString())).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        mockServer = MockRestServiceServer.createServer(restTemplate);

        kieProcessService = new KieProcessService(restTemplateBuilder);
    }

    @Test
    public void shouldListProcessDefinitions() throws Exception {
        // Given
        PagedListRequest pageRequest = new PagedListRequest(1, 2000000000, null, null);
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
    public void shouldListProcessDefinitionsWithFilterAndSort() throws Exception {
        // Given
        PagedListRequest pageRequest = new PagedListRequest(1, 2000000000, null, null);
        mockProcessDefinitionList(pageRequest);

        pageRequest.setPage(2);
        mockProcessDefinitionList(pageRequest);

        // When
        List<ProcessDefinition> processDefinitions = kieProcessService.listDefinitions(dummyConnection());

        // Then
        mockServer.verify();
        assertThat(processDefinitions).isEqualTo(KieProcessTestHelper.createKieProcessDefinitionList());
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
