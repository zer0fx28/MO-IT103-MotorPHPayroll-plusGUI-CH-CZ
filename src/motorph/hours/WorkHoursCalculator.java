// File: motorph/hours/WorkHoursCalculator.java
package motorph.hours;

import motorph.util.ValidationUtils;
import java.time.LocalTime;
import java.time.Duration;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Calculates working hours based on timeIn and timeOut
 *
 * This class is responsible for calculating regular and overtime hours
 * based on employee's time in and time out records, applying company policies
 * for late arrivals and extended hours.
 */
public class WorkHoursCalculator {
    // Logger for this class
    private static final Logger LOGGER = Logger.getLogger(WorkHoursCalculator.class.getName());

    // Work schedule time constants
    private static final LocalTime STANDARD_START_TIME = AttendanceRecord.STANDARD_START_TIME;
    private static final LocalTime STANDARD_END_TIME = AttendanceRecord.STANDARD_END_TIME;
    private static final LocalTime GRACE_PERIOD_END = AttendanceRecord.GRACE_PERIOD_END;

    // Hour calculation constants
    private static final double REGULAR_HOURS = 8.0; // 8 hours regular work day
    private static final double MINUTES_PER_HOUR = 60.0; // For time conversions
    private static final double EPSILON = 0.001; // For floating point comparisons

    // Overtime eligibility rules
    private static final boolean LATE_EMPLOYEES_ELIGIBLE_FOR_OVERTIME = false;

    /**
     * Calculate hours worked for a day
     *
     * Computes the total regular hours worked based on time in and time out,
     * with special handling for late arrivals. The result is capped at 8 hours
     * as overtime is calculated separately.
     *
     * @param timeIn Time employee clocked in (can be null)
     * @param timeOut Time employee clocked out (can be null)
     * @param isLate Whether employee was late (after grace period)
     * @return Total hours worked (max 8 hours for regular time), 0 if invalid input
     */
    public double calculateHoursWorked(LocalTime timeIn, LocalTime timeOut, boolean isLate) {
        // Input validation: Check for null time values
        if (timeIn == null || timeOut == null) {
            LOGGER.warning("Invalid time values (null) provided for hours calculation");
            return 0.0;
        }

        // Check for overnight work (time out before time in)
        if (timeOut.isBefore(timeIn)) {
            LOGGER.warning(String.format(
                    "Time out (%s) is before time in (%s) - possible overnight work",
                    timeOut, timeIn));

            // For overnight work, we could calculate hours like this:
            // Duration overnight = Duration.between(timeIn, LocalTime.of(23, 59, 59))
            //                    .plus(Duration.between(LocalTime.of(0, 0), timeOut));
            // But for now, we'll return 0 as an error case
            return 0.0;
        }

        // Calculate duration between clock in and out
        Duration workDuration = Duration.between(timeIn, timeOut);
        double hoursWorked = workDuration.toMinutes() / MINUTES_PER_HOUR;

        // Handle edge case: exactly 8 hours (using epsilon comparison)
        // Using proper epsilon comparison instead of direct equality check
        if (ValidationUtils.doubleEquals(hoursWorked, REGULAR_HOURS, EPSILON)) {
            LOGGER.fine("Exact 8 hours worked");
            return REGULAR_HOURS;
        }

        // Log for hours exceeding the regular workday
        if (hoursWorked > REGULAR_HOURS) {
            LOGGER.fine(String.format(
                    "Hours worked (%.2f) exceeds regular hours (%.2f). Capping at %.2f",
                    hoursWorked, REGULAR_HOURS, REGULAR_HOURS));
        }

        // Cap hours at 8 (overtime is calculated separately)
        // We handle even small variations with a max function to avoid precision issues
        return Math.min(hoursWorked, REGULAR_HOURS);
    }

