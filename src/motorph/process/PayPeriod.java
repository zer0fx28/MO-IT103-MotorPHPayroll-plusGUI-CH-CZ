// File: motorph/process/PayPeriod.java
package motorph.process;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles pay period calculations and date ranges
 *
 * This class represents a specific pay period with start and end dates,
 * and provides methods to check dates within the period and generate
 * weekly date ranges for reporting.
 */
public class PayPeriod {
    // Date formatter for consistent parsing
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    // Pay period types
    public static final int FIRST_HALF = 1;  // 1st-15th payday
    public static final int SECOND_HALF = 2; // 16th-30th/31st payday

    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate payDate;
    private int periodType;

    /**
     * Constructor for creating a pay period with specified date range
     *
     * @param startDate Start date of the pay period
     * @param endDate End date of the pay period
     * @param payDate Date when payment is made
     * @param periodType Type of pay period (FIRST_HALF or SECOND_HALF)
     */
    public PayPeriod(LocalDate startDate, LocalDate endDate, LocalDate payDate, int periodType) {
        // Validate inputs
        if (startDate == null || endDate == null || payDate == null) {
            throw new IllegalArgumentException("Pay period dates cannot be null");
        }

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        if (periodType != FIRST_HALF && periodType != SECOND_HALF) {
            throw new IllegalArgumentException("Invalid period type. Must be FIRST_HALF or SECOND_HALF");
        }

        this.startDate = startDate;
        this.endDate = endDate;
        this.payDate = payDate;
        this.periodType = periodType;
    }

    /**
     * Generate a pay period for a given pay date
     *
     * @param payDate The pay date to calculate the period for
     * @return PayPeriod object with calculated date ranges
     */
    public static PayPeriod generateForPayDate(LocalDate payDate) {
        if (payDate == null) {
            throw new IllegalArgumentException("Pay date cannot be null");
        }

        int day = payDate.getDayOfMonth();
        LocalDate startDate, endDate;
        int periodType;

        if (day == 15) {
            // For the 15th payday: Oct 28 - Nov 12 example
            periodType = FIRST_HALF;

            // End date is 3 days before payday
            endDate = payDate.minusDays(3);

            // Start date is typically 16 days before end date
            startDate = endDate.minusDays(15);
        } else {
            // For the end of month payday: Nov 13 - Nov 27 example
            periodType = SECOND_HALF;

            // End date is 3 days before payday
            endDate = payDate.minusDays(3);

            // Start date is the day after the previous cutoff
            startDate = payDate.withDayOfMonth(13);
        }

        return new PayPeriod(startDate, endDate, payDate, periodType);
    }

    /**
     * Get all weekly date ranges within this pay period
     *
     * @return List of date range pairs [startDate, endDate]
     */
    public List<LocalDate[]> getWeeklyDateRanges() {
        List<LocalDate[]> weekRanges = new ArrayList<>();
        LocalDate current = startDate;

        while (!current.isAfter(endDate)) {
            // Start of week is the current date
            LocalDate weekStart = current;

            // End of week is 6 days after start (7-day week)
            LocalDate weekEnd = current.plusDays(6);

            // Adjust if week end is after pay period end
            if (weekEnd.isAfter(endDate)) {
                weekEnd = endDate;
            }

            // Add the week range
            weekRanges.add(new LocalDate[]{weekStart, weekEnd});

            // Move to next week
            current = weekEnd.plusDays(1);

            // Break if we've passed the end date
            if (current.isAfter(endDate)) {
                break;
            }
        }

        return weekRanges;
    }

    /**
     * Check if a date is within this pay period
     *
     * @param date Date to check
     * @return true if date is within pay period
     */
    public boolean isDateInPeriod(LocalDate date) {
        if (date == null) {
            return false;
        }

        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * Check if a date string is within this pay period
     *
     * @param dateStr Date string in MM/dd/yyyy format
     * @return true if date is within pay period
     */
    public boolean isDateInPeriod(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return false;
        }

        try {
            LocalDate date = LocalDate.parse(dateStr, DATE_FORMAT);
            return isDateInPeriod(date);
        } catch (DateTimeParseException e) {
            System.out.println("Error parsing date: " + dateStr + " - " + e.getMessage());
            return false;
        }
    }

    // Getters
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public LocalDate getPayDate() { return payDate; }
    public int getPeriodType() { return periodType; }

    /**
     * Format date to string using standard format
     *
     * @param date Date to format
     * @return Formatted date string
     */
    public static String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }

        return date.format(DATE_FORMAT);
    }

    /**
     * String representation of the pay period
     *
     * @return Formatted string with pay period details
     */
    @Override
    public String toString() {
        return "Pay Period: " + formatDate(startDate) + " to " + formatDate(endDate) +
                " (Pay Date: " + formatDate(payDate) + ")";
    }
}