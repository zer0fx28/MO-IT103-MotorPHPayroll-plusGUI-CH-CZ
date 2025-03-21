package motorph.hours;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Formats attendance details for display
 * Handles the new table format with proper calculations
 */
public class AttendanceFormatter {
    // Work schedule constants
    private static final LocalTime STANDARD_START_TIME = LocalTime.of(8, 0); // 8:00 AM
    private static final LocalTime GRACE_PERIOD_END = LocalTime.of(8, 10);   // 8:10 AM (10-minute grace period)
    private static final LocalTime STANDARD_END_TIME = LocalTime.of(17, 0);  // 5:00 PM

    // Date/time formatters
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a");

    /**
     * Display attendance details with the new format
     *
     * @param attendanceRecords Map of dates to attendance details
     * @return Formatted attendance table as a string
     */
    public static String formatAttendanceTable(Map<LocalDate, Map<String, Object>> attendanceRecords) {
        StringBuilder sb = new StringBuilder();

        // Sort records by date
        Map<LocalDate, Map<String, Object>> sortedRecords = new TreeMap<>(attendanceRecords);

        // Display header
        sb.append("--- ATTENDANCE DETAILS ---\n");
        sb.append(String.format("%-10s %-5s %-8s %-8s %-5s %-5s %-5s %-5s\n",
                "Date", "Hrs", "Time In", "Time Out", "Late", "UT", "UA", "LV"));
        sb.append("----------------------------------------------------------\n");

        // Track totals
        double totalHours = 0;
        double totalLateMinutes = 0;
        double totalUndertimeMinutes = 0;
        int totalUnpaidAbsences = 0;
        int totalLeaves = 0;

        // Process each record
        for (Map.Entry<LocalDate, Map<String, Object>> entry : sortedRecords.entrySet()) {
            LocalDate date = entry.getKey();
            Map<String, Object> dayData = entry.getValue();

            // Get time values
            String timeInStr = (String) dayData.get("timeIn");
            String timeOutStr = (String) dayData.get("timeOut");
            LocalTime timeIn = parseTime(timeInStr);
            LocalTime timeOut = parseTime(timeOutStr);

            // Calculate values based on the new requirements
            double lateMinutes = calculateLateMinutes(timeIn);
            double undertimeMinutes = calculateUndertimeMinutes(timeOut);

            // Calculate hours worked with proper capping for late employees
            double hoursWorked = calculateHoursWorked(timeIn, timeOut, lateMinutes > 0);

            // Check for unpaid absence and leave (placeholders - no actual data in the original)
            boolean hasUnpaidAbsence = dayData.containsKey("isUnpaidAbsence") ?
                    (boolean) dayData.get("isUnpaidAbsence") : false;
            boolean hasLeave = dayData.containsKey("hasLeave") ?
                    (boolean) dayData.get("hasLeave") : false;

            // Format values for display
            String dateStr = formatDate(date);
            String hoursStr = String.format("%.2f", hoursWorked);
            String lateStr = lateMinutes > 0 ? (int)lateMinutes + "m" : "-";
            String undertimeStr = undertimeMinutes > 0 ? (int)undertimeMinutes + "m" : "-";
            String unpaidAbsenceStr = hasUnpaidAbsence ? "X" : "-";
            String leaveStr = hasLeave ? "X" : "-";

            // Format display time in and time out (capped at 5:00 PM for late employees)
            String displayTimeIn = formatTime(timeIn);
            String displayTimeOut = lateMinutes > 0 ?
                    formatTime(STANDARD_END_TIME) : formatTime(timeOut);

            // Add to table
            sb.append(String.format("%-10s %-5s %-8s %-8s %-5s %-5s %-5s %-5s\n",
                    dateStr, hoursStr, displayTimeIn, displayTimeOut,
                    lateStr, undertimeStr, unpaidAbsenceStr, leaveStr));

            // Update totals
            totalHours += hoursWorked;
            totalLateMinutes += lateMinutes;
            totalUndertimeMinutes += undertimeMinutes;
            if (hasUnpaidAbsence) totalUnpaidAbsences++;
            if (hasLeave) totalLeaves++;
        }

        // Display totals
        sb.append("----------------------------------------------------------\n");
        sb.append(String.format("%-10s %-5.2f %-17s %-5.0fm %-5.0fm %-5d %-5d\n",
                "TOTALS:", totalHours, "", totalLateMinutes, totalUndertimeMinutes,
                totalUnpaidAbsences, totalLeaves));

        // Add legends
        sb.append("\nLEGENDS:\n");
        sb.append("Hrs = Hours worked\n");
        sb.append("Late = Late minutes (after 8:10 AM grace period)\n");
        sb.append("UT = Undertime minutes (left before 5:00 PM)\n");
        sb.append("UA = Unapproved Absence\n");
        sb.append("LV = Leave\n");

        return sb.toString();
    }

    /**
     * Calculate minutes late (after 8:10 AM)
     *
     * @param timeIn Clock-in time
     * @return Minutes late (0 if not late)
     */
    private static double calculateLateMinutes(LocalTime timeIn) {
        if (timeIn == null || !timeIn.isAfter(GRACE_PERIOD_END)) {
            return 0.0;
        }

        Duration lateBy = Duration.between(GRACE_PERIOD_END, timeIn);
        return lateBy.toMinutes();
    }

    /**
     * Calculate undertime minutes (left before 5:00 PM)
     *
     * @param timeOut Clock-out time
     * @return Minutes undertime (0 if not undertime)
     */
    private static double calculateUndertimeMinutes(LocalTime timeOut) {
        if (timeOut == null || !timeOut.isBefore(STANDARD_END_TIME)) {
            return 0.0;
        }

        Duration undertimeBy = Duration.between(timeOut, STANDARD_END_TIME);
        return undertimeBy.toMinutes();
    }

    /**
     * Calculate hours worked with proper business rules
     *
     * @param timeIn Clock-in time
     * @param timeOut Clock-out time
     * @param isLate Whether employee was late
     * @return Hours worked
     */
    private static double calculateHoursWorked(LocalTime timeIn, LocalTime timeOut, boolean isLate) {
        if (timeIn == null || timeOut == null) {
            return 0.0;
        }

        // For late employees, cap time out at 5:00 PM
        LocalTime effectiveTimeOut = isLate ?
                (timeOut.isAfter(STANDARD_END_TIME) ? STANDARD_END_TIME : timeOut) :
                timeOut;

        Duration workDuration = Duration.between(timeIn, effectiveTimeOut);
        double hours = workDuration.toMinutes() / 60.0;

        // Round to 2 decimal places
        return Math.round(hours * 100) / 100.0;
    }

    /**
     * Parse time from string representation
     */
    private static LocalTime parseTime(String timeStr) {
        try {
            return LocalTime.parse(timeStr, TIME_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Format time to string representation
     */
    private static String formatTime(LocalTime time) {
        if (time == null) {
            return "";
        }
        return time.format(TIME_FORMATTER);
    }

    /**
     * Format date to string representation
     */
    private static String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DATE_FORMATTER);
    }
}