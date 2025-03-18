// File: motorph/holidays/HolidayManager.java
package motorph.holidays;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages holidays and calculates holiday pay
 *
 * This class maintains a list of regular and special non-working holidays
 * for the Philippines, and provides methods to check if a date is a holiday
 * and calculate holiday pay for employees.
 */
public class HolidayManager {
    // Lists of holidays by type
    private List<Holiday> regularHolidays;
    private List<Holiday> specialNonWorkingHolidays;

    /**
     * Initialize the holiday manager with holiday data
     */
    public HolidayManager() {
        regularHolidays = new ArrayList<>();
        specialNonWorkingHolidays = new ArrayList<>();

        // Set up current and future holidays
        setup2024Holidays();
        setup2025Holidays();
    }

    /**
     * Set up 2024 holidays based on official proclamation
     */
    private void setup2024Holidays() {
        // Regular Holidays 2024
        addRegularHoliday("New Year's Day", 2024, 1, 1);
        addRegularHoliday("Maundy Thursday", 2024, 3, 28);
        addRegularHoliday("Good Friday", 2024, 3, 29);
        addRegularHoliday("Araw ng Kagitingan", 2024, 4, 9);
        addRegularHoliday("Eid'l Fitr", 2024, 4, 10);
        addRegularHoliday("Labor Day", 2024, 5, 1);
        addRegularHoliday("Independence Day", 2024, 6, 12);
        addRegularHoliday("Eid'l Adha", 2024, 6, 17);
        addRegularHoliday("National Heroes Day", 2024, 8, 26);
        addRegularHoliday("Bonifacio Day", 2024, 11, 30);
        addRegularHoliday("Christmas Day", 2024, 12, 25);
        addRegularHoliday("Rizal Day", 2024, 12, 30);

        // Special Non-Working Holidays 2024
        addSpecialNonWorkingHoliday("Additional Special Day", 2024, 2, 9);
        addSpecialNonWorkingHoliday("Chinese New Year", 2024, 2, 10);
        addSpecialNonWorkingHoliday("Black Saturday", 2024, 3, 30);
        addSpecialNonWorkingHoliday("Ninoy Aquino Day", 2024, 8, 23);
        addSpecialNonWorkingHoliday("All Saints' Day", 2024, 11, 1);
        addSpecialNonWorkingHoliday("All Souls' Day", 2024, 11, 2);
        addSpecialNonWorkingHoliday("Feast of Immaculate Conception", 2024, 12, 8);
        addSpecialNonWorkingHoliday("Christmas Eve", 2024, 12, 24);
        addSpecialNonWorkingHoliday("Last Day of the Year", 2024, 12, 31);
    }

    /**
     * Set up 2025 holidays based on official proclamation
     */
    private void setup2025Holidays() {
        // Regular Holidays 2025
        addRegularHoliday("New Year's Day", 2025, 1, 1);
        addRegularHoliday("Araw ng Kagitingan", 2025, 4, 9);
        addRegularHoliday("Maundy Thursday", 2025, 4, 17);
        addRegularHoliday("Good Friday", 2025, 4, 18);
        addRegularHoliday("Labor Day", 2025, 5, 1);
        addRegularHoliday("Independence Day", 2025, 6, 12);
        addRegularHoliday("National Heroes Day", 2025, 8, 25);
        addRegularHoliday("Bonifacio Day", 2025, 11, 30);
        addRegularHoliday("Christmas Day", 2025, 12, 25);
        addRegularHoliday("Rizal Day", 2025, 12, 30);

        // Special Non-Working Holidays 2025
        addSpecialNonWorkingHoliday("Chinese New Year", 2025, 1, 29);
        addSpecialNonWorkingHoliday("Black Saturday", 2025, 4, 19);
        addSpecialNonWorkingHoliday("Ninoy Aquino Day", 2025, 8, 21);
        addSpecialNonWorkingHoliday("All Saints' Day Eve", 2025, 10, 31);
        addSpecialNonWorkingHoliday("All Saints' Day", 2025, 11, 1);
        addSpecialNonWorkingHoliday("Feast of Immaculate Conception", 2025, 12, 8);
        addSpecialNonWorkingHoliday("Christmas Eve", 2025, 12, 24);
        addSpecialNonWorkingHoliday("Last Day of the Year", 2025, 12, 31);
    }

    /**
     * Helper method to add a regular holiday
     */
    private void addRegularHoliday(String name, int year, int month, int day) {
        regularHolidays.add(new Holiday(name, LocalDate.of(year, month, day)));
    }

    /**
     * Helper method to add a special non-working holiday
     */
    private void addSpecialNonWorkingHoliday(String name, int year, int month, int day) {
        specialNonWorkingHolidays.add(new Holiday(name, LocalDate.of(year, month, day)));
    }

    /**
     * Check if a date is a holiday (regular or special non-working)
     *
     * @param date Date to check
     * @return true if the date is a holiday
     */
    public boolean isHoliday(LocalDate date) {
        if (date == null) {
            return false;
        }
        return isRegularHoliday(date) || isSpecialNonWorkingHoliday(date);
    }

