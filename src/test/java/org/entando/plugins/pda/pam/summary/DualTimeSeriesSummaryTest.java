package org.entando.plugins.pda.pam.summary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.entando.plugins.pda.pam.summary.DualTimeSeriesSummary.PDA_DUAL_TIME_BAR_DAYS;
import static org.entando.plugins.pda.pam.summary.DualTimeSeriesSummary.PDA_DUAL_TIME_BAR_MONTHS;
import static org.entando.plugins.pda.pam.summary.DualTimeSeriesSummary.PDA_DUAL_TIME_BAR_YEARS;
import static org.entando.plugins.pda.pam.summary.DualTimeSeriesSummary.PDA_DUAL_TIME_CARD_ONE_DAYS;
import static org.entando.plugins.pda.pam.summary.DualTimeSeriesSummary.PDA_DUAL_TIME_CARD_ONE_MONTHS;
import static org.entando.plugins.pda.pam.summary.DualTimeSeriesSummary.PDA_DUAL_TIME_CARD_ONE_TOTAL;
import static org.entando.plugins.pda.pam.summary.DualTimeSeriesSummary.PDA_DUAL_TIME_CARD_ONE_YEARS;
import static org.entando.plugins.pda.pam.summary.DualTimeSeriesSummary.PDA_DUAL_TIME_CARD_THREE_DAYS;
import static org.entando.plugins.pda.pam.summary.DualTimeSeriesSummary.PDA_DUAL_TIME_CARD_THREE_MONTHS;
import static org.entando.plugins.pda.pam.summary.DualTimeSeriesSummary.PDA_DUAL_TIME_CARD_THREE_TOTAL;
import static org.entando.plugins.pda.pam.summary.DualTimeSeriesSummary.PDA_DUAL_TIME_CARD_THREE_YEARS;
import static org.entando.plugins.pda.pam.summary.DualTimeSeriesSummary.PDA_DUAL_TIME_CARD_TWO_DAYS;
import static org.entando.plugins.pda.pam.summary.DualTimeSeriesSummary.PDA_DUAL_TIME_CARD_TWO_MONTHS;
import static org.entando.plugins.pda.pam.summary.DualTimeSeriesSummary.PDA_DUAL_TIME_CARD_TWO_TOTAL;
import static org.entando.plugins.pda.pam.summary.DualTimeSeriesSummary.PDA_DUAL_TIME_CARD_TWO_YEARS;
import static org.entando.plugins.pda.pam.summary.DualTimeSeriesSummary.PDA_DUAL_TIME_LINE_DAYS;
import static org.entando.plugins.pda.pam.summary.DualTimeSeriesSummary.PDA_DUAL_TIME_LINE_MONTHS;
import static org.entando.plugins.pda.pam.summary.DualTimeSeriesSummary.PDA_DUAL_TIME_LINE_YEARS;
import static org.entando.plugins.pda.pam.summary.SummaryTypeTestUtil.mockPercentageResultDays;
import static org.entando.plugins.pda.pam.summary.SummaryTypeTestUtil.mockTimeSeriesEmpty;
import static org.entando.plugins.pda.pam.summary.SummaryTypeTestUtil.mockTimeSeriesFullDays;
import static org.entando.plugins.pda.pam.summary.SummaryTypeTestUtil.mockTimeSeriesFullMonths;
import static org.entando.plugins.pda.pam.summary.SummaryTypeTestUtil.mockTimeSeriesFullYears;
import static org.entando.plugins.pda.pam.summary.SummaryTypeTestUtil.mockTimeSeriesSparseDays;
import static org.entando.plugins.pda.pam.summary.SummaryTypeTestUtil.mockTimeSeriesSparseMonths;
import static org.entando.plugins.pda.pam.summary.SummaryTypeTestUtil.mockTimeSeriesSparseYears;
import static org.entando.plugins.pda.pam.summary.SummaryTypeTestUtil.mockTotalResult;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import org.entando.plugins.pda.core.engine.Connection;
import org.entando.plugins.pda.core.model.summary.DualTimeSeriesSummaryValue;
import org.entando.plugins.pda.core.model.summary.FrequencyEnum;
import org.entando.plugins.pda.core.model.summary.ValuePercentageSummaryValue;
import org.entando.plugins.pda.pam.service.api.KieApiService;
import org.junit.Before;
import org.junit.Test;
import org.kie.server.client.QueryServicesClient;

