package org.entando.plugins.pda.pam.service.task;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.entando.plugins.pda.core.utils.TestUtils.NON_EXISTENT_TASK_ID;
import static org.entando.plugins.pda.core.utils.TestUtils.TASK_DEFINITION_ID;
import static org.entando.plugins.pda.core.utils.TestUtils.readFromFile;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.entando.plugins.pda.core.engine.Connection;
import org.entando.plugins.pda.core.exception.TaskNotFoundException;
import org.entando.plugins.pda.core.model.form.Form;
import org.entando.plugins.pda.pam.service.api.KieApiService;
import org.entando.plugins.pda.pam.service.util.KieInstanceId;
import org.entando.plugins.pda.pam.util.KieTaskFormTestHelper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kie.server.api.exception.KieServicesHttpException;
import org.kie.server.client.UIServicesClient;
import org.springframework.http.HttpStatus;

public class KieTaskFormServiceTest {

    private Connection connection;
    private KieTaskFormService kieTaskFormService;
    private UIServicesClient uiServicesClient;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        connection = Connection.builder().build();

        KieApiService kieApiService = mock(KieApiService.class);
        uiServicesClient = mock(UIServicesClient.class);

        when(kieApiService.getUiServicesClient(connection)).thenReturn(uiServicesClient);

        kieTaskFormService = new KieTaskFormService(kieApiService);
    }

    @Test
    public void shouldGetTaskForm() {
        KieInstanceId taskId = new KieInstanceId(TASK_DEFINITION_ID);

        // Given
        List<Form> expected = KieTaskFormTestHelper.createTaskForms();
        when(uiServicesClient.getTaskForm(anyString(), anyLong()))
            .thenReturn(readFromFile("task-form.json"));

        // When
        List<Form> result = kieTaskFormService.getTaskForm(connection, taskId.toString());

        // Then
        assertThat(result).isEqualTo(expected);
        verify(uiServicesClient)
                .getTaskForm(taskId.getContainerId(), taskId.getInstanceId());
    }

    @Test
    public void shouldThrowTaskNotFound() {
        when(uiServicesClient.getTaskForm(anyString(), anyLong()))
                .thenThrow(new KieServicesHttpException(null, HttpStatus.NOT_FOUND.value(), null, null));

        expectedException.expect(TaskNotFoundException.class);

        kieTaskFormService.getTaskForm(connection, NON_EXISTENT_TASK_ID);
    }

}
