// File: motorph/reports/WeeklyHoursReport.java
package motorph.reports;

import motorph.employee.Employee;
import motorph.employee.EmployeeDataReader;
import motorph.hours.AttendanceReader;
import motorph.hours.AttendanceRecord;
import motorph.hours.WorkHoursCalculator;
import motorph.process.PayPeriod;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Generates weekly and daily hours reports for employees within a pay period
 */
public class WeeklyHoursReport {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter DATE_DISPLAY_FORMAT = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    private final EmployeeDataReader employeeDataReader;
    private final AttendanceReader attendanceReader;
    private final WorkHoursCalculator hoursCalculator;

    /**
     * Constructor initializes with data sources
     * @param employeeFilePath Path to employee data CSV
     * @param attendanceFilePath Path to attendance data CSV
     */
    public WeeklyHoursReport(String employeeFilePath, String attendanceFilePath) {
        this.employeeDataReader = new EmployeeDataReader(employeeFilePath);
        this.attendanceReader = new AttendanceReader(attendanceFilePath);
        this.hoursCalculator = new WorkHoursCalculator();
    }

    /**
     * Generate a report of weekly hours for all employees in a pay period
     * @param payPeriod The pay period to report on
     * @return Map of employee IDs to their weekly hours data
     */
    public Map<String, Map<String, Double>> generateWeeklyHoursReport(PayPeriod payPeriod) {
        // Get all employees
        List<Employee> employees = employeeDataReader.getAllEmployees();

        // Get all weekly date ranges in the pay period
        List<LocalDate[]> weeklyRanges = payPeriod.getWeeklyDateRanges();

        // Create report data structure: employeeId -> weekId -> hours
        Map<String, Map<String, Double>> weeklyHoursReport = new HashMap<>();

        // Process each employee
        for (Employee employee : employees) {
            String employeeId = employee.getEmployeeId();

            // Get all attendance records for this employee
            List<AttendanceRecord> employeeRecords = attendanceReader.getRecordsForEmployee(employeeId);

            // Filter records to only those within the pay period
            List<AttendanceRecord> periodRecords = new ArrayList<>();
            for (AttendanceRecord record : employeeRecords) {
                if (payPeriod.isDateInPeriod(record.getDate())) {
                    periodRecords.add(record);
                }
            }

            // Group records by week
            Map<String, Double> weeklyHours = new HashMap<>();

            // Process each week range
            for (int i = 0; i < weeklyRanges.size(); i++) {
                LocalDate[] weekRange = weeklyRanges.get(i);
                LocalDate weekStart = weekRange[0];
                LocalDate weekEnd = weekRange[1];

                // Week identifier (e.g., "Week 1: Nov 1-7")
                String weekId = "Week " + (i + 1) + ": " +
                        weekStart.format(DateTimeFormatter.ofPattern("MMM d")) + "-" +
                        weekEnd.format(DateTimeFormatter.ofPattern("d"));

                // Calculate total hours for this week
                double totalHoursThisWeek = 0.0;

                for (AttendanceRecord record : periodRecords) {
                    LocalDate recordDate = record.getDate();

                    // Check if record is within this week
                    if (!recordDate.isBefore(weekStart) && !recordDate.isAfter(weekEnd)) {
                        // Calculate hours for this record
                        double hours = hoursCalculator.calculateHoursWorked(
                                record.getTimeIn(), record.getTimeOut());

                        totalHoursThisWeek += hours;
                    }
                }

                // Store the weekly total
                weeklyHours.put(weekId, totalHoursThisWeek);
            }

            // Add employee's weekly hours to the report
            weeklyHoursReport.put(employeeId, weeklyHours);
        }

        return weeklyHoursReport;
    }

