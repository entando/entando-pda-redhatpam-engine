package org.entando.plugins.pda.pam.summary;

import static org.entando.plugins.pda.core.model.summary.SummaryFrequency.ANNUALLY;
import static org.entando.plugins.pda.core.model.summary.SummaryFrequency.DAILY;
import static org.entando.plugins.pda.core.model.summary.SummaryFrequency.MONTHLY;

import java.io.IOException;
import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.entando.plugins.pda.core.engine.Connection;
import org.entando.plugins.pda.core.exception.SummaryFrequencyInvalidException;
import org.entando.plugins.pda.core.model.summary.PeriodicData;
import org.entando.plugins.pda.core.model.summary.SummaryFrequency;
import org.entando.plugins.pda.core.service.summary.DataRepository;
import org.entando.plugins.pda.pam.engine.KieEngine;
import org.entando.plugins.pda.pam.service.api.KieApiService;
import org.kie.server.api.model.definition.QueryDefinition;
import org.kie.server.client.QueryServicesClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

@Slf4j
@Getter
public class KieDataRepository implements DataRepository {
    private KieApiService kieApiService;

    private String id;

    private String daysQuery;
    private String monthsQuery;
    private String yearsQuery;

    public static final String PDA_DAYS_PREFIX = "pda-days-";
    public static final String PDA_MONTHS_PREFIX = "pda-months-";
    public static final String PDA_YEARS_PREFIX = "pda-years-";

    public static final String DAYS_QUERY_PROPERTY_KEY = "org.entando.pda.summary.query.days";
    public static final String MONTHS_QUERY_PROPERTY_KEY = "org.entando.pda.summary.query.months";
    public static final String YEARS_QUERY_PROPERTY_KEY = "org.entando.pda.summary.query.years";

    private static final String KIE_SERVER_PERSISTENCE_DS = "${org.kie.server.persistence.ds}";
    private static final String CUSTOM_TARGET = "CUSTOM";

    public KieDataRepository(KieApiService kieApiService, String name) {
        try {
            this.kieApiService = kieApiService;
            this.id = name.toLowerCase();

            Resource resource = new ClassPathResource(String.format("summary/datarepositories/%s.properties",
                    id));
            Properties props = PropertiesLoaderUtils.loadProperties(resource);

            this.daysQuery = props.getProperty(DAYS_QUERY_PROPERTY_KEY);
            this.monthsQuery = props.getProperty(MONTHS_QUERY_PROPERTY_KEY);
            this.yearsQuery = props.getProperty(YEARS_QUERY_PROPERTY_KEY);
        } catch (IOException e) {
            log.error("Error loading DataType config file: {}", id, e);
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public String getEngine() {
        return KieEngine.TYPE;
    }

    @Override
    public List<PeriodicData> getPeriodicData(Connection connection, SummaryFrequency frequency,
            Integer periods) {

        QueryServicesClient queryClient = kieApiService.getQueryServicesClient(connection);
        String queryName = createQuery(queryClient, frequency);

        List<List> data = fetchSeriesData(queryClient, queryName, periods);
        return buildSeries(data, frequency, periods);
    }

    private String createQuery(QueryServicesClient queryClient, SummaryFrequency frequency) {
        String queryName;
        String query;

        switch (frequency) {
            case DAILY:
                queryName = PDA_DAYS_PREFIX + getId();
                query = daysQuery;
                break;
            case MONTHLY:
                queryName = PDA_MONTHS_PREFIX + getId();
                query = monthsQuery;
                break;
            case ANNUALLY:
                queryName = PDA_YEARS_PREFIX + getId();
                query = this.yearsQuery;
                break;
            default:
                throw new SummaryFrequencyInvalidException();
        }

        QueryDefinition queryDef = QueryDefinition.builder()
                .name(queryName)
                .source(KIE_SERVER_PERSISTENCE_DS)
                .expression(query).target(CUSTOM_TARGET)
                .build();

        queryClient.replaceQuery(queryDef);

        return queryName;
    }

    private List<PeriodicData> buildSeries(List<List> data, SummaryFrequency frequency, Integer periods) {
        List<LocalDate> dateSeries = mapDateData(frequency, periods);
        Map<LocalDate, List> dataMapped = mapRowData(data, frequency);

        return mapSeries(dateSeries, dataMapped);
    }

    private List<PeriodicData> mapSeries(List<LocalDate> dateSeries, Map<LocalDate, List> seriesData) {
        return dateSeries.stream()
                .map(date -> {
                    Double value;
                    if (seriesData.containsKey(date)) {
                        List row = seriesData.get(date);
                        value = (Double) row.get(row.size() - 1);
                    } else {
                        value = 0.0;
                    }

                    return PeriodicData.builder()
                            .date(date)
                            .value(value)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private Map<LocalDate, List> mapRowData(List<List> rowData, SummaryFrequency frequency) {
        return rowData.stream()
                .map(row -> new AbstractMap.SimpleEntry<>(parseDateKeyFromRowData(row, frequency), row))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    //This method processes the query for the total number of row entries for the given frequency
    //and generates an ordered list of date keys that can be used to index the query response from PAM.
    //These date keys can be used to populate zeros for missing dates in the result set
    private List<LocalDate> mapDateData(SummaryFrequency frequencyEnum, Integer periods) {

        List<LocalDate> dateKeys = new ArrayList<>();
        LocalDate currDate = LocalDate.now();

        if (frequencyEnum.equals(MONTHLY)) {
            currDate = currDate.withDayOfMonth(1);
        } else if (frequencyEnum.equals(ANNUALLY)) {
            currDate = currDate.withMonth(1).withDayOfMonth(1);
        }

        for (int i = 0; i < periods; i++) {
            dateKeys.add(currDate);

            if (frequencyEnum.equals(DAILY)) {
                currDate = currDate.minusDays(1);
            } else if (frequencyEnum.equals(MONTHLY)) {
                currDate = currDate.minusMonths(1);
            } else if (frequencyEnum.equals(ANNUALLY)) {
                currDate = currDate.minusYears(1);
            }
        }

        return dateKeys;
    }

    private LocalDate parseDateKeyFromRowData(List data, SummaryFrequency frequency) {
        int day;
        int month;
        int year;

        switch (frequency) {
            case DAILY:
                day = ((Double) data.get(0)).intValue();
                month = ((Double) data.get(1)).intValue();
                year = ((Double) data.get(2)).intValue();
                break;
            case MONTHLY:
                day = 1;
                month = ((Double) data.get(0)).intValue();
                year = ((Double) data.get(1)).intValue();
                break;
            case ANNUALLY:
                day = 1;
                month = 1;
                year = ((Double) data.get(0)).intValue();
                break;
            default:
                throw new SummaryFrequencyInvalidException();
        }

        return LocalDate.of(year, month, day);
    }

    private List<List> fetchSeriesData(QueryServicesClient queryClient, String queryName, int periods) {
        return queryClient
                .query(queryName, QueryServicesClient.QUERY_MAP_RAW, 0, periods, List.class);
    }
}
