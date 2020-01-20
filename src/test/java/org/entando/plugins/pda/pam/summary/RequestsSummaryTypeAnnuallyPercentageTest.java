package org.entando.plugins.pda.pam.summary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.entando.plugins.pda.pam.summary.RequestsSummaryTypeTestUtil.mockEmptyPercentageResultYears;
import static org.entando.plugins.pda.pam.summary.RequestsSummaryTypeTestUtil.mockEmptyTotalResult;
import static org.entando.plugins.pda.pam.summary.RequestsSummaryTypeTestUtil.mockIncompletePercentageResultYears;
import static org.entando.plugins.pda.pam.summary.RequestsSummaryTypeTestUtil.mockPercentageResultYears;
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

@SuppressWarnings("PMD.TooManyMethods")
public class RequestsSummaryTypeAnnuallyPercentageTest {

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
    public void shouldCalculatePercentageForAnnuallyFrequency() {
        mockEmptyTotalResult(requestsSummaryType, queryClient);
        mockPercentageResultYears(requestsSummaryType, queryClient, LocalDate.now().getYear(),
                LocalDate.now().minusYears(1).getYear(), 75, 50);

        // When
        Summary summary = requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.ANNUALLY);

        // Then
        assertThat(summary.getPercentage()).isEqualTo("50");
    }

    @Test
    public void shouldHavePercentageDecreaseForAnnuallyFrequency() {
        // Given
        mockEmptyTotalResult(requestsSummaryType, queryClient);
        mockPercentageResultYears(requestsSummaryType, queryClient, LocalDate.now().getYear(),
                LocalDate.now().minusYears(1).getYear(), 50, 75);

        // When
        Summary summary = requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.ANNUALLY);

        // Then
        assertThat(summary.getPercentage()).isEqualTo("-50");
    }

    @Test
    public void shouldReturnPercentageZeroOnEmptyResult() {
        // Given
        mockEmptyTotalResult(requestsSummaryType, queryClient);
        mockEmptyPercentageResultYears(requestsSummaryType, queryClient);

        // When
        Summary summary = requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.ANNUALLY);

        // Then
        assertThat(summary.getPercentage()).isEqualTo("0");
    }

    @Test
    public void shouldHave100PercentIncreaseWhenThisYearHasValueAndYearBeforeIsZero() {
        // Given
        mockEmptyTotalResult(requestsSummaryType, queryClient);
        mockPercentageResultYears(requestsSummaryType, queryClient, LocalDate.now().getYear(),
                LocalDate.now().minusYears(1).getYear(), 50, 0);

        // When
        Summary summary = requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.ANNUALLY);

        // Then
        assertThat(summary.getPercentage()).isEqualTo("100");
    }

    @Test
    public void shouldHave100PercentDecreaseWhenThisYearIsZeroAndYearBeforeHasValue() {
        // Given
        mockEmptyTotalResult(requestsSummaryType, queryClient);
        mockPercentageResultYears(requestsSummaryType, queryClient, LocalDate.now().getYear(),
                LocalDate.now().minusYears(1).getYear(), 0, 50);

        // When
        Summary summary = requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.ANNUALLY);

        // Then
        assertThat(summary.getPercentage()).isEqualTo("-100");
    }

    @Test
    public void shouldHave100PercentDecreaseWhenThisYearIsMissingAndLastYearHasValue() {
        // Given
        mockEmptyTotalResult(requestsSummaryType, queryClient);
        mockPercentageResultYears(requestsSummaryType, queryClient, LocalDate.now().minusYears(1).getYear(),
                LocalDate.now().minusYears(2).getYear(), 10, 5);

        // When
        Summary summary = requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.ANNUALLY);

        // Then
        assertThat(summary.getPercentage()).isEqualTo("-100");
    }

    @Test
    public void shouldHaveZeroPercentWhenThisYearAndYearBeforeAreMissing() {
        // Given
        mockEmptyTotalResult(requestsSummaryType, queryClient);
        mockPercentageResultYears(requestsSummaryType, queryClient, LocalDate.now().minusYears(2).getYear(),
                LocalDate.now().minusYears(3).getYear(), 10, 5);

        // When
        Summary summary = requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.ANNUALLY);

        // Then
        assertThat(summary.getPercentage()).isEqualTo("0");
    }

    @Test
    public void shouldHaveZeroPercentWhenThisYearAndYearBeforeHaveZeroValue() {
        // Given
        mockEmptyTotalResult(requestsSummaryType, queryClient);
        mockPercentageResultYears(requestsSummaryType, queryClient, LocalDate.now().getYear(),
                LocalDate.now().minusYears(1).getYear(), 0, 0);

        // When
        Summary summary = requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.ANNUALLY);

        // Then
        assertThat(summary.getPercentage()).isEqualTo("0");
    }

    @Test
    public void shouldHave100PercentWhenThisYearHasValueAndYearBeforeIsMissing() {
        // Given
        mockEmptyTotalResult(requestsSummaryType, queryClient);
        mockPercentageResultYears(requestsSummaryType, queryClient, LocalDate.now().getYear(),
                LocalDate.now().minusYears(2).getYear(), 50, 10);

        // When
        Summary summary = requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.ANNUALLY);

        // Then
        assertThat(summary.getPercentage()).isEqualTo("100");
    }

    @Test
    public void shouldHave100PercentWhenThisYearHasValueAndIsTheOnlyOneOnResult() {
        // Given
        mockEmptyTotalResult(requestsSummaryType, queryClient);
        mockIncompletePercentageResultYears(requestsSummaryType, queryClient, LocalDate.now().getYear(), 10);

        // When
        Summary summary = requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.ANNUALLY);

        // Then
        assertThat(summary.getPercentage()).isEqualTo("100");
    }

    @Test
    public void shouldHave100PercentDecreaseWhenLastYearHasValueAndAndIsTheOnlyOneOnResult() {
        // Given
        mockEmptyTotalResult(requestsSummaryType, queryClient);
        mockIncompletePercentageResultYears(requestsSummaryType, queryClient, LocalDate.now().minusYears(1).getYear(),
                10);

        // When
        Summary summary = requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.ANNUALLY);

        // Then
        assertThat(summary.getPercentage()).isEqualTo("-100");
    }

    @Test
    public void shouldHaveZeroPercentWhenThisYearHasZeroValueAndAndIsTheOnlyOneOnResult() {
        // Given
        mockEmptyTotalResult(requestsSummaryType, queryClient);
        mockIncompletePercentageResultYears(requestsSummaryType, queryClient, LocalDate.now().getYear(), 0);

        // When
        Summary summary = requestsSummaryType.calculateSummary(Connection.builder().build(), FrequencyEnum.ANNUALLY);

        // Then
        assertThat(summary.getPercentage()).isEqualTo("0");
    }
}
