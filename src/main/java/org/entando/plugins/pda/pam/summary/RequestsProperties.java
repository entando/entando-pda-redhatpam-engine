package org.entando.plugins.pda.pam.summary;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:requests.properties")
@Getter
@SuppressWarnings("PMD.TooManyFields")
public class RequestsProperties  {

    @Value("${requests.query.total}")
    private String queryTotal;

    @Value("${requests.query.percentage.days}")
    private String queryPercentageDays;

    @Value("${requests.query.percentage.months}")
    private String queryPercentageMonths;

    @Value("${requests.query.percentage.years}")
    private String queryPercentageYears;

    @Value("${dualtimeseries.query.line.days}")
    private String dualTimeSeriesLineDays;

    @Value("${dualtimeseries.query.line.months}")
    private String dualTimeSeriesLineMonths;

    @Value("${dualtimeseries.query.line.years}")
    private String dualTimeSeriesLineYears;

    @Value("${dualtimeseries.query.bar.days}")
    private String dualTimeSeriesBarDays;

    @Value("${dualtimeseries.query.bar.months}")
    private String dualTimeSeriesBarMonths;

    @Value("${dualtimeseries.query.bar.years}")
    private String dualTimeSeriesBarYears;

    @Value("${dualtimeseries.query.cardOne.total}")
    private String dualTimeSeriesCardOneTotal;

    @Value("${dualtimeseries.query.cardOne.days}")
    private String dualTimeSeriesCardOneDays;

    @Value("${dualtimeseries.query.cardOne.months}")
    private String dualTimeSeriesCardOneMonths;

    @Value("${dualtimeseries.query.cardOne.years}")
    private String dualTimeSeriesCardOneYears;

    @Value("${dualtimeseries.query.cardTwo.total}")
    private String dualTimeSeriesCardTwoTotal;

    @Value("${dualtimeseries.query.cardTwo.days}")
    private String dualTimeSeriesCardTwoDays;

    @Value("${dualtimeseries.query.cardTwo.months}")
    private String dualTimeSeriesCardTwoMonths;

    @Value("${dualtimeseries.query.cardTwo.years}")
    private String dualTimeSeriesCardTwoYears;

    @Value("${dualtimeseries.query.cardThree.total}")
    private String dualTimeSeriesCardThreeTotal;

    @Value("${dualtimeseries.query.cardThree.days}")
    private String dualTimeSeriesCardThreeDays;

    @Value("${dualtimeseries.query.cardThree.months}")
    private String dualTimeSeriesCardThreeMonths;

    @Value("${dualtimeseries.query.cardThree.years}")
    private String dualTimeSeriesCardThreeYears;

    @Value("${dualtimeseries.graph.days.entries}")
    private Integer dualTimeSeriesDaysEntries;

    @Value("${dualtimeseries.graph.months.entries}")
    private Integer dualTimeSeriesMonthsEntries;

    @Value("${dualtimeseries.graph.years.entries}")
    private Integer dualTimeSeriesYearsEntries;

}