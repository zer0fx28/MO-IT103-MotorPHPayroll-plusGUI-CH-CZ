// File: motorph/hours/WorkHoursCalculator.java
package motorph.hours;

import motorph.util.TimeConverter;
import java.time.LocalTime;
import java.time.Duration;

/**
 * Calculates work hours, late time, undertime, and overtime
 */
public class WorkHoursCalculator {
    // Work schedule times
    private static final LocalTime START_TIME = AttendanceRecord.STANDARD_START_TIME; // 8:00 AM
    private static final LocalTime END_TIME = AttendanceRecord.STANDARD_END_TIME;     // 5:00 PM
    private static final LocalTime GRACE_PERIOD_END = AttendanceRecord.GRACE_PERIOD_END; // 8:10 AM
    private static final Duration LUNCH_BREAK = Duration.ofHours(1);         // 1 hour lunch

    /**
     * Calculate hours worked between login and logout times
     */
    public double calculateHoursWorked(LocalTime timeIn, LocalTime timeOut, boolean isLate) {
        if (timeIn == null || timeOut == null) {
            return 0.0;
        }

        // Handle case where timeOut is earlier than timeIn (next day)
        LocalTime adjustedTimeOut = timeOut;
        if (timeOut.isBefore(timeIn)) {
            adjustedTimeOut = timeOut.plusHours(24);
        }

        // For late employees: cap the ending time to 5:00 PM (no overtime)
        LocalTime effectiveTimeOut = adjustedTimeOut;
        if (isLate && adjustedTimeOut.isAfter(END_TIME)) {
            effectiveTimeOut = END_TIME;
        }

        // Calculate total duration
        Duration duration = Duration.between(timeIn, effectiveTimeOut);

        // Deduct lunch break (1 hour) if worked at least 5 hours
        if (duration.toHours() >= 5) {
            duration = duration.minus(LUNCH_BREAK);
        }

        // Convert to hours as a decimal
        return duration.toMinutes() / 60.0;
    }

    /**
     * Simplified method to calculate hours
     */
    public double calculateHoursWorked(LocalTime timeIn, LocalTime timeOut) {
        boolean isLate = isLate(timeIn);
        return calculateHoursWorked(timeIn, timeOut, isLate);
    }

    /**
     * Check if employee is late (after grace period)
     */
    public boolean isLate(LocalTime timeIn) {
        if (timeIn == null) {
            return false;
        }
        return timeIn.isAfter(GRACE_PERIOD_END);
    }

    /**
     * Calculate minutes late (after grace period)
     */
    public double calculateLateMinutes(LocalTime timeIn) {
        if (timeIn == null || !timeIn.isAfter(GRACE_PERIOD_END)) {
            return 0; // Not late if within grace period
        }

        Duration lateBy = Duration.between(GRACE_PERIOD_END, timeIn);
        return lateBy.toMinutes();
    }

    /**
     * Calculate undertime minutes (left before 5 PM)
     */
    public double calculateUndertimeMinutes(LocalTime timeOut) {
        if (timeOut == null || !timeOut.isBefore(END_TIME)) {
            return 0; // No undertime if left at or after 5 PM
        }

        Duration undertimeBy = Duration.between(timeOut, END_TIME);
        return undertimeBy.toMinutes();
    }

    /**
     * Calculate overtime hours (after 5 PM)
     * Only applies to employees who are not late
     */
    public double calculateOvertimeHours(LocalTime timeOut, boolean isLate) {
        if (timeOut == null || !timeOut.isAfter(END_TIME) || isLate) {
            return 0; // No overtime if left before 5 PM or was late
        }

        Duration overtime = Duration.between(END_TIME, timeOut);
        return overtime.toMinutes() / 60.0;
    }

    /**
     * Calculate hours worked from time strings
     */
    public double calculateDailyHoursFromStrings(String timeInStr, String timeOutStr) {
        try {
            LocalTime timeIn = TimeConverter.parseUserTime(timeInStr);
            LocalTime timeOut = TimeConverter.parseUserTime(timeOutStr);

            if (timeIn == null || timeOut == null) {
                return 0.0;
            }

            boolean isLate = isLate(timeIn);
            return calculateHoursWorked(timeIn, timeOut, isLate);
        } catch (Exception e) {
            System.out.println("Error calculating hours: " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * Calculate late minutes from time string
     */
    public double calculateLateMinutesFromString(String timeInStr) {
        try {
            LocalTime timeIn = TimeConverter.parseUserTime(timeInStr);
            return calculateLateMinutes(timeIn);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Calculate undertime minutes from time string
     */
    public double calculateUndertimeMinutesFromString(String timeOutStr) {
        try {
            LocalTime timeOut = TimeConverter.parseUserTime(timeOutStr);
            return calculateUndertimeMinutes(timeOut);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Calculate overtime hours from time string
     */
    public double calculateOvertimeFromString(String timeOutStr) {
        try {
            LocalTime timeOut = TimeConverter.parseUserTime(timeOutStr);
            return calculateOvertimeHours(timeOut, false); // Assuming not late
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Calculate overtime with lateness considered
     */
    public double calculateOvertimeFromStringWithLate(String timeOutStr, boolean isLate) {
        try {
            LocalTime timeOut = TimeConverter.parseUserTime(timeOutStr);
            return calculateOvertimeHours(timeOut, isLate);
        } catch (Exception e) {
            return 0;
        }
    }
}