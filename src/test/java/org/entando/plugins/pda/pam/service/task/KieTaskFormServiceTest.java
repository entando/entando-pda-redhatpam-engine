package org.entando.plugins.pda.pam.service.task;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.entando.plugins.pda.core.utils.TestUtils.CONTAINER_ID_1;
import static org.entando.plugins.pda.core.utils.TestUtils.TASK_FORM_ID_1;
import static org.entando.plugins.pda.core.utils.TestUtils.TASK_FORM_ID_2;
import static org.entando.plugins.pda.core.utils.TestUtils.TASK_FORM_PROP_1;
import static org.entando.plugins.pda.core.utils.TestUtils.TASK_FORM_PROP_2;
import static org.entando.plugins.pda.core.utils.TestUtils.TASK_FORM_PROP_3;
import static org.entando.plugins.pda.core.utils.TestUtils.TASK_FORM_PROP_KEY_1;
import static org.entando.plugins.pda.core.utils.TestUtils.TASK_FORM_PROP_KEY_2;
import static org.entando.plugins.pda.core.utils.TestUtils.TASK_FORM_PROP_KEY_3;
import static org.entando.plugins.pda.core.utils.TestUtils.TASK_ID_1;
import static org.entando.plugins.pda.core.utils.TestUtils.getDummyConnection;
import static org.entando.plugins.pda.core.utils.TestUtils.getDummyUser;
import static org.entando.plugins.pda.core.utils.TestUtils.randomLongId;
import static org.entando.plugins.pda.core.utils.TestUtils.randomStringId;
import static org.entando.plugins.pda.core.utils.TestUtils.readFromFile;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.entando.plugins.pda.core.exception.TaskNotFoundException;
import org.entando.plugins.pda.core.model.Task;
import org.entando.plugins.pda.core.model.form.Form;
import org.entando.plugins.pda.core.model.task.CreateTaskFormSubmissionRequest;
import org.entando.plugins.pda.pam.service.api.KieApiService;
import org.entando.plugins.pda.pam.service.util.KieInstanceId;
import org.entando.plugins.pda.pam.util.KieTaskFormTestHelper;
import org.entando.plugins.pda.pam.util.KieTaskTestHelper;
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

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        KieApiService kieApiService = mock(KieApiService.class);
        uiServicesClient = mock(UIServicesClient.class);
        taskServicesClient = mock(UserTaskServicesClient.class);

        when(kieApiService.getUiServicesClient(any())).thenReturn(uiServicesClient);
        when(kieApiService.getUserTaskServicesClient(any())).thenReturn(taskServicesClient);

        KieTaskService taskService = new KieTaskService(kieApiService);
        kieTaskFormService = new KieTaskFormService(taskService, kieApiService);
    }

    @Test
    public void shouldGetTaskForm() {
        KieInstanceId taskId = new KieInstanceId(CONTAINER_ID_1, TASK_ID_1);

        // Given
        List<Form> expected = KieTaskFormTestHelper.createTaskForms();
        when(uiServicesClient.getTaskForm(anyString(), anyLong()))
            .thenReturn(readFromFile("task-form.json"));

        // When
        List<Form> result = kieTaskFormService.get(getDummyConnection(), taskId.toString());

        // Then
        assertThat(result).isEqualTo(expected);
        verify(uiServicesClient)
                .getTaskForm(taskId.getContainerId(), taskId.getInstanceId());
    }

    @Test
    public void shouldThrowTaskNotFoundWhenGetForm() {
        KieInstanceId taskId = new KieInstanceId(randomStringId(), randomLongId());

        //Given
        expectedException.expect(TaskNotFoundException.class);

        when(uiServicesClient.getTaskForm(anyString(), anyLong()))
                .thenThrow(new KieServicesHttpException(null, HttpStatus.NOT_FOUND.value(), null, null));

        //When
        kieTaskFormService.get(getDummyConnection(), taskId.toString());
    }

    @Test
    public void shouldSubmitTaskForm() {
        // Given
        KieInstanceId taskId = new KieInstanceId(CONTAINER_ID_1, TASK_ID_1);

        Map<String, Object> variables1 = new ConcurrentHashMap<>();
        variables1.put(TASK_FORM_PROP_KEY_1, TASK_FORM_PROP_1);
        variables1.put(TASK_FORM_PROP_KEY_2, TASK_FORM_PROP_2);

        Map<String, Object> variables2 = new ConcurrentHashMap<>();
        variables2.put(TASK_FORM_PROP_KEY_3, TASK_FORM_PROP_3);

        CreateTaskFormSubmissionRequest request = CreateTaskFormSubmissionRequest.builder()
                .form(TASK_FORM_ID_1, variables1)
                .form(TASK_FORM_ID_2, variables2)
                .build();

        Map<String, Object> expected = Stream.concat(variables1.entrySet().stream(), variables2.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        when(taskServicesClient.getTaskInstance(anyString(), anyLong(), anyBoolean(), anyBoolean(), anyBoolean()))
                .thenReturn(KieTaskTestHelper.generateKieTask(taskId));

        // When
        Task result = kieTaskFormService.submit(getDummyConnection(), getDummyUser(), taskId.toString(), request);

        // Then
        assertThat(result.getId()).isEqualTo(taskId.toString());

        verify(taskServicesClient).saveTaskContent(eq(taskId.getContainerId()), eq(taskId.getInstanceId()),
                eq(expected));
    }

    @Test
    public void shouldThrowTaskNotFoundWhenSubmitFormWithInvalidTaskId() {
        KieInstanceId taskId = new KieInstanceId(randomStringId(), randomLongId());

        //Given
        expectedException.expect(TaskNotFoundException.class);

        when(taskServicesClient.saveTaskContent(anyString(), anyLong(), anyMap()))
                .thenThrow(new KieServicesHttpException(null, HttpStatus.NOT_FOUND.value(), null, null));

        //When
        kieTaskFormService.submit(getDummyConnection(), getDummyUser(), taskId.toString(),
                CreateTaskFormSubmissionRequest.builder().build());
    }

    @Test
    public void shouldThrowTaskNotFoundWhenSubmitFormWithInvalidContainerId() {
        KieInstanceId taskId = new KieInstanceId(randomStringId(), randomLongId());

        //Given
        expectedException.expect(TaskNotFoundException.class);

        when(taskServicesClient.saveTaskContent(anyString(), anyLong(), anyMap()))
                .thenThrow(new KieServicesHttpException(null, HttpStatus.INTERNAL_SERVER_ERROR.value(), null, null));

        //When
        kieTaskFormService.submit(getDummyConnection(), getDummyUser(), taskId.toString(),
                CreateTaskFormSubmissionRequest.builder().build());
    }

}
