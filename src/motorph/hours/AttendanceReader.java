// File: motorph/hours/AttendanceReader.java
package motorph.hours;

import motorph.employee.CSVReader;
import motorph.util.DateTimeUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;

/**
 * Reads and processes attendance data from CSV file
 * Handles attendance record retrieval and calculations
 */
public class AttendanceReader {
    // File path to attendance data
    private final String attendanceFilePath;

    // List of all attendance records
    private final List<AttendanceRecord> attendanceRecords;

    // Date formatter for consistent formatting
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    /**
     * Create a new AttendanceReader and load attendance data
     *
     * @param attendanceFilePath Path to the attendance CSV file
     */
    public AttendanceReader(String attendanceFilePath) {
        this.attendanceFilePath = attendanceFilePath;
        this.attendanceRecords = new ArrayList<>();
        loadAttendance();
    }

    /**
     * Load attendance data from CSV file
     */
    private void loadAttendance() {
        try {
            // Read CSV data
            List<String[]> attendanceData = CSVReader.read(attendanceFilePath);

            // Process each row
            for (String[] row : attendanceData) {
                if (row.length >= 6) {  // Make sure we have all needed columns
                    try {
                        // Create attendance record
                        AttendanceRecord record = new AttendanceRecord(row);

                        // Add to list if valid
                        if (record.getDate() != null &&
                                record.getTimeIn() != null &&
                                record.getTimeOut() != null) {
                            attendanceRecords.add(record);
                        }
                    } catch (Exception e) {
                        // Skip bad records
                        System.out.println("Error processing attendance record: " + e.getMessage());
                    }
                }
            }

            System.out.println("Loaded " + attendanceRecords.size() + " attendance records.");
        } catch (IOException e) {
            System.out.println("Error reading attendance file: " + e.getMessage());
        }
    }

    /**
     * Get all attendance records for one employee
     *
     * @param employeeId Employee ID to search for
     * @return List of attendance records for the employee
     */
    public List<AttendanceRecord> getRecordsForEmployee(String employeeId) {
        List<AttendanceRecord> employeeRecords = new ArrayList<>();

        for (AttendanceRecord record : attendanceRecords) {
            if (record.getEmployeeId().equals(employeeId)) {
                employeeRecords.add(record);
            }
        }

        return employeeRecords;
    }

    /**
     * Get daily attendance for an employee within date range
     *
     * @param employeeId Employee ID
     * @param startDate Start date of range
     * @param endDate End date of range
     * @return Map of dates to attendance details
     */
    public Map<LocalDate, Map<String, Object>> getDailyAttendanceForEmployee(
            String employeeId, LocalDate startDate, LocalDate endDate) {

        // Get all records for this employee
        List<AttendanceRecord> employeeRecords = getRecordsForEmployee(employeeId);

        // Create a map to store daily attendance: date -> details
        Map<LocalDate, Map<String, Object>> dailyAttendance = new TreeMap<>();

        // Process each record
        for (AttendanceRecord record : employeeRecords) {
            LocalDate recordDate = record.getDate();

            // Check if record is within date range
            if (DateTimeUtil.isDateInRange(recordDate, startDate, endDate)) {
                // Create a map for this day's data
                Map<String, Object> dayData = new HashMap<>();
                dayData.put("timeIn", record.getFormattedTimeIn());
                dayData.put("timeOut", record.getFormattedTimeOut());
                dayData.put("hours", record.getRegularHoursWorked());
                dayData.put("lateMinutes", record.getLateMinutes());
                dayData.put("undertimeMinutes", record.getUndertimeMinutes());
                dayData.put("overtimeHours", record.getOvertimeHours());
                dayData.put("isLate", record.isLate());
                dayData.put("isUndertime", record.isUndertime());

                // Add to daily attendance map
                dailyAttendance.put(recordDate, dayData);
            }
        }

        return dailyAttendance;
    }

    /**
     * Get weekly attendance summary for an employee
     *
     * @param employeeId Employee ID
     * @param startDate Start date of range
     * @param endDate End date of range
     * @return Map of week labels to attendance summaries
     */
    public Map<String, Map<String, Double>> getWeeklyAttendanceForEmployee(
            String employeeId, LocalDate startDate, LocalDate endDate) {

        // Get weekly attendance with daily logs
        Map<String, Object> attendanceData = getWeeklyAttendanceWithDailyLogs(employeeId, startDate, endDate);

        @SuppressWarnings("unchecked")
        Map<String, Map<String, Double>> weeklyAttendance =
                (Map<String, Map<String, Double>>) attendanceData.get("weeklyRecords");

        return weeklyAttendance;
    }

