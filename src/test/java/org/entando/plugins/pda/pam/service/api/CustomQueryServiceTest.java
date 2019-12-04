package org.entando.plugins.pda.pam.service.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.entando.plugins.pda.core.engine.Connection;
import org.junit.Before;
import org.junit.Test;
import org.kie.server.api.model.definition.QueryFilterSpec;
import org.kie.server.client.QueryServicesClient;

public class CustomQueryServiceTest {

    private static final String GROUP_1 = "group1";
    private static final String GROUP_2 = "group2";
    private static final String GROUP_3 = "group3";

    private KieApiService kieApiService;
    private CustomQueryService customQueryService;

    @Before
    public void init() {
        kieApiService = mock(KieApiService.class);
        customQueryService = new CustomQueryService(kieApiService);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldGetAllGroups() {
        // Given
        Connection connection = Connection.builder().build();
        QueryServicesClient queryClient = mock(QueryServicesClient.class);
        List<List> returnedValues = Arrays
                .asList(Collections.singletonList(GROUP_1), Collections.singletonList(GROUP_2),
                        Collections.singletonList(GROUP_3));
        when(queryClient.query(anyString(), anyString(), anyInt(), anyInt(), any(Class.class)))
                .thenReturn(returnedValues);
        when(kieApiService.getQueryServicesClient(connection)).thenReturn(queryClient);

        // When
        List<String> groups = customQueryService.getGroups(connection);

        // Then
        assertThat(groups).containsExactlyInAnyOrder(GROUP_1, GROUP_2, GROUP_3);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldGetGroupsUsingFilter() {
        // Given
        Connection connection = Connection.builder().build();
        QueryServicesClient queryClient = mock(QueryServicesClient.class);
        List<List> returnedValues = Collections.singletonList(Collections.singletonList(GROUP_1));
        when(queryClient
                .query(anyString(), anyString(), any(QueryFilterSpec.class), anyInt(), anyInt(), any(Class.class)))
                .thenReturn(returnedValues);
        when(kieApiService.getQueryServicesClient(connection)).thenReturn(queryClient);

        // When
        List<String> groups = customQueryService.getGroups(connection, GROUP_1);

        // Then
        verify(queryClient)
                .query(anyString(), anyString(), any(QueryFilterSpec.class), anyInt(), anyInt(), any(Class.class));
        assertThat(groups).containsExactlyInAnyOrder(GROUP_1);
    }
}
