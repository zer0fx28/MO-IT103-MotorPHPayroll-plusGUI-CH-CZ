// File: motorph/process/PayrollDateManager.java
package motorph.process;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * Manages payroll dates and cutoff periods
 */
public class PayrollDateManager {
    // Date formatter for display
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy");

    // Payroll types
    public static final int MID_MONTH = 1;
    public static final int END_MONTH = 2;

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
            LocalDate midMonth = LocalDate.of(year, month, 15);

            // If 15th falls on Sunday, move to Saturday (14th)
            if (midMonth.getDayOfWeek() == DayOfWeek.SUNDAY) {
                return midMonth.minusDays(1);
            }

            return midMonth;
        } else {
            // End-month is the last day of the month
            LocalDate lastDayOfMonth = YearMonth.of(year, month).atEndOfMonth();

            // If last day falls on Sunday, move to Saturday (day before)
            if (lastDayOfMonth.getDayOfWeek() == DayOfWeek.SUNDAY) {
                return lastDayOfMonth.minusDays(1);
            }

            return lastDayOfMonth;
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
            LocalDate firstDayOfMonth = LocalDate.of(
                    payrollDate.getYear(),
                    payrollDate.getMonth(),
                    1);

            // Start date is the 27th of previous month
            startDate = firstDayOfMonth.minusDays(4); // Go back to previous month
            startDate = LocalDate.of(startDate.getYear(), startDate.getMonth(), 27); // Set to 27th

            // End date is the 12th of current month
            endDate = LocalDate.of(payrollDate.getYear(), payrollDate.getMonth(), 12);
        } else {
            // End-month cutoff: 13th to 26th of current month
            startDate = LocalDate.of(payrollDate.getYear(), payrollDate.getMonth(), 13);
            endDate = LocalDate.of(payrollDate.getYear(), payrollDate.getMonth(), 26);
        }

        return new LocalDate[] {startDate, endDate};
    }

    /**
     * Get the formatted date range string for display
     *
     * @param startDate Start date
     * @param endDate End date
     * @return Formatted date range string
     */
    public static String getFormattedDateRange(LocalDate startDate, LocalDate endDate) {
        return startDate.format(DATE_FORMATTER) + " to " + endDate.format(DATE_FORMATTER);
    }

    /**
     * Calculate the number of working days in a year
     * (Excluding weekends and holidays)
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
     */
    public static String formatDate(LocalDate date) {
        return date.format(DATE_FORMATTER);
    }
}