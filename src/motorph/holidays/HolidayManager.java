package motorph.holidays;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages holidays and calculates holiday pay
 * Tracks both regular and special non-working holidays
 */
public class HolidayManager {
    // Holiday lists by type
    private List<Holiday> regularHolidays;
    private List<Holiday> specialNonWorkingHolidays;

    // Holiday premium rates
    private static final double REGULAR_HOLIDAY_RATE = 1.0; // 100% of daily rate
    private static final double SPECIAL_HOLIDAY_RATE = 0.3; // 30% of daily rate
    private static final double OVERTIME_PREMIUM = 0.25; // 25% overtime premium

    /**
     * Create a new holiday manager with predefined holidays
     */
    public HolidayManager() {
        regularHolidays = new ArrayList<>();
        specialNonWorkingHolidays = new ArrayList<>();

        // Set up holidays
        setup2024Holidays();
        setup2025Holidays();
    }

    /**
     * Set up 2024 holidays
     */
    private void setup2024Holidays() {
        // Regular Holidays 2024
        regularHolidays.add(new Holiday("New Year's Day", LocalDate.of(2024, 1, 1)));
        regularHolidays.add(new Holiday("Maundy Thursday", LocalDate.of(2024, 3, 28)));
        regularHolidays.add(new Holiday("Good Friday", LocalDate.of(2024, 3, 29)));
        regularHolidays.add(new Holiday("Araw ng Kagitingan", LocalDate.of(2024, 4, 9)));
        regularHolidays.add(new Holiday("Eid'l Fitr", LocalDate.of(2024, 4, 10)));
        regularHolidays.add(new Holiday("Labor Day", LocalDate.of(2024, 5, 1)));
        regularHolidays.add(new Holiday("Independence Day", LocalDate.of(2024, 6, 12)));
        regularHolidays.add(new Holiday("Eid'l Adha", LocalDate.of(2024, 6, 17)));
        regularHolidays.add(new Holiday("National Heroes Day", LocalDate.of(2024, 8, 26)));
        regularHolidays.add(new Holiday("Bonifacio Day", LocalDate.of(2024, 11, 30)));
        regularHolidays.add(new Holiday("Christmas Day", LocalDate.of(2024, 12, 25)));
        regularHolidays.add(new Holiday("Rizal Day", LocalDate.of(2024, 12, 30)));

        // Special Non-Working Holidays 2024
        specialNonWorkingHolidays.add(new Holiday("Additional Special Day", LocalDate.of(2024, 2, 9)));
        specialNonWorkingHolidays.add(new Holiday("Chinese New Year", LocalDate.of(2024, 2, 10)));
        specialNonWorkingHolidays.add(new Holiday("Black Saturday", LocalDate.of(2024, 3, 30)));
        specialNonWorkingHolidays.add(new Holiday("Ninoy Aquino Day", LocalDate.of(2024, 8, 23)));
        specialNonWorkingHolidays.add(new Holiday("All Saints' Day", LocalDate.of(2024, 11, 1)));
        specialNonWorkingHolidays.add(new Holiday("All Souls' Day", LocalDate.of(2024, 11, 2)));
        specialNonWorkingHolidays.add(new Holiday("Feast of Immaculate Conception", LocalDate.of(2024, 12, 8)));
        specialNonWorkingHolidays.add(new Holiday("Christmas Eve", LocalDate.of(2024, 12, 24)));
        specialNonWorkingHolidays.add(new Holiday("Last Day of the Year", LocalDate.of(2024, 12, 31)));
    }

