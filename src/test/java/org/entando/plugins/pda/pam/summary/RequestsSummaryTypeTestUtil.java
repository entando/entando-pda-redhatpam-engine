package org.entando.plugins.pda.pam.summary;

import static org.entando.plugins.pda.pam.summary.RequestsSummaryType.PDA_PERC_DAYS_PREFIX;
import static org.entando.plugins.pda.pam.summary.RequestsSummaryType.PDA_PERC_MONTHS_PREFIX;
import static org.entando.plugins.pda.pam.summary.RequestsSummaryType.PDA_PERC_YEARS_PREFIX;
import static org.entando.plugins.pda.pam.summary.RequestsSummaryType.PDA_TOTAL_PREFIX;
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
import lombok.experimental.UtilityClass;
import org.kie.server.client.QueryServicesClient;

@UtilityClass
public class RequestsSummaryTypeTestUtil {

    public static void mockTotalResult(RequestsSummaryType requestsSummaryType, QueryServicesClient queryClient,
            LocalDate first, LocalDate last, double total) {
        List<Object> resultTotal = new ArrayList<>();
        long firstDate = Date.from(first.atStartOfDay(ZoneId.systemDefault()).toInstant()).getTime();
        long lastDate = Date.from(last.atStartOfDay(ZoneId.systemDefault()).toInstant()).getTime();
        resultTotal.add(Arrays.asList(firstDate, lastDate, total));
        when(queryClient
                .query(eq(PDA_TOTAL_PREFIX + requestsSummaryType.getId()), anyString(), any(), any(), any()))
                .thenReturn(resultTotal);
    }

    public static void mockEmptyTotalResult(RequestsSummaryType requestsSummaryType, QueryServicesClient queryClient) {
        when(queryClient
                .query(eq(PDA_TOTAL_PREFIX + requestsSummaryType.getId()), anyString(), any(), any(), any()))
                .thenReturn(new ArrayList<>());
    }

    public static void mockPercentageResultYears(RequestsSummaryType requestsSummaryType,
            QueryServicesClient queryClient, int last, int before, double lastValue, double beforeValue) {
        List<Object> resultPercentage = new ArrayList<>();
        resultPercentage.add(Arrays.asList((double) last, lastValue));
        resultPercentage.add(Arrays.asList((double) before, beforeValue));
        when(queryClient
                .query(eq(PDA_PERC_YEARS_PREFIX + requestsSummaryType.getId()), anyString(), any(), any(), any()))
                .thenReturn(resultPercentage);
    }

    public static void mockEmptyPercentageResultYears(RequestsSummaryType requestsSummaryType,
            QueryServicesClient queryClient) {
        when(queryClient
                .query(eq(PDA_PERC_YEARS_PREFIX + requestsSummaryType.getId()), anyString(), any(), any(), any()))
                .thenReturn(new ArrayList<>());
    }

    public static void mockPercentageResultMonths(RequestsSummaryType requestsSummaryType,
            QueryServicesClient queryClient, LocalDate last, LocalDate before, double lastValue, double beforeValue) {
        List<Object> resultPercentage = new ArrayList<>();
        resultPercentage.add(Arrays.asList((double) last.getMonthValue(), (double) last.getYear(), lastValue));
        resultPercentage.add(Arrays.asList((double) before.getMonthValue(), (double) before.getYear(), beforeValue));
        when(queryClient
                .query(eq(PDA_PERC_MONTHS_PREFIX + requestsSummaryType.getId()), anyString(), any(), any(), any()))
                .thenReturn(resultPercentage);
    }

    public static void mockEmptyPercentageResultMonths(RequestsSummaryType requestsSummaryType,
            QueryServicesClient queryClient) {
        when(queryClient
                .query(eq(PDA_PERC_MONTHS_PREFIX + requestsSummaryType.getId()), anyString(), any(), any(), any()))
                .thenReturn(new ArrayList<>());
    }

    public static void mockPercentageResultDays(RequestsSummaryType requestsSummaryType,
            QueryServicesClient queryClient, LocalDate last, LocalDate before, double lastValue, double beforeValue) {
        List<Object> resultPercentage = new ArrayList<>();
        resultPercentage.add(Arrays
                .asList((double) last.getDayOfMonth(), (double) last.getMonthValue(), (double) last.getYear(),
                        lastValue));
        resultPercentage.add(Arrays
                .asList((double) before.getDayOfMonth(), (double) before.getMonthValue(), (double) before.getYear(),
                        beforeValue));
        when(queryClient
                .query(eq(PDA_PERC_DAYS_PREFIX + requestsSummaryType.getId()), anyString(), any(), any(), any()))
                .thenReturn(resultPercentage);
    }

    public static void mockEmptyPercentageResultDays(RequestsSummaryType requestsSummaryType,
            QueryServicesClient queryClient) {
        when(queryClient
                .query(eq(PDA_PERC_DAYS_PREFIX + requestsSummaryType.getId()), anyString(), any(), any(), any()))
                .thenReturn(new ArrayList<>());
    }
}
