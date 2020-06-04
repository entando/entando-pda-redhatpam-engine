package org.entando.plugins.pda.pam.service.process;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.entando.plugins.pda.core.utils.TestUtils.PROCESS_DEFINITION_ID;
import static org.entando.plugins.pda.core.utils.TestUtils.createFullProcessForm;
import static org.entando.plugins.pda.core.utils.TestUtils.createSimpleProcessForm;
import static org.entando.plugins.pda.core.utils.TestUtils.getDummyUser;
import static org.entando.plugins.pda.core.utils.TestUtils.randomLongId;
import static org.entando.plugins.pda.core.utils.TestUtils.readFromFile;
import static org.entando.plugins.pda.pam.service.process.KieProcessFormService.INITIATOR_VAR;
import static org.entando.plugins.pda.pam.service.util.KieUtils.createFormSubmission;
import static org.entando.plugins.pda.pam.util.KieFormTestHelper.trimIgnoreProperties;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.entando.keycloak.security.AuthenticatedUser;
import org.entando.plugins.pda.core.engine.Connection;
import org.entando.plugins.pda.core.exception.ProcessDefinitionNotFoundException;
import org.entando.plugins.pda.core.model.form.Form;
import org.entando.plugins.pda.pam.exception.KieInvalidResponseException;
import org.entando.plugins.pda.pam.service.api.KieApiService;
import org.entando.plugins.pda.pam.service.util.KieDefinitionId;
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

    private static final String PROCESS_FORM_JSON_1 = "form/simple-process-form.json";
    private static final String SUBMIT_PROCESS_FORM_JSON_1 = "form/simple-process-form-submission.json";
    private static final String KIE_SUBMIT_PROCESS_FORM_JSON_1 = "form/simple-kie-process-form-submission.json";

    private static final String PROCESS_FORM_JSON_2 = "form/full-process-form.json";
    private static final String SUBMIT_PROCESS_FORM_JSON_2 = "form/full-process-form-submission.json";
    private static final String KIE_SUBMIT_PROCESS_FORM_JSON_2 = "form/full-kie-process-form-submission.json";

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
    public void shouldGetSimpleProcessForm() {

        KieDefinitionId processId = new KieDefinitionId(PROCESS_DEFINITION_ID);

        // Given
        Form expected = createSimpleProcessForm();
        when(uiServicesClient.getProcessForm(anyString(), anyString())).thenReturn(readFromFile(PROCESS_FORM_JSON_1));

        // When
        Form result = kieProcessFormService.get(connection, processId.toString());

        // Then
        assertThat(result).isEqualTo(expected);
        verify(uiServicesClient).getProcessForm(processId.getContainerId(), processId.getDefinitionId());
    }

    @Test
    public void shouldGetFullProcessForm() {

        KieDefinitionId processId = new KieDefinitionId(PROCESS_DEFINITION_ID);

        // Given
        Form expected = createFullProcessForm();
        when(uiServicesClient.getProcessForm(anyString(), anyString())).thenReturn(readFromFile(PROCESS_FORM_JSON_2));

        // When
        Form result = kieProcessFormService.get(connection, processId.toString());

        // Then
        assertThat(result).isEqualTo(expected);
        verify(uiServicesClient).getProcessForm(processId.getContainerId(), processId.getDefinitionId());
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
    public void shouldSubmitSimpleProcessForm() throws Exception {

        KieDefinitionId processId = new KieDefinitionId(PROCESS_DEFINITION_ID);

        // Given
        Map<String, Object> request = MAPPER.readValue(readFromFile(SUBMIT_PROCESS_FORM_JSON_1), Map.class);
        Map<String, Object> kieRequest = MAPPER.readValue(readFromFile(KIE_SUBMIT_PROCESS_FORM_JSON_1), Map.class);

        Long expected = randomLongId();

        when(processServicesClient.startProcess(anyString(), anyString(), anyMap())).thenReturn(expected);

        when(uiServicesClient.getProcessForm(anyString(), anyString())).thenReturn(readFromFile(PROCESS_FORM_JSON_1));

        // When
        AuthenticatedUser user = getDummyUser();
        String processInstanceId = kieProcessFormService.submit(connection, processId.toString(), request, user);

        // Then
        verify(processServicesClient).startProcess(processId.getContainerId(), processId.getDefinitionId(), kieRequest);
        verify(processServicesClient)
                .setProcessVariable(processId.getContainerId(), Long.valueOf(processInstanceId), INITIATOR_VAR,
                        user.getAccessToken().getPreferredUsername());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldSubmitFullProcessForm() throws Exception {
        KieDefinitionId processId = new KieDefinitionId(PROCESS_DEFINITION_ID);

        // Given
        Map<String, Object> request = MAPPER.readValue(readFromFile(SUBMIT_PROCESS_FORM_JSON_2), Map.class);
        Map<String, Object> expected = trimIgnoreProperties(
                MAPPER.readValue(readFromFile(KIE_SUBMIT_PROCESS_FORM_JSON_2), Map.class));

        when(processServicesClient.startProcess(anyString(), anyString(), anyMap())).thenReturn(randomLongId());

        when(uiServicesClient.getProcessForm(anyString(), anyString())).thenReturn(readFromFile(PROCESS_FORM_JSON_2));

        // When
        Form form = kieProcessFormService.get(connection, processId.toString());
        Map<String, Object> submission = trimIgnoreProperties(createFormSubmission(form, request, true));
        AuthenticatedUser user = getDummyUser();
        String processInstanceId = kieProcessFormService.submit(connection, processId.toString(), request, user);

        // Then
        verify(processServicesClient)
                .startProcess(eq(processId.getContainerId()), eq(processId.getDefinitionId()), anyMap());
        verify(processServicesClient)
                .setProcessVariable(processId.getContainerId(), Long.valueOf(processInstanceId), INITIATOR_VAR,
                        user.getAccessToken().getPreferredUsername());

        assertThat(submission).isEqualTo(expected);
    }

    @Test
    public void shouldThrowNotFoundWhenSubmitProcessFormWithInvalidProcessDefinitionId() {
        when(uiServicesClient.getProcessForm(anyString(), anyString())).thenReturn(readFromFile(PROCESS_FORM_JSON_1));

        when(processServicesClient.startProcess(anyString(), anyString(), anyMap()))
                .thenThrow(new KieServicesHttpException(null, HttpStatus.NOT_FOUND.value(), null, null));

        expectedException.expect(ProcessDefinitionNotFoundException.class);

        kieProcessFormService.submit(connection, PROCESS_DEFINITION_ID, new HashMap<>(), getDummyUser());
    }

    @Test
    public void shouldThrowKieInvalidResponseWhenSubmitProcessFormWithInvalidContainerId() {
        when(uiServicesClient.getProcessForm(anyString(), anyString())).thenReturn(readFromFile(PROCESS_FORM_JSON_1));

        when(processServicesClient.startProcess(anyString(), anyString(), anyMap()))
                .thenThrow(new KieServicesHttpException(null, HttpStatus.INTERNAL_SERVER_ERROR.value(), null, null));

        expectedException.expect(KieInvalidResponseException.class);

        kieProcessFormService.submit(connection, PROCESS_DEFINITION_ID, new HashMap<>(), getDummyUser());
    }
}
