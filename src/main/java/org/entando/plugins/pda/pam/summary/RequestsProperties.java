package org.entando.plugins.pda.pam.summary;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:requests.properties")
@Getter
public class RequestsProperties {

    @Value("${requests.query.total}")
    private String queryTotal;

    @Value("${requests.query.percentage.days}")
    private String queryPercentageDays;

    @Value("${requests.query.percentage.months}")
    private String queryPercentageMonths;

    @Value("${requests.query.percentage.years}")
    private String queryPercentageYears;
}
