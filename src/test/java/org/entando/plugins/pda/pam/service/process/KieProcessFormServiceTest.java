package org.entando.plugins.pda.pam.service.process;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.entando.plugins.pda.core.utils.TestUtils.PROCESS_DEFINITION_ID;
import static org.entando.plugins.pda.core.utils.TestUtils.readFromFile;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.entando.plugins.pda.core.engine.Connection;
import org.entando.plugins.pda.core.exception.ProcessNotFoundException;
import org.entando.plugins.pda.core.model.form.Form;
import org.entando.plugins.pda.pam.service.api.KieApiService;
import org.entando.plugins.pda.pam.service.util.KieDefinitionId;
import org.entando.plugins.pda.pam.util.KieProcessFormTestHelper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kie.server.api.exception.KieServicesHttpException;
import org.kie.server.client.UIServicesClient;
import org.springframework.http.HttpStatus;

public class KieProcessFormServiceTest {

    private Connection connection;
    private KieProcessFormService kieProcessFormService;
    private UIServicesClient uiServicesClient;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        connection = Connection.builder().build();

        KieApiService kieApiService = mock(KieApiService.class);
        uiServicesClient = mock(UIServicesClient.class);

        when(kieApiService.getUiServicesClient(connection)).thenReturn(uiServicesClient);

        kieProcessFormService = new KieProcessFormService(kieApiService);
    }

    @Test
    public void shouldGetProcessForm() {

        KieDefinitionId processId = new KieDefinitionId(PROCESS_DEFINITION_ID);

        // Given
        List<Form> expected = KieProcessFormTestHelper.createProcessForms();
        when(uiServicesClient.getProcessForm(anyString(), anyString()))
            .thenReturn(readFromFile("process-form.json"));

        // When
        List<Form> result = kieProcessFormService.getProcessForm(connection, processId.toString());

        // Then
        assertThat(result).isEqualTo(expected);
        verify(uiServicesClient)
                .getProcessForm(processId.getContainerId(), processId.getDefinitionId());
    }

    @Test
    public void shouldThrowProcessNotFound() {
        when(uiServicesClient.getProcessForm(anyString(), anyString()))
                .thenThrow(new KieServicesHttpException(null, HttpStatus.NOT_FOUND.value(), null, null));

        expectedException.expect(ProcessNotFoundException.class);

        kieProcessFormService.getProcessForm(connection, PROCESS_DEFINITION_ID);
    }

}
