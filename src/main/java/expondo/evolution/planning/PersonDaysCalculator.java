package expondo.evolution.planning;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Utility for calculating person days from timebox effort data.
 * Shared across packages (reporting, okr) to avoid duplication
 * while keeping the dependency direction clean:
 * reporting -> planning <- okr
 */
public final class PersonDaysCalculator {

    private PersonDaysCalculator() {}

    /**
     * Calculate person days from a percentage of team effort within a timebox.
     *
     * @param timeboxStart    start date of the timebox
     * @param timeboxEnd      end date of the timebox
     * @param teamMemberCount number of engineers in the team
     * @param effortPercentage percentage of effort (0-100)
     * @return person days, rounded to 2 decimal places
     */
    public static BigDecimal calculate(LocalDate timeboxStart, LocalDate timeboxEnd,
                                       int teamMemberCount, BigDecimal effortPercentage) {
        int workingDays = countWorkingDays(timeboxStart, timeboxEnd);
        BigDecimal availableDays = BigDecimal.valueOf((long) workingDays * teamMemberCount);
        return availableDays
                .multiply(effortPercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * Count working days (Mon-Fri) between two dates, inclusive.
     */
    public static int countWorkingDays(LocalDate start, LocalDate end) {
        int count = 0;
        LocalDate current = start;
        while (!current.isAfter(end)) {
            DayOfWeek day = current.getDayOfWeek();
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                count++;
            }
            current = current.plusDays(1);
        }
        return count;
    }
}