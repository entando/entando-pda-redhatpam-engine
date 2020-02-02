package org.entando.plugins.pda.pam.summary;

import static org.entando.plugins.pda.pam.summary.KieSummaryUtils.calculatePercentageDays;
import static org.entando.plugins.pda.pam.summary.KieSummaryUtils.calculatePercentageDaysSingleResult;
import static org.entando.plugins.pda.pam.summary.KieSummaryUtils.calculatePercentageMonths;
import static org.entando.plugins.pda.pam.summary.KieSummaryUtils.calculatePercentageMonthsSingleResult;
import static org.entando.plugins.pda.pam.summary.KieSummaryUtils.calculatePercentageYears;
import static org.entando.plugins.pda.pam.summary.KieSummaryUtils.calculatePercentageYearsSingleResult;
import static org.entando.plugins.pda.pam.summary.KieSummaryUtils.calculateTotal;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import org.entando.plugins.pda.core.model.summary.FrequencyEnum;
import org.kie.server.api.model.definition.QueryDefinition;
import org.kie.server.client.QueryServicesClient;

public final class ValuePercentageCalculator {

    private static final int SINGLE_RESULT_SIZE = 1;
    private static final String KIE_SERVER_PERSISTENCE_DS = "${org.kie.server.persistence.ds}";
    private static final String CUSTOM_TARGET = "CUSTOM";

    private ValuePercentageCalculator() {
    }

    public static double getTotal(QueryServicesClient queryClient,
            FrequencyEnum frequency,
            String queryName,
            String query) {

        QueryDefinition queryDefinition = getQueryDefinition(query, queryName);
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

    public static double getPercentageDays(QueryServicesClient queryClient, String queryName, String query) {

        QueryDefinition queryDefinition = getQueryDefinition(queryName, query);
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
        if (result.size() > SINGLE_RESULT_SIZE) {
            LocalDate beforeLastDate = LocalDate
                    .of(((Double) result.get(1).get(2)).intValue(), ((Double) result.get(1).get(1)).intValue(),
                            ((Double) result.get(1).get(0)).intValue());
            Double beforeLastDateValue = (Double) result.get(1).get(3);
            return calculatePercentageDays(lastDate, lastDateValue, beforeLastDate, beforeLastDateValue);
        }
        return calculatePercentageDaysSingleResult(lastDate, lastDateValue);
    }

    public static double getPercentageMonths(QueryServicesClient queryClient, String queryName, String query) {

        QueryDefinition queryDefinition = getQueryDefinition(queryName, query);
        queryClient.replaceQuery(queryDefinition);

        List<List> result = queryClient
                .query(queryName, QueryServicesClient.QUERY_MAP_RAW, 0, -1, List.class);

        if (result.isEmpty()) {
            return 0.0;
        }
        LocalDate lastDate = LocalDate
                .of(((Double) result.get(0).get(1)).intValue(), ((Double) result.get(0).get(0)).intValue(), 1);
        Double lastDateValue = (Double) result.get(0).get(2);
        if (result.size() > SINGLE_RESULT_SIZE) {
            LocalDate beforeLastDate = LocalDate
                    .of(((Double) result.get(1).get(1)).intValue(), ((Double) result.get(1).get(0)).intValue(), 1);
            Double beforeLastDateValue = (Double) result.get(1).get(2);
            return calculatePercentageMonths(lastDate, lastDateValue, beforeLastDate, beforeLastDateValue);
        }
        return calculatePercentageMonthsSingleResult(lastDate, lastDateValue);
    }

    public static double getPercentageYears(QueryServicesClient queryClient, String queryName, String query) {

        QueryDefinition queryDefinition = getQueryDefinition(queryName, query);
        queryClient.replaceQuery(queryDefinition);

        List<List> result = queryClient
                .query(queryName, QueryServicesClient.QUERY_MAP_RAW, 0, -1, List.class);

        if (result.isEmpty()) {
            return 0.0;
        }
        int lastYear = ((Double) result.get(0).get(0)).intValue();
        Double lastYearValue = (Double) result.get(0).get(1);
        if (result.size() > SINGLE_RESULT_SIZE) {
            int beforeLastYear = ((Double) result.get(1).get(0)).intValue();
            Double beforeLastYearValue = (Double) result.get(1).get(1);
            return calculatePercentageYears(lastYear, lastYearValue, beforeLastYear, beforeLastYearValue);
        }
        return calculatePercentageYearsSingleResult(lastYear, lastYearValue);
    }

    private static QueryDefinition getQueryDefinition(String queryName, String query) {
        return QueryDefinition.builder()
                .name(queryName)
                .source(KIE_SERVER_PERSISTENCE_DS)
                .expression(query).target(CUSTOM_TARGET)
                .build();
    }
}
