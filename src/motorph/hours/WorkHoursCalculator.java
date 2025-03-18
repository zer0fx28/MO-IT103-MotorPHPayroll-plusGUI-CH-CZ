// File: motorph/hours/WorkHoursCalculator.java
package motorph.hours;

import java.time.LocalTime;
import java.time.Duration;

/**
 * Calculates working hours based on timeIn and timeOut
 *
 * This class is responsible for calculating regular and overtime hours
 * based on employee's time in and time out records, applying company policies
 * for late arrivals and extended hours.
 */
public class WorkHoursCalculator {
    // Work schedule time constants
    private static final LocalTime STANDARD_START_TIME = AttendanceRecord.STANDARD_START_TIME;
    private static final LocalTime STANDARD_END_TIME = AttendanceRecord.STANDARD_END_TIME;
    private static final LocalTime GRACE_PERIOD_END = AttendanceRecord.GRACE_PERIOD_END;
    private static final double REGULAR_HOURS = 8.0; // 8 hours regular work day

    /**
     * Calculate hours worked for a day
     *
     * Computes the total regular hours worked based on time in and time out,
     * with special handling for late arrivals.
     *
     * @param timeIn Time employee clocked in (can be null)
     * @param timeOut Time employee clocked out (can be null)
     * @param isLate Whether employee was late (after grace period)
     * @return Total hours worked (max 8 hours for regular time), 0 if invalid input
     */
    public double calculateHoursWorked(LocalTime timeIn, LocalTime timeOut, boolean isLate) {
        // Validate inputs
        if (timeIn == null || timeOut == null) {
            return 0.0;
        }

        // Check for overnight work (time out before time in)
        if (timeOut.isBefore(timeIn)) {
            System.out.println("Warning: Time out is before time in - possible overnight work. Returning 0 hours.");
            return 0.0;
        }

        // Calculate duration between clock in and out
        Duration workDuration = Duration.between(timeIn, timeOut);
        double hoursWorked = workDuration.toMinutes() / 60.0;

        // Handle edge case: exactly 8 hours
        if (Math.abs(hoursWorked - REGULAR_HOURS) < 0.001) {
            return REGULAR_HOURS;
        }

        // Cap hours at 8 (overtime is calculated separately)
        return Math.min(hoursWorked, REGULAR_HOURS);
    }

    /**
     * Calculate overtime hours
     *
     * Computes overtime hours as any time worked after the standard end time.
     * Late employees are not eligible for overtime.
     *
     * @param timeOut Time employee clocked out (can be null)
     * @param isLate Whether employee was late (ineligible for overtime)
     * @return Overtime hours worked (0 if employee was late or invalid input)
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

    /**
     * Calculate late minutes
     *
     * Determines how many minutes an employee was late based on
     * their actual arrival time compared to the grace period end time.
     *
     * @param timeIn Time employee clocked in
     * @return Minutes late (0 if on time or early)
     */
    public double calculateLateMinutes(LocalTime timeIn) {
        if (timeIn == null || !timeIn.isAfter(GRACE_PERIOD_END)) {
            return 0.0;
        }

        Duration lateBy = Duration.between(GRACE_PERIOD_END, timeIn);
        return lateBy.toMinutes();
    }

    /**
     * Calculate undertime minutes
     *
     * Determines how many minutes an employee left early based on
     * their actual departure time compared to standard end time.
     *
     * @param timeOut Time employee clocked out
     * @return Minutes of undertime (0 if stayed full time or late)
     */
    public double calculateUndertimeMinutes(LocalTime timeOut) {
        if (timeOut == null || !timeOut.isBefore(STANDARD_END_TIME)) {
            return 0.0;
        }

        Duration undertimeBy = Duration.between(timeOut, STANDARD_END_TIME);
        return undertimeBy.toMinutes();
    }
}