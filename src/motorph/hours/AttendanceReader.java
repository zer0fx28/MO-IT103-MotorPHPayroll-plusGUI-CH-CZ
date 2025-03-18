// File: motorph/hours/AttendanceReader.java
package motorph.hours;

import motorph.employee.CSVReader;
import motorph.util.TimeConverter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;

/**
 * Reads and processes attendance data from CSV file
 *
 * This class handles loading attendance records from a CSV file, calculating
 * hours worked and overtime, and generating attendance summaries for employees.
 */
public class AttendanceReader {
    private final String attendanceFilePath;
    private final List<AttendanceRecord> attendanceRecords;
    private final WorkHoursCalculator hoursCalculator;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    /**
     * Load attendance data from CSV file
     *
     * @param attendanceFilePath Path to the attendance CSV file
     */
    public AttendanceReader(String attendanceFilePath) {
        this.attendanceFilePath = attendanceFilePath;
        this.attendanceRecords = new ArrayList<>();
        this.hoursCalculator = new WorkHoursCalculator();
        loadAttendance();
    }

    /**
     * Read attendance data from file
     */
    private void loadAttendance() {
        if (attendanceFilePath == null || attendanceFilePath.trim().isEmpty()) {
            System.out.println("Error: Attendance file path is null or empty");
            return;
        }

        List<String[]> attendanceData = new ArrayList<>();
        try {
            attendanceData = CSVReader.read(attendanceFilePath);
            System.out.println("Successfully read " + attendanceData.size() + " attendance records");
        } catch (Exception e) {
            System.out.println("Error reading attendance file: " + e.getMessage());
            return;
        }

        // Process each row into an attendance record
        int recordCount = 0;
        int skippedRecords = 0;

        for (String[] row : attendanceData) {
            if (row.length >= 6) {  // Make sure we have all needed columns
                try {
                    // Parse the date and times
                    LocalDate date = LocalDate.parse(row[3], DATE_FORMAT);
                    LocalTime timeIn = TimeConverter.parseUserTime(row[4]);
                    LocalTime timeOut = TimeConverter.parseUserTime(row[5]);

                    // Skip records with null times (likely invalid)
                    if (timeIn == null || timeOut == null) {
                        skippedRecords++;
                        continue;
                    }

                    // Skip records where timeout is before timein (likely error)
                    if (timeOut.isBefore(timeIn)) {
                        System.out.println("Warning: Time out before time in for employee " +
                                row[0] + " on " + row[3] + ". Skipping record.");
                        skippedRecords++;
                        continue;
                    }

                    // Create attendance record
                    AttendanceRecord record = new AttendanceRecord();
                    record.setEmployeeId(row[0]);
                    record.setLastName(row[1]);
                    record.setFirstName(row[2]);
                    record.setDate(date);
                    record.setTimeIn(timeIn);
                    record.setTimeOut(timeOut);

                    attendanceRecords.add(record);
                    recordCount++;
                } catch (Exception e) {
                    System.out.println("Error processing attendance record: " + e.getMessage());
                    skippedRecords++;
                }
            } else {
                System.out.println("Warning: Incomplete attendance record - expected 6 fields, got " + row.length);
                skippedRecords++;
            }
        }

        System.out.println("Processed " + recordCount + " attendance records successfully");
        System.out.println("Skipped " + skippedRecords + " invalid records");
    }

