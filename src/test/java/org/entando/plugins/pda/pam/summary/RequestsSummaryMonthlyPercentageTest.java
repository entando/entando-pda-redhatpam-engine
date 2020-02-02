package org.entando.plugins.pda.pam.summary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.entando.plugins.pda.pam.summary.RequestsSummaryType.PDA_PERC_MONTHS_PREFIX;
import static org.entando.plugins.pda.pam.summary.RequestsSummaryType.PDA_TOTAL_PREFIX;
import static org.entando.plugins.pda.pam.summary.SummaryTypeTestUtil.mockEmptyPercentageResultMonths;
import static org.entando.plugins.pda.pam.summary.SummaryTypeTestUtil.mockEmptyTotalResult;
import static org.entando.plugins.pda.pam.summary.SummaryTypeTestUtil.mockIncompletePercentageResultMonths;
import static org.entando.plugins.pda.pam.summary.SummaryTypeTestUtil.mockPercentageResultMonths;
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
public class RequestsSummaryMonthlyPercentageTest {

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
    public void shouldCalculatePercentageForMonthlyFrequency() {
        mockEmptyTotalResult(requestsSummaryType, queryClient, PDA_TOTAL_PREFIX);
        mockPercentageResultMonths(requestsSummaryType, queryClient, LocalDate.now(), LocalDate.now().minusMonths(1),
                75, 50, PDA_PERC_MONTHS_PREFIX);

        // When
        ValuePercentageSummaryValue summary = (ValuePercentageSummaryValue)requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.MONTHLY);

