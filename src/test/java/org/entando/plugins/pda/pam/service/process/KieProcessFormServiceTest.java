package org.entando.plugins.pda.pam.service.process;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.entando.plugins.pda.core.utils.TestUtils.PROCESS_DEFINITION_ID;
import static org.entando.plugins.pda.core.utils.TestUtils.randomLongId;
import static org.entando.plugins.pda.core.utils.TestUtils.readFromFile;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.entando.plugins.pda.core.engine.Connection;
import org.entando.plugins.pda.core.exception.ProcessDefinitionNotFoundException;
import org.entando.plugins.pda.core.model.form.Form;
import org.entando.plugins.pda.pam.service.api.KieApiService;
import org.entando.plugins.pda.pam.service.util.KieDefinitionId;
import org.entando.plugins.pda.pam.util.KieProcessFormTestHelper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kie.server.api.exception.KieServicesHttpException;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.UIServicesClient;
import org.springframework.http.HttpStatus;

public class KieProcessFormServiceTest {

    private Connection connection;
    private KieProcessFormService kieProcessFormService;
    private UIServicesClient uiServicesClient;
    private ProcessServicesClient processServicesClient;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String PROCESS_FORM_JSON_1 = "form/process-form-mortgage.json";
    private static final String SUBMIT_PROCESS_FORM_JSON_1 = "form/process-form-submission-mortgage.json";
    private static final String KIE_SUBMIT_PROCESS_FORM_JSON_1 = "form/kie-process-form-submission-mortgage.json";

    private static final String PROCESS_FORM_JSON_2 = "form/process-form-sample.json";
    private static final String SUBMIT_PROCESS_FORM_JSON_2 = "form/process-form-submission-sample.json";
    private static final String KIE_SUBMIT_PROCESS_FORM_JSON_2 = "form/kie-process-form-submission-sample.json";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        connection = Connection.builder().build();

        KieApiService kieApiService = mock(KieApiService.class);
        uiServicesClient = mock(UIServicesClient.class);
        processServicesClient = mock(ProcessServicesClient.class);

        when(kieApiService.getUiServicesClient(connection)).thenReturn(uiServicesClient);
        when(kieApiService.getProcessServicesClient(connection)).thenReturn(processServicesClient);

        kieProcessFormService = new KieProcessFormService(kieApiService);
    }

    @Test
    public void shouldGetMortgageProcessForm() {

        KieDefinitionId processId = new KieDefinitionId(PROCESS_DEFINITION_ID);

        // Given
        Form expected = KieProcessFormTestHelper.createMortgageProcessForm();
        when(uiServicesClient.getProcessForm(anyString(), anyString()))
            .thenReturn(readFromFile(PROCESS_FORM_JSON_1));

        // When
        Form result = kieProcessFormService.get(connection, processId.toString());

        // Then
        assertThat(result).isEqualTo(expected);
        verify(uiServicesClient)
                .getProcessForm(processId.getContainerId(), processId.getDefinitionId());
    }

    @Test
    public void shouldGetSampleProcessForm() {

        KieDefinitionId processId = new KieDefinitionId(PROCESS_DEFINITION_ID);

        // Given
        Form expected = KieProcessFormTestHelper.createSampleProcessForm();
        when(uiServicesClient.getProcessForm(anyString(), anyString()))
                .thenReturn(readFromFile(PROCESS_FORM_JSON_2));

        // When
        Form result = kieProcessFormService.get(connection, processId.toString());

        // Then
        assertThat(result).isEqualTo(expected);
        verify(uiServicesClient)
                .getProcessForm(processId.getContainerId(), processId.getDefinitionId());
    }

    @Test
    public void shouldThrowProcessDefinitionNotFoundWhenGetProcessForm() {
        when(uiServicesClient.getProcessForm(anyString(), anyString()))
                .thenThrow(new KieServicesHttpException(null, HttpStatus.NOT_FOUND.value(), null, null));

        expectedException.expect(ProcessDefinitionNotFoundException.class);

        kieProcessFormService.get(connection, PROCESS_DEFINITION_ID);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldSubmitMortgageProcessForm() throws Exception {

        KieDefinitionId processId = new KieDefinitionId(PROCESS_DEFINITION_ID);

        // Given
        Map<String, Object> request = MAPPER.readValue(
                readFromFile(SUBMIT_PROCESS_FORM_JSON_1), Map.class);
        Map<String, Object> kieRequest = MAPPER.readValue(
                readFromFile(KIE_SUBMIT_PROCESS_FORM_JSON_1), Map.class);

        Long expected = randomLongId();

        when(processServicesClient.startProcess(anyString(), anyString(), anyMap()))
                .thenReturn(expected);

        when(uiServicesClient.getProcessForm(anyString(), anyString()))
                .thenReturn(readFromFile(PROCESS_FORM_JSON_1));

        // When
        String result = kieProcessFormService.submit(connection, processId.toString(), request);

        // Then
        assertThat(result).isEqualTo(expected.toString());
        verify(processServicesClient)
                .startProcess(processId.getContainerId(), processId.getDefinitionId(), kieRequest);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldSubmitSampleProcessForm() throws Exception {

        KieDefinitionId processId = new KieDefinitionId(PROCESS_DEFINITION_ID);

        // Given
        Map<String, Object> request = MAPPER.readValue(
                readFromFile(SUBMIT_PROCESS_FORM_JSON_2), Map.class);
        Map<String, Object> kieRequest = MAPPER.readValue(
                readFromFile(KIE_SUBMIT_PROCESS_FORM_JSON_2), Map.class);

        Long expected = randomLongId();

        when(processServicesClient.startProcess(anyString(), anyString(), anyMap()))
                .thenReturn(expected);

        when(uiServicesClient.getProcessForm(anyString(), anyString()))
                .thenReturn(readFromFile(PROCESS_FORM_JSON_2));

        // When
        String result = kieProcessFormService.submit(connection, processId.toString(), request);

        // Then
        assertThat(result).isEqualTo(expected.toString());
        verify(processServicesClient)
                .startProcess(processId.getContainerId(), processId.getDefinitionId(), kieRequest);
    }

    @Test
    public void shouldThrowNotFoundWhenSubmitProcessFormWithInvalidProcessDefinitionId() {
        when(uiServicesClient.getProcessForm(anyString(), anyString()))
                .thenReturn(readFromFile(PROCESS_FORM_JSON_1));

        when(processServicesClient.startProcess(anyString(), anyString(), anyMap()))
                .thenThrow(new KieServicesHttpException(null, HttpStatus.NOT_FOUND.value(), null, null));

        expectedException.expect(ProcessDefinitionNotFoundException.class);

        kieProcessFormService.submit(connection, PROCESS_DEFINITION_ID, new HashMap<>());
    }

    @Test
    public void shouldThrowNotFoundWhenSubmitProcessFormWithInvalidContainerId() {
        when(uiServicesClient.getProcessForm(anyString(), anyString()))
                .thenReturn(readFromFile(PROCESS_FORM_JSON_1));

        when(processServicesClient.startProcess(anyString(), anyString(), anyMap()))
                .thenThrow(new KieServicesHttpException(null, HttpStatus.INTERNAL_SERVER_ERROR.value(), null, null));

        expectedException.expect(ProcessDefinitionNotFoundException.class);

        kieProcessFormService.submit(connection, PROCESS_DEFINITION_ID, new HashMap<>());
    }

}
