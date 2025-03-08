// File: motorph/hours/WorkHoursCalculator.java
package motorph.hours;

import java.time.LocalTime;
import java.time.Duration;

/**
 * Calculates working hours based on timeIn and timeOut
 */
public class WorkHoursCalculator {
    // Work schedule times
    private static final LocalTime STANDARD_START_TIME = AttendanceRecord.STANDARD_START_TIME;
    private static final LocalTime STANDARD_END_TIME = AttendanceRecord.STANDARD_END_TIME;
    private static final LocalTime GRACE_PERIOD_END = AttendanceRecord.GRACE_PERIOD_END;
    private static final double REGULAR_HOURS = 8.0; // 8 hours regular work day

    /**
     * Calculate hours worked for a day
     * @param timeIn Time employee clocked in
     * @param timeOut Time employee clocked out
     * @param isLate Whether employee was late (after grace period)
     * @return Total hours worked (max 8 hours for regular time)
     */
    public double calculateHoursWorked(LocalTime timeIn, LocalTime timeOut, boolean isLate) {
        if (timeIn == null || timeOut == null) {
            return 0.0;
        }

        // If time out is before time in (overnight work), return 0
        if (timeOut.isBefore(timeIn)) {
            return 0.0;
        }

        // Calculate duration between clock in and out
        Duration workDuration = Duration.between(timeIn, timeOut);
        double hoursWorked = workDuration.toMinutes() / 60.0;

        // Cap hours at 8 (overtime is calculated separately)
        return Math.min(hoursWorked, REGULAR_HOURS);
    }

    /**
     * Calculate overtime hours
     * @param timeOut Time employee clocked out
     * @param isLate Whether employee was late (ineligible for overtime)
     * @return Overtime hours worked (0 if employee was late)
     */
    public double calculateOvertimeHours(LocalTime timeOut, boolean isLate) {
        // Late employees don't get overtime
        if (isLate || timeOut == null) {
            return 0.0;
        }

        // Check if employee worked past standard end time
        if (timeOut.isAfter(STANDARD_END_TIME)) {
            Duration overtimeDuration = Duration.between(STANDARD_END_TIME, timeOut);
            return overtimeDuration.toMinutes() / 60.0;
        }

        return 0.0;
    }
}