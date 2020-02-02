package org.entando.plugins.pda.pam.summary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.entando.plugins.pda.pam.summary.RequestsSummaryType.PDA_PERC_DAYS_PREFIX;
import static org.entando.plugins.pda.pam.summary.RequestsSummaryType.PDA_PERC_MONTHS_PREFIX;
import static org.entando.plugins.pda.pam.summary.RequestsSummaryType.PDA_PERC_YEARS_PREFIX;
import static org.entando.plugins.pda.pam.summary.RequestsSummaryType.PDA_TOTAL_PREFIX;
import static org.entando.plugins.pda.pam.summary.SummaryTypeTestUtil.mockEmptyPercentageResultMonths;
import static org.entando.plugins.pda.pam.summary.SummaryTypeTestUtil.mockEmptyTotalResult;
import static org.entando.plugins.pda.pam.summary.SummaryTypeTestUtil.mockPercentageResultDays;
import static org.entando.plugins.pda.pam.summary.SummaryTypeTestUtil.mockPercentageResultMonths;
import static org.entando.plugins.pda.pam.summary.SummaryTypeTestUtil.mockPercentageResultYears;
import static org.entando.plugins.pda.pam.summary.SummaryTypeTestUtil.mockTotalResult;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import org.entando.plugins.pda.core.engine.Connection;
import org.entando.plugins.pda.core.model.summary.FrequencyEnum;
import org.entando.plugins.pda.core.model.summary.ValuePercentageSummaryValue;
import org.entando.plugins.pda.pam.engine.KieEngine;
import org.entando.plugins.pda.pam.service.api.KieApiService;
import org.junit.Before;
import org.junit.Test;
import org.kie.server.client.QueryServicesClient;

public class RequestsSummaryTest {

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
        mockEmptyTotalResult(requestsSummaryType, queryClient, PDA_TOTAL_PREFIX);
        mockEmptyPercentageResultMonths(requestsSummaryType, queryClient, PDA_PERC_MONTHS_PREFIX);

        // When
        ValuePercentageSummaryValue summary = (ValuePercentageSummaryValue)requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.MONTHLY);

        assertThat(requestsSummaryType.getDescription()).isEqualTo(RequestsSummaryType.REQUESTS_TITLE);
        assertThat(requestsSummaryType.getEngine()).isEqualTo(KieEngine.TYPE);
        assertThat(summary.getTitle()).isEqualTo(RequestsSummaryType.REQUESTS_TITLE);
        assertThat(summary.getTotalLabel()).isEqualTo(RequestsSummaryType.REQUESTS_TOTAL_LABEL);
    }

    @Test
    public void shouldCalculateTotalForDailyFrequency() {
        // Given
        mockTotalResult(requestsSummaryType, queryClient, LocalDate.of(2019, 1, 1), LocalDate.of(2019, 9, 1), 10_000.0, PDA_TOTAL_PREFIX);
        mockPercentageResultDays(requestsSummaryType, queryClient, LocalDate.now(), LocalDate.now().minusDays(1), 150,
                100, PDA_PERC_DAYS_PREFIX);

        // When
        ValuePercentageSummaryValue summary = (ValuePercentageSummaryValue)requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.DAILY);

        // Then
        assertThat(summary.getTotal()).isEqualTo("41.15");
    }

    @Test
    public void shouldCalculateTotalForMonthlyFrequency() {
        // Given
        mockTotalResult(requestsSummaryType, queryClient, LocalDate.of(2019, 1, 1), LocalDate.of(2019, 9, 1), 10_000.0, PDA_TOTAL_PREFIX);
        mockPercentageResultMonths(requestsSummaryType, queryClient, LocalDate.now(), LocalDate.now().minusMonths(1),
                75, 50, PDA_PERC_MONTHS_PREFIX);

        // When
        ValuePercentageSummaryValue summary = (ValuePercentageSummaryValue)requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.MONTHLY);

        // Then
        assertThat(summary.getTotal()).isEqualTo("1250");
    }

    @Test
    public void shouldCalculateTotalForAnnuallyFrequency() {
        // Given
        mockTotalResult(requestsSummaryType, queryClient, LocalDate.of(2016, 1, 1), LocalDate.of(2020, 1, 1), 10_000.0, PDA_TOTAL_PREFIX);
        mockPercentageResultYears(requestsSummaryType, queryClient, LocalDate.now().getYear(),
                LocalDate.now().minusYears(1).getYear(), 0, 0, PDA_PERC_YEARS_PREFIX);

        // When
        ValuePercentageSummaryValue summary = (ValuePercentageSummaryValue)requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.ANNUALLY);

        // Then
        assertThat(summary.getTotal()).isEqualTo("2500");
    }
}