    /**
     * Set up 2025 holidays
     */
    private void setup2025Holidays() {
        // Regular Holidays 2025
        regularHolidays.add(new Holiday("New Year's Day", LocalDate.of(2025, 1, 1)));
        regularHolidays.add(new Holiday("Araw ng Kagitingan", LocalDate.of(2025, 4, 9)));
        regularHolidays.add(new Holiday("Maundy Thursday", LocalDate.of(2025, 4, 17)));
        regularHolidays.add(new Holiday("Good Friday", LocalDate.of(2025, 4, 18)));
        regularHolidays.add(new Holiday("Labor Day", LocalDate.of(2025, 5, 1)));
        regularHolidays.add(new Holiday("Independence Day", LocalDate.of(2025, 6, 12)));
        regularHolidays.add(new Holiday("National Heroes Day", LocalDate.of(2025, 8, 25)));
        regularHolidays.add(new Holiday("Bonifacio Day", LocalDate.of(2025, 11, 30)));
        regularHolidays.add(new Holiday("Christmas Day", LocalDate.of(2025, 12, 25)));
        regularHolidays.add(new Holiday("Rizal Day", LocalDate.of(2025, 12, 30)));

        // Special Non-Working Holidays 2025
        specialNonWorkingHolidays.add(new Holiday("Chinese New Year", LocalDate.of(2025, 1, 29)));
        specialNonWorkingHolidays.add(new Holiday("Black Saturday", LocalDate.of(2025, 4, 19)));
        specialNonWorkingHolidays.add(new Holiday("Ninoy Aquino Day", LocalDate.of(2025, 8, 21)));
        specialNonWorkingHolidays.add(new Holiday("All Saints' Day Eve", LocalDate.of(2025, 10, 31)));
        specialNonWorkingHolidays.add(new Holiday("All Saints' Day", LocalDate.of(2025, 11, 1)));
        specialNonWorkingHolidays.add(new Holiday("Feast of Immaculate Conception", LocalDate.of(2025, 12, 8)));
        specialNonWorkingHolidays.add(new Holiday("Christmas Eve", LocalDate.of(2025, 12, 24)));
        specialNonWorkingHolidays.add(new Holiday("Last Day of the Year", LocalDate.of(2025, 12, 31)));
    }

    /**
     * Check if a date is a holiday
     *
     * @param date Date to check
     * @return true if the date is a holiday (regular or special)
     */
    public boolean isHoliday(LocalDate date) {
        return isRegularHoliday(date) || isSpecialNonWorkingHoliday(date);
    }

    /**
     * Check if a date is a regular holiday
     *
     * @param date Date to check
     * @return true if the date is a regular holiday
     */
    public boolean isRegularHoliday(LocalDate date) {
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
     * Get all holidays for a given year
     *
     * @param year Year to get holidays for
     * @return Map of dates to holiday names
     */
    public Map<LocalDate, String> getHolidaysForYear(int year) {
        Map<LocalDate, String> yearHolidays = new HashMap<>();

        // Add regular holidays
        for (Holiday holiday : regularHolidays) {
            LocalDate holidayDate = holiday.getDate();
            if (holidayDate.getYear() == year) {
                yearHolidays.put(holidayDate, holiday.getName() + " (Regular)");
            }
        }

        // Add special holidays
        for (Holiday holiday : specialNonWorkingHolidays) {
            LocalDate holidayDate = holiday.getDate();
            if (holidayDate.getYear() == year) {
                yearHolidays.put(holidayDate, holiday.getName() + " (Special)");
            }
        }

        return yearHolidays;
    }

    /**
     * Add a custom holiday
     *
     * @param name Holiday name
     * @param date Holiday date
     * @param isRegular Whether it's a regular holiday
     */
    public void addHoliday(String name, LocalDate date, boolean isRegular) {
        Holiday holiday = new Holiday(name, date);
        if (isRegular) {
            regularHolidays.add(holiday);
        } else {
            specialNonWorkingHolidays.add(holiday);
        }
    }
}

/**
 * Simple class to store holiday information
 */
class Holiday {
    private String name;
    private LocalDate date;

    /**
     * Create a new holiday
     *
     * @param name Holiday name
     * @param date Holiday date
     */
    public Holiday(String name, LocalDate date) {
        this.name = name;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public LocalDate getDate() {
        return date;
    }
}