    /**
     * Get all attendance records for one employee
     *
     * @param employeeId ID of the employee
     * @return List of attendance records for the employee
     */
    public List<AttendanceRecord> getRecordsForEmployee(String employeeId) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            System.out.println("Warning: Empty employee ID provided");
            return new ArrayList<>();
        }

        List<AttendanceRecord> employeeRecords = new ArrayList<>();

        for (AttendanceRecord record : attendanceRecords) {
            if (record.getEmployeeId().equals(employeeId)) {
                employeeRecords.add(record);
            }
        }

        if (employeeRecords.isEmpty()) {
            System.out.println("No attendance records found for employee ID: " + employeeId);
        }

        return employeeRecords;
    }

    /**
     * Get daily attendance for an employee within date range
     *
     * @param employeeId Employee ID
     * @param startDate Start date of the range
     * @param endDate End date of the range
     * @return Map of dates to attendance data
     */
    public Map<LocalDate, Map<String, Object>> getDailyAttendanceForEmployee(
            String employeeId, LocalDate startDate, LocalDate endDate) {

        // Input validation
        if (employeeId == null || employeeId.trim().isEmpty()) {
            System.out.println("Warning: Empty employee ID provided");
            return new HashMap<>();
        }

        if (startDate == null || endDate == null) {
            System.out.println("Warning: Invalid date range provided");
            return new HashMap<>();
        }

        if (endDate.isBefore(startDate)) {
            System.out.println("Warning: End date is before start date");
            return new HashMap<>();
        }

        // Get all records for this employee
        List<AttendanceRecord> employeeRecords = getRecordsForEmployee(employeeId);

        // Create a map to store daily attendance: date -> details
        Map<LocalDate, Map<String, Object>> dailyAttendance = new TreeMap<>();

        // Process each record
        for (AttendanceRecord record : employeeRecords) {
            LocalDate recordDate = record.getDate();

            // Check if record is within date range
            if ((recordDate.isEqual(startDate) || recordDate.isAfter(startDate)) &&
                    (recordDate.isEqual(endDate) || recordDate.isBefore(endDate))) {

                // Check if employee is late or undertime
                boolean isLate = record.isLate();
                boolean isUndertime = record.isUndertime();
                double lateMinutes = isLate ? record.getLateMinutes() : 0;
                double undertimeMinutes = isUndertime ? record.getUndertimeMinutes() : 0;

                // Calculate hours worked with policy applied
                double hoursWorked = hoursCalculator.calculateHoursWorked(
                        record.getTimeIn(), record.getTimeOut(), isLate);

                // Late employees get no overtime
                double overtimeHours = 0.0;
                if (!isLate) {
                    // Only non-late employees can get overtime
                    overtimeHours = hoursCalculator.calculateOvertimeHours(record.getTimeOut(), isLate);
                    // Cap regular hours at 8
                    hoursWorked = Math.min(hoursWorked, 8.0);
                }

                // Create a map for this day's data
                Map<String, Object> dayData = new HashMap<>();
                dayData.put("timeIn", TimeConverter.formatToStandardTime(record.getTimeIn()));
                dayData.put("timeOut", TimeConverter.formatToStandardTime(record.getTimeOut()));
                dayData.put("hours", hoursWorked);
                dayData.put("lateMinutes", lateMinutes);
                dayData.put("undertimeMinutes", undertimeMinutes);
                dayData.put("overtimeHours", overtimeHours);
                dayData.put("isLate", isLate);
                dayData.put("isUndertime", isUndertime);

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
     * @param startDate Start date of the range
     * @param endDate End date of the range
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
     * @param startDate Start date of the range
     * @param endDate End date of the range
     * @return Map containing weekly records and daily logs
     */
    public Map<String, Object> getWeeklyAttendanceWithDailyLogs(
            String employeeId, LocalDate startDate, LocalDate endDate) {

        // Input validation
        if (employeeId == null || employeeId.trim().isEmpty()) {
            System.out.println("Warning: Empty employee ID provided");
            return new HashMap<>();
        }

        if (startDate == null || endDate == null) {
            System.out.println("Warning: Invalid date range provided");
            return new HashMap<>();
        }

        if (endDate.isBefore(startDate)) {
            System.out.println("Warning: End date is before start date");
            return new HashMap<>();
        }

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

            // Get week of year and year
            int weekOfYear = date.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
            int year = date.getYear();

            // Create a week label (e.g., "Week 1: 3/1-3/7")
            LocalDate weekStart = date.minusDays(date.getDayOfWeek().getValue() - 1);
            LocalDate weekEnd = weekStart.plusDays(6);

            String weekLabel = "Week " + weekOfYear + " (" +
                    formatShortDate(weekStart) + " - " + formatShortDate(weekEnd) + ")";

            // Get or create week data
            Map<String, Double> weekData = weeklyAttendance.getOrDefault(weekLabel, new HashMap<>());
            List<Map<String, Object>> weekLogs = dailyLogsByWeek.getOrDefault(weekLabel, new ArrayList<>());

            // Add this day's values to week totals
            double hoursWorked = (double) dayData.get("hours");
            double lateMinutes = (double) dayData.get("lateMinutes");
            double undertimeMinutes = (double) dayData.getOrDefault("undertimeMinutes", 0.0);
            double overtimeHours = (double) dayData.get("overtimeHours");

            weekData.put("hours", weekData.getOrDefault("hours", 0.0) + hoursWorked);
            weekData.put("lateMinutes", weekData.getOrDefault("lateMinutes", 0.0) + lateMinutes);
            weekData.put("undertimeMinutes", weekData.getOrDefault("undertimeMinutes", 0.0) + undertimeMinutes);
            weekData.put("overtimeHours", weekData.getOrDefault("overtimeHours", 0.0) + overtimeHours);

            // Check for late and undertime flags
            if ((boolean) dayData.get("isLate")) {
                weekData.put("isLateCount", weekData.getOrDefault("isLateCount", 0.0) + 1.0);
            }

            if ((boolean) dayData.get("isUndertime")) {
                weekData.put("isUndertimeCount", weekData.getOrDefault("isUndertimeCount", 0.0) + 1.0);
            }

            // Create a copy of the day data with the date included
            Map<String, Object> dayLog = new HashMap<>(dayData);
            dayLog.put("date", date);
            weekLogs.add(dayLog);

            // Update the maps
            weeklyAttendance.put(weekLabel, weekData);
            dailyLogsByWeek.put(weekLabel, weekLogs);
        }

        // Create combined result
        Map<String, Object> result = new HashMap<>();
        result.put("weeklyRecords", weeklyAttendance);
        result.put("dailyLogsByWeek", dailyLogsByWeek);

        return result;
    }

    /**
     * Format date as MM/dd for display
     *
     * @param date Date to format
     * @return Formatted date string
     */
    private String formatShortDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return (date.getMonthValue() + "/" + date.getDayOfMonth());
    }

    /**
     * Calculate total hours per employee
     *
     * @return Map of employee IDs to total hours worked
     */
    public Map<String, Double> calculateTotalHoursPerEmployee() {
        Map<String, Double> hoursPerEmployee = new HashMap<>();

        for (AttendanceRecord record : attendanceRecords) {
            String employeeId = record.getEmployeeId();

            // Apply policies
            boolean isLate = record.isLate();
            double hours = hoursCalculator.calculateHoursWorked(record.getTimeIn(), record.getTimeOut(), isLate);

            // Cap hours at 8.0
            hours = Math.min(hours, 8.0);

            // Add hours to total
            double currentTotal = hoursPerEmployee.getOrDefault(employeeId, 0.0);
            hoursPerEmployee.put(employeeId, currentTotal + hours);
        }

        return hoursPerEmployee;
    }

    /**
     * Calculate overtime details for all employees
     *
     * @return Map of employee IDs to overtime information
     */
    public Map<String, OvertimeInfo> calculateOvertimeDetails() {
        Map<String, OvertimeInfo> overtimeDetails = new HashMap<>();

        for (AttendanceRecord record : attendanceRecords) {
            String employeeId = record.getEmployeeId();

            // Check if employee is late or undertime
            boolean isLate = record.isLate();
            boolean isUndertime = record.isUndertime();
            double lateMinutes = isLate ? record.getLateMinutes() : 0;
            double undertimeMinutes = isUndertime ? record.getUndertimeMinutes() : 0;

            // Calculate hours with policy applied
            double totalHours = hoursCalculator.calculateHoursWorked(
                    record.getTimeIn(), record.getTimeOut(), isLate);

            // Apply policies
            double regularHours = 0.0;
            double overtimeHours = 0.0;

            if (!isLate) {
                // Regular employees get overtime after 8 hours
                regularHours = Math.min(totalHours, 8.0);
                overtimeHours = totalHours > 8.0 ? (totalHours - 8.0) : 0.0;
            } else {
                // Late employees get capped at 8 hours, no overtime
                regularHours = Math.min(totalHours, 8.0);
                overtimeHours = 0.0;
            }

            // Get or create overtime info for this employee
            OvertimeInfo info = overtimeDetails.getOrDefault(employeeId, new OvertimeInfo());

            // Update totals
            info.regularHours += regularHours;
            info.overtimeHours += overtimeHours;
            info.lateMinutes += lateMinutes;
            info.undertimeMinutes += undertimeMinutes;

            // If late or undertime on any day, mark accordingly
            if (isLate) {
                info.isLate = true;
            }
            if (isUndertime) {
                info.isUndertime = true;
            }

            // Update map
            overtimeDetails.put(employeeId, info);
        }

        return overtimeDetails;
    }

    /**
     * Class to store overtime information
     */
    public static class OvertimeInfo {
        public double regularHours = 0.0;
        public double overtimeHours = 0.0;
        public double lateMinutes = 0.0;
        public double undertimeMinutes = 0.0;
        public boolean isLate = false;
        public boolean isUndertime = false;
    }
}