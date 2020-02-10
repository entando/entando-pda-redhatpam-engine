package org.entando.plugins.pda.pam.summary;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.entando.plugins.pda.core.utils.TestUtils.getDummyConnection;
import static org.entando.plugins.pda.pam.summary.KieDataRepository.PDA_YEARS_PREFIX;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.entando.plugins.pda.core.model.summary.SummaryFrequency;
import org.entando.plugins.pda.pam.engine.KieEngine;
import org.entando.plugins.pda.pam.service.api.KieApiService;
import org.junit.Before;
import org.junit.Test;
import org.kie.server.client.QueryServicesClient;

public class KieDataRepositoryTest {
    private static final String TYPE_1 = "datarepository1";

    private KieDataRepository repository;
    private QueryServicesClient queryClient;

    @Before
    public void setUp() {
        queryClient = mock(QueryServicesClient.class);

        KieApiService kieApiService = mock(KieApiService.class);
        when(kieApiService.getQueryServicesClient(any())).thenReturn(queryClient);

        repository = new KieDataRepository(kieApiService, TYPE_1);
    }

    @Test
    public void shouldLoadPropertiesFromConfigFile() {
        assertThat(repository.getId()).isEqualTo(TYPE_1);
        assertThat(repository.getEngine()).isEqualTo(KieEngine.TYPE);

        assertThat(repository.getDaysQuery()).isNotNull();
        assertThat(repository.getMonthsQuery()).isNotNull();
        assertThat(repository.getYearsQuery()).isNotNull();
    }

    @Test
    public void shouldFetchSeries() {
        //When
        repository.getPeriodicData(getDummyConnection(), SummaryFrequency.ANNUALLY, 5);

        //Then
        verify(queryClient).replaceQuery(any());
        verify(queryClient).query(eq(PDA_YEARS_PREFIX + TYPE_1), eq(QueryServicesClient.QUERY_MAP_RAW),
                eq(0), eq(5), eq(List.class));
    }
}
