// File: motorph/reports/WeeklyHoursReport.java
package motorph.reports;

import motorph.employee.Employee;
import motorph.employee.EmployeeDataReader;
import motorph.hours.AttendanceReader;
import motorph.hours.AttendanceRecord;
import motorph.hours.WorkHoursCalculator;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates weekly hour reports for employees
 *
 * This class provides functionality to generate detailed reports on
 * employee work hours, overtime, and lateness on a weekly basis.
 */
public class WeeklyHoursReport {
    private final EmployeeDataReader employeeDataReader;
    private final AttendanceReader attendanceReader;
    private final WorkHoursCalculator hoursCalculator;
    private final DateTimeFormatter dateFormatter;

    /**
     * Create a new report generator
     *
     * @param employeeFilePath Path to employee data CSV
     * @param attendanceFilePath Path to attendance data CSV
     */
    public WeeklyHoursReport(String employeeFilePath, String attendanceFilePath) {
        this.employeeDataReader = new EmployeeDataReader(employeeFilePath);
        this.attendanceReader = new AttendanceReader(attendanceFilePath);
        this.hoursCalculator = new WorkHoursCalculator();
        this.dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    }

    /**
     * Generate report for a specific employee
     *
     * @param employeeId ID of the employee
     * @param startDate Start date of the report period
     * @param endDate End date of the report period
     */
    public void generateReportForEmployee(String employeeId, LocalDate startDate, LocalDate endDate) {
        // Validate inputs
        if (employeeId == null || employeeId.trim().isEmpty()) {
            System.out.println("Error: Employee ID cannot be empty");
            return;
        }

        if (startDate == null || endDate == null) {
            System.out.println("Error: Invalid date range");
            return;
        }

        if (endDate.isBefore(startDate)) {
            System.out.println("Error: End date cannot be before start date");
            return;
        }

        // Get employee data
        Employee employee = employeeDataReader.findEmployee(employeeId);
        if (employee == null) {
            System.out.println("Employee not found: " + employeeId);
            return;
        }

        // Get attendance records
        List<AttendanceRecord> records = attendanceReader.getRecordsForEmployee(employeeId);
        if (records.isEmpty()) {
            System.out.println("No attendance records found for employee: " + employeeId);
            return;
        }

        // Filter records within date range
        Map<LocalDate, AttendanceRecord> recordsByDate = new HashMap<>();
        for (AttendanceRecord record : records) {
            LocalDate date = record.getDate();
            if ((date.isEqual(startDate) || date.isAfter(startDate)) &&
                    (date.isEqual(endDate) || date.isBefore(endDate))) {
                recordsByDate.put(date, record);
            }
        }

        if (recordsByDate.isEmpty()) {
            System.out.println("No records found within date range for employee: " + employeeId);
            return;
        }

        // Calculate hours worked
        double totalRegularHours = 0.0;
        double totalOvertimeHours = 0.0;
        double totalLateMinutes = 0.0;
        double totalUndertimeMinutes = 0.0;

        // Print report header
        printReportHeader(employee, employeeId, startDate, endDate);

        // Print column headers
        printColumnHeaders();

        // Process each day's records
        for (LocalDate date : recordsByDate.keySet()) {
            AttendanceRecord record = recordsByDate.get(date);
            LocalTime timeIn = record.getTimeIn();
            LocalTime timeOut = record.getTimeOut();
            boolean isLate = record.isLate();

            // Calculate hours with proper isLate flag
            double regularHours = hoursCalculator.calculateHoursWorked(timeIn, timeOut, isLate);
            double overtimeHours = hoursCalculator.calculateOvertimeHours(timeOut, isLate);
            double lateMinutes = record.getLateMinutes();
            double undertimeMinutes = record.getUndertimeMinutes();

            // Add to totals
            totalRegularHours += regularHours;
            totalOvertimeHours += overtimeHours;
            totalLateMinutes += lateMinutes;
            totalUndertimeMinutes += undertimeMinutes;

            // Display daily data
            printDailyRecord(date, timeIn, timeOut, regularHours, overtimeHours, lateMinutes, undertimeMinutes);
        }

        // Print totals
        printTotals(totalRegularHours, totalOvertimeHours, totalLateMinutes, totalUndertimeMinutes);

        // Calculate potential deductions
        printFinancialSummary(employee, totalRegularHours, totalOvertimeHours, totalLateMinutes, totalUndertimeMinutes);
    }

