package org.entando.plugins.pda.pam.summary;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.entando.plugins.pda.core.engine.Connection;
import org.entando.plugins.pda.core.model.summary.DualTimeSeriesSummaryValue;
import org.entando.plugins.pda.core.model.summary.FrequencyEnum;
import org.entando.plugins.pda.core.model.summary.Summary;
import org.entando.plugins.pda.core.model.summary.SummaryType;
import org.entando.plugins.pda.core.model.summary.SummaryValue;
import org.entando.plugins.pda.core.model.summary.ValuePercentageSummaryValue;
import org.entando.plugins.pda.pam.engine.KieEngine;
import org.entando.plugins.pda.pam.service.api.KieApiService;
import org.kie.server.api.model.definition.QueryDefinition;
import org.kie.server.client.QueryServicesClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DualTimeSeriesSummary implements Summary {

    //Labels for UI
    public static final String DUAL_TIME_SERIES_MAIN_TITLE = "DUAL_TIME_SERIES.MAIN_TITLE";
    public static final String DUAL_TIME_SERIES_BAR_TITLE = "DUAL_TIME_SERIES.BAR_TITLE";
    public static final String DUAL_TIME_SERIES_LINE_TITLE = "DUAL_TIME_SERIES.LINE_TITLE";
    public static final String DUAL_TIME_SERIES_CARD_ONE_TITLE = "DUAL_TIME_SERIES.CARD_ONE_TITLE";
    public static final String DUAL_TIME_SERIES_CARD_ONE_TOTAL = "DUAL_TIME_SERIES.CARD_ONE_TOTAL";
    public static final String DUAL_TIME_SERIES_CARD_TWO_TITLE = "DUAL_TIME_SERIES.CARD_TWO_TITLE";
    public static final String DUAL_TIME_SERIES_CARD_TWO_TOTAL = "DUAL_TIME_SERIES.CARD_TWO_TOTAL";
    public static final String DUAL_TIME_SERIES_CARD_THREE_TITLE = "DUAL_TIME_SERIES.CARD_THREE_TITLE";
    public static final String DUAL_TIME_SERIES_CARD_THREE_TOTAL = "DUAL_TIME_SERIES.CARD_THREE_TOTAL";

    //Query ids for PAM
    public static final String PDA_DUAL_TIME_CARD_ONE_TOTAL = "pda-dual-time-card-one-total-";
    public static final String PDA_DUAL_TIME_CARD_ONE_DAYS = "pda-dual-time-card-one-days-";
    public static final String PDA_DUAL_TIME_CARD_ONE_MONTHS = "pda-dual-time-card-one-months-";
    public static final String PDA_DUAL_TIME_CARD_ONE_YEARS = "pda-dual-time-card-one-years-";

    public static final String PDA_DUAL_TIME_CARD_TWO_TOTAL = "pda-dual-time-card-two-total-";
    public static final String PDA_DUAL_TIME_CARD_TWO_DAYS = "pda-dual-time-card-two-days-";
    public static final String PDA_DUAL_TIME_CARD_TWO_MONTHS = "pda-dual-time-card-two-months-";
    public static final String PDA_DUAL_TIME_CARD_TWO_YEARS = "pda-dual-time-card-two-years-";

    public static final String PDA_DUAL_TIME_CARD_THREE_TOTAL = "pda-dual-time-card-three-total-";
    public static final String PDA_DUAL_TIME_CARD_THREE_DAYS = "pda-dual-time-card-three-days-";
    public static final String PDA_DUAL_TIME_CARD_THREE_MONTHS = "pda-dual-time-card-three-months-";
    public static final String PDA_DUAL_TIME_CARD_THREE_YEARS = "pda-dual-time-card-three-years-";

    public static final String PDA_DUAL_TIME_BAR_DAYS = "pda-dual-time-card-bar-days-";
    public static final String PDA_DUAL_TIME_BAR_MONTHS = "pda-dual-time-card-bar-months-";
    public static final String PDA_DUAL_TIME_BAR_YEARS = "pda-dual-time-card-bar-years-";

    public static final String PDA_DUAL_TIME_LINE_DAYS = "pda-dual-time-card-line-days-";
    public static final String PDA_DUAL_TIME_LINE_MONTHS = "pda-dual-time-card-line-months-";
    public static final String PDA_DUAL_TIME_LINE_YEARS = "pda-dual-time-card-line-years-";

    //Query tags
    private static final String KIE_SERVER_PERSISTENCE_DS = "${org.kie.server.persistence.ds}";
    private static final String CUSTOM_TARGET = "CUSTOM";

    private final KieApiService kieApiService;
    private final RequestsProperties requestsProperties;

    private DecimalFormat decimalFormat = new DecimalFormat("#.##");

    @Override
    public SummaryValue calculateSummary(Connection connection, FrequencyEnum frequency) {

        QueryServicesClient queryClient = kieApiService.getQueryServicesClient(connection);

        //TODO parallelize queries
        double cardOneTotal = ValuePercentageCalculator.getTotal(queryClient,
                frequency,
                PDA_DUAL_TIME_CARD_ONE_TOTAL + getId(),
                requestsProperties.getDualTimeSeriesCardOneTotal());
        double cardTwoTotal = ValuePercentageCalculator.getTotal(queryClient,
                frequency,
                PDA_DUAL_TIME_CARD_TWO_TOTAL + getId(),
                requestsProperties.getDualTimeSeriesCardTwoTotal());
        double cardThreeTotal = ValuePercentageCalculator.getTotal(queryClient,
                frequency,
                PDA_DUAL_TIME_CARD_THREE_TOTAL + getId(),
                requestsProperties.getDualTimeSeriesCardThreeTotal());
        double percentageCardOne = 0.0;
        double percentageCardTwo = 0.0;
        double percentageCardThree = 0.0;

        if (frequency.equals(FrequencyEnum.DAILY)) {
            percentageCardOne = ValuePercentageCalculator.getPercentageDays(queryClient,
                    PDA_DUAL_TIME_CARD_ONE_DAYS + getId(),
                    requestsProperties.getDualTimeSeriesCardOneDays());
            percentageCardTwo = ValuePercentageCalculator.getPercentageDays(queryClient,
                    PDA_DUAL_TIME_CARD_TWO_DAYS + getId(),
                    requestsProperties.getDualTimeSeriesCardTwoDays());
            percentageCardThree = ValuePercentageCalculator.getPercentageDays(queryClient,
                    PDA_DUAL_TIME_CARD_THREE_DAYS + getId(),
                    requestsProperties.getDualTimeSeriesCardThreeDays());
        } else if (frequency.equals(FrequencyEnum.MONTHLY)) {
            percentageCardOne = ValuePercentageCalculator.getPercentageMonths(queryClient,
                    PDA_DUAL_TIME_CARD_ONE_MONTHS + getId(),
                    requestsProperties.getDualTimeSeriesCardOneMonths());
            percentageCardTwo = ValuePercentageCalculator.getPercentageMonths(queryClient,
                    PDA_DUAL_TIME_CARD_TWO_MONTHS + getId(),
                    requestsProperties.getDualTimeSeriesCardTwoMonths());
            percentageCardThree = ValuePercentageCalculator.getPercentageMonths(queryClient,
                    PDA_DUAL_TIME_CARD_THREE_MONTHS + getId(),
                    requestsProperties.getDualTimeSeriesCardThreeMonths());
        } else if (frequency.equals(FrequencyEnum.ANNUALLY)) {
            percentageCardOne = ValuePercentageCalculator.getPercentageYears(queryClient,
                    PDA_DUAL_TIME_CARD_ONE_YEARS + getId(),
                    requestsProperties.getDualTimeSeriesCardOneYears());
            percentageCardTwo = ValuePercentageCalculator.getPercentageYears(queryClient,
                    PDA_DUAL_TIME_CARD_TWO_YEARS + getId(),
                    requestsProperties.getDualTimeSeriesCardTwoYears());
            percentageCardThree = ValuePercentageCalculator.getPercentageYears(queryClient,
                    PDA_DUAL_TIME_CARD_THREE_YEARS + getId(),
                    requestsProperties.getDualTimeSeriesCardThreeYears());
        }

        ValuePercentageSummaryValue cardOne = ValuePercentageSummaryValue.builder()
                .title(DUAL_TIME_SERIES_CARD_ONE_TITLE)
                .totalLabel(DUAL_TIME_SERIES_CARD_ONE_TOTAL)
                .total(decimalFormat.format(cardOneTotal))
                .percentage(decimalFormat.format(percentageCardOne))
                .build();

        ValuePercentageSummaryValue cardTwo = ValuePercentageSummaryValue.builder()
                .title(DUAL_TIME_SERIES_CARD_TWO_TITLE)
                .totalLabel(DUAL_TIME_SERIES_CARD_TWO_TOTAL)
                .total(decimalFormat.format(cardTwoTotal))
                .percentage(decimalFormat.format(percentageCardTwo))
                .build();

        ValuePercentageSummaryValue cardThree = ValuePercentageSummaryValue.builder()
                .title(DUAL_TIME_SERIES_CARD_THREE_TITLE)
                .totalLabel(DUAL_TIME_SERIES_CARD_THREE_TOTAL)
                .total(decimalFormat.format(cardThreeTotal))
                .percentage(decimalFormat.format(percentageCardThree))
                .build();

        List[] seriesData = calculateSeriesData(queryClient, frequency);

        return DualTimeSeriesSummaryValue.builder()
                .mainTitle(getDescription())
                .cardOne(cardOne)
                .cardTwo(cardTwo)
                .cardThree(cardThree)
                .dateData(seriesData[0])
                .barData(seriesData[1])
                .lineData(seriesData[2])
                .barTitle(DUAL_TIME_SERIES_BAR_TITLE)
                .lineTitle(DUAL_TIME_SERIES_LINE_TITLE)
                .build();

    }

    @Override
    public SummaryType getSummaryType() {
        return SummaryType.DUAL_TIME_SERIES;
    }

    @Override
    public String getEngine() {
        return KieEngine.TYPE;
    }

    @Override
    public String getId() {
        return "dualTimeSeries";
    }

    @Override
    public String getDescription() {
        return DUAL_TIME_SERIES_MAIN_TITLE;
    }

    private List[] calculateSeriesData(QueryServicesClient queryClient, FrequencyEnum frequency) {

        String barQueryName = "";
        String barQuery = "";
        String lineQueryName = "";
        String lineQuery = "";

        if (frequency.equals(FrequencyEnum.DAILY)) {
            barQueryName = PDA_DUAL_TIME_BAR_DAYS + getId();
            barQuery = requestsProperties.getDualTimeSeriesBarDays();

            lineQueryName = PDA_DUAL_TIME_LINE_DAYS + getId();
            lineQuery = requestsProperties.getDualTimeSeriesLineDays();

        } else if (frequency.equals(FrequencyEnum.MONTHLY)) {
            barQueryName = PDA_DUAL_TIME_BAR_MONTHS + getId();
            barQuery = requestsProperties.getDualTimeSeriesBarMonths();

            lineQueryName = PDA_DUAL_TIME_LINE_MONTHS + getId();
            lineQuery = requestsProperties.getDualTimeSeriesLineMonths();

        } else if (frequency.equals(FrequencyEnum.ANNUALLY)) {
            barQueryName = PDA_DUAL_TIME_BAR_YEARS + getId();
            barQuery = requestsProperties.getDualTimeSeriesBarYears();

            lineQueryName = PDA_DUAL_TIME_LINE_YEARS + getId();
            lineQuery = requestsProperties.getDualTimeSeriesLineYears();

        }

        List<List> barData = fetchSeriesData(queryClient, barQueryName, barQuery);
        List<List> lineData = fetchSeriesData(queryClient, lineQueryName, lineQuery);

        return buildSeries(barData, lineData, frequency);

    }

    private List[] buildSeries(List<List> barData, List<List> lineData, FrequencyEnum frequency) {

        List<String> dateSeries = mapDateData(frequency);

        //Map the row data by date string relative to the frequency so we can detect missing dates
        Map<String, List> barDataMapped = mapRowData(barData, frequency);
        Map<String, List> lineDataMapped = mapRowData(lineData, frequency);

        List<Double> barSeries = mapSeries(dateSeries, barDataMapped);
        List<Double> lineSeries = mapSeries(dateSeries, lineDataMapped);

        List[] seriesData = {dateSeries, barSeries, lineSeries}; //NOPMD
        return seriesData; //NOPMD
    }

    private List<Double> mapSeries(List<String> dateSeries, Map<String, List> seriesData) {
        List<Double> seriesValues = new ArrayList<>();

        for (String dateKey : dateSeries) {
            if (seriesData.containsKey(dateKey)) {
                List row = seriesData.get(dateKey);
                seriesValues.add((Double) row.get(row.size() - 1));
            } else {
                seriesValues.add(0.0);
            }
        }

        return seriesValues;
    }

    private Map<String, List> mapRowData(List<List> rowData, FrequencyEnum frequency) {

        Map<String, List> mappedData = new ConcurrentHashMap<>();
        rowData.stream()
                .forEach(row -> mappedData.put(parseDateKeyFromRowData(row, frequency), row));

        return mappedData;

    }

    //This method processes the query for the total number of row entries for the given frequency
    //and generates an ordered list of date keys that can be used to index the query response from PAM.
    //These date keys can be used to populate zeros for missing dates in the result set
    private List<String> mapDateData(FrequencyEnum frequencyEnum) {

        List<String> dateKeys = new ArrayList<>();
        LocalDate currDate = LocalDate.now();

        if (frequencyEnum.equals(FrequencyEnum.DAILY)) {
            Integer dayEntries = requestsProperties.getDualTimeSeriesDaysEntries();
            LocalDate startDate = currDate.minusDays(dayEntries);

            for (int i = 0; i < dayEntries; i++) {
                LocalDate rowDate = startDate.plusDays(i);
                StringBuilder keyBuilder = new StringBuilder(); //NOPMD
                keyBuilder.append(rowDate.getDayOfMonth()).append(rowDate.getMonthValue()).append(rowDate.getYear());
                dateKeys.add(keyBuilder.toString());
            }
        } else if (frequencyEnum.equals(FrequencyEnum.MONTHLY)) {
            Integer monthEntries = requestsProperties.getDualTimeSeriesMonthsEntries();

            LocalDate startDate = currDate.minusMonths(monthEntries);
            for (int i = 0; i < monthEntries; i++) {
                LocalDate rowDate = startDate.plusMonths(i);
                StringBuilder keyBuilder = new StringBuilder(); //NOPMD
                keyBuilder.append(rowDate.getMonthValue()).append(rowDate.getYear());
                dateKeys.add(keyBuilder.toString());
            }
        } else if (frequencyEnum.equals(FrequencyEnum.ANNUALLY)) {
            Integer yearsEntries = requestsProperties.getDualTimeSeriesYearsEntries();
            LocalDate startDate = currDate.minusYears(yearsEntries);
            for (int i = 0; i < yearsEntries; i++) {
                LocalDate rowDate = startDate.plusYears(i);
                StringBuilder keyBuilder = new StringBuilder(); //NOPMD
                keyBuilder.append(rowDate.getYear());
                dateKeys.add(keyBuilder.toString());
            }
        }

        return dateKeys;

    }

    private String parseDateKeyFromRowData(List data, FrequencyEnum frequency) {

        String key = null;
        if (frequency.equals(FrequencyEnum.DAILY)) {
            String day = String.valueOf(((Double) data.get(0)).intValue());
            String month = String.valueOf(((Double) data.get(1)).intValue());
            String year = String.valueOf(((Double) data.get(2)).intValue());

            key = day + month + year;

        } else if (frequency.equals(FrequencyEnum.MONTHLY)) {
            String month = String.valueOf(((Double) data.get(0)).intValue());
            String year = String.valueOf(((Double) data.get(1)).intValue());
            key = month + year;

        } else if (frequency.equals(FrequencyEnum.ANNUALLY)) {
            String year = String.valueOf(((Double) data.get(0)).intValue());
            key = year;
        }

        return key;

    }

    private List<List> fetchSeriesData(QueryServicesClient queryClient, String queryName, String query) {
        QueryDefinition queryDef = QueryDefinition.builder()
                .name(queryName)
                .source(KIE_SERVER_PERSISTENCE_DS)
                .expression(query).target(CUSTOM_TARGET)
                .build();
        queryClient.replaceQuery(queryDef);

        return queryClient
                .query(queryName, QueryServicesClient.QUERY_MAP_RAW, 0, -1, List.class);

    }
}
