package org.entando.plugins.pda.pam.summary;

import static org.entando.plugins.pda.pam.summary.KieSummaryUtils.calculatePercentageDays;
import static org.entando.plugins.pda.pam.summary.KieSummaryUtils.calculatePercentageMonths;
import static org.entando.plugins.pda.pam.summary.KieSummaryUtils.calculatePercentageYears;
import static org.entando.plugins.pda.pam.summary.KieSummaryUtils.calculateTotal;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.entando.plugins.pda.core.engine.Connection;
import org.entando.plugins.pda.core.model.summary.FrequencyEnum;
import org.entando.plugins.pda.core.model.summary.Summary;
import org.entando.plugins.pda.core.model.summary.SummaryType;
import org.entando.plugins.pda.pam.engine.KieEngine;
import org.entando.plugins.pda.pam.service.api.KieApiService;
import org.kie.server.api.model.definition.QueryDefinition;
import org.kie.server.client.QueryServicesClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RequestsSummaryType implements SummaryType {

    public static final String PDA_TOTAL_PREFIX = "pda-total-";
    public static final String PDA_PERC_DAYS_PREFIX = "pda-perc-days-";
    public static final String PDA_PERC_MONTHS_PREFIX = "pda-perc-months-";
    public static final String PDA_PERC_YEARS_PREFIX = "pda-perc-years-";
    public static final String KIE_SERVER_PERSISTENCE_DS = "${org.kie.server.persistence.ds}";
    public static final String CUSTOM_TARGET = "CUSTOM";
    public static final String REQUESTS_TITLE = "REQUESTS.TITLE";
    public static final String REQUESTS_TOTAL_LABEL = "REQUESTS.TOTAL_LABEL";

    private final KieApiService kieApiService;

    @Override
    public Summary calculateSummary(Connection connection, FrequencyEnum frequency) {
        String totalQuery = "SELECT min(startdate) as first_date, max(startdate) as end_date, count(*) as total\n"
                + "FROM processinstanceinfo\n";
        QueryServicesClient queryClient = kieApiService.getQueryServicesClient(connection);
        String total = String.valueOf(getTotal(queryClient, frequency, totalQuery));
        double percentage = 0.0;
        if (frequency.equals(FrequencyEnum.DAILY)) {
            percentage = getPercentageDays(queryClient);
        } else if (frequency.equals(FrequencyEnum.MONTHLY)) {
            percentage = getPercentageMonths(queryClient);
        } else if (frequency.equals(FrequencyEnum.ANNUALLY)) {
            percentage = getPercentageYears(queryClient);
        }
        return Summary.builder()
                .title(getDescription())
                .totalLabel(REQUESTS_TOTAL_LABEL)
                .total(total)
                .percentage(percentage)
                .build();
    }

    @Override
    public String getEngine() {
        return KieEngine.TYPE;
    }

    @Override
    public String getId() {
        return "requests";
    }

    @Override
    public String getDescription() {
        return REQUESTS_TITLE;
    }

    private double getTotal(QueryServicesClient queryClient, FrequencyEnum frequency, String query) {
        String queryName = PDA_TOTAL_PREFIX + getId();
        QueryDefinition queryDefinition = QueryDefinition.builder()
                .name(queryName)
                .source(KIE_SERVER_PERSISTENCE_DS)
                .expression(query).target(CUSTOM_TARGET)
                .build();
        queryClient.replaceQuery(queryDefinition);

        List<List> result = queryClient
                .query(queryName, QueryServicesClient.QUERY_MAP_RAW, 0, -1, List.class);
        if (result.isEmpty()) {
            return 0.0;
        }
        LocalDate firstDate = new Date((Long) result.get(0).get(0)).toInstant().atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate lastDate = new Date((Long) result.get(0).get(1)).toInstant().atZone(ZoneId.systemDefault())
                .toLocalDate();
        Double totalRecords = (Double) result.get(0).get(2);
        return calculateTotal(frequency, firstDate, lastDate, totalRecords);
    }

    private double getPercentageDays(QueryServicesClient queryClient) {
        String queryName = PDA_PERC_DAYS_PREFIX + getId();
        QueryDefinition queryDefinition = QueryDefinition.builder()
                .name(queryName)
                .source(KIE_SERVER_PERSISTENCE_DS)
                .expression(getQueryPercentageDays()).target(CUSTOM_TARGET)
                .build();
        queryClient.replaceQuery(queryDefinition);

        List<List> result = queryClient
                .query(queryName, QueryServicesClient.QUERY_MAP_RAW, 0, -1, List.class);

        if (result.isEmpty()) {
            return 0.0;
        }
        LocalDate lastDate = LocalDate
                .of(((Double) result.get(0).get(2)).intValue(), ((Double) result.get(0).get(1)).intValue(),
                        ((Double) result.get(0).get(0)).intValue());
        Double lastDateValue = (Double) result.get(0).get(3);
        LocalDate beforeLastDate = LocalDate
                .of(((Double) result.get(1).get(2)).intValue(), ((Double) result.get(1).get(1)).intValue(),
                        ((Double) result.get(1).get(0)).intValue());
        Double beforeLastDateValue = (Double) result.get(1).get(3);
        return calculatePercentageDays(lastDate, lastDateValue, beforeLastDate, beforeLastDateValue);
    }

    private String getQueryPercentageDays() {
        return "SELECT day(startdate) as day, month(startdate) as month, year(startdate) as year, count(*) as total\n"
                + "FROM processinstanceinfo\n"
                + "GROUP BY day, month, year\n"
                + "ORDER BY year DESC, month DESC, day DESC\n"
                + "LIMIT 2\n";
    }

    private double getPercentageMonths(QueryServicesClient queryClient) {
        String queryName = PDA_PERC_MONTHS_PREFIX + getId();
        QueryDefinition queryDefinition = QueryDefinition.builder()
                .name(queryName)
                .source(KIE_SERVER_PERSISTENCE_DS)
                .expression(getQueryPercentageMonths()).target(CUSTOM_TARGET)
                .build();
        queryClient.replaceQuery(queryDefinition);

        List<List> result = queryClient
                .query(queryName, QueryServicesClient.QUERY_MAP_RAW, 0, -1, List.class);

        if (result.isEmpty()) {
            return 0.0;
        }
        LocalDate lastDate = LocalDate
                .of(((Double) result.get(0).get(1)).intValue(), ((Double) result.get(0).get(0)).intValue(), 1);
        Double lastDateValue = (Double) result.get(0).get(2);
        LocalDate beforeLastDate = LocalDate
                .of(((Double) result.get(1).get(1)).intValue(), ((Double) result.get(1).get(0)).intValue(), 1);
        Double beforeLastDateValue = (Double) result.get(1).get(2);
        return calculatePercentageMonths(lastDate, lastDateValue, beforeLastDate, beforeLastDateValue);
    }

    private String getQueryPercentageMonths() {
        return "SELECT month(startdate) as month, year(startdate) as year, count(*) as total FROM processinstanceinfo\n"
                + "GROUP BY month, year\n"
                + "ORDER BY year DESC, month DESC\n"
                + "LIMIT 2\n";
    }

    private double getPercentageYears(QueryServicesClient queryClient) {
        String queryName = PDA_PERC_YEARS_PREFIX + getId();
        QueryDefinition queryDefinition = QueryDefinition.builder()
                .name(queryName)
                .source(KIE_SERVER_PERSISTENCE_DS)
                .expression(getQueryPercentageYears()).target(CUSTOM_TARGET)
                .build();
        queryClient.replaceQuery(queryDefinition);

        List<List> result = queryClient
                .query(queryName, QueryServicesClient.QUERY_MAP_RAW, 0, -1, List.class);

        if (result.isEmpty()) {
            return 0.0;
        }
        int lastYear = ((Double) result.get(0).get(0)).intValue();
        Double lastYearValue = (Double) result.get(0).get(1);
        int beforeLastYear = ((Double) result.get(1).get(0)).intValue();
        Double beforeLastYearValue = (Double) result.get(1).get(1);
        return calculatePercentageYears(lastYear, lastYearValue, beforeLastYear, beforeLastYearValue);
    }

    private String getQueryPercentageYears() {
        return "SELECT year(startdate) as year, count(*) as total FROM processinstanceinfo\n"
                + "GROUP BY year\n"
                + "ORDER BY year DESC\n"
                + "LIMIT 2\n";
    }
}
