package org.entando.plugins.pda.pam.summary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.entando.plugins.pda.pam.summary.RequestsSummaryType.PDA_PERC_DAYS_PREFIX;
import static org.entando.plugins.pda.pam.summary.RequestsSummaryType.PDA_TOTAL_PREFIX;
import static org.entando.plugins.pda.pam.summary.SummaryTypeTestUtil.mockEmptyPercentageResultDays;
import static org.entando.plugins.pda.pam.summary.SummaryTypeTestUtil.mockEmptyTotalResult;
import static org.entando.plugins.pda.pam.summary.SummaryTypeTestUtil.mockIncompletePercentageResultDays;
import static org.entando.plugins.pda.pam.summary.SummaryTypeTestUtil.mockPercentageResultDays;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import org.entando.plugins.pda.core.engine.Connection;
import org.entando.plugins.pda.core.model.summary.FrequencyEnum;
import org.entando.plugins.pda.core.model.summary.ValuePercentageSummaryValue;
import org.entando.plugins.pda.pam.service.api.KieApiService;
import org.junit.Before;
import org.junit.Test;
import org.kie.server.client.QueryServicesClient;

@SuppressWarnings("PMD.TooManyMethods")
public class RequestsSummaryDailyPercentageTest {

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
    public void shouldCalculatePercentageForDailyFrequency() {
        // Given
        mockEmptyTotalResult(requestsSummaryType, queryClient, PDA_TOTAL_PREFIX);
        mockPercentageResultDays(requestsSummaryType, queryClient, LocalDate.now(), LocalDate.now().minusDays(1), 150,
                100, PDA_PERC_DAYS_PREFIX);

        // When
        ValuePercentageSummaryValue summary = (ValuePercentageSummaryValue)requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.DAILY);