    /**
     * Calculate overtime hours
     *
     * Computes overtime hours as any time worked after the standard end time.
     * Late employees are typically not eligible for overtime according to company
     * policy, but this can be configured via the LATE_EMPLOYEES_ELIGIBLE_FOR_OVERTIME
     * constant.
     *
     * @param timeOut Time employee clocked out (can be null)
     * @param isLate Whether employee was late (potentially ineligible for overtime)
     * @return Overtime hours worked (0 if employee was late or invalid input)
     */
    public double calculateOvertimeHours(LocalTime timeOut, boolean isLate) {
        // Late employees don't get overtime as per company policy (unless configured otherwise)
        if (isLate && !LATE_EMPLOYEES_ELIGIBLE_FOR_OVERTIME) {
            LOGGER.fine("Late employee ineligible for overtime");
            return 0.0;
        }

        // Validate input: Check for null time values
        if (timeOut == null) {
            LOGGER.warning("Invalid time out value (null) for overtime calculation");
            return 0.0;
        }

        // Check if employee worked past standard end time
        if (timeOut.isAfter(STANDARD_END_TIME)) {
            // Calculate overtime minutes and convert to hours
            Duration overtimeDuration = Duration.between(STANDARD_END_TIME, timeOut);
            double overtimeHours = overtimeDuration.toMinutes() / MINUTES_PER_HOUR;

            LOGGER.fine(String.format("Overtime hours calculated: %.2f", overtimeHours));
            return overtimeHours;
        }

        // No overtime if employee left before or at standard end time
        return 0.0;
    }

    /**
     * Calculate late minutes
     *
     * Determines how many minutes an employee was late based on
     * their actual arrival time compared to the grace period end time.
     * When an employee arrives within the grace period, they are not
     * considered late.
     *
     * @param timeIn Time employee clocked in
     * @return Minutes late (0 if on time or early)
     */
    public double calculateLateMinutes(LocalTime timeIn) {
        // Input validation: Check for null time values
        if (timeIn == null) {
            LOGGER.warning("Invalid time in value (null) for late minutes calculation");
            return 0.0;
        }

        // Not late if within grace period
        if (!timeIn.isAfter(GRACE_PERIOD_END)) {
            return 0.0;
        }

        // Calculate minutes late beyond grace period
        Duration lateBy = Duration.between(GRACE_PERIOD_END, timeIn);
        double lateMinutes = lateBy.toMinutes();

        LOGGER.fine(String.format("Late minutes calculated: %.0f", lateMinutes));
        return lateMinutes;
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
        // Input validation: Check for null time values
        if (timeOut == null) {
            LOGGER.warning("Invalid time out value (null) for undertime calculation");
            return 0.0;
        }

        // Not undertime if left at or after standard end time
        if (!timeOut.isBefore(STANDARD_END_TIME)) {
            return 0.0;
        }

        // Calculate minutes of undertime
        Duration undertimeBy = Duration.between(timeOut, STANDARD_END_TIME);
        double undertimeMinutes = undertimeBy.toMinutes();

        LOGGER.fine(String.format("Undertime minutes calculated: %.0f", undertimeMinutes));
        return undertimeMinutes;
    }

    /**
     * Calculate total work hours including overtime
     *
     * This method combines regular hours and overtime hours to provide
     * the total hours worked for the day. It respects the company policy
     * on overtime eligibility for late employees.
     *
     * @param timeIn Time employee clocked in
     * @param timeOut Time employee clocked out
     * @param isLate Whether employee was late (affects overtime eligibility)
     * @return Total work hours including overtime
     */
    public double calculateTotalWorkHours(LocalTime timeIn, LocalTime timeOut, boolean isLate) {
        // Validate inputs: Ensure valid time range
        if (!ValidationUtils.validateTimeRange(timeIn, timeOut)) {
            LOGGER.warning("Invalid time range for total hours calculation");
            return 0.0;
        }

        // Calculate regular and overtime hours
        double regularHours = calculateHoursWorked(timeIn, timeOut, isLate);
        double overtimeHours = calculateOvertimeHours(timeOut, isLate);

        // Sum total hours
        double totalHours = regularHours + overtimeHours;
        LOGGER.fine(String.format(
                "Total hours calculated: %.2f (regular: %.2f, overtime: %.2f)",
                totalHours, regularHours, overtimeHours));

        return totalHours;
    }

    /**
     * Check if work hours meet minimum requirement
     *
     * This utility method checks if an employee has worked at least
     * the minimum required hours for a day. Useful for determining
     * if a day counts as a full workday for benefits or attendance.
     *
     * @param hoursWorked Actual hours worked
     * @param minimumHours Minimum required hours (default is 4.0)
     * @return true if minimum hours requirement is met
     */
    public boolean meetsMinimumHours(double hoursWorked, double minimumHours) {
        // Default minimum hours if not specified
        if (minimumHours <= 0) {
            minimumHours = 4.0; // Half day by default
        }

        return hoursWorked >= minimumHours;
    }
}