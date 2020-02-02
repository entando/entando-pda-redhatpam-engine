package org.entando.plugins.pda.pam.summary;

import java.text.DecimalFormat;
import lombok.RequiredArgsConstructor;
import org.entando.plugins.pda.core.engine.Connection;
import org.entando.plugins.pda.core.model.summary.FrequencyEnum;
import org.entando.plugins.pda.core.model.summary.Summary;
import org.entando.plugins.pda.core.model.summary.SummaryType;
import org.entando.plugins.pda.core.model.summary.SummaryValue;
import org.entando.plugins.pda.core.model.summary.ValuePercentageSummaryValue;
import org.entando.plugins.pda.pam.engine.KieEngine;
import org.entando.plugins.pda.pam.service.api.KieApiService;
import org.kie.server.client.QueryServicesClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RequestsSummaryType implements Summary {

    public static final String PDA_TOTAL_PREFIX = "pda-total-";
    public static final String PDA_PERC_DAYS_PREFIX = "pda-perc-days-";
    public static final String PDA_PERC_MONTHS_PREFIX = "pda-perc-months-";
    public static final String PDA_PERC_YEARS_PREFIX = "pda-perc-years-";
    public static final String REQUESTS_TITLE = "REQUESTS.TITLE";
    public static final String REQUESTS_TOTAL_LABEL = "REQUESTS.TOTAL_LABEL";



    private final KieApiService kieApiService;
    private final RequestsProperties requestsProperties;

    private DecimalFormat decimalFormat = new DecimalFormat("#.##");

    @Override
    public SummaryValue calculateSummary(Connection connection, FrequencyEnum frequency) {
        QueryServicesClient queryClient = kieApiService.getQueryServicesClient(connection);
        double total = ValuePercentageCalculator.getTotal(queryClient,
                frequency,
                PDA_TOTAL_PREFIX + getId(),
                requestsProperties.getQueryTotal());
        double percentage = 0.0;
        if (frequency.equals(FrequencyEnum.DAILY)) {
            percentage = ValuePercentageCalculator.getPercentageDays(queryClient,
                                                            PDA_PERC_DAYS_PREFIX + getId(),
                                                            requestsProperties.getQueryPercentageDays());
        } else if (frequency.equals(FrequencyEnum.MONTHLY)) {
            percentage = ValuePercentageCalculator.getPercentageMonths(queryClient,
                                                    PDA_PERC_MONTHS_PREFIX + getId(),
                                                    requestsProperties.getQueryPercentageMonths());
        } else if (frequency.equals(FrequencyEnum.ANNUALLY)) {
            percentage = ValuePercentageCalculator.getPercentageYears(queryClient,
                                                            PDA_PERC_YEARS_PREFIX + getId(),
                                                                requestsProperties.getQueryPercentageYears());
        }
        return ValuePercentageSummaryValue.builder()
                .title(getDescription())
                .totalLabel(REQUESTS_TOTAL_LABEL)
                .total(decimalFormat.format(total))
                .percentage(decimalFormat.format(percentage))
                .build();
    }

    @Override
    public SummaryType getSummaryType() {
        return SummaryType.VALUE_PERCENTAGE;
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


}
