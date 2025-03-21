// File: motorph/holidays/HolidayManager.java
package motorph.holidays;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages holidays and calculates holiday pay
 */
public class HolidayManager {
    private List<Holiday> regularHolidays;
    private List<Holiday> specialNonWorkingHolidays;

    public HolidayManager() {
        regularHolidays = new ArrayList<>();
        specialNonWorkingHolidays = new ArrayList<>();

        setup2024Holidays();
        setup2025Holidays();
    }

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

    private void addRegularHoliday(String name, int year, int month, int day) {
        regularHolidays.add(new Holiday(name, LocalDate.of(year, month, day)));
    }

    private void addSpecialNonWorkingHoliday(String name, int year, int month, int day) {
        specialNonWorkingHolidays.add(new Holiday(name, LocalDate.of(year, month, day)));
    }

    public boolean isHoliday(LocalDate date) {
        if (date == null) {
            return false;
        }
        return isRegularHoliday(date) || isSpecialNonWorkingHoliday(date);
    }

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

    public double calculateHolidayPay(double basePay, double hoursWorked,
                                      boolean isRegularHoliday, boolean isRestDay,
                                      double overtimeHours, boolean isLate) {
        if (basePay < 0) {
            basePay = 0.0;
        }

        if (hoursWorked < 0) {
            hoursWorked = 0.0;
        }

        if (overtimeHours < 0) {
            overtimeHours = 0.0;
        }

        double holidayPay = 0.0;

        if (isRegularHoliday) {
            if (hoursWorked == 0) {
                holidayPay = basePay;
            } else {
                double regularHours = Math.min(hoursWorked, 8.0);
                holidayPay = regularHours * (basePay / 8) * 2;

                if (overtimeHours > 0 && !isLate) {
                    double hourlyRate = basePay / 8;
                    double overtimeRate = hourlyRate * 2 * 1.3;

                    if (!isLate) {
                        overtimeRate *= 1.25;
                    }

                    holidayPay += overtimeHours * overtimeRate;
                }

                if (isRestDay) {
                    holidayPay *= 1.3;
                }
            }
        } else {
            if (hoursWorked == 0) {
                holidayPay = 0;
            } else {
                double regularHours = Math.min(hoursWorked, 8.0);
                holidayPay = regularHours * (basePay / 8) * 1.3;

                if (overtimeHours > 0 && !isLate) {
                    double hourlyRate = basePay / 8;
                    double overtimeRate = hourlyRate * 1.3;

                    if (!isLate) {
                        overtimeRate *= 1.25;
                    }

                    holidayPay += overtimeHours * overtimeRate;
                }

                if (isRestDay) {
                    holidayPay *= 1.3;
                }
            }
        }

        return holidayPay;
    }

    public Map<LocalDate, String> getHolidaysInRange(LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, String> holidaysInRange = new HashMap<>();

        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            return holidaysInRange;
        }

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