    /**
     * Check if a date is a regular holiday
     *
     * @param date Date to check
     * @return true if the date is a regular holiday
     */
    public boolean isRegularHoliday(LocalDate date) {
        if (date == null) {
            return false;
        }

        for (Holiday holiday : regularHolidays) {
            if (holiday.getDate().equals(date)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if a date is a special non-working holiday
     *
     * @param date Date to check
     * @return true if the date is a special non-working holiday
     */
    public boolean isSpecialNonWorkingHoliday(LocalDate date) {
        if (date == null) {
            return false;
        }

        for (Holiday holiday : specialNonWorkingHolidays) {
            if (holiday.getDate().equals(date)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get holiday name if it's a holiday
     *
     * @param date Date to check
     * @return Holiday name with type, or null if not a holiday
     */
    public String getHolidayName(LocalDate date) {
        if (date == null) {
            return null;
        }

        for (Holiday holiday : regularHolidays) {
            if (holiday.getDate().equals(date)) {
                return holiday.getName() + " (Regular Holiday)";
            }
        }

        for (Holiday holiday : specialNonWorkingHolidays) {
            if (holiday.getDate().equals(date)) {
                return holiday.getName() + " (Special Non-Working Holiday)";
            }
        }

        return null;
    }

    /**
     * Calculate holiday pay for working on a holiday
     *
     * This method implements the complex holiday pay calculations based on Philippine labor laws:
     * - Regular holidays: 200% of daily rate for first 8 hours, plus overtime premiums
     * - Special non-working holidays: 130% of daily rate for first 8 hours, plus overtime premiums
     * Additional premiums for rest days and non-late employees
     *
     * @param basePay The employee's base pay for that day
     * @param hoursWorked Number of hours worked
     * @param isRegularHoliday Whether it's a regular holiday
     * @param isRestDay Whether it's the employee's rest day
     * @param overtimeHours Number of overtime hours worked
     * @param isLate Whether the employee was late
     * @return The holiday pay amount
     */
    public double calculateHolidayPay(double basePay, double hoursWorked,
                                      boolean isRegularHoliday, boolean isRestDay,
                                      double overtimeHours, boolean isLate) {
        // Validate inputs
        if (basePay < 0) {
            System.out.println("Warning: Negative base pay provided for holiday pay calculation. Using 0.0");
            basePay = 0.0;
        }

        if (hoursWorked < 0) {
            System.out.println("Warning: Negative hours worked provided for holiday pay calculation. Using 0.0");
            hoursWorked = 0.0;
        }

        if (overtimeHours < 0) {
            System.out.println("Warning: Negative overtime hours provided for holiday pay calculation. Using 0.0");
            overtimeHours = 0.0;
        }

        double holidayPay = 0.0;

        if (isRegularHoliday) {
            // Regular holiday pay calculation
            if (hoursWorked == 0) {
                // Non-working employees get 100% of base pay
                holidayPay = basePay;
            } else {
                // First 8 hours: 200% of basic wage
                double regularHours = Math.min(hoursWorked, 8.0);
                holidayPay = regularHours * (basePay / 8) * 2;

                // Overtime hours: additional 30% + 25% for non-late employees
                if (overtimeHours > 0 && !isLate) {
                    double hourlyRate = basePay / 8;
                    double overtimeRate = hourlyRate * 1.3; // 30% additional

                    if (!isLate) {
                        overtimeRate *= 1.25; // Additional 25% for non-late
                    }

                    holidayPay += overtimeHours * overtimeRate;
                }

                // If holiday falls on rest day: additional 30%
                if (isRestDay) {
                    holidayPay *= 1.3; // Additional 30%
                }
            }
        } else {
            // Special non-working holiday pay calculation
            if (hoursWorked == 0) {
                // No extra pay for non-working employees
                holidayPay = 0;
            } else {
                // First 8 hours: additional 30% of basic wage
                double regularHours = Math.min(hoursWorked, 8.0);
                holidayPay = regularHours * (basePay / 8) * 1.3;

                // Overtime: 30% holiday + 25% overtime for non-late
                if (overtimeHours > 0 && !isLate) {
                    double hourlyRate = basePay / 8;
                    double overtimeRate = hourlyRate * 1.3; // 30% holiday premium

                    if (!isLate) {
                        overtimeRate *= 1.25; // Additional 25% for non-late
                    }

                    holidayPay += overtimeHours * overtimeRate;
                }
            }
        }

        return holidayPay;
    }

    /**
     * Get a list of all holidays in a date range
     *
     * @param startDate Start date
     * @param endDate End date
     * @return Map of holiday dates to their names and types
     */
    public Map<LocalDate, String> getHolidaysInRange(LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, String> holidaysInRange = new HashMap<>();

        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            return holidaysInRange;
        }

        // Check each date in the range
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            String holidayName = getHolidayName(currentDate);
            if (holidayName != null) {
                holidaysInRange.put(currentDate, holidayName);
            }
            currentDate = currentDate.plusDays(1);
        }

        return holidaysInRange;
    }
}