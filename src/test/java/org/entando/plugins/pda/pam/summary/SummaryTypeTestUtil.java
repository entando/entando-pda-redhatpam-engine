package org.entando.plugins.pda.pam.summary;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import lombok.experimental.UtilityClass;
import org.entando.plugins.pda.core.model.summary.Summary;
import org.kie.server.client.QueryServicesClient;

@UtilityClass
@SuppressWarnings("PMD.TooManyMethods")
public class SummaryTypeTestUtil {

    public static void mockTotalResult(Summary summary, QueryServicesClient queryClient,
            LocalDate first, LocalDate last, double total, String queryPrefix) {
        List<Object> resultTotal = new ArrayList<>();
        long firstDate = Date.from(first.atStartOfDay(ZoneId.systemDefault()).toInstant()).getTime();
        long lastDate = Date.from(last.atStartOfDay(ZoneId.systemDefault()).toInstant()).getTime();
        resultTotal.add(Arrays.asList(firstDate, lastDate, total));
        when(queryClient
                .query(eq(queryPrefix + summary.getId()), anyString(), any(), any(), any()))
                .thenReturn(resultTotal);
    }

    public static void mockEmptyTotalResult(Summary summary, QueryServicesClient queryClient, String queryPrefix) {
        when(queryClient
                .query(eq(queryPrefix + summary.getId()), anyString(), any(), any(), any()))
                .thenReturn(new ArrayList<>());
    }

    public static void mockPercentageResultYears(Summary summary,
            QueryServicesClient queryClient, int last, int before, double lastValue, double beforeValue,
            String queryPrefix) {
        List<Object> resultPercentage = new ArrayList<>();
        resultPercentage.add(Arrays.asList((double) last, lastValue));
        resultPercentage.add(Arrays.asList((double) before, beforeValue));
        when(queryClient
                .query(eq(queryPrefix + summary.getId()), anyString(), any(), any(), any()))
                .thenReturn(resultPercentage);
    }

    public static void mockIncompletePercentageResultYears(Summary summary,
            QueryServicesClient queryClient, int last, double lastValue, String queryPrefix) {
        List<Object> resultPercentage = new ArrayList<>();
        resultPercentage.add(Arrays.asList((double) last, lastValue));
        when(queryClient
                .query(eq(queryPrefix + summary.getId()), anyString(), any(), any(), any()))
                .thenReturn(resultPercentage);
    }

    public static void mockEmptyPercentageResultYears(Summary summary,
            QueryServicesClient queryClient, String queryPrefix) {
        when(queryClient
                .query(eq(queryPrefix + summary.getId()), anyString(), any(), any(), any()))
                .thenReturn(new ArrayList<>());
    }

    public static void mockPercentageResultMonths(Summary summary,
            QueryServicesClient queryClient, LocalDate last, LocalDate before, double lastValue, double beforeValue,
            String queryPrefix) {
        List<Object> resultPercentage = new ArrayList<>();
        resultPercentage.add(Arrays.asList((double) last.getMonthValue(), (double) last.getYear(), lastValue));
        resultPercentage.add(Arrays.asList((double) before.getMonthValue(), (double) before.getYear(), beforeValue));
        when(queryClient
                .query(eq(queryPrefix + summary.getId()), anyString(), any(), any(), any()))
                .thenReturn(resultPercentage);
    }

    public static void mockIncompletePercentageResultMonths(Summary summary,
            QueryServicesClient queryClient, LocalDate last, double lastValue, String queryPrefix) {
        List<Object> resultPercentage = new ArrayList<>();
        resultPercentage.add(Arrays.asList((double) last.getMonthValue(), (double) last.getYear(), lastValue));
        when(queryClient
                .query(eq(queryPrefix + summary.getId()), anyString(), any(), any(), any()))
                .thenReturn(resultPercentage);
    }

    public static void mockEmptyPercentageResultMonths(Summary summary,
            QueryServicesClient queryClient, String queryPrefix) {
        when(queryClient
                .query(eq(queryPrefix + summary.getId()), anyString(), any(), any(), any()))
                .thenReturn(new ArrayList<>());
    }

    public static void mockPercentageResultDays(Summary summary,
            QueryServicesClient queryClient, LocalDate last, LocalDate before, double lastValue, double beforeValue,
            String queryPrefix) {
        List<Object> resultPercentage = new ArrayList<>();
        resultPercentage.add(Arrays
                .asList((double) last.getDayOfMonth(), (double) last.getMonthValue(), (double) last.getYear(),
                        lastValue));
        resultPercentage.add(Arrays
                .asList((double) before.getDayOfMonth(), (double) before.getMonthValue(), (double) before.getYear(),
                        beforeValue));
        when(queryClient
                .query(eq(queryPrefix + summary.getId()), anyString(), any(), any(), any()))
                .thenReturn(resultPercentage);
    }