@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyMethods"})
public class DualTimeSeriesSummaryTest {


    public DualTimeSeriesSummary dualTimeSeriesSummary;
    private QueryServicesClient queryClient;
    private RequestsProperties requestsProperties;

    @Before
    public void init() {
        KieApiService kieApiService = mock(KieApiService.class);
        queryClient = mock(QueryServicesClient.class);
        requestsProperties = mock(RequestsProperties.class);
        when(kieApiService.getQueryServicesClient(any())).thenReturn(queryClient);
        dualTimeSeriesSummary = new DualTimeSeriesSummary(kieApiService, requestsProperties);
    }

    @Test
    public void shouldPopulateCardsDays() {
        mockTotals();

        mockPercentageResultDays(dualTimeSeriesSummary, queryClient, LocalDate.now(), LocalDate.now().minusDays(1), 150,
                100, PDA_DUAL_TIME_CARD_ONE_DAYS);
        mockPercentageResultDays(dualTimeSeriesSummary, queryClient, LocalDate.now(), LocalDate.now().minusDays(1), 150,
                100, PDA_DUAL_TIME_CARD_TWO_DAYS);
        mockPercentageResultDays(dualTimeSeriesSummary, queryClient, LocalDate.now(), LocalDate.now().minusDays(1), 150,
                100, PDA_DUAL_TIME_CARD_THREE_DAYS);

        DualTimeSeriesSummaryValue dualValue = (DualTimeSeriesSummaryValue)
                dualTimeSeriesSummary.calculateSummary(Connection.builder().build(), FrequencyEnum.DAILY);

        ValuePercentageSummaryValue cardOne = dualValue.getCardOne();
        ValuePercentageSummaryValue cardTwo = dualValue.getCardTwo();
        ValuePercentageSummaryValue cardThree = dualValue.getCardThree();

        assertThat(cardOne).isNotNull();
        assertThat(cardOne.getTitle()).isEqualTo(DualTimeSeriesSummary.DUAL_TIME_SERIES_CARD_ONE_TITLE);
        assertThat(cardOne.getTotal()).isEqualTo("41.15");

        assertThat(cardTwo).isNotNull();
        assertThat(cardTwo.getTitle()).isEqualTo(DualTimeSeriesSummary.DUAL_TIME_SERIES_CARD_TWO_TITLE);
        assertThat(cardTwo.getTotal()).isEqualTo("82.3");

        assertThat(cardThree).isNotNull();
        assertThat(cardThree.getTitle()).isEqualTo(DualTimeSeriesSummary.DUAL_TIME_SERIES_CARD_THREE_TITLE);
        assertThat(cardThree.getTotal()).isEqualTo("123.46");

    }

    @Test
    public void shouldPopulateCardsMonths() {

        mockTotals();

        mockPercentageResultDays(dualTimeSeriesSummary, queryClient, LocalDate.now(), LocalDate.now().minusMonths(1),
                150,
                100, PDA_DUAL_TIME_CARD_ONE_MONTHS);
        mockPercentageResultDays(dualTimeSeriesSummary, queryClient, LocalDate.now(), LocalDate.now().minusMonths(1),
                150,
                100, PDA_DUAL_TIME_CARD_TWO_MONTHS);
        mockPercentageResultDays(dualTimeSeriesSummary, queryClient, LocalDate.now(), LocalDate.now().minusMonths(1),
                150,
                100, PDA_DUAL_TIME_CARD_THREE_MONTHS);

        DualTimeSeriesSummaryValue dualValue = (DualTimeSeriesSummaryValue)
                dualTimeSeriesSummary.calculateSummary(Connection.builder().build(), FrequencyEnum.MONTHLY);

        ValuePercentageSummaryValue cardOne = dualValue.getCardOne();
        ValuePercentageSummaryValue cardTwo = dualValue.getCardTwo();
        ValuePercentageSummaryValue cardThree = dualValue.getCardThree();

        assertThat(cardOne).isNotNull();
        assertThat(cardOne.getTitle()).isEqualTo(DualTimeSeriesSummary.DUAL_TIME_SERIES_CARD_ONE_TITLE);
        assertThat(cardOne.getTotal()).isEqualTo("1250");

        assertThat(cardTwo).isNotNull();
        assertThat(cardTwo.getTitle()).isEqualTo(DualTimeSeriesSummary.DUAL_TIME_SERIES_CARD_TWO_TITLE);
        assertThat(cardTwo.getTotal()).isEqualTo("2500");

        assertThat(cardThree).isNotNull();
        assertThat(cardThree.getTitle()).isEqualTo(DualTimeSeriesSummary.DUAL_TIME_SERIES_CARD_THREE_TITLE);
        assertThat(cardThree.getTotal()).isEqualTo("3750");
    }

