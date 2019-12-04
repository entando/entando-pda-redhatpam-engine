package org.entando.plugins.pda.pam.service.group;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.entando.plugins.pda.core.engine.Connection;
import org.entando.plugins.pda.pam.service.api.CustomQueryService;
import org.entando.plugins.pda.pam.service.api.KieApiService;
import org.entando.plugins.pda.pam.service.process.model.KieProcessId;
import org.junit.Before;
import org.junit.Test;
import org.kie.server.api.model.definition.AssociatedEntitiesDefinition;
import org.kie.server.client.ProcessServicesClient;

public class KieGroupServiceTest {

    private static final String GROUP_1 = "group1";
    private static final String GROUP_2 = "group2";
    private static final String GROUP_3 = "group3";

    private CustomQueryService customQueryService;
    private KieApiService kieApiService;
    private KieGroupService kieGroupService;

    @Before
    public void init() {
        customQueryService = mock(CustomQueryService.class);
        kieApiService = mock(KieApiService.class);
        kieGroupService = new KieGroupService(customQueryService, kieApiService);
    }

    @Test
    public void shouldListAllGroups() {
        // Given
        Connection connection = Connection.builder().build();
        List<String> returnedGroups = Arrays.asList(GROUP_1, GROUP_2, GROUP_3);
        when(customQueryService.getGroups(connection)).thenReturn(returnedGroups);

        // When
        List<String> groups = kieGroupService.list(connection, null);

        // Then
        assertThat(groups).containsExactlyElementsOf(returnedGroups);
    }

    @Test
    public void shouldListGroupsFromProcessId() {
        // Given
        ProcessServicesClient processServicesClient = mock(ProcessServicesClient.class);
        String containerId = "container-1";
        String processId = "1";
        String[] processEntities = {GROUP_1, GROUP_2, GROUP_3};
        when(processServicesClient.getAssociatedEntityDefinitions(containerId, processId))
                .thenReturn(getAssociatedEntities(processEntities));
        Connection connection = Connection.builder().build();
        when(kieApiService.getProcessServicesClient(connection)).thenReturn(processServicesClient);
        List<String> returnedGroups = Arrays.asList(GROUP_1, GROUP_2);
        when(customQueryService.getGroups(connection, processEntities)).thenReturn(returnedGroups);

        // When
        List<String> groups = kieGroupService.list(connection, processId + KieProcessId.SEPARATOR + containerId);

        // Then
        assertThat(groups).containsExactlyElementsOf(returnedGroups);
    }

    @Test
    public void shouldReturnEmptyIfThereIsNoGroupAssociatedWithTheProcess() {
        // Given
        ProcessServicesClient processServicesClient = mock(ProcessServicesClient.class);
        String containerId = "container-1";
        String processId = "1";
        when(processServicesClient.getAssociatedEntityDefinitions(containerId, processId))
                .thenReturn(getAssociatedEntities());
        Connection connection = Connection.builder().build();
        when(kieApiService.getProcessServicesClient(connection)).thenReturn(processServicesClient);
        List<String> returnedGroups = Arrays.asList(GROUP_1, GROUP_2);
        when(customQueryService.getGroups(connection)).thenReturn(returnedGroups);

        // When
        List<String> groups = kieGroupService.list(connection, processId + KieProcessId.SEPARATOR + containerId);

        // Then
        verify(customQueryService, never()).getGroups(connection);
        assertThat(groups).isEmpty();
    }

    private AssociatedEntitiesDefinition getAssociatedEntities(String... groups) {
        return new AssociatedEntitiesDefinition(
                Collections.singletonMap("Approve Task", groups));
    }
}
