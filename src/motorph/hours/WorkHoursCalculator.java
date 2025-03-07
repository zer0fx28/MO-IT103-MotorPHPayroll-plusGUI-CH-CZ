// File: motorph/hours/WorkHoursCalculator.java
package motorph.hours;

import motorph.util.TimeConverter;
import java.time.LocalTime;
import java.time.Duration;

/**
 * Calculator for work hours, lateness, and overtime
 */
public class WorkHoursCalculator {
    // Standard work schedule (using the constants from AttendanceRecord)
    private static final LocalTime STANDARD_START_TIME = AttendanceRecord.STANDARD_START_TIME; // 8:00 AM
    private static final LocalTime STANDARD_END_TIME = AttendanceRecord.STANDARD_END_TIME;     // 5:00 PM
    private static final LocalTime GRACE_PERIOD_END = AttendanceRecord.GRACE_PERIOD_END;      // 8:10 AM (grace period)
    private static final Duration LUNCH_BREAK = Duration.ofHours(1);         // 1 hour lunch
    private static final Duration COFFEE_BREAK = Duration.ofMinutes(30);     // 30 min coffee break

    /**
     * Calculate hours worked between login and logout times
     * @param timeIn Login time
     * @param timeOut Logout time
     * @return Hours worked (excluding lunch break, including coffee break)
     */
    public double calculateHoursWorked(LocalTime timeIn, LocalTime timeOut) {
        if (timeIn == null || timeOut == null) {
            return 0.0;
        }

        // Handle case where timeOut is earlier than timeIn (next day)
        LocalTime adjustedTimeOut = timeOut;
        if (timeOut.isBefore(timeIn)) {
            // Add 24 hours by using a placeholder date
            adjustedTimeOut = timeOut.plusHours(24);
        }

        // Calculate total duration
        Duration duration = Duration.between(timeIn, adjustedTimeOut);

        // Deduct lunch break (1 hour) if worked at least 5 hours
        if (duration.toHours() >= 5) {
            duration = duration.minus(LUNCH_BREAK);
        }

        // Convert to hours as a decimal
        return duration.toMinutes() / 60.0;
    }

    /**
     * Calculate number of minutes late (after grace period)
     * @param timeIn Actual login time
     * @return Minutes late (0 if on time or within grace period)
     */
    public double calculateLateMinutes(LocalTime timeIn) {
        return calculateLateMinutes(timeIn, GRACE_PERIOD_END);
    }

    /**
     * Calculate number of minutes late (after specified grace period)
     * @param timeIn Actual login time
     * @param graceEndTime End of grace period time
     * @return Minutes late (0 if on time or within grace period)
     */
    public double calculateLateMinutes(LocalTime timeIn, LocalTime graceEndTime) {
        if (timeIn == null || !timeIn.isAfter(graceEndTime)) {
            return 0; // Not late if within grace period
        }

        Duration lateBy = Duration.between(graceEndTime, timeIn);
        return lateBy.toMinutes();
    }

    /**
     * Calculate overtime hours (after standard end time)
     * @param timeOut Actual logout time
     * @return Overtime hours (0 if no overtime)
     */
    public double calculateOvertimeHours(LocalTime timeOut) {
        if (timeOut == null || !timeOut.isAfter(STANDARD_END_TIME)) {
            return 0; // No overtime
        }

        Duration overtime = Duration.between(STANDARD_END_TIME, timeOut);
        return overtime.toMinutes() / 60.0;
    }

    /**
     * Calculate hours worked for a day with specified login/logout time strings
     * Handles standard time format conversion
     * @param timeInStr Time in string (e.g., "8:00" or "8:00 AM")
     * @param timeOutStr Time out string (e.g., "5:00" or "5:00 PM")
     * @return Hours worked, considering breaks
     */
    public double calculateDailyHoursFromStrings(String timeInStr, String timeOutStr) {
        try {
            // Parse times with intelligent format handling
            LocalTime timeIn = TimeConverter.parseUserTime(timeInStr);
            LocalTime timeOut = TimeConverter.parseUserTime(timeOutStr);

            if (timeIn == null || timeOut == null) {
                System.out.println("Invalid time format. Using default 8:00 AM - 5:00 PM");
                return 8.0; // Default to 8 hours if time parsing fails
            }

            return calculateHoursWorked(timeIn, timeOut);
        } catch (Exception e) {
            System.out.println("Error calculating hours: " + e.getMessage());
            return 8.0; // Default to 8 hours on error
        }
    }

    /**
     * Calculate late minutes from a login time string
     * @param timeInStr Time in string (e.g., "8:15" or "8:15 AM")
     * @return Minutes late (after grace period)
     */
    public double calculateLateMinutesFromString(String timeInStr) {
        try {
            LocalTime timeIn = TimeConverter.parseUserTime(timeInStr);
            if (timeIn == null) {
                return 0;
            }
            return calculateLateMinutes(timeIn, GRACE_PERIOD_END);
        } catch (Exception e) {
            System.out.println("Error calculating lateness: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Calculate overtime hours from a logout time string
     * @param timeOutStr Time out string (e.g., "6:30" or "6:30 PM")
     * @return Overtime hours
     */
    public double calculateOvertimeFromString(String timeOutStr) {
        try {
            LocalTime timeOut = TimeConverter.parseUserTime(timeOutStr);
            if (timeOut == null) {
                return 0;
            }
            return calculateOvertimeHours(timeOut);
        } catch (Exception e) {
            System.out.println("Error calculating overtime: " + e.getMessage());
            return 0;
        }
    }
}