    /**
     * Generate a report of daily hours for a specific employee in a pay period
     * @param employeeId Employee ID to report on
     * @param payPeriod The pay period to report on
     * @return Map of dates to hours worked
     */
    public Map<LocalDate, Double> generateDailyHoursReport(String employeeId, PayPeriod payPeriod) {
        // Get all attendance records for this employee
        List<AttendanceRecord> employeeRecords = attendanceReader.getRecordsForEmployee(employeeId);

        // Create sorted map to store daily hours
        Map<LocalDate, Double> dailyHours = new TreeMap<>();

        // Process each record
        for (AttendanceRecord record : employeeRecords) {
            LocalDate recordDate = record.getDate();

            // Check if record is within the pay period
            if (payPeriod.isDateInPeriod(recordDate)) {
                // Calculate hours for this record
                double hours = hoursCalculator.calculateHoursWorked(
                        record.getTimeIn(), record.getTimeOut());

                // Add to total for this date
                double currentTotal = dailyHours.getOrDefault(recordDate, 0.0);
                dailyHours.put(recordDate, currentTotal + hours);
            }
        }

        return dailyHours;
    }

    /**
     * Display the weekly hours report in a formatted way
     * @param weeklyHoursReport The report data to display
     */
    public void displayWeeklyHoursReport(Map<String, Map<String, Double>> weeklyHoursReport) {
        System.out.println("\n===== WEEKLY HOURS REPORT =====");

        // Get unique week IDs across all employees
        Set<String> allWeekIds = new LinkedHashSet<>();
        for (Map<String, Double> employeeWeeks : weeklyHoursReport.values()) {
            allWeekIds.addAll(employeeWeeks.keySet());
        }

        // Sort week IDs alphabetically 
        List<String> sortedWeekIds = new ArrayList<>(allWeekIds);
        Collections.sort(sortedWeekIds);

        // Build header row
        StringBuilder header = new StringBuilder();
        header.append(String.format("%-10s %-20s", "Emp ID", "Employee Name"));

        for (String weekId : sortedWeekIds) {
            header.append(String.format(" | %-15s", weekId));
        }

        header.append(" | TOTAL HOURS");
        System.out.println(header.toString());

        // Print separator line
        System.out.println("-".repeat(header.length()));

        // Print each employee's data
        for (String employeeId : weeklyHoursReport.keySet()) {
            Employee employee = employeeDataReader.getEmployee(employeeId);
            if (employee == null) continue;

            StringBuilder row = new StringBuilder();
            row.append(String.format("%-10s %-20s",
                    employeeId,
                    employee.getLastName() + ", " + employee.getFirstName()));

            Map<String, Double> employeeWeeks = weeklyHoursReport.get(employeeId);
            double totalHours = 0.0;

            for (String weekId : sortedWeekIds) {
                double hours = employeeWeeks.getOrDefault(weekId, 0.0);
                totalHours += hours;
                row.append(String.format(" | %15.2f", hours));
            }

            row.append(String.format(" | %11.2f", totalHours));
            System.out.println(row.toString());
        }

        System.out.println("-".repeat(header.length()));
    }

    /**
     * Display the daily hours report for a specific employee
     * @param employeeId Employee ID to display
     * @param dailyHours Daily hours data to display
     */
    public void displayDailyHoursReport(String employeeId, Map<LocalDate, Double> dailyHours) {
        Employee employee = employeeDataReader.getEmployee(employeeId);
        if (employee == null) {
            System.out.println("Employee not found: " + employeeId);
            return;
        }

        System.out.println("\n===== DAILY HOURS REPORT =====");
        System.out.println("Employee: " + employee.getFullName() + " (ID: " + employeeId + ")");
        System.out.println("Position: " + employee.getPosition());
        System.out.println("\nDate\t\t\tDay\t\tHours Worked");
        System.out.println("-".repeat(50));

        double totalHours = 0.0;

        for (Map.Entry<LocalDate, Double> entry : dailyHours.entrySet()) {
            LocalDate date = entry.getKey();
            double hours = entry.getValue();
            totalHours += hours;

            String dayOfWeek = date.getDayOfWeek().toString();
            dayOfWeek = dayOfWeek.charAt(0) + dayOfWeek.substring(1).toLowerCase();

            System.out.printf("%s\t%s\t\t%.2f\n",
                    date.format(DATE_DISPLAY_FORMAT),
                    dayOfWeek,
                    hours);
        }

        System.out.println("-".repeat(50));
        System.out.printf("Total Hours: %.2f\n", totalHours);
    }
}