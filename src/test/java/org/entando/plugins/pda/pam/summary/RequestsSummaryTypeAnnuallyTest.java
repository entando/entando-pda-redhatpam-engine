package org.entando.plugins.pda.pam.summary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.entando.plugins.pda.pam.summary.RequestsSummaryTestUtil.mockPercentageResultYears;
import static org.entando.plugins.pda.pam.summary.RequestsSummaryTestUtil.mockTotalResult;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import org.entando.plugins.pda.core.engine.Connection;
import org.entando.plugins.pda.core.model.summary.FrequencyEnum;
import org.entando.plugins.pda.core.model.summary.Summary;
import org.entando.plugins.pda.pam.service.api.KieApiService;
import org.junit.Before;
import org.junit.Test;
import org.kie.server.client.QueryServicesClient;

public class RequestsSummaryTypeAnnuallyTest {

    private RequestsSummaryType requestsSummaryType;
    private QueryServicesClient queryClient;

    @Before
    public void init() {
        KieApiService kieApiService = mock(KieApiService.class);
        queryClient = mock(QueryServicesClient.class);
        when(kieApiService.getQueryServicesClient(any())).thenReturn(queryClient);
        requestsSummaryType = new RequestsSummaryType(kieApiService);
    }

    @Test
    public void shouldCalculateTotalForAnnuallyFrequency() {
        // Given
        mockTotalResult(requestsSummaryType, queryClient, LocalDate.of(2016, 1, 1), LocalDate.of(2020, 1, 1), 10_000.0);
        mockPercentageResultYears(requestsSummaryType, queryClient, LocalDate.now().getYear(),
                LocalDate.now().minusYears(1).getYear(), 0, 0);

        // When
        Summary summary = requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.ANNUALLY);

        // Then
        assertThat(summary.getTotal()).isEqualTo("2500.0");
    }

    @Test
    public void shouldCalculatePercentageForAnnuallyFrequency() {
        mockTotalResult(requestsSummaryType, queryClient, LocalDate.of(2016, 1, 1), LocalDate.of(2020, 1, 1), 10_000.0);
        mockPercentageResultYears(requestsSummaryType, queryClient, LocalDate.now().getYear(),
                LocalDate.now().minusYears(1).getYear(), 75, 50);

        // When
        Summary summary = requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.ANNUALLY);

        // Then
        assertThat(summary.getPercentage()).isEqualTo(50.0);
    }
}
