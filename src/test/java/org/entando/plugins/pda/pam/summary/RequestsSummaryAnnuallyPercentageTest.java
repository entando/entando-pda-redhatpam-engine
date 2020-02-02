package org.entando.plugins.pda.pam.summary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.entando.plugins.pda.pam.summary.RequestsSummaryType.PDA_PERC_YEARS_PREFIX;
import static org.entando.plugins.pda.pam.summary.RequestsSummaryType.PDA_TOTAL_PREFIX;
import static org.entando.plugins.pda.pam.summary.SummaryTypeTestUtil.mockEmptyPercentageResultYears;
import static org.entando.plugins.pda.pam.summary.SummaryTypeTestUtil.mockEmptyTotalResult;
import static org.entando.plugins.pda.pam.summary.SummaryTypeTestUtil.mockIncompletePercentageResultYears;
import static org.entando.plugins.pda.pam.summary.SummaryTypeTestUtil.mockPercentageResultYears;
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
public class RequestsSummaryAnnuallyPercentageTest {

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
    public void shouldCalculatePercentageForAnnuallyFrequency() {
        mockEmptyTotalResult(requestsSummaryType, queryClient, PDA_TOTAL_PREFIX);
        mockPercentageResultYears(requestsSummaryType, queryClient, LocalDate.now().getYear(),
                LocalDate.now().minusYears(1).getYear(), 75, 50, PDA_PERC_YEARS_PREFIX);

        // When
        ValuePercentageSummaryValue summary = (ValuePercentageSummaryValue)requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.ANNUALLY);

