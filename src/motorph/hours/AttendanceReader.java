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
 */
public class AttendanceReader {
    private final String attendanceFilePath;
    private final List<AttendanceRecord> attendanceRecords;
    private final WorkHoursCalculator hoursCalculator;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    /**
     * Load attendance data from CSV file
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
        List<String[]> attendanceData = new ArrayList<>();
        try {
            attendanceData = CSVReader.read(attendanceFilePath);
        } catch (Exception e) {
            System.out.println("Error reading attendance file: " + e.getMessage());
            return;
        }

        // Process each row into an attendance record
        for (String[] row : attendanceData) {
            if (row.length >= 6) {  // Make sure we have all needed columns
                try {
                    // Parse the date and times
                    LocalDate date = LocalDate.parse(row[3], DATE_FORMAT);
                    LocalTime timeIn = TimeConverter.parseUserTime(row[4]);
                    LocalTime timeOut = TimeConverter.parseUserTime(row[5]);

                    // Create attendance record
                    AttendanceRecord record = new AttendanceRecord();
                    record.setEmployeeId(row[0]);
                    record.setLastName(row[1]);
                    record.setFirstName(row[2]);
                    record.setDate(date);
                    record.setTimeIn(timeIn);
                    record.setTimeOut(timeOut);

                    attendanceRecords.add(record);
                } catch (Exception e) {
                    // Skip bad records
                }
            }
        }
    }

    /**
     * Get all attendance records for one employee
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
     */
    private String formatShortDate(LocalDate date) {
        return (date.getMonthValue() + "/" + date.getDayOfMonth());
    }

    /**
     * Calculate total hours per employee
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