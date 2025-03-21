// File: motorph/holidays/HolidayPayCalculator.java
package motorph.holidays;

import java.time.LocalDate;

/**
 * Calculates holiday pay based on Philippine labor laws
 * Applies the proper rates for regular and special non-working holidays
 */
public class HolidayPayCalculator {

    // Holiday pay rates
    private static final double REGULAR_HOLIDAY_NONWORKING_RATE = 1.0; // 100% of daily rate for non-working employees
    private static final double REGULAR_HOLIDAY_WORKING_RATE = 2.0;    // 200% of daily rate for first 8 hours
    private static final double SPECIAL_HOLIDAY_WORKING_RATE = 0.3;    // 30% additional of daily rate
    private static final double OVERTIME_PREMIUM = 0.3;                // 30% additional for overtime
    private static final double OVERTIME_NON_LATE_PREMIUM = 0.25;      // 25% additional for non-late employees working overtime
    private static final double REST_DAY_PREMIUM = 0.3;                // 30% additional if holiday falls on rest day

    // Reference to holiday manager
    private final HolidayManager holidayManager;

    /**
     * Create a new holiday pay calculator with the given holiday manager
     *
     * @param holidayManager The holiday manager containing holiday information
     */
    public HolidayPayCalculator(HolidayManager holidayManager) {
        this.holidayManager = holidayManager;
    }

    /**
     * Calculate the holiday pay for a given workday
     *
     * @param date The date to check
     * @param dailyRate Employee's daily rate
     * @param hoursWorked Number of hours worked
     * @param isRestDay Whether the date is the employee's rest day
     * @param isLate Whether the employee was late
     * @param overtimeHours Number of overtime hours worked
     * @return The calculated holiday pay
     */
    public double calculateHolidayPay(LocalDate date, double dailyRate, double hoursWorked,
                                      boolean isRestDay, boolean isLate, double overtimeHours) {
        // Check if date is a holiday
        if (!holidayManager.isHoliday(date)) {
            return 0.0; // Not a holiday, no holiday pay
        }

        boolean isRegularHoliday = holidayManager.isRegularHoliday(date);
        boolean isSpecialHoliday = holidayManager.isSpecialNonWorkingHoliday(date);

        // Calculate holiday pay based on type
        if (isRegularHoliday) {
            return calculateRegularHolidayPay(dailyRate, hoursWorked, isRestDay, isLate, overtimeHours);
        } else if (isSpecialHoliday) {
            return calculateSpecialHolidayPay(dailyRate, hoursWorked, isLate, overtimeHours);
        }

        return 0.0;
    }

    /**
     * Calculate pay for a regular holiday
     *
     * @param dailyRate Employee's daily rate
     * @param hoursWorked Number of hours worked
     * @param isRestDay Whether the date is the employee's rest day
     * @param isLate Whether the employee was late
     * @param overtimeHours Number of overtime hours worked
     * @return The calculated regular holiday pay
     */
    private double calculateRegularHolidayPay(double dailyRate, double hoursWorked,
                                              boolean isRestDay, boolean isLate, double overtimeHours) {
        double holidayPay;

        if (hoursWorked == 0) {
            // Non-working employees get 100% of daily rate
            holidayPay = dailyRate * REGULAR_HOLIDAY_NONWORKING_RATE;
        } else {
            // First 8 hours: 200% of daily rate
            double regularHours = Math.min(hoursWorked, 8.0);
            holidayPay = regularHours * (dailyRate / 8) * REGULAR_HOLIDAY_WORKING_RATE;

            // Overtime hours: additional 30% + 25% for non-late employees
            if (overtimeHours > 0) {
                double hourlyRate = dailyRate / 8;
                double overtimeRate = hourlyRate * (1 + OVERTIME_PREMIUM); // 30% additional

                if (!isLate) {
                    overtimeRate *= (1 + OVERTIME_NON_LATE_PREMIUM); // Additional 25% for non-late
                }

                holidayPay += overtimeHours * overtimeRate;
            }

            // If holiday falls on rest day: additional 30%
            if (isRestDay) {
                holidayPay *= (1 + REST_DAY_PREMIUM); // Additional 30%
            }
        }

        return holidayPay;
    }

    /**
     * Calculate pay for a special non-working holiday
     *
     * @param dailyRate Employee's daily rate
     * @param hoursWorked Number of hours worked
     * @param isLate Whether the employee was late
     * @param overtimeHours Number of overtime hours worked
     * @return The calculated special holiday pay
     */
    private double calculateSpecialHolidayPay(double dailyRate, double hoursWorked,
                                              boolean isLate, double overtimeHours) {
        double holidayPay;

        if (hoursWorked == 0) {
            // No extra pay for non-working employees on special holidays
            holidayPay = 0;
        } else {
            // First 8 hours: additional 30% of daily rate
            double regularHours = Math.min(hoursWorked, 8.0);
            holidayPay = regularHours * (dailyRate / 8) * (1 + SPECIAL_HOLIDAY_WORKING_RATE);

            // Overtime: 30% holiday + 25% overtime for non-late
            if (overtimeHours > 0) {
                double hourlyRate = dailyRate / 8;
                double overtimeRate = hourlyRate * (1 + SPECIAL_HOLIDAY_WORKING_RATE); // 30% holiday premium

                if (!isLate) {
                    overtimeRate *= (1 + OVERTIME_NON_LATE_PREMIUM); // Additional 25% for non-late
                }

                holidayPay += overtimeHours * overtimeRate;
            }
        }

        return holidayPay;
    }
}