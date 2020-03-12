package org.entando.plugins.pda.pam.service.task;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.entando.plugins.pda.core.utils.TestUtils.getDummyConnection;
import static org.entando.plugins.pda.core.utils.TestUtils.getDummyUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.entando.plugins.pda.core.service.task.response.TaskBulkActionResponse;
import org.entando.plugins.pda.pam.service.api.KieApiService;
import org.entando.plugins.pda.pam.service.util.KieInstanceId;
import org.junit.Before;
import org.junit.Test;
import org.kie.server.api.exception.KieServicesHttpException;
import org.kie.server.client.UserTaskServicesClient;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;

@SuppressWarnings("PMD.TooManyMethods")
public class KieTaskLifecycleBulkServiceTest {

    private static final String CONTAINER_ID = "container-test";
    private static final String USERNAME = "test";
    private static final String ASSIGNEE = "test2";
    private static final String NOT_FOUND_MESSAGE = "not found";
    private static final String INTERNAL_ERROR_MESSAGE = "internal error";

    private KieTaskLifecycleBulkService taskLifecycleBulkService;
    private UserTaskServicesClient taskServicesClient;

    private KieInstanceId taskId1;
    private KieInstanceId taskId2;
    private KieInstanceId taskId3;
    private List<String> ids;

    @Before
    public void setUp() {
        KieApiService kieApiService = mock(KieApiService.class);

        taskServicesClient = mock(UserTaskServicesClient.class);
        when(kieApiService.getUserTaskServicesClient(any())).thenReturn(taskServicesClient);

        taskLifecycleBulkService = new KieTaskLifecycleBulkService(kieApiService);

        taskId1 = new KieInstanceId(CONTAINER_ID, randomNumeric(5));
        taskId2 = new KieInstanceId(CONTAINER_ID, randomNumeric(5));
        taskId3 = new KieInstanceId(CONTAINER_ID, randomNumeric(5));
        ids = Arrays.asList(taskId1.toString(), taskId2.toString(), taskId3.toString());
    }

    @Test
    public void shouldBulkClaimTasks() {
        // When
        List<TaskBulkActionResponse> responses = taskLifecycleBulkService
                .bulkClaim(getDummyConnection(), getDummyUser(USERNAME), ids);

        // Then
        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        verify(taskServicesClient, times(ids.size())).claimTask(eq(CONTAINER_ID), idCaptor.capture(), eq(USERNAME));
        assertBulkOperation(taskId1, taskId2, taskId3, responses, idCaptor);
    }