    /**
     * Print the report header
     */
    private void printReportHeader(Employee employee, String employeeId, LocalDate startDate, LocalDate endDate) {
        System.out.println("\n===== WEEKLY HOURS REPORT =====");
        System.out.println("Employee: " + employee.getFullName() + " (ID: " + employeeId + ")");
        System.out.println("Position: " + employee.getPosition());
        System.out.println("Hourly Rate: ₱" + String.format("%.2f", employee.getHourlyRate()));
        System.out.println("Period: " + startDate.format(dateFormatter) + " to " + endDate.format(dateFormatter));
    }

    /**
     * Print the column headers
     */
    private void printColumnHeaders() {
        System.out.println("\nDAILY BREAKDOWN:");
        System.out.printf("%-12s %-10s %-10s %-12s %-12s %-10s %-10s\n",
                "Date", "Time In", "Time Out", "Regular Hrs", "Overtime", "Late", "Undertime");
        System.out.println("----------------------------------------------------------------------------");
    }

    /**
     * Print a daily record
     */
    private void printDailyRecord(LocalDate date, LocalTime timeIn, LocalTime timeOut,
                                  double regularHours, double overtimeHours,
                                  double lateMinutes, double undertimeMinutes) {
        System.out.printf("%-12s %-10s %-10s %-12.2f %-12.2f %-10.0f %-10.0f\n",
                date.format(dateFormatter),
                formatTime(timeIn), formatTime(timeOut),
                regularHours, overtimeHours,
                lateMinutes, undertimeMinutes);
    }

    /**
     * Print the totals row
     */
    private void printTotals(double totalRegularHours, double totalOvertimeHours,
                             double totalLateMinutes, double totalUndertimeMinutes) {
        System.out.println("----------------------------------------------------------------------------");
        System.out.printf("%-34s %-12.2f %-12.2f %-10.0f %-10.0f\n",
                "TOTALS:", totalRegularHours, totalOvertimeHours,
                totalLateMinutes, totalUndertimeMinutes);
    }

    /**
     * Print the financial summary
     */
    private void printFinancialSummary(Employee employee, double totalRegularHours,
                                       double totalOvertimeHours, double totalLateMinutes,
                                       double totalUndertimeMinutes) {
        // Calculate potential deductions
        double hourlyRate = employee.getHourlyRate();
        double lateDeduction = (hourlyRate / 60.0) * totalLateMinutes;
        double undertimeDeduction = (hourlyRate / 60.0) * totalUndertimeMinutes;
        double overtimePay = hourlyRate * 1.25 * totalOvertimeHours;
        double regularPay = hourlyRate * totalRegularHours;
        double totalPay = regularPay + overtimePay - lateDeduction - undertimeDeduction;

        System.out.println("\nSUMMARY FOR PAYROLL:");
        System.out.printf("Regular Hours Pay: ₱%.2f (%.2f hours @ ₱%.2f/hr)\n",
                regularPay, totalRegularHours, hourlyRate);
        System.out.printf("Overtime Pay: ₱%.2f (%.2f hours @ ₱%.2f/hr)\n",
                overtimePay, totalOvertimeHours, hourlyRate * 1.25);
        System.out.printf("Late Deduction: ₱%.2f (%.0f minutes @ ₱%.4f/min)\n",
                lateDeduction, totalLateMinutes, hourlyRate / 60.0);
        System.out.printf("Undertime Deduction: ₱%.2f (%.0f minutes @ ₱%.4f/min)\n",
                undertimeDeduction, totalUndertimeMinutes, hourlyRate / 60.0);
        System.out.println("------------------------------------------------");
        System.out.printf("Total Net Pay: ₱%.2f\n", totalPay);
    }

    /**
     * Format time for display
     *
     * @param time Time to format
     * @return Formatted time string
     */
    private String formatTime(LocalTime time) {
        if (time == null) {
            return "-";
        }
        return time.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    /**
     * Main method for running standalone report
     *
     * @param args Command line arguments: employeeId, startDate, endDate
     */
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: WeeklyHoursReport <employeeId> <startDate MM/dd/yyyy> <endDate MM/dd/yyyy>");
            return;
        }

        String employeeId = args[0];

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            LocalDate startDate = LocalDate.parse(args[1], formatter);
            LocalDate endDate = LocalDate.parse(args[2], formatter);

            if (endDate.isBefore(startDate)) {
                System.out.println("Error: End date cannot be before start date");
                return;
            }

            WeeklyHoursReport report = new WeeklyHoursReport(
                    "resources/MotorPH Employee Data - Employee Details.csv",
                    "resources/MotorPH Employee Data - Attendance Record.csv");

            report.generateReportForEmployee(employeeId, startDate, endDate);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("Usage: WeeklyHoursReport <employeeId> <startDate MM/dd/yyyy> <endDate MM/dd/yyyy>");
        }
    }
}