    /**
     * Get weekly attendance with daily logs for an employee
     *
     * @param employeeId Employee ID
     * @param startDate Start date of range
     * @param endDate End date of range
     * @return Map containing weekly summaries and daily logs
     */
    public Map<String, Object> getWeeklyAttendanceWithDailyLogs(
            String employeeId, LocalDate startDate, LocalDate endDate) {

        // Get daily attendance records
        Map<LocalDate, Map<String, Object>> dailyAttendance =
                getDailyAttendanceForEmployee(employeeId, startDate, endDate);

        // Create maps to store weekly totals and daily logs by week
        Map<String, Map<String, Double>> weeklyAttendance = new LinkedHashMap<>();
        Map<String, List<Map<String, Object>>> dailyLogsByWeek = new LinkedHashMap<>();

        // Group by week
        for (Map.Entry<LocalDate, Map<String, Object>> entry : dailyAttendance.entrySet()) {
            LocalDate date = entry.getKey();
            Map<String, Object> dayData = entry.getValue();

            // Get week of year
            int weekOfYear = date.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());

            // Create week label
            LocalDate weekStart = date.minusDays(date.getDayOfWeek().getValue() - 1);
            LocalDate weekEnd = weekStart.plusDays(6);
            String weekLabel = "Week " + weekOfYear + " (" +
                    DateTimeUtil.formatDateShort(weekStart) + " - " +
                    DateTimeUtil.formatDateShort(weekEnd) + ")";

            // Get or create week data
            Map<String, Double> weekData = weeklyAttendance.getOrDefault(weekLabel, new HashMap<>());
            List<Map<String, Object>> weekLogs = dailyLogsByWeek.getOrDefault(weekLabel, new ArrayList<>());

            // Add this day's values to week totals
            double hoursWorked = (double) dayData.get("hours");
            double lateMinutes = (double) dayData.get("lateMinutes");
            double undertimeMinutes = (double) dayData.getOrDefault("undertimeMinutes", 0.0);
            double overtimeHours = (double) dayData.get("overtimeHours");

            // Update totals
            weekData.put("hours", weekData.getOrDefault("hours", 0.0) + hoursWorked);
            weekData.put("lateMinutes", weekData.getOrDefault("lateMinutes", 0.0) + lateMinutes);
            weekData.put("undertimeMinutes", weekData.getOrDefault("undertimeMinutes", 0.0) + undertimeMinutes);
            weekData.put("overtimeHours", weekData.getOrDefault("overtimeHours", 0.0) + overtimeHours);

            // Create daily log with date
            Map<String, Object> dayLog = new HashMap<>(dayData);
            dayLog.put("date", date);
            weekLogs.add(dayLog);

            // Update maps
            weeklyAttendance.put(weekLabel, weekData);
            dailyLogsByWeek.put(weekLabel, weekLogs);
        }

        // Create result
        Map<String, Object> result = new HashMap<>();
        result.put("weeklyRecords", weeklyAttendance);
        result.put("dailyLogsByWeek", dailyLogsByWeek);

        return result;
    }

    /**
     * Calculate attendance summary for a date range
     *
     * @param employeeId Employee ID
     * @param startDate Start date of range
     * @param endDate End date of range
     * @return Map with attendance summary
     */
    public Map<String, Object> getAttendanceSummary(
            String employeeId, LocalDate startDate, LocalDate endDate) {

        // Get daily attendance records
        Map<LocalDate, Map<String, Object>> dailyAttendance =
                getDailyAttendanceForEmployee(employeeId, startDate, endDate);

        // Calculate totals
        double totalHours = 0;
        double totalOvertimeHours = 0;
        double totalLateMinutes = 0;
        double totalUndertimeMinutes = 0;
        boolean isLateAnyDay = false;
        boolean hasUnpaidAbsences = false;
        int unpaidAbsenceCount = 0;

        for (Map<String, Object> dayData : dailyAttendance.values()) {
            // Sum up totals
            totalHours += (double) dayData.get("hours");
            totalOvertimeHours += (double) dayData.get("overtimeHours");
            totalLateMinutes += (double) dayData.get("lateMinutes");
            totalUndertimeMinutes += (double) dayData.getOrDefault("undertimeMinutes", 0.0);

            // Check flags
            if ((boolean) dayData.get("isLate")) {
                isLateAnyDay = true;
            }

            // Check for unpaid absences (simplified)
            String absenceType = (String) dayData.getOrDefault("absenceType", "");
            boolean isUnpaidAbsence = false;

            if (dayData.containsKey("isUnpaidAbsence")) {
                isUnpaidAbsence = (boolean) dayData.get("isUnpaidAbsence");
            } else if (absenceType != null && !absenceType.isEmpty()) {
                // For backward compatibility
                String type = absenceType.toLowerCase();
                isUnpaidAbsence = type.contains("unpaid") ||
                        type.contains("unauthoriz") ||
                        type.contains("unapproved");
            }

            if (isUnpaidAbsence) {
                hasUnpaidAbsences = true;
                unpaidAbsenceCount++;
            }
        }

        // Create summary
        Map<String, Object> summary = new HashMap<>();
        summary.put("hours", totalHours);
        summary.put("overtimeHours", totalOvertimeHours);
        summary.put("lateMinutes", totalLateMinutes);
        summary.put("undertimeMinutes", totalUndertimeMinutes);
        summary.put("isLateAnyDay", isLateAnyDay);
        summary.put("hasUnpaidAbsences", hasUnpaidAbsences);
        summary.put("unpaidAbsenceCount", unpaidAbsenceCount);
        summary.put("recordCount", dailyAttendance.size());

        return summary;
    }
}