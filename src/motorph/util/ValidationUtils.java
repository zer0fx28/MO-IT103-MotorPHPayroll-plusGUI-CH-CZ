// File: motorph/util/ValidationUtils.java
package motorph.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Common validation utilities for the payroll system
 *
 * This class provides reusable validation methods for various input types
 * including numbers, dates, times, and strings. It centralizes validation
 * logic to ensure consistent handling across the application.
 */
public class ValidationUtils {
    // Logger for this class
    private static final Logger LOGGER = Logger.getLogger(ValidationUtils.class.getName());

    // Date and time formatters
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Validate that a value is non-negative
     *
     * @param value The value to validate
     * @param fieldName Name of the field for error messages
     * @param defaultValue Default value to return if invalid
     * @return The original value if valid, or the default value if invalid
     */
    public static double validateNonNegative(double value, String fieldName, double defaultValue) {
        if (value < 0) {
            LOGGER.warning(String.format(
                    "Negative %s (%.2f) provided. Using default value: %.2f",
                    fieldName, value, defaultValue));
            return defaultValue;
        }
        return value;
    }

    /**
     * Validate the pay period type
     *
     * @param payPeriodType Pay period type to validate
     * @param validType1 First valid type
     * @param validType2 Second valid type
     * @param defaultType Default type to return if invalid
     * @return The original type if valid, or the default type if invalid
     */
    public static int validatePayPeriodType(int payPeriodType, int validType1, int validType2, int defaultType) {
        if (payPeriodType != validType1 && payPeriodType != validType2) {
            LOGGER.warning(String.format(
                    "Invalid pay period type: %d. Using default: %d",
                    payPeriodType, defaultType));
            return defaultType;
        }
        return payPeriodType;
    }

    /**
     * Validate a string is not null or empty
     *
     * @param value String to validate
     * @param fieldName Name of the field for error messages
     * @return true if string is valid, false otherwise
     */
    public static boolean validateString(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            LOGGER.warning(String.format("Empty or null %s provided", fieldName));
            return false;
        }
        return true;
    }

    /**
     * Validate a time range (start time before end time)
     *
     * @param startTime Start time
     * @param endTime End time
     * @return true if range is valid, false otherwise
     */
    public static boolean validateTimeRange(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null) {
            LOGGER.warning("Null time value in time range validation");
            return false;
        }

        if (endTime.isBefore(startTime)) {
            LOGGER.warning(String.format(
                    "Invalid time range: end time %s is before start time %s",
                    endTime, startTime));
            return false;
        }

        return true;
    }

    /**
     * Validate a date range (start date before or equal to end date)
     *
     * @param startDate Start date
     * @param endDate End date
     * @return true if range is valid, false otherwise
     */
    public static boolean validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            LOGGER.warning("Null date value in date range validation");
            return false;
        }

        if (endDate.isBefore(startDate)) {
            LOGGER.warning(String.format(
                    "Invalid date range: end date %s is before start date %s",
                    endDate, startDate));
            return false;
        }

        return true;
    }

    /**
     * Parse a date string with proper error handling
     *
     * @param dateStr Date string to parse
     * @return Parsed LocalDate or null if parsing fails
     */
    public static LocalDate parseDate(String dateStr) {
        if (!validateString(dateStr, "date string")) {
            return null;
        }

        try {
            return LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            LOGGER.warning(String.format(
                    "Error parsing date string: '%s'. Format should be MM/dd/yyyy. Error: %s",
                    dateStr, e.getMessage()));
            return null;
        }
    }

    /**
     * Parse a time string with proper error handling
     *
     * @param timeStr Time string to parse
     * @return Parsed LocalTime or null if parsing fails
     */
    public static LocalTime parseTime(String timeStr) {
        if (!validateString(timeStr, "time string")) {
            return null;
        }

        try {
            return LocalTime.parse(timeStr.trim(), TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            LOGGER.warning(String.format(
                    "Error parsing time string: '%s'. Format should be HH:mm. Error: %s",
                    timeStr, e.getMessage()));
            return null;
        }
    }

    /**
     * Check if two double values are equal within a tolerance
     * This helps avoid floating point precision issues
     *
     * @param a First value
     * @param b Second value
     * @param epsilon Tolerance for equality check
     * @return true if values are equal within tolerance
     */
    public static boolean doubleEquals(double a, double b, double epsilon) {
        return Math.abs(a - b) <= epsilon;
    }

    /**
     * Validate an employee ID format
     * Format expected: 5-digit number or alphanumeric with specific pattern
     *
     * @param employeeId Employee ID to validate
     * @return true if format is valid, false otherwise
     */
    public static boolean validateEmployeeId(String employeeId) {
        if (!validateString(employeeId, "employee ID")) {
            return false;
        }

        // Check for 5-digit numeric ID
        if (employeeId.matches("\\d{5}")) {
            return true;
        }

        // Check for alphanumeric format (e.g., EMP001)
        if (employeeId.matches("[A-Za-z]{3}\\d{3}")) {
            return true;
        }

        LOGGER.warning(String.format(
                "Invalid employee ID format: '%s'. Expected 5 digits or 3 letters followed by 3 digits",
                employeeId));
        return false;
    }

    /**
     * Validate a monetary amount (positive with max 2 decimal places)
     *
     * @param amount Amount to validate
     * @param fieldName Name of the field for error messages
     * @return true if amount is valid, false otherwise
     */
    public static boolean validateMonetaryAmount(double amount, String fieldName) {
        if (amount < 0) {
            LOGGER.warning(String.format(
                    "Negative %s (%.2f) provided. Monetary amounts must be positive",
                    fieldName, amount));
            return false;
        }

        // Check for max 2 decimal places
        double multiplied = amount * 100;
        if (Math.abs(multiplied - Math.round(multiplied)) > 0.00001) {
            LOGGER.warning(String.format(
                    "Invalid %s (%.5f) provided. Monetary amounts must have max 2 decimal places",
                    fieldName, amount));
            return false;
        }

        return true;
    }

    /**
     * Validate a percentage value (0-100 range)
     *
     * @param percentage Percentage to validate
     * @param fieldName Name of the field for error messages
     * @return true if percentage is valid, false otherwise
     */
    public static boolean validatePercentage(double percentage, String fieldName) {
        if (percentage < 0 || percentage > 100) {
            LOGGER.warning(String.format(
                    "Invalid %s (%.2f) provided. Percentage must be between 0 and 100",
                    fieldName, percentage));
            return false;
        }

        return true;
    }
}