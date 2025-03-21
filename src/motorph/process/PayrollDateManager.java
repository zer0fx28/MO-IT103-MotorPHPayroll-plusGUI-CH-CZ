// File: motorph/process/PayrollDateManager.java
package motorph.process;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * Manages payroll dates and cutoff periods
 * Handles all date-related calculations for payroll processing
 */
public class PayrollDateManager {
    // Date formatter for display
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy");

    // Payroll types
    public static final int MID_MONTH = 1;
    public static final int END_MONTH = 2;

    // Cutoff days
    private static final int MID_MONTH_PAY_DAY = 15;
    private static final int MID_MONTH_CUTOFF_START_DAY = 27;  // of previous month
    private static final int MID_MONTH_CUTOFF_END_DAY = 12;
    private static final int END_MONTH_CUTOFF_START_DAY = 13;
    private static final int END_MONTH_CUTOFF_END_DAY = 26;

    /**
     * Get the payroll date for a given month, year, and type
     *
     * @param year The year
     * @param month The month (1-12)
     * @param payrollType Either MID_MONTH or END_MONTH
     * @return The payroll date
     */
    public static LocalDate getPayrollDate(int year, int month, int payrollType) {
        if (payrollType == MID_MONTH) {
            // Mid-month is the 15th
            LocalDate midMonth = LocalDate.of(year, month, MID_MONTH_PAY_DAY);

            // If 15th falls on weekend, move to previous Friday
            return adjustForWeekend(midMonth);
        } else {
            // End-month is the last day of the month
            LocalDate lastDayOfMonth = YearMonth.of(year, month).atEndOfMonth();

            // If last day falls on weekend, move to previous Friday
            return adjustForWeekend(lastDayOfMonth);
        }
    }

    /**
     * Get the cutoff date range for a payroll date
     *
     * @param payrollDate The payroll date
     * @param payrollType Either MID_MONTH or END_MONTH
     * @return Array with start and end dates of the cutoff period
     */
    public static LocalDate[] getCutoffDateRange(LocalDate payrollDate, int payrollType) {
        LocalDate startDate, endDate;

        if (payrollType == MID_MONTH) {
            // Mid-month cutoff: 27th of previous month to 12th of current month
            int year = payrollDate.getYear();
            int month = payrollDate.getMonthValue();

            // End date is the 12th of current month
            endDate = LocalDate.of(year, month, MID_MONTH_CUTOFF_END_DAY);

            // Start date is the 27th of previous month
            LocalDate prevMonth = payrollDate.minusMonths(1);
            startDate = LocalDate.of(prevMonth.getYear(), prevMonth.getMonth(), MID_MONTH_CUTOFF_START_DAY);
        } else {
            // End-month cutoff: 13th to 26th of current month
            int year = payrollDate.getYear();
            int month = payrollDate.getMonthValue();

            startDate = LocalDate.of(year, month, END_MONTH_CUTOFF_START_DAY);
            endDate = LocalDate.of(year, month, END_MONTH_CUTOFF_END_DAY);
        }

        return new LocalDate[] {startDate, endDate};
    }

    /**
     * Adjust a date if it falls on a weekend
     * Moves to the previous Friday if it's a Saturday or Sunday
     *
     * @param date The date to adjust
     * @return Adjusted date (same date if it's not a weekend)
     */
    private static LocalDate adjustForWeekend(LocalDate date) {
        if (date.getDayOfWeek() == DayOfWeek.SATURDAY) {
            return date.minusDays(1);  // Friday
        } else if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return date.minusDays(2);  // Friday
        }
        return date;
    }

    /**
     * Calculate the number of working days in a date range
     * Excludes weekends but not holidays (simplified version)
     *
     * @param startDate Start date
     * @param endDate End date
     * @return Number of working days
     */
    public static int getWorkingDaysInPeriod(LocalDate startDate, LocalDate endDate) {
        int workingDays = 0;
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            // Skip weekends
            if (currentDate.getDayOfWeek() != DayOfWeek.SATURDAY &&
                    currentDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
                workingDays++;
            }
            currentDate = currentDate.plusDays(1);
        }

        return workingDays;
    }

    /**
     * Calculate the number of working days in a year
     * (Excluding weekends and estimated holidays)
     *
     * @param year The year
     * @return Number of working days
     */
    public static int getWorkingDaysInYear(int year) {
        // For simplicity, we'll use a standard approximation
        // In reality, you would iterate through each day of the year
        // and check if it's a weekend or holiday

        // Approximate: 52 weeks × 5 workdays = 260 days
        // Minus approximately 15 holidays
        return 245;
    }

    /**
     * Get name of the month
     *
     * @param month Month number (1-12)
     * @return Month name
     */
    public static String getMonthName(int month) {
        return Month.of(month).toString();
    }

    /**
     * Format a date for display
     *
     * @param date Date to format
     * @return Formatted date string
     */
    public static String formatDate(LocalDate date) {
        return date.format(DATE_FORMATTER);
    }

    /**
     * Get the formatted date range string for display
     *
     * @param startDate Start date
     * @param endDate End date
     * @return Formatted date range string
     */
    public static String getFormattedDateRange(LocalDate startDate, LocalDate endDate) {
        return formatDate(startDate) + " to " + formatDate(endDate);
    }
}