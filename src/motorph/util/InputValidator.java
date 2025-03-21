// File: motorph/util/InputValidator.java
package motorph.util;

import java.time.LocalDate;
import java.time.Year;

/**
 * Utility class for validating user input
 * Provides methods to validate various types of input data
 */
public class InputValidator {

    // Constants for validation
    private static final double MAX_REASONABLE_HOURS = 16.0;
    private static final double MAX_REASONABLE_SALARY = 1000000.0;
    private static final int MIN_VALID_YEAR = 2020;
    private static final int MAX_VALID_YEAR = 2050;

    /**
     * Validate an employee ID
     *
     * @param employeeId Employee ID to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidEmployeeId(String employeeId) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            return false;
        }

        // Employee IDs should match the expected pattern
        return employeeId.matches("^[A-Za-z0-9-]+$");
    }

    /**
     * Validate a year
     *
     * @param year Year to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidYear(int year) {
        return year >= MIN_VALID_YEAR && year <= MAX_VALID_YEAR;
    }

    /**
     * Validate a month
     *
     * @param month Month to validate (1-12)
     * @return true if valid, false otherwise
     */
    public static boolean isValidMonth(int month) {
        return month >= 1 && month <= 12;
    }

    /**
     * Validate a day of month
     *
     * @param year Year
     * @param month Month (1-12)
     * @param day Day to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidDayOfMonth(int year, int month, int day) {
        if (day < 1) {
            return false;
        }

        return day <= Year.of(year).atMonth(month).lengthOfMonth();
    }

    /**
     * Validate a date
     *
     * @param date Date to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidDate(LocalDate date) {
        if (date == null) {
            return false;
        }

        return isValidYear(date.getYear()) &&
                isValidMonth(date.getMonthValue()) &&
                isValidDayOfMonth(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
    }

    /**
     * Validate a date range
     *
     * @param startDate Start date
     * @param endDate End date
     * @return true if valid, false otherwise
     */
    public static boolean isValidDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return false;
        }

        return isValidDate(startDate) &&
                isValidDate(endDate) &&
                !startDate.isAfter(endDate);
    }

    /**
     * Validate working hours
     *
     * @param hours Working hours to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidWorkHours(double hours) {
        return hours >= 0 && hours <= MAX_REASONABLE_HOURS;
    }

    /**
     * Validate overtime hours
     *
     * @param hours Overtime hours to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidOvertimeHours(double hours) {
        return hours >= 0 && hours <= MAX_REASONABLE_HOURS - 8.0;
    }

    /**
     * Validate salary amount
     *
     * @param salary Salary to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidSalary(double salary) {
        return salary >= 0 && salary <= MAX_REASONABLE_SALARY;
    }

    /**
     * Validate hourly rate
     *
     * @param rate Hourly rate to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidHourlyRate(double rate) {
        return rate >= 0 && rate <= 10000.0;
    }

    /**
     * Validate minutes (late or undertime)
     *
     * @param minutes Minutes to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidMinutes(double minutes) {
        return minutes >= 0 && minutes <= 480.0; // Max 8 hours (480 minutes)
    }

    /**
     * Validate a name (first name, last name)
     *
     * @param name Name to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        // Names should only contain letters, spaces, hyphens, and apostrophes
        return name.matches("^[A-Za-z\\s\\-']+$");
    }

    /**
     * Validate a menu choice
     *
     * @param choice Menu choice to validate
     * @param min Minimum valid choice
     * @param max Maximum valid choice
     * @return true if valid, false otherwise
     */
    public static boolean isValidMenuChoice(String choice, int min, int max) {
        try {
            int choiceNum = Integer.parseInt(choice.trim());
            return choiceNum >= min && choiceNum <= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validate a yes/no response
     *
     * @param response Response to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidYesNoResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            return false;
        }

        String normalized = response.trim().toUpperCase();
        return normalized.equals("Y") ||
                normalized.equals("YES") ||
                normalized.equals("N") ||
                normalized.equals("NO");
    }

    /**
     * Check if response is affirmative (Y/Yes)
     *
     * @param response Response to check
     * @return true if affirmative, false otherwise
     */
    public static boolean isAffirmative(String response) {
        if (response == null || response.trim().isEmpty()) {
            return false;
        }

        String normalized = response.trim().toUpperCase();
        return normalized.equals("Y") || normalized.equals("YES");
    }

    /**
     * Validate a phone number
     *
     * @param phoneNumber Phone number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }

        // Phone numbers should only contain digits, plus sign, hyphens, parentheses, and spaces
        return phoneNumber.matches("^[0-9\\+\\-\\(\\)\\s]+$");
    }

    /**
     * Validate an email address
     *
     * @param email Email to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        // Simple email validation
        return email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    /**
     * Validate a tax ID number (TIN)
     *
     * @param tin TIN to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidTIN(String tin) {
        if (tin == null || tin.trim().isEmpty()) {
            return false;
        }

        // TIN format: XXX-XXX-XXX or XXX-XXX-XXX-XXX
        return tin.matches("^[0-9]{3}-[0-9]{3}-[0-9]{3}(-[0-9]{3})?$");
    }

    /**
     * Validate an SSS number
     *
     * @param sssNo SSS number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidSSSNumber(String sssNo) {
        if (sssNo == null || sssNo.trim().isEmpty()) {
            return false;
        }

        // SSS format: XX-XXXXXXX-X
        return sssNo.matches("^[0-9]{2}-[0-9]{7}-[0-9]$");
    }

    /**
     * Validate a PhilHealth number
     *
     * @param philhealthNo PhilHealth number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPhilHealthNumber(String philhealthNo) {
        if (philhealthNo == null || philhealthNo.trim().isEmpty()) {
            return false;
        }

        // PhilHealth format: XX-XXXXXXXXX-X
        return philhealthNo.matches("^[0-9]{2}-[0-9]{9}-[0-9]$");
    }

    /**
     * Validate a Pag-IBIG number
     *
     * @param pagIbigNo Pag-IBIG number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPagIbigNumber(String pagIbigNo) {
        if (pagIbigNo == null || pagIbigNo.trim().isEmpty()) {
            return false;
        }

        // Pag-IBIG format: XXXX-XXXX-XXXX
        return pagIbigNo.matches("^[0-9]{4}-[0-9]{4}-[0-9]{4}$");
    }
}