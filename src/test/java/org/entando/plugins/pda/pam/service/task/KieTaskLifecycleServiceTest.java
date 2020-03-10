package org.entando.plugins.pda.pam.service.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.entando.plugins.pda.core.utils.TestUtils.getDummyConnection;
import static org.entando.plugins.pda.core.utils.TestUtils.getDummyUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.apache.commons.lang3.RandomStringUtils;
import org.entando.plugins.pda.core.exception.TaskNotFoundException;
import org.entando.plugins.pda.core.model.Task;
import org.entando.plugins.pda.pam.exception.KieInvalidResponseException;
import org.entando.plugins.pda.pam.service.api.KieApiService;
import org.entando.plugins.pda.pam.service.util.KieInstanceId;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kie.server.api.exception.KieServicesHttpException;
import org.kie.server.client.UserTaskServicesClient;
import org.springframework.http.HttpStatus;

public class KieTaskLifecycleServiceTest {

    private static final String TEST_USERNAME = "test";

    private KieTaskLifecycleService kieTaskLifecycleService;
    private UserTaskServicesClient taskServicesClient;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();


    @Before
    public void setUp() {
        KieApiService kieApiService = mock(KieApiService.class);

        taskServicesClient = mock(UserTaskServicesClient.class);
        when(kieApiService.getUserTaskServicesClient(any())).thenReturn(taskServicesClient);

        kieTaskLifecycleService = new KieTaskLifecycleService(kieApiService);
    }

    @Test
    public void shouldClaimTask() {
        // Given
        KieInstanceId taskId = new KieInstanceId(RandomStringUtils.randomAlphabetic(10),
                RandomStringUtils.randomNumeric(5));

        // When
        Task taskResult = kieTaskLifecycleService
                .claim(getDummyConnection(), getDummyUser(TEST_USERNAME), taskId.toString());

        // Then
        verify(taskServicesClient).claimTask(taskId.getContainerId(), taskId.getInstanceId(), TEST_USERNAME);
        assertThat(taskResult.getId()).isEqualTo(taskId.toString());
    }

    @Test
    public void shouldHandleKieServiceHttpExceptionOnClaimWith404() {
        // Given
        expectedException.expect(TaskNotFoundException.class);
        KieInstanceId taskId = new KieInstanceId(RandomStringUtils.randomAlphabetic(10),
                RandomStringUtils.randomNumeric(5));
        doThrow(new KieServicesHttpException("not found", HttpStatus.NOT_FOUND.value(), "", ""))
                .when(taskServicesClient).claimTask(anyString(), anyLong(), anyString());

        // When
        kieTaskLifecycleService
                .claim(getDummyConnection(), getDummyUser(TEST_USERNAME), taskId.toString());
    }

    @Test
    public void shouldHandleKieServiceHttpExceptionOnClaimWith500() {
        // Given
        expectedException.expect(KieInvalidResponseException.class);
        KieInstanceId taskId = new KieInstanceId(RandomStringUtils.randomAlphabetic(10),
                RandomStringUtils.randomNumeric(5));
        doThrow(new KieServicesHttpException("internal error", HttpStatus.INTERNAL_SERVER_ERROR.value(), "", ""))
                .when(taskServicesClient).claimTask(anyString(), anyLong(), anyString());

        // When
        kieTaskLifecycleService
                .claim(getDummyConnection(), getDummyUser(TEST_USERNAME), taskId.toString());
    }

    @Test
    public void shouldUnclaimTask() {
        // Given
        KieInstanceId taskId = new KieInstanceId(RandomStringUtils.randomAlphabetic(10),
                RandomStringUtils.randomNumeric(5));

        // When
        Task taskResult = kieTaskLifecycleService
                .unclaim(getDummyConnection(), getDummyUser(TEST_USERNAME), taskId.toString());

        // Then
        verify(taskServicesClient).releaseTask(taskId.getContainerId(), taskId.getInstanceId(), TEST_USERNAME);
        assertThat(taskResult.getId()).isEqualTo(taskId.toString());
    }

    @Test
    public void shouldAssignTask() {
        // Given
        KieInstanceId taskId = new KieInstanceId(RandomStringUtils.randomAlphabetic(10),
                RandomStringUtils.randomNumeric(5));
        String assignee = "test2";

        // When
        Task taskResult = kieTaskLifecycleService
                .assign(getDummyConnection(), getDummyUser(TEST_USERNAME), taskId.toString(), assignee);

        // Then
        verify(taskServicesClient).nominateTask(taskId.getContainerId(), taskId.getInstanceId(), TEST_USERNAME,
                Collections.singletonList(assignee));
        assertThat(taskResult.getId()).isEqualTo(taskId.toString());
    }

    @Test
    public void shouldStartTask() {
        // Given
        KieInstanceId taskId = new KieInstanceId(RandomStringUtils.randomAlphabetic(10),
                RandomStringUtils.randomNumeric(5));

        // When
        Task taskResult = kieTaskLifecycleService
                .start(getDummyConnection(), getDummyUser(TEST_USERNAME), taskId.toString());

        // Then
        verify(taskServicesClient).startTask(taskId.getContainerId(), taskId.getInstanceId(), TEST_USERNAME);
        assertThat(taskResult.getId()).isEqualTo(taskId.toString());
    }

    @Test
    public void shouldPauseTask() {
        // Given
        KieInstanceId taskId = new KieInstanceId(RandomStringUtils.randomAlphabetic(10),
                RandomStringUtils.randomNumeric(5));

        // When
        Task taskResult = kieTaskLifecycleService
                .pause(getDummyConnection(), getDummyUser(TEST_USERNAME), taskId.toString());

        // Then
        verify(taskServicesClient).suspendTask(taskId.getContainerId(), taskId.getInstanceId(), TEST_USERNAME);
        assertThat(taskResult.getId()).isEqualTo(taskId.toString());
    }

    @Test
    public void shouldCompleteTask() {
        // Given
        KieInstanceId taskId = new KieInstanceId(RandomStringUtils.randomAlphabetic(10),
                RandomStringUtils.randomNumeric(5));

        // When
        Task taskResult = kieTaskLifecycleService
                .complete(getDummyConnection(), getDummyUser(TEST_USERNAME), taskId.toString());

        // Then
        verify(taskServicesClient)
                .completeTask(taskId.getContainerId(), taskId.getInstanceId(), TEST_USERNAME, Collections.emptyMap());
        assertThat(taskResult.getId()).isEqualTo(taskId.toString());
    }
}