    @Test
    public void shouldPopulateCardsYears() {


        mockTotals();
        mockPercentageResultDays(dualTimeSeriesSummary, queryClient, LocalDate.now(), LocalDate.now().minusYears(1),
                150,
                100, PDA_DUAL_TIME_CARD_ONE_YEARS);
        mockPercentageResultDays(dualTimeSeriesSummary, queryClient, LocalDate.now(), LocalDate.now().minusYears(1),
                150,
                100, PDA_DUAL_TIME_CARD_TWO_YEARS);
        mockPercentageResultDays(dualTimeSeriesSummary, queryClient, LocalDate.now(), LocalDate.now().minusYears(1),
                150,
                100, PDA_DUAL_TIME_CARD_THREE_YEARS);

        DualTimeSeriesSummaryValue dualValue = (DualTimeSeriesSummaryValue)
                dualTimeSeriesSummary.calculateSummary(Connection.builder().build(), FrequencyEnum.ANNUALLY);

        ValuePercentageSummaryValue cardOne = dualValue.getCardOne();
        ValuePercentageSummaryValue cardTwo = dualValue.getCardTwo();
        ValuePercentageSummaryValue cardThree = dualValue.getCardThree();

        assertThat(cardOne).isNotNull();
        assertThat(cardOne.getTitle()).isEqualTo(DualTimeSeriesSummary.DUAL_TIME_SERIES_CARD_ONE_TITLE);
        assertThat(cardOne.getTotal()).isEqualTo("10000");

        assertThat(cardTwo).isNotNull();
        assertThat(cardTwo.getTitle()).isEqualTo(DualTimeSeriesSummary.DUAL_TIME_SERIES_CARD_TWO_TITLE);
        assertThat(cardTwo.getTotal()).isEqualTo("20000");

        assertThat(cardThree).isNotNull();
        assertThat(cardThree.getTitle()).isEqualTo(DualTimeSeriesSummary.DUAL_TIME_SERIES_CARD_THREE_TITLE);
        assertThat(cardThree.getTotal()).isEqualTo("30000");
    }

    private void mockTotals(){
        mockTotalResult(dualTimeSeriesSummary, queryClient, LocalDate.of(2019, 1, 1),
                LocalDate.of(2019, 9, 1), 10_000.0, PDA_DUAL_TIME_CARD_ONE_TOTAL);
        mockTotalResult(dualTimeSeriesSummary, queryClient, LocalDate.of(2019, 1, 1),
                LocalDate.of(2019, 9, 1), 20_000.0, PDA_DUAL_TIME_CARD_TWO_TOTAL);
        mockTotalResult(dualTimeSeriesSummary, queryClient, LocalDate.of(2019, 1, 1),
                LocalDate.of(2019, 9, 1), 30_000.0, PDA_DUAL_TIME_CARD_THREE_TOTAL);


    }
    @Test
    public void shouldPopulateYearsNoResults() {
        mockTimeSeriesEmpty(dualTimeSeriesSummary, queryClient, PDA_DUAL_TIME_BAR_YEARS);
        mockTimeSeriesEmpty(dualTimeSeriesSummary, queryClient, PDA_DUAL_TIME_LINE_YEARS);

        when(requestsProperties.getDualTimeSeriesYearsEntries()).thenReturn(30);

        DualTimeSeriesSummaryValue dualValue = (DualTimeSeriesSummaryValue)
                dualTimeSeriesSummary.calculateSummary(Connection.builder().build(), FrequencyEnum.ANNUALLY);

        assertThat(dualValue.getBarData().size()).isEqualTo(30);
        assertThat(dualValue.getLineData().size()).isEqualTo(30);

        for (int i = 0; i < 30; i++) {
            assertThat(dualValue.getBarData().get(i)).isEqualTo(0.0);
            assertThat(dualValue.getLineData().get(i)).isEqualTo(0.0);
        }
    }

