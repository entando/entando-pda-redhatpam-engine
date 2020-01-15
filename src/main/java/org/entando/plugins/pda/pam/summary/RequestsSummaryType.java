package org.entando.plugins.pda.pam.summary;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MONTHS;
import static java.time.temporal.ChronoUnit.YEARS;

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

    private final KieApiService kieApiService;

    @Override
    public Summary calculateSummary(Connection connection, FrequencyEnum frequency) {
        String totalQuery = "SELECT min(startdate) as first_date, max(startdate) as end_date, count(*) as total\n"
                + "FROM processinstanceinfo\n";
        String total = String.valueOf(getTotal(connection, frequency, totalQuery));
        double percentage = 0.0;
        if (frequency.equals(FrequencyEnum.DAILY)) {
            percentage = getPercentageDays(connection);
        }
        return Summary.builder()
                .title(getDescription())
                .totalLabel("Total requests")
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
        return "Requests";
    }

    private double getTotal(Connection connection, FrequencyEnum frequency, String query) {
        QueryServicesClient queryClient = kieApiService.getQueryServicesClient(connection);
        String queryName = "pda-total-" + getId();
        QueryDefinition queryDefinition = QueryDefinition.builder()
                .name(queryName)
                .source("${org.kie.server.persistence.ds}")
                .expression(query).target("CUSTOM")
                .build();
        queryClient.replaceQuery(queryDefinition);

        List<List> result = queryClient
                .query(queryName, QueryServicesClient.QUERY_MAP_RAW, 0, -1, List.class);
        if (!result.isEmpty()) {
            LocalDate firstDate = new Date((Long) result.get(0).get(0)).toInstant().atZone(ZoneId.systemDefault())
                    .toLocalDate();
            LocalDate lastDate = new Date((Long) result.get(0).get(1)).toInstant().atZone(ZoneId.systemDefault())
                    .toLocalDate();
            Double totalRecords = (Double) result.get(0).get(2);
            if (frequency.equals(FrequencyEnum.DAILY)) {
                long days = DAYS.between(firstDate, lastDate);
                return days > 0 ? totalRecords / days : totalRecords;
            } else if (frequency.equals(FrequencyEnum.MONTHLY)) {
                long months = MONTHS.between(firstDate, lastDate);
                return months > 0 ? totalRecords / months : totalRecords;
            } else if (frequency.equals(FrequencyEnum.ANNUALLY)) {
                long years = YEARS.between(firstDate, lastDate);
                return years > 0 ? totalRecords / years : totalRecords;
            }
        }
        return 0.0;
    }

    private double getPercentageDays(Connection connection) {
        QueryServicesClient queryClient = kieApiService.getQueryServicesClient(connection);
        String queryName = "pda-perc-days-" + getId();
        QueryDefinition queryDefinition = QueryDefinition.builder()
                .name(queryName)
                .source("${org.kie.server.persistence.ds}")
                .expression(getQueryPercentageDays()).target("CUSTOM")
                .build();
        queryClient.replaceQuery(queryDefinition);

        List<List> result = queryClient
                .query(queryName, QueryServicesClient.QUERY_MAP_RAW, 0, -1, List.class);

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

    private double calculatePercentageDays(LocalDate lastDate, Double lastDateValue, LocalDate beforeLastDate,
            Double beforeLastDateValue) {
        LocalDate today = LocalDate.now();
        if (today.compareTo(lastDate) > 0) {
            if (today.minusDays(1).compareTo(lastDate) == 0 && lastDateValue > 0) {
                return -100;
            }
        } else if (today.compareTo(lastDate) == 0 && today.minusDays(1).compareTo(beforeLastDate) == 0
                && beforeLastDateValue > 0) {
            return (lastDateValue - beforeLastDateValue) / Math.min(beforeLastDateValue, lastDateValue) * 100;
        }
        return 0;
    }

    private String getQueryPercentageDays() {
        return "SELECT day(startdate) as day, month(startdate) as month, year(startdate) as year, count(*) as total\n"
                + "FROM processinstanceinfo\n"
                + "GROUP BY day, month, year\n"
                + "ORDER BY year DESC, month DESC, day DESC\n"
                + "LIMIT 2\n";
    }
}
