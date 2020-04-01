package org.entando.plugins.pda.pam.service.task;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.entando.plugins.pda.core.utils.TestUtils.CONTAINER_ID_1;
import static org.entando.plugins.pda.core.utils.TestUtils.TASK_ID_1;
import static org.entando.plugins.pda.core.utils.TestUtils.createSimpleTaskForm;
import static org.entando.plugins.pda.core.utils.TestUtils.getDummyConnection;
import static org.entando.plugins.pda.core.utils.TestUtils.getDummyUser;
import static org.entando.plugins.pda.core.utils.TestUtils.randomLongId;
import static org.entando.plugins.pda.core.utils.TestUtils.randomStringId;
import static org.entando.plugins.pda.core.utils.TestUtils.readFromFile;
import static org.entando.plugins.pda.pam.service.util.KieUtils.createFormSubmission;
import static org.entando.plugins.pda.pam.util.KieFormTestHelper.trimIgnoreProperties;
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
import org.entando.plugins.pda.core.engine.Connection;
import org.entando.plugins.pda.core.exception.TaskNotFoundException;
import org.entando.plugins.pda.core.model.form.Form;
import org.entando.plugins.pda.pam.exception.KieInvalidResponseException;
import org.entando.plugins.pda.pam.service.api.KieApiService;
import org.entando.plugins.pda.pam.service.util.KieInstanceId;
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
    private static final String TASK_FORM_JSON_1 = "form/simple-task-form.json";
    private static final String SUBMIT_TASK_FORM_JSON_1 = "form/simple-task-form-submission.json";
    private static final String KIE_SUBMIT_TASK_FORM_JSON_1 = "form/simple-kie-task-form-submission.json";

    private static final String TASK_FORM_JSON_2 = "form/full-task-form.json";
    private static final String SUBMIT_TASK_FORM_JSON_2 = "form/full-task-form-submission.json";
    private static final String KIE_SUBMIT_TASK_FORM_JSON_2 = "form/full-kie-task-form-submission.json";

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
    public void shouldGetSimpleTaskForm() {
        KieInstanceId taskId = new KieInstanceId(CONTAINER_ID_1, TASK_ID_1);

        // Given
        Form expected = createSimpleTaskForm();
        when(uiServicesClient.getTaskForm(anyString(), anyLong()))
            .thenReturn(readFromFile(TASK_FORM_JSON_1));

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
    public void shouldThrowKieInvalidResponseWhenGetFormWithInvalidContainerId() {
        KieInstanceId taskId = new KieInstanceId(randomStringId(), randomLongId());

        //Given
        expectedException.expect(KieInvalidResponseException.class);

        when(uiServicesClient.getTaskForm(anyString(), anyLong()))
                .thenThrow(new KieServicesHttpException(null, HttpStatus.INTERNAL_SERVER_ERROR.value(), null, null));

        //When
        kieTaskFormService.get(getDummyConnection(), taskId.toString());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldSubmitSimpleTaskForm() throws Exception {
        // Given
        KieInstanceId taskId = new KieInstanceId(CONTAINER_ID_1, TASK_ID_1);

        Map<String, Object> request = MAPPER.readValue(
                readFromFile(SUBMIT_TASK_FORM_JSON_1), Map.class);
        Map<String, Object> kieRequest = MAPPER.readValue(
                readFromFile(KIE_SUBMIT_TASK_FORM_JSON_1), Map.class);

        when(uiServicesClient.getTaskForm(anyString(), anyLong()))
                .thenReturn(readFromFile(TASK_FORM_JSON_1));

        // When
        String result = kieTaskFormService.submit(getDummyConnection(), getDummyUser(), taskId.toString(), request);

        // Then
        assertThat(result).isEqualTo(taskId.toString());
        verify(taskServicesClient).saveTaskContent(eq(taskId.getContainerId()), eq(taskId.getInstanceId()),
                eq(kieRequest));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldSubmitFullTaskForm() throws Exception {
        // Given
        KieInstanceId taskId = new KieInstanceId(CONTAINER_ID_1, TASK_ID_1);

        Map<String, Object> request = MAPPER.readValue(
                readFromFile(SUBMIT_TASK_FORM_JSON_2), Map.class);
        Map<String, Object> kieRequest = trimIgnoreProperties(MAPPER.readValue(
                readFromFile(KIE_SUBMIT_TASK_FORM_JSON_2), Map.class));

        when(uiServicesClient.getTaskForm(anyString(), anyLong()))
                .thenReturn(readFromFile(TASK_FORM_JSON_2));

        // When
        Form form = kieTaskFormService.get(Connection.builder().build(), taskId.toString());
        Map<String, Object> submission = trimIgnoreProperties(createFormSubmission(form, request));
        String result = kieTaskFormService.submit(getDummyConnection(), getDummyUser(), taskId.toString(), request);

        // Then
        assertThat(result).isEqualTo(taskId.toString());
        verify(taskServicesClient).saveTaskContent(eq(taskId.getContainerId()), eq(taskId.getInstanceId()),
                anyMap());

        assertThat(submission).isEqualTo(kieRequest);
    }

    @Test
    public void shouldThrowTaskNotFoundWhenSubmitFormWithInvalidTaskId() {
        //Given
        KieInstanceId taskId = new KieInstanceId(randomStringId(), randomLongId());

        expectedException.expect(TaskNotFoundException.class);

        when(uiServicesClient.getTaskForm(anyString(), anyLong()))
                .thenReturn(readFromFile(TASK_FORM_JSON_1));

        when(taskServicesClient.saveTaskContent(anyString(), anyLong(), anyMap()))
                .thenThrow(new KieServicesHttpException(null, HttpStatus.NOT_FOUND.value(), null, null));

        //When
        kieTaskFormService.submit(getDummyConnection(), getDummyUser(), taskId.toString(), new ConcurrentHashMap<>());
    }

    @Test
    public void shouldThrowKieInvalidResponseWhenSubmitFormWithInvalidContainerId() {
        //Given
        KieInstanceId taskId = new KieInstanceId(randomStringId(), randomLongId());

        expectedException.expect(KieInvalidResponseException.class);

        when(uiServicesClient.getTaskForm(anyString(), anyLong()))
                .thenReturn(readFromFile(TASK_FORM_JSON_1));

        when(taskServicesClient.saveTaskContent(anyString(), anyLong(), anyMap()))
                .thenThrow(new KieServicesHttpException(null, HttpStatus.INTERNAL_SERVER_ERROR.value(), null, null));

        //When
        kieTaskFormService.submit(getDummyConnection(), getDummyUser(), taskId.toString(), new ConcurrentHashMap<>());
    }

}