    @Test
    public void shouldPopulateMonthsNoResults() {
        mockTimeSeriesEmpty(dualTimeSeriesSummary, queryClient, PDA_DUAL_TIME_BAR_MONTHS);
        mockTimeSeriesEmpty(dualTimeSeriesSummary, queryClient, PDA_DUAL_TIME_LINE_MONTHS);

        when(requestsProperties.getDualTimeSeriesMonthsEntries()).thenReturn(30);

        DualTimeSeriesSummaryValue dualValue = (DualTimeSeriesSummaryValue)
                dualTimeSeriesSummary.calculateSummary(Connection.builder().build(), FrequencyEnum.MONTHLY);

        assertThat(dualValue.getBarData().size()).isEqualTo(30);
        assertThat(dualValue.getLineData().size()).isEqualTo(30);

        for (int i = 0; i < 30; i++) {
            assertThat(dualValue.getBarData().get(i)).isEqualTo(0.0);
            assertThat(dualValue.getLineData().get(i)).isEqualTo(0.0);
        }
    }

    @Test
    public void shouldPopulateDaysNoResults() {
        mockTimeSeriesEmpty(dualTimeSeriesSummary, queryClient, PDA_DUAL_TIME_BAR_DAYS);
        mockTimeSeriesEmpty(dualTimeSeriesSummary, queryClient, PDA_DUAL_TIME_LINE_DAYS);

        when(requestsProperties.getDualTimeSeriesDaysEntries()).thenReturn(30);

        DualTimeSeriesSummaryValue dualValue = (DualTimeSeriesSummaryValue)
                dualTimeSeriesSummary.calculateSummary(Connection.builder().build(), FrequencyEnum.DAILY);

        assertThat(dualValue.getBarData().size()).isEqualTo(30);
        assertThat(dualValue.getLineData().size()).isEqualTo(30);

        for (int i = 0; i < 30; i++) {
            assertThat(dualValue.getBarData().get(i)).isEqualTo(0.0);
            assertThat(dualValue.getLineData().get(i)).isEqualTo(0.0);
        }
    }


    @Test
    public void shouldPopulateDaysFullResults() {

        mockTimeSeriesFullDays(dualTimeSeriesSummary, queryClient, PDA_DUAL_TIME_BAR_DAYS, 36, 106);
        mockTimeSeriesFullDays(dualTimeSeriesSummary, queryClient, PDA_DUAL_TIME_LINE_DAYS, 36, 206);

        when(requestsProperties.getDualTimeSeriesDaysEntries()).thenReturn(36);

        DualTimeSeriesSummaryValue dualValue = (DualTimeSeriesSummaryValue)
                dualTimeSeriesSummary.calculateSummary(Connection.builder().build(), FrequencyEnum.DAILY);

        assertThat(dualValue.getBarData().size()).isEqualTo(36);
        assertThat(dualValue.getLineData().size()).isEqualTo(36);

        for (int i = 0; i < 30; i++) {
            assertThat(dualValue.getBarData().get(i)).isEqualTo(106.0);
            assertThat(dualValue.getLineData().get(i)).isEqualTo(206.0);
        }

    }


    @Test
    public void shouldPopulateDaysSparseResults() {
        mockTimeSeriesSparseDays(dualTimeSeriesSummary, queryClient, PDA_DUAL_TIME_BAR_DAYS, 35, 105);
        mockTimeSeriesSparseDays(dualTimeSeriesSummary, queryClient, PDA_DUAL_TIME_LINE_DAYS, 35, 205);

        when(requestsProperties.getDualTimeSeriesDaysEntries()).thenReturn(35);

        DualTimeSeriesSummaryValue dualValue = (DualTimeSeriesSummaryValue)
                dualTimeSeriesSummary.calculateSummary(Connection.builder().build(), FrequencyEnum.DAILY);

        assertThat(dualValue.getBarData().size()).isEqualTo(35);
        assertThat(dualValue.getLineData().size()).isEqualTo(35);

        for (int i = 0; i < 30; i++) {
            assertThat(dualValue.getBarData().get(i)).isIn(0.0, 105.0);
            assertThat(dualValue.getLineData().get(i)).isIn(0.0, 205.0);
        }
    }