    public static void mockIncompletePercentageResultDays(Summary summary,
            QueryServicesClient queryClient, LocalDate last, double lastValue, String queryPrefix) {
        List<Object> resultPercentage = new ArrayList<>();
        resultPercentage.add(Arrays
                .asList((double) last.getDayOfMonth(), (double) last.getMonthValue(), (double) last.getYear(),
                        lastValue));
        when(queryClient
                .query(eq(queryPrefix + summary.getId()), anyString(), any(), any(), any()))
                .thenReturn(resultPercentage);
    }

    public static void mockEmptyPercentageResultDays(Summary summary,
            QueryServicesClient queryClient, String queryPrefix) {
        when(queryClient
                .query(eq(queryPrefix + summary.getId()), anyString(), any(), any(), any()))
                .thenReturn(new ArrayList<>());

    }


    public static void mockTimeSeriesFullDays(Summary summary, QueryServicesClient queryClient, String queryPrefix, int entries, double value) {

        List values = new ArrayList<>();
        LocalDate local = LocalDate.now();
        local = local.minusDays(entries);
        for(int i =0;i<entries;i++) {
            values.add(Arrays
                    .asList((double) local.getDayOfMonth(), (double) local.getMonthValue(), (double) local.getYear(),
                            value));

            local = local.plusDays(1);

        }

        when(queryClient
                .query(eq(queryPrefix + summary.getId()), anyString(), any(), any(), any()))
                .thenReturn(values);
    }


    public static void mockTimeSeriesFullMonths(Summary summary, QueryServicesClient queryClient, String queryPrefix, int entries, double value) {

        List values = new ArrayList<>();
        LocalDate local = LocalDate.now();
        local = local.minusMonths(entries);
        for(int i =0;i<entries;i++) {
            values.add(Arrays
                    .asList((double) local.getMonthValue(), (double) local.getYear(),
                            value));

            local = local.plusMonths(1);

        }

        when(queryClient
                .query(eq(queryPrefix + summary.getId()), anyString(), any(), any(), any()))
                .thenReturn(values);
    }

    public static void mockTimeSeriesFullYears(Summary summary, QueryServicesClient queryClient, String queryPrefix, int entries, double value) {

        List values = new ArrayList<>();
        LocalDate local = LocalDate.now();
        local = local.minusYears(entries);
        for(int i =0;i<entries;i++) {
            values.add(Arrays
                    .asList((double) local.getYear(),
                            value));

            local = local.plusYears(1);

        }

        when(queryClient
                .query(eq(queryPrefix + summary.getId()), anyString(), any(), any(), any()))
                .thenReturn(values);
    }

    public static void mockTimeSeriesEmpty(Summary summary, QueryServicesClient queryClient, String queryPrefix) {
        when(queryClient
                .query(eq(queryPrefix + summary.getId()), anyString(), any(), any(), any()))
                .thenReturn(new ArrayList<>());
    }

    public static void mockTimeSeriesSparseDays(Summary summary, QueryServicesClient queryClient, String queryPrefix,
                int entries, double value) {

        Random random = new Random();
        List values = new ArrayList<>();
        LocalDate local = LocalDate.now();
        local = local.minusDays(entries);
        for(int i =0;i<entries;i++) {

            if(random.nextBoolean()) {
                values.add(Arrays
                        .asList((double) local.getDayOfMonth(), (double) local.getMonthValue(),
                                (double) local.getYear(),
                                value));
            }
            local = local.plusDays(1);

        }

        when(queryClient
                .query(eq(queryPrefix + summary.getId()), anyString(), any(), any(), any()))
                .thenReturn(values);
    }


    public static void mockTimeSeriesSparseMonths(Summary summary, QueryServicesClient queryClient, String queryPrefix,
            int entries, double value) {

        Random random = new Random();
        List values = new ArrayList<>();
        LocalDate local = LocalDate.now();
        local = local.minusMonths(entries);
        for(int i =0;i<entries;i++) {

            if(random.nextBoolean()) {
                values.add(Arrays
                        .asList((double) local.getMonthValue(), (double) local.getYear(),
                                value));

            }

            local = local.plusMonths(1);

        }

        when(queryClient
                .query(eq(queryPrefix + summary.getId()), anyString(), any(), any(), any()))
                .thenReturn(values);
    }


    public static void mockTimeSeriesSparseYears(Summary summary, QueryServicesClient queryClient, String queryPrefix,
                                                    int entries, double value) {
        Random random = new Random();
        List values = new ArrayList<>();
        LocalDate local = LocalDate.now();
        local = local.minusYears(entries);
        for(int i =0;i<entries;i++) {

            //Randomly don't include some values in the response to create a partial result set simulating missing
            //days in the query or days with no activity
            if(random.nextBoolean()) {
                values.add(Arrays
                        .asList((double) local.getYear(),
                                value));
            }
            local = local.plusYears(1);

        }

        when(queryClient
                .query(eq(queryPrefix + summary.getId()), anyString(), any(), any(), any()))
                .thenReturn(values);

    }
}
