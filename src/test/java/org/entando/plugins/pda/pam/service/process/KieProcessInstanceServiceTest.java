package org.entando.plugins.pda.pam.service.process;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.entando.plugins.pda.pam.service.process.KieProcessInstanceService.ACTIVE_STATUSES;
import static org.entando.plugins.pda.pam.service.process.KieProcessInstanceService.ALL_ITEMS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.entando.plugins.pda.core.engine.Connection;
import org.entando.plugins.pda.core.model.ProcessInstance;
import org.entando.plugins.pda.core.utils.TestUtils;
import org.entando.plugins.pda.pam.service.api.KieApiService;
import org.junit.Before;
import org.junit.Test;
import org.kie.server.api.model.instance.TaskSummary;
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
    public void shouldListProcessInstances() {
        // Given
        org.kie.server.api.model.instance.ProcessInstance kieProcessInstance1 = getKieProcessInstance();
        org.kie.server.api.model.instance.ProcessInstance kieProcessInstance2 = getKieProcessInstance();
        String processDefinitionId = "myProcess";
        when(queryServicesClient
                .findProcessInstancesByProcessIdAndInitiator(eq(processDefinitionId), eq(TEST_USER), eq(null), anyInt(),
                        anyInt())).thenReturn(Arrays.asList(kieProcessInstance1, kieProcessInstance2));
        List<TaskSummary> tasks = Arrays.asList(
                TaskSummary.builder().name(randomAlphabetic(10)).build(),
                TaskSummary.builder().name(randomAlphabetic(10)).build());
        when(userTaskServicesClient.findTasksByStatusByProcessInstanceId(anyLong(), any(), anyInt(), anyInt()))
                .thenReturn(tasks);

        // When
        List<ProcessInstance> processInstances = processInstanceService
                .list(Connection.builder().build(), processDefinitionId, TestUtils.getDummyUser(TEST_USER));

        // Then
        verify(queryServicesClient)
                .findProcessInstancesByProcessIdAndInitiator(processDefinitionId, TEST_USER, null, 0, ALL_ITEMS);
        verify(userTaskServicesClient, times(2)).findTasksByStatusByProcessInstanceId(anyLong(),
                eq(ACTIVE_STATUSES), eq(0), eq(ALL_ITEMS));
        assertThat(processInstances).extracting(ProcessInstance::getId, ProcessInstance::getProcessName)
                .containsExactlyInAnyOrder(
                        tuple(String.valueOf(kieProcessInstance1.getId()), kieProcessInstance1.getProcessName()),
                        tuple(String.valueOf(kieProcessInstance2.getId()), kieProcessInstance2.getProcessName()));
        List<String> taskNames = tasks.stream().map(TaskSummary::getName).collect(Collectors.toList());
        assertThat(processInstances.get(0).getActiveUserTasks()).containsExactlyElementsOf(taskNames);
        assertThat(processInstances.get(1).getActiveUserTasks()).containsExactlyElementsOf(taskNames);
    }

    private org.kie.server.api.model.instance.ProcessInstance getKieProcessInstance() {
        org.kie.server.api.model.instance.ProcessInstance kieProcessInstance =
                new org.kie.server.api.model.instance.ProcessInstance();
        kieProcessInstance.setContainerId(randomAlphabetic(10));
        kieProcessInstance.setId(Long.valueOf(randomNumeric(5)));
        kieProcessInstance.setProcessId(randomAlphabetic(10));
        kieProcessInstance.setProcessName(randomAlphabetic(10));
        return kieProcessInstance;
    }
}
