package org.entando.plugins.pda.pam.summary;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MONTHS;
import static java.time.temporal.ChronoUnit.YEARS;

import java.time.LocalDate;
import lombok.experimental.UtilityClass;
import org.entando.plugins.pda.core.model.summary.FrequencyEnum;

@UtilityClass
public class KieSummaryUtils {

    private static final int ONE_HUNDRED_INCREASE = 100;
    private static final int ONE_HUNDRED_DECREASE = -100;

    public static double calculatePercentageDays(LocalDate lastDate, Double lastDateValue, LocalDate beforeLastDate,
            Double beforeLastDateValue) {
        LocalDate today = LocalDate.now();
        if (today.isEqual(lastDate) && today.minusDays(1).isEqual(beforeLastDate)) {
            return calculateDirectPercent(lastDateValue, beforeLastDateValue);
        }
        if (today.isEqual(lastDate) && today.minusDays(1).isAfter(beforeLastDate) && lastDateValue > 0) {
            return ONE_HUNDRED_INCREASE;
        }
        if (today.isAfter(lastDate) && today.minusDays(1).isEqual(lastDate) && lastDateValue > 0) {
            return ONE_HUNDRED_DECREASE;
        }
        return 0;
    }

    public static double calculatePercentageMonths(LocalDate lastDate, Double lastDateValue, LocalDate beforeLastDate,
            Double beforeLastDateValue) {
        LocalDate thisMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate lastMonth = lastDate.withDayOfMonth(1);
        LocalDate beforeLastMonth = beforeLastDate.withDayOfMonth(1);
        if (thisMonth.isEqual(lastMonth) && thisMonth.minusMonths(1).isEqual(beforeLastMonth)) {
            return calculateDirectPercent(lastDateValue, beforeLastDateValue);
        }
        if (thisMonth.isEqual(lastMonth) && thisMonth.minusMonths(1).isAfter(beforeLastMonth) && lastDateValue > 0) {
            return ONE_HUNDRED_INCREASE;
        }
        if (thisMonth.isAfter(lastMonth) && thisMonth.minusMonths(1).isEqual(lastMonth) && lastDateValue > 0) {
            return ONE_HUNDRED_DECREASE;
        }
        return 0;
    }

    public static double calculatePercentageYears(int lastYear, Double lastYearValue, int beforeLastYear,
            Double beforeLastYearValue) {
        int thisYear = LocalDate.now().getYear();
        if (thisYear == lastYear && thisYear - 1 == beforeLastYear) {
            return calculateDirectPercent(lastYearValue, beforeLastYearValue);
        }
        if (thisYear == lastYear && thisYear - 1 > beforeLastYear && lastYearValue > 0) {
            return ONE_HUNDRED_INCREASE;
        }
        if (thisYear > lastYear && thisYear - 1 == lastYear && lastYearValue > 0) {
            return ONE_HUNDRED_DECREASE;
        }
        return 0;
    }

    public static double calculateTotal(FrequencyEnum frequency, LocalDate firstDate, LocalDate lastDate,
            Double totalRecords) {
        if (frequency.equals(FrequencyEnum.DAILY)) {
            long days = DAYS.between(firstDate, lastDate);
            return days > 0 ? totalRecords / days : totalRecords;
        }
        if (frequency.equals(FrequencyEnum.MONTHLY)) {
            long months = MONTHS.between(firstDate, lastDate);
            return months > 0 ? totalRecords / months : totalRecords;
        }
        if (frequency.equals(FrequencyEnum.ANNUALLY)) {
            long years = YEARS.between(firstDate, lastDate);
            return years > 0 ? totalRecords / years : totalRecords;
        }
        return 0.0;
    }

    private static double calculateDirectPercent(Double lastDateValue, Double beforeLastDateValue) {
        if (lastDateValue > 0 && beforeLastDateValue == 0) {
            return ONE_HUNDRED_INCREASE;
        }
        if (lastDateValue == 0 && beforeLastDateValue > 0) {
            return ONE_HUNDRED_DECREASE;
        }
        if (lastDateValue == 0 && beforeLastDateValue == 0) {
            return 0;
        }
        return (lastDateValue - beforeLastDateValue) / Math.min(beforeLastDateValue, lastDateValue) * 100;
    }
}
