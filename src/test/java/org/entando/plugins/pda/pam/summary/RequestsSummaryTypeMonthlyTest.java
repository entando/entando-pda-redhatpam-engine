package org.entando.plugins.pda.pam.summary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.entando.plugins.pda.pam.summary.RequestsSummaryTestUtil.mockEmptyPercentageResultMonths;
import static org.entando.plugins.pda.pam.summary.RequestsSummaryTestUtil.mockEmptyTotalResult;
import static org.entando.plugins.pda.pam.summary.RequestsSummaryTestUtil.mockPercentageResultMonths;
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

public class RequestsSummaryTypeMonthlyTest {

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
    public void shouldReturnConstantsForLabelValues() {
        // Given
        mockEmptyTotalResult(requestsSummaryType, queryClient);
        mockEmptyPercentageResultMonths(requestsSummaryType, queryClient);

        // When
        Summary summary = requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.MONTHLY);

        assertThat(summary.getTitle()).isEqualTo(RequestsSummaryType.REQUESTS_TITLE);
        assertThat(summary.getTotalLabel()).isEqualTo(RequestsSummaryType.REQUESTS_TOTAL_LABEL);
    }

    @Test
    public void shouldCalculateTotalForMonthlyFrequency() {
        // Given
        mockTotalResult(requestsSummaryType, queryClient, LocalDate.of(2019, 1, 1), LocalDate.of(2019, 9, 1), 10_000.0);
        mockPercentageResultMonths(requestsSummaryType, queryClient, LocalDate.now(), LocalDate.now().minusMonths(1),
                75, 50);

        // When
        Summary summary = requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.MONTHLY);

        // Then
        assertThat(summary.getTotal()).isEqualTo("1250.0");
    }

    @Test
    public void shouldCalculatePercentageForMonthlyFrequency() {
        mockEmptyTotalResult(requestsSummaryType, queryClient);
        mockPercentageResultMonths(requestsSummaryType, queryClient, LocalDate.now(), LocalDate.now().minusMonths(1),
                75, 50);

        // When
        Summary summary = requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.MONTHLY);

        // Then
        assertThat(summary.getPercentage()).isEqualTo(50.0);
    }
}
