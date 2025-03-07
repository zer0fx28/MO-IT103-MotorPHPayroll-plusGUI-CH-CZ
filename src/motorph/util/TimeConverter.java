// File: motorph/util/TimeConverter.java
package motorph.util;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utility class for handling time conversions between standard and military formats
 */
public class TimeConverter {

    private static final DateTimeFormatter MILITARY_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter STANDARD_FORMAT = DateTimeFormatter.ofPattern("h:mm a");

    /**
     * Convert user input time to LocalTime object
     * Intelligently handles both formats:
     * - Simple hour format (8:00 → 8:00 AM, 5:00 → 5:00 PM)
     * - Standard time with AM/PM (8:00 AM, 5:00 PM)
     * - Military time (08:00, 17:00)
     *
     * Assumes times between 0:00-11:59 are AM and 12:00-23:59 are PM if no AM/PM specified
     * For simple times like "5:00", assumes PM if hour is between 1:00-7:59
     *
     * @param timeStr Time as string input by user
     * @return LocalTime object
     */
    public static LocalTime parseUserTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return null;
        }

        String normalizedTime = timeStr.trim().toUpperCase();

        try {
            // First check if time already has AM/PM specified
            if (normalizedTime.endsWith("AM") || normalizedTime.endsWith("PM")) {
                return LocalTime.parse(normalizedTime, STANDARD_FORMAT);
            }

            // Try parsing as military time
            try {
                return LocalTime.parse(normalizedTime, MILITARY_FORMAT);
            } catch (DateTimeParseException e) {
                // Continue to other formats
            }

            // Simple hour format without AM/PM
            // Add AM or PM based on typical work hours
            String[] parts = normalizedTime.split(":");
            if (parts.length == 2) {
                int hour = Integer.parseInt(parts[0]);

                // Typical work hour assumptions:
                // - 8:00 to 11:59 → AM
                // - 12:00 to 12:59 → PM (noon)
                // - 1:00 to 7:59 → PM (afternoon/evening)
                // - 0:00 to 7:59 → AM (early morning)

                if (hour >= 0 && hour < 8) {
                    // Early morning hours (0:00-7:59) → AM
                    return LocalTime.parse(normalizedTime + " AM", STANDARD_FORMAT);
                } else if (hour >= 8 && hour < 12) {
                    // Morning work hours (8:00-11:59) → AM
                    return LocalTime.parse(normalizedTime + " AM", STANDARD_FORMAT);
                } else if (hour == 12) {
                    // Noon (12:00-12:59) → PM
                    return LocalTime.parse(normalizedTime + " PM", STANDARD_FORMAT);
                } else if (hour >= 1 && hour <= 7) {
                    // Afternoon/evening (1:00-7:59) → PM
                    return LocalTime.parse(normalizedTime + " PM", STANDARD_FORMAT);
                }
            }

            // If none of the above worked, try direct military format
            return LocalTime.parse(normalizedTime, MILITARY_FORMAT);

        } catch (DateTimeParseException e) {
            System.out.println("Error parsing time: " + timeStr + " - " + e.getMessage());
            return null;
        }
    }

    /**
     * Format LocalTime to standard time (e.g., 5:00 PM)
     */
    public static String formatToStandardTime(LocalTime time) {
        if (time == null) {
            return "";
        }
        return time.format(STANDARD_FORMAT);
    }

    /**
     * Format LocalTime to military time (e.g., 17:00)
     */
    public static String formatToMilitaryTime(LocalTime time) {
        if (time == null) {
            return "";
        }
        return time.format(MILITARY_FORMAT);
    }
}