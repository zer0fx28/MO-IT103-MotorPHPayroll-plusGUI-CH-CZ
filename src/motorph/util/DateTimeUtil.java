// File: motorph/util/DateTimeUtil.java
package motorph.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for all date and time operations
 * This centralizes all date/time parsing and formatting for the system
 */
public class DateTimeUtil {

    // Date formatters
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter DATE_FORMAT_DISPLAY = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
    private static final DateTimeFormatter DATE_FORMAT_SHORT = DateTimeFormatter.ofPattern("MM/dd");

    // Time formatters
    private static final DateTimeFormatter MILITARY_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter STANDARD_FORMAT = DateTimeFormatter.ofPattern("h:mm a");

    // Time patterns
    private static final Pattern FOUR_DIGIT_PATTERN = Pattern.compile("(\\d{2})(\\d{2})");
    private static final Pattern THREE_DIGIT_PATTERN = Pattern.compile("(\\d)(\\d{2})");
    private static final Pattern STANDARD_PATTERN = Pattern.compile("(\\d{1,2}):(\\d{2})\\s*(AM|PM|am|pm)?");

    /**
     * Parse a date string in MM/dd/yyyy format
     *
     * @param dateStr Date string in MM/dd/yyyy format
     * @return LocalDate object or null if parsing fails
     */
    public static LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            System.out.println("Error parsing date: " + dateStr);
            return null;
        }
    }

    /**
     * Format a date in standard display format (Month DD, YYYY)
     *
     * @param date Date to format
     * @return Formatted date string
     */
    public static String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DATE_FORMAT_DISPLAY);
    }

    /**
     * Format a date in MM/dd/yyyy format
     *
     * @param date Date to format
     * @return Formatted date string
     */
    public static String formatDateStandard(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DATE_FORMAT);
    }

    /**
     * Format a date in short format (MM/dd)
     *
     * @param date Date to format
     * @return Formatted date string
     */
    public static String formatDateShort(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DATE_FORMAT_SHORT);
    }

    /**
     * Parse a time string in various formats to LocalTime
     * Handles common formats found in attendance data
     *
     * @param timeStr Time string in various formats
     * @return LocalTime object or null if parsing fails
     */
    public static LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return null;
        }

        String cleanTime = timeStr.trim();

        try {
            // Try pattern matching approach first (most reliable)

            // Check for four-digit format (e.g., "0800", "1700")
            Matcher fourDigitMatcher = FOUR_DIGIT_PATTERN.matcher(cleanTime);
            if (fourDigitMatcher.matches()) {
                int hour = Integer.parseInt(fourDigitMatcher.group(1));
                int minute = Integer.parseInt(fourDigitMatcher.group(2));
                return LocalTime.of(hour, minute);
            }

            // Check for three-digit format (e.g., "800", "130")
            Matcher threeDigitMatcher = THREE_DIGIT_PATTERN.matcher(cleanTime);
            if (threeDigitMatcher.matches()) {
                int hour = Integer.parseInt(threeDigitMatcher.group(1));
                int minute = Integer.parseInt(threeDigitMatcher.group(2));
                return LocalTime.of(hour, minute);
            }

            // Check for standard time format with optional AM/PM (e.g., "8:00", "8:00 AM", "17:00")
            Matcher standardMatcher = STANDARD_PATTERN.matcher(cleanTime);
            if (standardMatcher.matches()) {
                int hour = Integer.parseInt(standardMatcher.group(1));
                int minute = Integer.parseInt(standardMatcher.group(2));
                String amPm = standardMatcher.group(3);

                // Adjust for 12-hour format with AM/PM
                if (amPm != null) {
                    if ((amPm.equalsIgnoreCase("PM")) && hour < 12) {
                        hour += 12;
                    } else if ((amPm.equalsIgnoreCase("AM")) && hour == 12) {
                        hour = 0;
                    }
                } else {
                    // No AM/PM indicator - use common sense for work hours
                    // If it's 1:00-7:59 with no AM/PM, assume PM during work hours
                    if (hour >= 1 && hour <= 7) {
                        hour += 12;
                    }
                }

                return LocalTime.of(hour, minute);
            }

            // Fallback to standard parsers if pattern matching fails

            // Try with standard AM/PM format
            if (cleanTime.toUpperCase().contains("AM") || cleanTime.toUpperCase().contains("PM")) {
                try {
                    return LocalTime.parse(cleanTime.toUpperCase(), STANDARD_FORMAT);
                } catch (Exception e) {
                    // Try next method
                }
            }

            // Try military time format
            try {
                return LocalTime.parse(cleanTime, MILITARY_FORMAT);
            } catch (Exception e) {
                // Try next method
            }

            // Last resort: try default parsing
            return LocalTime.parse(cleanTime);

        } catch (Exception e) {
            System.out.println("Error parsing time: " + timeStr);
            return null;
        }
    }

    /**
     * Format time to standard format (e.g., 5:00 PM)
     *
     * @param time Time to format
     * @return Formatted time string
     */
    public static String formatTimeStandard(LocalTime time) {
        if (time == null) {
            return "";
        }
        return time.format(STANDARD_FORMAT);
    }

    /**
     * Format time to military format (e.g., 17:00)
     *
     * @param time Time to format
     * @return Formatted time string
     */
    public static String formatTimeMilitary(LocalTime time) {
        if (time == null) {
            return "";
        }
        return time.format(MILITARY_FORMAT);
    }

    /**
     * Check if a date falls within a date range
     *
     * @param date Date to check
     * @param startDate Start of date range
     * @param endDate End of date range
     * @return true if date is within the range, false otherwise
     */
    public static boolean isDateInRange(LocalDate date, LocalDate startDate, LocalDate endDate) {
        if (date == null || startDate == null || endDate == null) {
            return false;
        }
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * Calculate the number of days between two dates (inclusive)
     *
     * @param startDate Start date
     * @param endDate End date
     * @return Number of days in the range
     */
    public static int daysBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return (int) (endDate.toEpochDay() - startDate.toEpochDay() + 1);
    }

    /**
     * Validate a date string
     *
     * @param dateStr Date string to validate
     * @return true if the date is valid, false otherwise
     */
    public static boolean isValidDate(String dateStr) {
        try {
            LocalDate.parse(dateStr, DATE_FORMAT);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Validate a time string
     *
     * @param timeStr Time string to validate
     * @return true if the time is valid, false otherwise
     */
    public static boolean isValidTime(String timeStr) {
        return parseTime(timeStr) != null;
    }
}