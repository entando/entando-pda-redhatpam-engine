package org.entando.plugins.pda.pam.service.process;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.RandomStringUtils;
import org.entando.plugins.pda.core.engine.Connection;
import org.entando.plugins.pda.core.model.ProcessInstance;
import org.entando.plugins.pda.core.utils.TestUtils;
import org.entando.plugins.pda.pam.service.api.KieApiService;
import org.entando.plugins.pda.pam.service.task.model.KieTask;
import org.junit.Before;
import org.junit.Test;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.api.model.instance.TaskSummaryList;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.client.UserTaskServicesClient;

public class KieProcessInstanceServiceTest {

    private static final String TEST_USER = "test";

    private QueryServicesClient queryServicesClient;
    private UserTaskServicesClient userTaskServicesClient;
    private KieProcessInstanceService processInstanceService;

    @Before
    public void setUp() {
        queryServicesClient = mock(QueryServicesClient.class);
        userTaskServicesClient = mock(UserTaskServicesClient.class);
        KieApiService kieApiService = mock(KieApiService.class);
        when(kieApiService.getQueryServicesClient(any())).thenReturn(queryServicesClient);
        when(kieApiService.getUserTaskServicesClient(any())).thenReturn(userTaskServicesClient);
        processInstanceService = new KieProcessInstanceService(kieApiService);
    }

    @Test
    public void shouldListProcessInstancesByUser() {
        // Given
        org.kie.server.api.model.instance.ProcessInstance kieProcessInstance =
                new org.kie.server.api.model.instance.ProcessInstance();
        kieProcessInstance.setContainerId(RandomStringUtils.randomAlphabetic(10));
        kieProcessInstance.setId(Long.valueOf(RandomStringUtils.randomNumeric(5)));
        kieProcessInstance.setProcessId(RandomStringUtils.randomAlphabetic(10));
        TaskSummary task = new TaskSummary();
        task.setId(Long.valueOf(RandomStringUtils.randomNumeric(5)));
        task.setName(RandomStringUtils.randomAlphabetic(10));
        task.setStatus(KieTask.KIE_STATUS_CREATED);
        task.setContainerId(RandomStringUtils.randomAlphabetic(10));
        List<TaskSummary> tasks = Collections.singletonList(task);
        TaskSummaryList taskSummaryList = new TaskSummaryList(tasks);
        kieProcessInstance.setActiveUserTasks(taskSummaryList);
        String processDefinitionId = "myProcess";
        when(queryServicesClient
                .findProcessInstancesByProcessIdAndInitiator(eq(processDefinitionId), eq(TEST_USER), eq(null), anyInt(),
                        anyInt())).thenReturn(Collections.singletonList(kieProcessInstance));
        when(userTaskServicesClient.findTasksByStatusByProcessInstanceId(anyLong(), eq(null), anyInt(), anyInt()))
                .thenReturn(tasks);

        // When
        List<ProcessInstance> processInstances = processInstanceService
                .list(Connection.builder().build(), processDefinitionId, TestUtils.getDummyUser(TEST_USER));

        // Then
        verify(queryServicesClient)
                .findProcessInstancesByProcessIdAndInitiator(processDefinitionId, TEST_USER, null, 0, -1);
        verify(userTaskServicesClient).findTasksByStatusByProcessInstanceId(anyLong(), eq(null), eq(0), eq(-1));
        assertThat(kieProcessInstance.getActiveUserTasks().getItems().get(0).getName())
                .isEqualTo(processInstances.get(0).getActiveUserTasks().get(0));
    }
}
