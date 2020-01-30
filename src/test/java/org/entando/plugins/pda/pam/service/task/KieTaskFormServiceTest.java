package org.entando.plugins.pda.pam.service.task;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.entando.plugins.pda.core.utils.TestUtils.CONTAINER_ID_1;
import static org.entando.plugins.pda.core.utils.TestUtils.TASK_ID_1;
import static org.entando.plugins.pda.core.utils.TestUtils.getDummyConnection;
import static org.entando.plugins.pda.core.utils.TestUtils.getDummyUser;
import static org.entando.plugins.pda.core.utils.TestUtils.randomLongId;
import static org.entando.plugins.pda.core.utils.TestUtils.randomStringId;
import static org.entando.plugins.pda.core.utils.TestUtils.readFromFile;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
import org.kie.server.client.UserTaskServicesClient;
import org.springframework.http.HttpStatus;

@SuppressWarnings("PMD.ExcessiveImports")
public class KieTaskFormServiceTest {

    private KieTaskFormService kieTaskFormService;
    private UIServicesClient uiServicesClient;
    private UserTaskServicesClient taskServicesClient;

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String TASK_FORM_JSON = "task-form.json";
    private static final String SUBMIT_TASK_FORM_JSON = "task-form-submission.json";
    private static final String KIE_SUBMIT_TASK_FORM_JSON = "kie-task-form-submission.json";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        KieApiService kieApiService = mock(KieApiService.class);
        uiServicesClient = mock(UIServicesClient.class);
        taskServicesClient = mock(UserTaskServicesClient.class);

        when(kieApiService.getUiServicesClient(any())).thenReturn(uiServicesClient);
        when(kieApiService.getUserTaskServicesClient(any())).thenReturn(taskServicesClient);

        kieTaskFormService = new KieTaskFormService(kieApiService);
    }

    @Test
    public void shouldGetTaskForm() {
        KieInstanceId taskId = new KieInstanceId(CONTAINER_ID_1, TASK_ID_1);

        // Given
        Form expected = KieTaskFormTestHelper.createTaskForm();
        when(uiServicesClient.getTaskForm(anyString(), anyLong()))
            .thenReturn(readFromFile(TASK_FORM_JSON));

        // When
        Form result = kieTaskFormService.get(getDummyConnection(), taskId.toString());

        // Then
        assertThat(result).isEqualTo(expected);
        verify(uiServicesClient)
                .getTaskForm(taskId.getContainerId(), taskId.getInstanceId());
    }

    @Test
    public void shouldThrowTaskNotFoundWhenGetFormWithInvalidTaskId() {
        KieInstanceId taskId = new KieInstanceId(randomStringId(), randomLongId());

        //Given
        expectedException.expect(TaskNotFoundException.class);

        when(uiServicesClient.getTaskForm(anyString(), anyLong()))
                .thenThrow(new KieServicesHttpException(null, HttpStatus.NOT_FOUND.value(), null, null));

        //When
        kieTaskFormService.get(getDummyConnection(), taskId.toString());
    }

    @Test
    public void shouldThrowTaskNotFoundWhenGetFormWithInvalidContainerId() {
        KieInstanceId taskId = new KieInstanceId(randomStringId(), randomLongId());

        //Given
        expectedException.expect(TaskNotFoundException.class);

        when(uiServicesClient.getTaskForm(anyString(), anyLong()))
                .thenThrow(new KieServicesHttpException(null, HttpStatus.INTERNAL_SERVER_ERROR.value(), null, null));

        //When
        kieTaskFormService.get(getDummyConnection(), taskId.toString());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldSubmitTaskForm() throws Exception {
        // Given
        KieInstanceId taskId = new KieInstanceId(CONTAINER_ID_1, TASK_ID_1);

        Map<String, Object> request = MAPPER.readValue(
                readFromFile(SUBMIT_TASK_FORM_JSON), Map.class);
        Map<String, Object> kieRequest = MAPPER.readValue(
                readFromFile(KIE_SUBMIT_TASK_FORM_JSON), Map.class);

        when(uiServicesClient.getTaskForm(anyString(), anyLong()))
                .thenReturn(readFromFile(TASK_FORM_JSON));

        // When
        String result = kieTaskFormService.submit(getDummyConnection(), getDummyUser(), taskId.toString(), request);

        // Then
        assertThat(result).isEqualTo(taskId.toString());
        verify(taskServicesClient).saveTaskContent(eq(taskId.getContainerId()), eq(taskId.getInstanceId()),
                eq(kieRequest));
    }

    @Test
    public void shouldThrowTaskNotFoundWhenSubmitFormWithInvalidTaskId() {
        //Given
        KieInstanceId taskId = new KieInstanceId(randomStringId(), randomLongId());

        expectedException.expect(TaskNotFoundException.class);

        when(uiServicesClient.getTaskForm(anyString(), anyLong()))
                .thenReturn(readFromFile(TASK_FORM_JSON));

        when(taskServicesClient.saveTaskContent(anyString(), anyLong(), anyMap()))
                .thenThrow(new KieServicesHttpException(null, HttpStatus.NOT_FOUND.value(), null, null));

        //When
        kieTaskFormService.submit(getDummyConnection(), getDummyUser(), taskId.toString(), new ConcurrentHashMap<>());
    }

    @Test
    public void shouldThrowNotFoundWhenSubmitFormWithInvalidContainerId() {
        //Given
        KieInstanceId taskId = new KieInstanceId(randomStringId(), randomLongId());

        expectedException.expect(TaskNotFoundException.class);

        when(uiServicesClient.getTaskForm(anyString(), anyLong()))
                .thenReturn(readFromFile(TASK_FORM_JSON));

        when(taskServicesClient.saveTaskContent(anyString(), anyLong(), anyMap()))
                .thenThrow(new KieServicesHttpException(null, HttpStatus.INTERNAL_SERVER_ERROR.value(), null, null));

        //When
        kieTaskFormService.submit(getDummyConnection(), getDummyUser(), taskId.toString(), new ConcurrentHashMap<>());
    }

}
