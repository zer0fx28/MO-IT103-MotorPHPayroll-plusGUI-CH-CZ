// File: motorph/util/TimeConverter.java
package motorph.util;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles time conversions between different formats
 *
 * This utility class provides methods to parse various time formats commonly found
 * in employee attendance records and convert them to standardized formats.
 * It handles multiple input formats including:
 * - Four-digit format (e.g., "0800", "1700")
 * - Three-digit format (e.g., "800", "130")
 * - Standard time format with optional AM/PM (e.g., "8:00", "8:00 AM", "17:00")
 */
public class TimeConverter {

    // Time format constants
    private static final DateTimeFormatter MILITARY_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter STANDARD_FORMAT = DateTimeFormatter.ofPattern("h:mm a");

    // Common patterns for time formats in attendance data
    private static final Pattern FOUR_DIGIT_PATTERN = Pattern.compile("(\\d{2})(\\d{2})");
    private static final Pattern THREE_DIGIT_PATTERN = Pattern.compile("(\\d)(\\d{2})");
    private static final Pattern STANDARD_PATTERN = Pattern.compile("(\\d{1,2}):(\\d{2})\\s*(AM|PM|am|pm)?");

    /**
     * Convert time string to LocalTime
     * Handles common formats found in attendance data
     *
     * @param timeStr Time string to parse (can be in various formats)
     * @return LocalTime object representing the parsed time, or null if parsing fails
     */
    public static LocalTime parseUserTime(String timeStr) {
        // Handle null or empty inputs
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

                // Validate hour and minute values
                if (hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59) {
                    return LocalTime.of(hour, minute);
                } else {
                    System.out.println("Invalid time values in '" + timeStr + "': hour=" + hour + ", minute=" + minute);
                    return null;
                }
            }

            // Check for three-digit format (e.g., "800", "130")
            Matcher threeDigitMatcher = THREE_DIGIT_PATTERN.matcher(cleanTime);
            if (threeDigitMatcher.matches()) {
                int hour = Integer.parseInt(threeDigitMatcher.group(1));
                int minute = Integer.parseInt(threeDigitMatcher.group(2));

                // Validate hour and minute values
                if (hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59) {
                    return LocalTime.of(hour, minute);
                } else {
                    System.out.println("Invalid time values in '" + timeStr + "': hour=" + hour + ", minute=" + minute);
                    return null;
                }
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

                // Validate hour and minute values
                if (hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59) {
                    return LocalTime.of(hour, minute);
                } else {
                    System.out.println("Invalid time values in '" + timeStr + "': hour=" + hour + ", minute=" + minute);
                    return null;
                }
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
            System.out.println("Error parsing time: " + timeStr + " - " + e.getMessage());
            return null;
        }
    }

    /**
     * Format time to standard format (e.g., 5:00 PM)
     *
     * @param time LocalTime object to format
     * @return Formatted time string in standard format, or empty string if time is null
     */
    public static String formatToStandardTime(LocalTime time) {
        if (time == null) {
            return "";
        }
        return time.format(STANDARD_FORMAT);
    }

    /**
     * Format time to military format (e.g., 17:00)
     *
     * @param time LocalTime object to format
     * @return Formatted time string in military format, or empty string if time is null
     */
    public static String formatToMilitaryTime(LocalTime time) {
        if (time == null) {
            return "";
        }
        return time.format(MILITARY_FORMAT);
    }
}