    @Test
    public void shouldHandleInternalServerErrorOnBulkClaim() {
        // Given
        doThrow(new KieServicesHttpException(INTERNAL_ERROR_MESSAGE, HttpStatus.INTERNAL_SERVER_ERROR.value(), "", ""))
                .when(taskServicesClient).claimTask(CONTAINER_ID, taskId1.getInstanceId(), USERNAME);

        // When
        List<TaskBulkActionResponse> responses = taskLifecycleBulkService
                .bulkClaim(getDummyConnection(), getDummyUser(USERNAME), ids);

        // Then
        assertResponseWithException(responses, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void shouldHandleNotFoundOnBulkClaim() {
        // Given
        doThrow(new KieServicesHttpException(NOT_FOUND_MESSAGE, HttpStatus.NOT_FOUND.value(), "", ""))
                .when(taskServicesClient).claimTask(CONTAINER_ID, taskId1.getInstanceId(), USERNAME);

        // When
        List<TaskBulkActionResponse> responses = taskLifecycleBulkService
                .bulkClaim(getDummyConnection(), getDummyUser(USERNAME), ids);

        // Then
        assertResponseWithException(responses, HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldBulkUnclaimTasks() {
        // When
        List<TaskBulkActionResponse> responses = taskLifecycleBulkService
                .bulkUnclaim(getDummyConnection(), getDummyUser(USERNAME), ids);

        // Then
        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        verify(taskServicesClient, times(ids.size())).releaseTask(eq(CONTAINER_ID), idCaptor.capture(), eq(USERNAME));
        assertBulkOperation(taskId1, taskId2, taskId3, responses, idCaptor);
    }

    @Test
    public void shouldHandleInternalServerErrorOnBulkUnclaim() {
        // Given
        doThrow(new KieServicesHttpException(INTERNAL_ERROR_MESSAGE, HttpStatus.INTERNAL_SERVER_ERROR.value(), "", ""))
                .when(taskServicesClient).releaseTask(CONTAINER_ID, taskId1.getInstanceId(), USERNAME);

        // When
        List<TaskBulkActionResponse> responses = taskLifecycleBulkService
                .bulkUnclaim(getDummyConnection(), getDummyUser(USERNAME), ids);

        // Then
        assertResponseWithException(responses, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void shouldHandleNotFoundOnBulkUnclaim() {
        // Given
        doThrow(new KieServicesHttpException(NOT_FOUND_MESSAGE, HttpStatus.NOT_FOUND.value(), "", ""))
                .when(taskServicesClient).releaseTask(CONTAINER_ID, taskId1.getInstanceId(), USERNAME);

        // When
        List<TaskBulkActionResponse> responses = taskLifecycleBulkService
                .bulkUnclaim(getDummyConnection(), getDummyUser(USERNAME), ids);

        // Then
        assertResponseWithException(responses, HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldBulkAssignTasks() {
        // When
        List<TaskBulkActionResponse> responses = taskLifecycleBulkService
                .bulkAssign(getDummyConnection(), getDummyUser(USERNAME), ids, ASSIGNEE);

        // Then
        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        verify(taskServicesClient, times(ids.size()))
                .delegateTask(eq(CONTAINER_ID), idCaptor.capture(), eq(USERNAME), eq(ASSIGNEE));
        assertBulkOperation(taskId1, taskId2, taskId3, responses, idCaptor);
    }

    @Test
    public void shouldHandleInternalServerErrorOnBulkAssign() {
        // Given
        doThrow(new KieServicesHttpException(INTERNAL_ERROR_MESSAGE, HttpStatus.INTERNAL_SERVER_ERROR.value(), "", ""))
                .when(taskServicesClient).delegateTask(CONTAINER_ID, taskId1.getInstanceId(), USERNAME, ASSIGNEE);

        // When
        List<TaskBulkActionResponse> responses = taskLifecycleBulkService
                .bulkAssign(getDummyConnection(), getDummyUser(USERNAME), ids, ASSIGNEE);

        // Then
        assertResponseWithException(responses, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void shouldHandleNotFoundOnBulkAssign() {
        // Given
        doThrow(new KieServicesHttpException(NOT_FOUND_MESSAGE, HttpStatus.NOT_FOUND.value(), "", ""))
                .when(taskServicesClient).delegateTask(CONTAINER_ID, taskId1.getInstanceId(), USERNAME, ASSIGNEE);

        // When
        List<TaskBulkActionResponse> responses = taskLifecycleBulkService
                .bulkAssign(getDummyConnection(), getDummyUser(USERNAME), ids, ASSIGNEE);

        // Then
        assertResponseWithException(responses, HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldBulkStartTasks() {
        // When
        List<TaskBulkActionResponse> responses = taskLifecycleBulkService
                .bulkStart(getDummyConnection(), getDummyUser(USERNAME), ids);

        // Then
        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        verify(taskServicesClient, times(ids.size())).startTask(eq(CONTAINER_ID), idCaptor.capture(), eq(USERNAME));
        assertBulkOperation(taskId1, taskId2, taskId3, responses, idCaptor);
    }

    @Test
    public void shouldHandleInternalServerErrorOnBulkStart() {
        // Given
        doThrow(new KieServicesHttpException(INTERNAL_ERROR_MESSAGE, HttpStatus.INTERNAL_SERVER_ERROR.value(), "", ""))
                .when(taskServicesClient).startTask(CONTAINER_ID, taskId1.getInstanceId(), USERNAME);

        // When
        List<TaskBulkActionResponse> responses = taskLifecycleBulkService
                .bulkStart(getDummyConnection(), getDummyUser(USERNAME), ids);

        // Then
        assertResponseWithException(responses, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void shouldHandleNotFoundOnBulkStart() {
        // Given
        doThrow(new KieServicesHttpException(NOT_FOUND_MESSAGE, HttpStatus.NOT_FOUND.value(), "", ""))
                .when(taskServicesClient).startTask(CONTAINER_ID, taskId1.getInstanceId(), USERNAME);

        // When
        List<TaskBulkActionResponse> responses = taskLifecycleBulkService
                .bulkStart(getDummyConnection(), getDummyUser(USERNAME), ids);

        // Then
        assertResponseWithException(responses, HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldBulkPauseTasks() {
        // When
        List<TaskBulkActionResponse> responses = taskLifecycleBulkService
                .bulkPause(getDummyConnection(), getDummyUser(USERNAME), ids);

        // Then
        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        verify(taskServicesClient, times(ids.size())).suspendTask(eq(CONTAINER_ID), idCaptor.capture(), eq(USERNAME));
        assertBulkOperation(taskId1, taskId2, taskId3, responses, idCaptor);
    }

    @Test
    public void shouldHandleInternalServerErrorOnBulkPause() {
        // Given
        doThrow(new KieServicesHttpException(INTERNAL_ERROR_MESSAGE, HttpStatus.INTERNAL_SERVER_ERROR.value(), "", ""))
                .when(taskServicesClient).suspendTask(CONTAINER_ID, taskId1.getInstanceId(), USERNAME);

        // When
        List<TaskBulkActionResponse> responses = taskLifecycleBulkService
                .bulkPause(getDummyConnection(), getDummyUser(USERNAME), ids);

        // Then
        assertResponseWithException(responses, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void shouldHandleNotFoundOnBulkPause() {
        // Given
        doThrow(new KieServicesHttpException(NOT_FOUND_MESSAGE, HttpStatus.NOT_FOUND.value(), "", ""))
                .when(taskServicesClient).suspendTask(CONTAINER_ID, taskId1.getInstanceId(), USERNAME);

        // When
        List<TaskBulkActionResponse> responses = taskLifecycleBulkService
                .bulkPause(getDummyConnection(), getDummyUser(USERNAME), ids);

        // Then
        assertResponseWithException(responses, HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldBulkResumeTasks() {
        // When
        List<TaskBulkActionResponse> responses = taskLifecycleBulkService
                .bulkResume(getDummyConnection(), getDummyUser(USERNAME), ids);

        // Then
        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        verify(taskServicesClient, times(ids.size())).resumeTask(eq(CONTAINER_ID), idCaptor.capture(), eq(USERNAME));
        assertBulkOperation(taskId1, taskId2, taskId3, responses, idCaptor);
    }

    @Test
    public void shouldHandleInternalServerErrorOnBulkResume() {
        // Given
        doThrow(new KieServicesHttpException(INTERNAL_ERROR_MESSAGE, HttpStatus.INTERNAL_SERVER_ERROR.value(), "", ""))
                .when(taskServicesClient).resumeTask(CONTAINER_ID, taskId1.getInstanceId(), USERNAME);

        // When
        List<TaskBulkActionResponse> responses = taskLifecycleBulkService
                .bulkResume(getDummyConnection(), getDummyUser(USERNAME), ids);

        // Then
        assertResponseWithException(responses, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void shouldHandleNotFoundOnBulkResume() {
        // Given
        doThrow(new KieServicesHttpException(NOT_FOUND_MESSAGE, HttpStatus.NOT_FOUND.value(), "", ""))
                .when(taskServicesClient).resumeTask(CONTAINER_ID, taskId1.getInstanceId(), USERNAME);

        // When
        List<TaskBulkActionResponse> responses = taskLifecycleBulkService
                .bulkResume(getDummyConnection(), getDummyUser(USERNAME), ids);

        // Then
        assertResponseWithException(responses, HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldBulkCompleteTasks() {
        // When
        List<TaskBulkActionResponse> responses = taskLifecycleBulkService
                .bulkComplete(getDummyConnection(), getDummyUser(USERNAME), ids);

        // Then
        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        verify(taskServicesClient, times(ids.size()))
                .completeTask(eq(CONTAINER_ID), idCaptor.capture(), eq(USERNAME), eq(Collections.emptyMap()));
        assertBulkOperation(taskId1, taskId2, taskId3, responses, idCaptor);
    }

    @Test
    public void shouldHandleInternalServerErrorOnBulkComplete() {
        // Given
        doThrow(new KieServicesHttpException(INTERNAL_ERROR_MESSAGE, HttpStatus.INTERNAL_SERVER_ERROR.value(), "", ""))
                .when(taskServicesClient)
                .completeTask(CONTAINER_ID, taskId1.getInstanceId(), USERNAME, Collections.emptyMap());

        // When
        List<TaskBulkActionResponse> responses = taskLifecycleBulkService
                .bulkComplete(getDummyConnection(), getDummyUser(USERNAME), ids);

        // Then
        assertResponseWithException(responses, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void shouldHandleNotFoundOnBulkComplete() {
        // Given
        doThrow(new KieServicesHttpException(NOT_FOUND_MESSAGE, HttpStatus.NOT_FOUND.value(), "", ""))
                .when(taskServicesClient)
                .completeTask(CONTAINER_ID, taskId1.getInstanceId(), USERNAME, Collections.emptyMap());

        // When
        List<TaskBulkActionResponse> responses = taskLifecycleBulkService
                .bulkComplete(getDummyConnection(), getDummyUser(USERNAME), ids);

        // Then
        assertResponseWithException(responses, HttpStatus.NOT_FOUND);
    }

    private void assertBulkOperation(KieInstanceId taskId1, KieInstanceId taskId2, KieInstanceId taskId3,
            List<TaskBulkActionResponse> responses, ArgumentCaptor<Long> idCaptor) {
        assertThat(idCaptor.getAllValues())
                .containsExactlyInAnyOrder(taskId1.getInstanceId(), taskId2.getInstanceId(), taskId3.getInstanceId());
        int okStatusCode = HttpStatus.OK.value();
        assertThat(responses).extracting(TaskBulkActionResponse::getId, TaskBulkActionResponse::getStatusCode)
                .containsExactlyInAnyOrder(
                        tuple(taskId1.toString(), okStatusCode),
                        tuple(taskId2.toString(), okStatusCode),
                        tuple(taskId3.toString(), okStatusCode)
                );
    }

    private void assertResponseWithException(List<TaskBulkActionResponse> responses, HttpStatus httpStatus) {
        assertThat(responses).extracting(TaskBulkActionResponse::getId, TaskBulkActionResponse::getStatusCode)
                .containsExactlyInAnyOrder(
                        tuple(taskId1.toString(), httpStatus.value()),
                        tuple(taskId2.toString(), HttpStatus.OK.value()),
                        tuple(taskId3.toString(), HttpStatus.OK.value())
                );
    }
}
