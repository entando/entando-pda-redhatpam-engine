package org.entando.plugins.pda.pam.summary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.entando.plugins.pda.pam.summary.RequestsSummaryTypeTestUtil.mockEmptyPercentageResultMonths;
import static org.entando.plugins.pda.pam.summary.RequestsSummaryTypeTestUtil.mockEmptyTotalResult;
import static org.entando.plugins.pda.pam.summary.RequestsSummaryTypeTestUtil.mockPercentageResultDays;
import static org.entando.plugins.pda.pam.summary.RequestsSummaryTypeTestUtil.mockPercentageResultMonths;
import static org.entando.plugins.pda.pam.summary.RequestsSummaryTypeTestUtil.mockPercentageResultYears;
import static org.entando.plugins.pda.pam.summary.RequestsSummaryTypeTestUtil.mockTotalResult;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import org.entando.plugins.pda.core.engine.Connection;
import org.entando.plugins.pda.core.model.summary.FrequencyEnum;
import org.entando.plugins.pda.core.model.summary.Summary;
import org.entando.plugins.pda.pam.engine.KieEngine;
import org.entando.plugins.pda.pam.service.api.KieApiService;
import org.junit.Before;
import org.junit.Test;
import org.kie.server.client.QueryServicesClient;

public class RequestsSummaryTypeTest {

    private RequestsSummaryType requestsSummaryType;
    private QueryServicesClient queryClient;

    @Before
    public void init() {
        KieApiService kieApiService = mock(KieApiService.class);
        queryClient = mock(QueryServicesClient.class);
        when(kieApiService.getQueryServicesClient(any())).thenReturn(queryClient);
        requestsSummaryType = new RequestsSummaryType(kieApiService, new RequestsProperties());
    }

    @Test
    public void shouldReturnConstantsForLabelValues() {
        // Given
        mockEmptyTotalResult(requestsSummaryType, queryClient);
        mockEmptyPercentageResultMonths(requestsSummaryType, queryClient);

        // When
        Summary summary = requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.MONTHLY);

        assertThat(requestsSummaryType.getDescription()).isEqualTo(RequestsSummaryType.REQUESTS_TITLE);
        assertThat(requestsSummaryType.getEngine()).isEqualTo(KieEngine.TYPE);
        assertThat(summary.getTitle()).isEqualTo(RequestsSummaryType.REQUESTS_TITLE);
        assertThat(summary.getTotalLabel()).isEqualTo(RequestsSummaryType.REQUESTS_TOTAL_LABEL);
    }

    @Test
    public void shouldCalculateTotalForDailyFrequency() {
        // Given
        mockTotalResult(requestsSummaryType, queryClient, LocalDate.of(2019, 1, 1), LocalDate.of(2019, 9, 1), 10_000.0);
        mockPercentageResultDays(requestsSummaryType, queryClient, LocalDate.now(), LocalDate.now().minusDays(1), 150,
                100);

        // When
        Summary summary = requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.DAILY);

        // Then
        assertThat(summary.getTotal()).isEqualTo("41.15");
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
        assertThat(summary.getTotal()).isEqualTo("1250");
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
        assertThat(summary.getTotal()).isEqualTo("2500");
    }
}