        // Then
        assertThat(summary.getPercentage()).isEqualTo("50");
    }

    @Test
    public void shouldHavePercentageDecreaseForDailyFrequency() {
        // Given
        mockEmptyTotalResult(requestsSummaryType, queryClient, PDA_TOTAL_PREFIX);
        mockPercentageResultDays(requestsSummaryType, queryClient, LocalDate.now(), LocalDate.now().minusDays(1), 100,
                150, PDA_PERC_DAYS_PREFIX);

        // When
        ValuePercentageSummaryValue summary = (ValuePercentageSummaryValue)requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.DAILY);

        // Then
        assertThat(summary.getPercentage()).isEqualTo("-50");
    }

    @Test
    public void shouldReturnPercentageZeroOnEmptyResult() {
        // Given
        mockEmptyTotalResult(requestsSummaryType, queryClient, PDA_TOTAL_PREFIX);
        mockEmptyPercentageResultDays(requestsSummaryType, queryClient, PDA_PERC_DAYS_PREFIX);

        // When
        ValuePercentageSummaryValue summary = (ValuePercentageSummaryValue)requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.DAILY);

        // Then
        assertThat(summary.getPercentage()).isEqualTo("0");
    }

    @Test
    public void shouldHave100PercentIncreaseWhenTodayHasValueAndDayBeforeIsZero() {
        // Given
        mockEmptyTotalResult(requestsSummaryType, queryClient, PDA_TOTAL_PREFIX);
        mockPercentageResultDays(requestsSummaryType, queryClient, LocalDate.now(), LocalDate.now().minusDays(1), 50,
                0, PDA_PERC_DAYS_PREFIX);

        // When
        ValuePercentageSummaryValue summary = (ValuePercentageSummaryValue)requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.DAILY);

        // Then
        assertThat(summary.getPercentage()).isEqualTo("100");
    }

    @Test
    public void shouldHave100PercentDecreaseWhenTodayIsZeroAndDayBeforeHasValue() {
        // Given
        mockEmptyTotalResult(requestsSummaryType, queryClient, PDA_TOTAL_PREFIX);
        mockPercentageResultDays(requestsSummaryType, queryClient, LocalDate.now(), LocalDate.now().minusDays(1), 0,
                50, PDA_PERC_DAYS_PREFIX);

        // When
        ValuePercentageSummaryValue summary = (ValuePercentageSummaryValue)requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.DAILY);

        // Then
        assertThat(summary.getPercentage()).isEqualTo("-100");
    }

    @Test
    public void shouldHave100PercentDecreaseWhenTodayIsMissingAndYesterdayHasValue() {
        // Given
        mockEmptyTotalResult(requestsSummaryType, queryClient, PDA_TOTAL_PREFIX);
        mockPercentageResultDays(requestsSummaryType, queryClient, LocalDate.now().minusDays(1),
                LocalDate.now().minusDays(2), 10, 5, PDA_PERC_DAYS_PREFIX);

        // When
        ValuePercentageSummaryValue summary = (ValuePercentageSummaryValue)requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.DAILY);

        // Then
        assertThat(summary.getPercentage()).isEqualTo("-100");
    }

    @Test
    public void shouldHaveZeroPercentWhenTodayAndDayBeforeAreMissing() {
        // Given
        mockEmptyTotalResult(requestsSummaryType, queryClient, PDA_TOTAL_PREFIX);
        mockPercentageResultDays(requestsSummaryType, queryClient, LocalDate.now().minusDays(2),
                LocalDate.now().minusDays(3), 10, 5, PDA_PERC_DAYS_PREFIX);

        // When
        ValuePercentageSummaryValue summary = (ValuePercentageSummaryValue)requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.DAILY);

        // Then
        assertThat(summary.getPercentage()).isEqualTo("0");
    }

    @Test
    public void shouldHaveZeroPercentWhenTodayAndDayBeforeHaveZeroValue() {
        // Given
        mockEmptyTotalResult(requestsSummaryType, queryClient, PDA_TOTAL_PREFIX);
        mockPercentageResultDays(requestsSummaryType, queryClient, LocalDate.now(), LocalDate.now().minusDays(1), 0, 0, PDA_PERC_DAYS_PREFIX);

        // When
        ValuePercentageSummaryValue summary = (ValuePercentageSummaryValue)requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.DAILY);

        // Then
        assertThat(summary.getPercentage()).isEqualTo("0");
    }

    @Test
    public void shouldHave100PercentWhenTodayHasValueAndDayBeforeIsMissing() {
        // Given
        mockEmptyTotalResult(requestsSummaryType, queryClient, PDA_TOTAL_PREFIX);
        mockPercentageResultDays(requestsSummaryType, queryClient, LocalDate.now(), LocalDate.now().minusDays(2), 50,
                10, PDA_PERC_DAYS_PREFIX);

        // When
        ValuePercentageSummaryValue summary = (ValuePercentageSummaryValue)requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.DAILY);

        // Then
        assertThat(summary.getPercentage()).isEqualTo("100");
    }

    @Test
    public void shouldHave100PercentWhenTodayHasValueAndIsTheOnlyOneOnResult() {
        // Given
        mockEmptyTotalResult(requestsSummaryType, queryClient, PDA_TOTAL_PREFIX);
        mockIncompletePercentageResultDays(requestsSummaryType, queryClient, LocalDate.now(), 10, PDA_PERC_DAYS_PREFIX);

        // When
        ValuePercentageSummaryValue summary = (ValuePercentageSummaryValue)requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.DAILY);

        // Then
        assertThat(summary.getPercentage()).isEqualTo("100");
    }

    @Test
    public void shouldHave100PercentDecreaseWhenYesterdayHasValueAndAndIsTheOnlyOneOnResult() {
        // Given
        mockEmptyTotalResult(requestsSummaryType, queryClient, PDA_TOTAL_PREFIX);
        mockIncompletePercentageResultDays(requestsSummaryType, queryClient, LocalDate.now().minusDays(1), 10, PDA_PERC_DAYS_PREFIX);

        // When
        ValuePercentageSummaryValue summary = (ValuePercentageSummaryValue)requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.DAILY);

        // Then
        assertThat(summary.getPercentage()).isEqualTo("-100");
    }

    @Test
    public void shouldHaveZeroPercentWhenTodayHasZeroValueAndAndIsTheOnlyOneOnResult() {
        // Given
        mockEmptyTotalResult(requestsSummaryType, queryClient, PDA_TOTAL_PREFIX);
        mockIncompletePercentageResultDays(requestsSummaryType, queryClient, LocalDate.now(), 0, PDA_PERC_DAYS_PREFIX);

        // When
        ValuePercentageSummaryValue summary = (ValuePercentageSummaryValue)requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.DAILY);

        // Then
        assertThat(summary.getPercentage()).isEqualTo("0");
    }
}