    @Test
    public void shouldPopulateMonthsFullResults() {

        mockTimeSeriesFullMonths(dualTimeSeriesSummary, queryClient, PDA_DUAL_TIME_BAR_MONTHS, 34, 104);
        mockTimeSeriesFullMonths(dualTimeSeriesSummary, queryClient, PDA_DUAL_TIME_LINE_MONTHS, 34, 204);

        when(requestsProperties.getDualTimeSeriesMonthsEntries()).thenReturn(34);

        DualTimeSeriesSummaryValue dualValue = (DualTimeSeriesSummaryValue)
                dualTimeSeriesSummary.calculateSummary(Connection.builder().build(), FrequencyEnum.MONTHLY);

        assertThat(dualValue.getBarData().size()).isEqualTo(34);
        assertThat(dualValue.getLineData().size()).isEqualTo(34);

        for (int i = 0; i < 30; i++) {
            assertThat(dualValue.getBarData().get(i)).isEqualTo(104.0);
            assertThat(dualValue.getLineData().get(i)).isEqualTo(204.0);
        }
    }

    @Test
    public void shouldPopulateMonthsSparseResults() {
        mockTimeSeriesSparseMonths(dualTimeSeriesSummary, queryClient, PDA_DUAL_TIME_BAR_MONTHS, 33, 103);
        mockTimeSeriesSparseMonths(dualTimeSeriesSummary, queryClient, PDA_DUAL_TIME_LINE_MONTHS, 33, 203);

        when(requestsProperties.getDualTimeSeriesMonthsEntries()).thenReturn(33);

        DualTimeSeriesSummaryValue dualValue = (DualTimeSeriesSummaryValue)
                dualTimeSeriesSummary.calculateSummary(Connection.builder().build(), FrequencyEnum.MONTHLY);

        assertThat(dualValue.getBarData().size()).isEqualTo(33);
        assertThat(dualValue.getLineData().size()).isEqualTo(33);

        for (int i = 0; i < 30; i++) {
            assertThat(dualValue.getBarData().get(i)).isIn(0.0, 103.0);
            assertThat(dualValue.getLineData().get(i)).isIn(0.0, 203.0);
        }
    }


    @Test
    public void shouldPopulateYearsFullResults() {

        mockTimeSeriesFullYears(dualTimeSeriesSummary, queryClient, PDA_DUAL_TIME_BAR_YEARS, 31, 101);
        mockTimeSeriesFullYears(dualTimeSeriesSummary, queryClient, PDA_DUAL_TIME_LINE_YEARS, 31, 201);

        when(requestsProperties.getDualTimeSeriesYearsEntries()).thenReturn(31);

        @SuppressWarnings("CPD-START")
        DualTimeSeriesSummaryValue dualValue = (DualTimeSeriesSummaryValue)
                dualTimeSeriesSummary.calculateSummary(Connection.builder().build(), FrequencyEnum.ANNUALLY);

        assertThat(dualValue.getBarData().size()).isEqualTo(31);
        assertThat(dualValue.getLineData().size()).isEqualTo(31);

        for (int i = 0; i < 30; i++) {
            assertThat(dualValue.getBarData().get(i)).isEqualTo(101.0);
            assertThat(dualValue.getLineData().get(i)).isEqualTo(201.0);
        }

    }

    @Test
    public void shouldPopulateYearsSparseResults() {

        mockTimeSeriesSparseYears(dualTimeSeriesSummary, queryClient, PDA_DUAL_TIME_BAR_YEARS, 32, 102);
        mockTimeSeriesSparseYears(dualTimeSeriesSummary, queryClient, PDA_DUAL_TIME_LINE_YEARS, 32, 202);

        when(requestsProperties.getDualTimeSeriesYearsEntries()).thenReturn(32);

        DualTimeSeriesSummaryValue dualValue = (DualTimeSeriesSummaryValue)
                dualTimeSeriesSummary.calculateSummary(Connection.builder().build(), FrequencyEnum.ANNUALLY);

        assertThat(dualValue.getBarData().size()).isEqualTo(32);
        assertThat(dualValue.getLineData().size()).isEqualTo(32);

        for (int i = 0; i < 30; i++) {
            assertThat(dualValue.getBarData().get(i)).isIn(0.0, 102.0);
            assertThat(dualValue.getLineData().get(i)).isIn(0.0, 202.0);
        }
    }
}