        // Then
        assertThat(summary.getPercentage()).isEqualTo("50");
    }

    @Test
    public void shouldHavePercentageDecreaseForAnnuallyFrequency() {
        // Given
        mockEmptyTotalResult(requestsSummaryType, queryClient, PDA_TOTAL_PREFIX);
        mockPercentageResultYears(requestsSummaryType, queryClient, LocalDate.now().getYear(),
                LocalDate.now().minusYears(1).getYear(), 50, 75, PDA_PERC_YEARS_PREFIX);

        // When
        ValuePercentageSummaryValue summary = (ValuePercentageSummaryValue)requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.ANNUALLY);

        // Then
        assertThat(summary.getPercentage()).isEqualTo("-50");
    }

    @Test
    public void shouldReturnPercentageZeroOnEmptyResult() {
        // Given
        mockEmptyTotalResult(requestsSummaryType, queryClient, PDA_TOTAL_PREFIX);
        mockEmptyPercentageResultYears(requestsSummaryType, queryClient, PDA_PERC_YEARS_PREFIX);

        // When
        ValuePercentageSummaryValue summary = (ValuePercentageSummaryValue)requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.ANNUALLY);

        // Then
        assertThat(summary.getPercentage()).isEqualTo("0");
    }

    @Test
    public void shouldHave100PercentIncreaseWhenThisYearHasValueAndYearBeforeIsZero() {
        // Given
        mockEmptyTotalResult(requestsSummaryType, queryClient, PDA_TOTAL_PREFIX);
        mockPercentageResultYears(requestsSummaryType, queryClient, LocalDate.now().getYear(),
                LocalDate.now().minusYears(1).getYear(), 50, 0, PDA_PERC_YEARS_PREFIX);

        // When
        ValuePercentageSummaryValue summary = (ValuePercentageSummaryValue)requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.ANNUALLY);

        // Then
        assertThat(summary.getPercentage()).isEqualTo("100");
    }

    @Test
    public void shouldHave100PercentDecreaseWhenThisYearIsZeroAndYearBeforeHasValue() {
        // Given
        mockEmptyTotalResult(requestsSummaryType, queryClient, PDA_TOTAL_PREFIX);
        mockPercentageResultYears(requestsSummaryType, queryClient, LocalDate.now().getYear(),
                LocalDate.now().minusYears(1).getYear(), 0, 50, PDA_PERC_YEARS_PREFIX);

        // When
        ValuePercentageSummaryValue summary = (ValuePercentageSummaryValue)requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.ANNUALLY);

        // Then
        assertThat(summary.getPercentage()).isEqualTo("-100");
    }

    @Test
    public void shouldHave100PercentDecreaseWhenThisYearIsMissingAndLastYearHasValue() {
        // Given
        mockEmptyTotalResult(requestsSummaryType, queryClient, PDA_TOTAL_PREFIX);
        mockPercentageResultYears(requestsSummaryType, queryClient, LocalDate.now().minusYears(1).getYear(),
                LocalDate.now().minusYears(2).getYear(), 10, 5, PDA_PERC_YEARS_PREFIX);

        // When
        ValuePercentageSummaryValue summary = (ValuePercentageSummaryValue)requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.ANNUALLY);

        // Then
        assertThat(summary.getPercentage()).isEqualTo("-100");
    }

    @Test
    public void shouldHaveZeroPercentWhenThisYearAndYearBeforeAreMissing() {
        // Given
        mockEmptyTotalResult(requestsSummaryType, queryClient, PDA_TOTAL_PREFIX);
        mockPercentageResultYears(requestsSummaryType, queryClient, LocalDate.now().minusYears(2).getYear(),
                LocalDate.now().minusYears(3).getYear(), 10, 5, PDA_PERC_YEARS_PREFIX);

        // When
        ValuePercentageSummaryValue summary = (ValuePercentageSummaryValue)requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.ANNUALLY);

        // Then
        assertThat(summary.getPercentage()).isEqualTo("0");
    }

    @Test
    public void shouldHaveZeroPercentWhenThisYearAndYearBeforeHaveZeroValue() {
        // Given
        mockEmptyTotalResult(requestsSummaryType, queryClient, PDA_TOTAL_PREFIX);
        mockPercentageResultYears(requestsSummaryType, queryClient, LocalDate.now().getYear(),
                LocalDate.now().minusYears(1).getYear(), 0, 0, PDA_PERC_YEARS_PREFIX);

        // When
        ValuePercentageSummaryValue summary = (ValuePercentageSummaryValue)requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.ANNUALLY);

        // Then
        assertThat(summary.getPercentage()).isEqualTo("0");
    }

    @Test
    public void shouldHave100PercentWhenThisYearHasValueAndYearBeforeIsMissing() {
        // Given
        mockEmptyTotalResult(requestsSummaryType, queryClient, PDA_TOTAL_PREFIX);
        mockPercentageResultYears(requestsSummaryType, queryClient, LocalDate.now().getYear(),
                LocalDate.now().minusYears(2).getYear(), 50, 10, PDA_PERC_YEARS_PREFIX);

        // When
        ValuePercentageSummaryValue summary = (ValuePercentageSummaryValue)requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.ANNUALLY);

        // Then
        assertThat(summary.getPercentage()).isEqualTo("100");
    }

    @Test
    public void shouldHave100PercentWhenThisYearHasValueAndIsTheOnlyOneOnResult() {
        // Given
        mockEmptyTotalResult(requestsSummaryType, queryClient, PDA_TOTAL_PREFIX);
        mockIncompletePercentageResultYears(requestsSummaryType, queryClient, LocalDate.now().getYear(), 10,
                PDA_PERC_YEARS_PREFIX);

        // When
        ValuePercentageSummaryValue summary = (ValuePercentageSummaryValue)requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.ANNUALLY);

        // Then
        assertThat(summary.getPercentage()).isEqualTo("100");
    }

    @Test
    public void shouldHave100PercentDecreaseWhenLastYearHasValueAndAndIsTheOnlyOneOnResult() {
        // Given
        mockEmptyTotalResult(requestsSummaryType, queryClient, PDA_TOTAL_PREFIX);
        mockIncompletePercentageResultYears(requestsSummaryType, queryClient, LocalDate.now().minusYears(1).getYear(),
                10, PDA_PERC_YEARS_PREFIX);

        // When
        ValuePercentageSummaryValue summary = (ValuePercentageSummaryValue)requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.ANNUALLY);

        // Then
        assertThat(summary.getPercentage()).isEqualTo("-100");
    }

    @Test
    public void shouldHaveZeroPercentWhenThisYearHasZeroValueAndAndIsTheOnlyOneOnResult() {
        // Given
        mockEmptyTotalResult(requestsSummaryType, queryClient, PDA_TOTAL_PREFIX);
        mockIncompletePercentageResultYears(requestsSummaryType, queryClient, LocalDate.now().getYear(), 0, PDA_PERC_YEARS_PREFIX);

        // When
        ValuePercentageSummaryValue summary = (ValuePercentageSummaryValue)requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.ANNUALLY);

        // Then
        assertThat(summary.getPercentage()).isEqualTo("0");
    }
}