        // Then
        assertThat(summary.getPercentage()).isEqualTo("50");
    }

    @Test
    public void shouldHavePercentageDecreaseForMonthlyFrequency() {
        // Given
        mockEmptyTotalResult(requestsSummaryType, queryClient, PDA_TOTAL_PREFIX);
        mockPercentageResultMonths(requestsSummaryType, queryClient, LocalDate.now(), LocalDate.now().minusMonths(1),
                100, 150, PDA_PERC_MONTHS_PREFIX);

        // When
        ValuePercentageSummaryValue summary = (ValuePercentageSummaryValue)requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.MONTHLY);

        // Then
        assertThat(summary.getPercentage()).isEqualTo("-50");
    }

    @Test
    public void shouldReturnPercentageZeroOnEmptyMonthlyResult() {
        // Given
        mockEmptyTotalResult(requestsSummaryType, queryClient, PDA_TOTAL_PREFIX);
        mockEmptyPercentageResultMonths(requestsSummaryType, queryClient, PDA_PERC_MONTHS_PREFIX);

        // When
        ValuePercentageSummaryValue summary = (ValuePercentageSummaryValue)requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.MONTHLY);

        // Then
        assertThat(summary.getPercentage()).isEqualTo("0");
    }

    @Test
    public void shouldHave100PercentIncreaseWhenThisMonthHasValueAndMonthBeforeIsZero() {
        // Given
        mockEmptyTotalResult(requestsSummaryType, queryClient, PDA_TOTAL_PREFIX);
        mockPercentageResultMonths(requestsSummaryType, queryClient, LocalDate.now(), LocalDate.now().minusMonths(1),
                50,
                0, PDA_PERC_MONTHS_PREFIX);

        // When
        ValuePercentageSummaryValue summary = (ValuePercentageSummaryValue)requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.MONTHLY);

        // Then
        assertThat(summary.getPercentage()).isEqualTo("100");
    }

    @Test
    public void shouldHave100PercentDecreaseWhenThisMonthIsZeroAndMonthBeforeHasValue() {
        // Given
        mockEmptyTotalResult(requestsSummaryType, queryClient, PDA_TOTAL_PREFIX);
        mockPercentageResultMonths(requestsSummaryType, queryClient, LocalDate.now(), LocalDate.now().minusMonths(1), 0,
                50, PDA_PERC_MONTHS_PREFIX);

        // When
        ValuePercentageSummaryValue summary = (ValuePercentageSummaryValue)requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.MONTHLY);

        // Then
        assertThat(summary.getPercentage()).isEqualTo("-100");
    }

    @Test
    public void shouldHave100PercentDecreaseWhenThisMonthIsMissingAndMonthBeforeHasValue() {
        // Given
        mockEmptyTotalResult(requestsSummaryType, queryClient, PDA_TOTAL_PREFIX);
        mockPercentageResultMonths(requestsSummaryType, queryClient, LocalDate.now().minusMonths(1),
                LocalDate.now().minusMonths(2), 10, 5, PDA_PERC_MONTHS_PREFIX);

        // When
        ValuePercentageSummaryValue summary = (ValuePercentageSummaryValue)requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.MONTHLY);

        // Then
        assertThat(summary.getPercentage()).isEqualTo("-100");
    }

    @Test
    public void shouldHaveZeroPercentWhenThisMonthAndMonthBeforeAreMissing() {
        // Given
        mockEmptyTotalResult(requestsSummaryType, queryClient, PDA_TOTAL_PREFIX);
        mockPercentageResultMonths(requestsSummaryType, queryClient, LocalDate.now().minusMonths(2),
                LocalDate.now().minusMonths(3), 10, 5, PDA_PERC_MONTHS_PREFIX);

        // When
        ValuePercentageSummaryValue summary = (ValuePercentageSummaryValue)requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.MONTHLY);

        // Then
        assertThat(summary.getPercentage()).isEqualTo("0");
    }

    @Test
    public void shouldHaveZeroPercentWhenThisMonthAndMonthBeforeHaveZeroValue() {
        // Given
        mockEmptyTotalResult(requestsSummaryType, queryClient, PDA_TOTAL_PREFIX);
        mockPercentageResultMonths(requestsSummaryType, queryClient, LocalDate.now(), LocalDate.now().minusMonths(1), 0,
                0, PDA_PERC_MONTHS_PREFIX);

        // When
        ValuePercentageSummaryValue summary = (ValuePercentageSummaryValue)requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.MONTHLY);

        // Then
        assertThat(summary.getPercentage()).isEqualTo("0");
    }

    @Test
    public void shouldHave100PercentWhenThisMonthHasValueAndMonthBeforeIsMissing() {
        // Given
        mockEmptyTotalResult(requestsSummaryType, queryClient, PDA_TOTAL_PREFIX);
        mockPercentageResultMonths(requestsSummaryType, queryClient, LocalDate.now(), LocalDate.now().minusMonths(2),
                50, 10, PDA_PERC_MONTHS_PREFIX);

        // When
        ValuePercentageSummaryValue summary = (ValuePercentageSummaryValue)requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.MONTHLY);

        // Then
        assertThat(summary.getPercentage()).isEqualTo("100");
    }

    @Test
    public void shouldHave100PercentWhenThisMonthHasValueAndIsTheOnlyOneOnResult() {
        // Given
        mockEmptyTotalResult(requestsSummaryType, queryClient, PDA_TOTAL_PREFIX);
        mockIncompletePercentageResultMonths(requestsSummaryType, queryClient, LocalDate.now(), 10, PDA_PERC_MONTHS_PREFIX);

        // When
        ValuePercentageSummaryValue summary = (ValuePercentageSummaryValue)requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.MONTHLY);

        // Then
        assertThat(summary.getPercentage()).isEqualTo("100");
    }

    @Test
    public void shouldHave100PercentDecreaseWhenLastMonthHasValueAndAndIsTheOnlyOneOnResult() {
        // Given
        mockEmptyTotalResult(requestsSummaryType, queryClient, PDA_TOTAL_PREFIX);
        mockIncompletePercentageResultMonths(requestsSummaryType, queryClient, LocalDate.now().minusMonths(1), 10, PDA_PERC_MONTHS_PREFIX);

        // When
        ValuePercentageSummaryValue summary = (ValuePercentageSummaryValue)requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.MONTHLY);

        // Then
        assertThat(summary.getPercentage()).isEqualTo("-100");
    }

    @Test
    public void shouldHaveZeroPercentWhenThisMonthHasZeroValueAndAndIsTheOnlyOneOnResult() {
        // Given
        mockEmptyTotalResult(requestsSummaryType, queryClient, PDA_TOTAL_PREFIX);
        mockIncompletePercentageResultMonths(requestsSummaryType, queryClient, LocalDate.now(), 0, PDA_PERC_MONTHS_PREFIX);

        // When
        ValuePercentageSummaryValue summary = (ValuePercentageSummaryValue)requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.MONTHLY);

        // Then
        assertThat(summary.getPercentage()).isEqualTo("0");
    }
}
