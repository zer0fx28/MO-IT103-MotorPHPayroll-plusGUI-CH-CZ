// File: motorph/util/TimeConverter.java
package motorph.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for time format conversion and parsing
 */
public class TimeConverter {
    // Common time formats
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter STANDARD_TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a");

    // Pattern for simple time format "H:MM" or "HH:MM"
    private static final Pattern SIMPLE_TIME_PATTERN = Pattern.compile("^(\\d{1,2}):(\\d{2})$");

    /**
     * Parse time from user input in various formats
     *
     * @param timeStr Time string (HH:mm, H:MM, etc.)
     * @return LocalTime object or null if parsing fails
     */
    public static LocalTime parseUserTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return null;
        }

        String cleanTimeStr = timeStr.trim();

        // Try parsing with simple pattern first
        Matcher matcher = SIMPLE_TIME_PATTERN.matcher(cleanTimeStr);
        if (matcher.matches()) {
            try {
                int hours = Integer.parseInt(matcher.group(1));
                int minutes = Integer.parseInt(matcher.group(2));

                // Basic validation
                if (hours >= 0 && hours <= 23 && minutes >= 0 && minutes <= 59) {
                    return LocalTime.of(hours, minutes);
                }
            } catch (NumberFormatException e) {
                // Fall through to other parsing methods
            }
        }

        // Try with standard time format directly
        try {
            return LocalTime.parse(cleanTimeStr, TIME_FORMAT);
        } catch (DateTimeParseException e) {
            // Fall through to other methods
        }

        // Try with numeric time formats
        try {
            // Handle "HHMM" format (e.g., "0800")
            if (cleanTimeStr.matches("\\d{4}")) {
                int hours = Integer.parseInt(cleanTimeStr.substring(0, 2));
                int minutes = Integer.parseInt(cleanTimeStr.substring(2, 4));
                return LocalTime.of(hours, minutes);
            }

            // Handle "HMM" format (e.g., "800")
            if (cleanTimeStr.matches("\\d{3}")) {
                int hours = Integer.parseInt(cleanTimeStr.substring(0, 1));
                int minutes = Integer.parseInt(cleanTimeStr.substring(1, 3));
                return LocalTime.of(hours, minutes);
            }
        } catch (Exception ignored) {
            // Fall through to final attempt
        }

        // Try to parse in various formats as a last resort
        try {
            // Try with 12-hour format
            if (cleanTimeStr.toLowerCase().contains("am") || cleanTimeStr.toLowerCase().contains("pm")) {
                return LocalTime.parse(cleanTimeStr, DateTimeFormatter.ofPattern("h:mm a"));
            }

            // Try to extract hours and minutes with regex if all else fails
            Pattern anyTimePattern = Pattern.compile("(\\d{1,2})[:\\s]?(\\d{2})");
            Matcher anyMatcher = anyTimePattern.matcher(cleanTimeStr);
            if (anyMatcher.find()) {
                int hours = Integer.parseInt(anyMatcher.group(1));
                int minutes = Integer.parseInt(anyMatcher.group(2));

                // Basic validation
                if (hours >= 0 && hours <= 23 && minutes >= 0 && minutes <= 59) {
                    return LocalTime.of(hours, minutes);
                }
            }
        } catch (Exception e) {
            // Give up and return null
        }

        return null;
    }

    /**
     * Format time to standard format (HH:MM)
     *
     * @param time LocalTime to format
     * @return Formatted time string
     */
    public static String formatToStandardTime(LocalTime time) {
        if (time == null) {
            return "";
        }
        return time.format(TIME_FORMAT);
    }

    /**
     * Format time to 12-hour format (hh:mm AM/PM)
     *
     * @param time LocalTime to format
     * @return Formatted time string
     */
    public static String format12HourTime(LocalTime time) {
        if (time == null) {
            return "";
        }
        return time.format(STANDARD_TIME_FORMAT);
    }
}