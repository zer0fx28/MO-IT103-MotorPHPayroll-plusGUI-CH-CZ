// File: motorph/reports/WeeklyHoursReport.java
package motorph.reports;

import motorph.employee.EmployeeDataReader;
import motorph.employee.Employee;
import motorph.hours.AttendanceReader;
import motorph.hours.AttendanceRecord;
import motorph.util.DateTimeUtil;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates weekly hour reports for employees
 * Used for tracking and reporting hours worked
 */
public class WeeklyHoursReport {
    // Data sources
    private final EmployeeDataReader employeeDataReader;
    private final AttendanceReader attendanceReader;

    /**
     * Create a new weekly hours report generator
     *
     * @param employeeFilePath Path to employee data CSV
     * @param attendanceFilePath Path to attendance data CSV
     */
    public WeeklyHoursReport(String employeeFilePath, String attendanceFilePath) {
        this.employeeDataReader = new EmployeeDataReader(employeeFilePath);
        this.attendanceReader = new AttendanceReader(attendanceFilePath);
    }

    /**
     * Generate report for a specific employee
     *
     * @param employeeId Employee ID to generate report for
     * @param startDate Start date of report period
     * @param endDate End date of report period
     */
    public void generateReportForEmployee(String employeeId, LocalDate startDate, LocalDate endDate) {
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
            if (DateTimeUtil.isDateInRange(date, startDate, endDate)) {
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
        System.out.println("\n===== WEEKLY HOURS REPORT =====");
        System.out.println("Employee: " + employee.getFullName() + " (ID: " + employeeId + ")");
        System.out.println("Period: " + DateTimeUtil.formatDateStandard(startDate) + " to " +
                DateTimeUtil.formatDateStandard(endDate));
        System.out.println("\nDAILY BREAKDOWN:");
        System.out.printf("%-12s %-10s %-10s %-12s %-12s %-10s %-10s\n",
                "Date", "Time In", "Time Out", "Regular Hrs", "Overtime", "Late", "Undertime");
        System.out.println("----------------------------------------------------------------------------");

        // Sort dates and display daily breakdown
        recordsByDate.keySet().stream().sorted().forEach(date -> {
            AttendanceRecord record = recordsByDate.get(date);

            // Get hours worked
            double regularHours = record.getRegularHoursWorked();
            double overtimeHours = record.getOvertimeHours();
            double lateMinutes = record.getLateMinutes();
            double undertimeMinutes = record.getUndertimeMinutes();

            // Display daily data
            System.out.printf("%-12s %-10s %-10s %-12.2f %-12.2f %-10.0f %-10.0f\n",
                    DateTimeUtil.formatDateStandard(date),
                    record.getFormattedTimeIn(),
                    record.getFormattedTimeOut(),
                    regularHours, overtimeHours,
                    lateMinutes, undertimeMinutes);
        });

        // Calculate totals
        for (AttendanceRecord record : recordsByDate.values()) {
            totalRegularHours += record.getRegularHoursWorked();
            totalOvertimeHours += record.getOvertimeHours();
            totalLateMinutes += record.getLateMinutes();
            totalUndertimeMinutes += record.getUndertimeMinutes();
        }

        // Display totals
        System.out.println("----------------------------------------------------------------------------");
        System.out.printf("%-34s %-12.2f %-12.2f %-10.0f %-10.0f\n",
                "TOTALS:", totalRegularHours, totalOvertimeHours,
                totalLateMinutes, totalUndertimeMinutes);

        // Calculate potential deductions
        double hourlyRate = employee.getHourlyRate();
        double lateDeduction = (hourlyRate / 60.0) * totalLateMinutes;
        double undertimeDeduction = (hourlyRate / 60.0) * totalUndertimeMinutes;
        double overtimePay = hourlyRate * 1.25 * totalOvertimeHours;

        // Display payroll summary
        System.out.println("\nSUMMARY FOR PAYROLL:");
        System.out.printf("Total Regular Hours: %.2f hours\n", totalRegularHours);
        System.out.printf("Total Overtime Hours: %.2f hours (₱%.2f)\n", totalOvertimeHours, overtimePay);
        System.out.printf("Late Deduction: ₱%.2f (%.0f minutes)\n", lateDeduction, totalLateMinutes);
        System.out.printf("Undertime Deduction: ₱%.2f (%.0f minutes)\n", undertimeDeduction, totalUndertimeMinutes);

        // Display attendance status
        System.out.println("\nATTENDANCE STATUS:");
        int totalWorkDays = recordsByDate.size();
        int expectedWorkDays = DateTimeUtil.daysBetween(startDate, endDate);
        int absentDays = expectedWorkDays - totalWorkDays;

        System.out.printf("Total Work Days: %d\n", totalWorkDays);
        System.out.printf("Expected Work Days: %d\n", expectedWorkDays);

        if (absentDays > 0) {
            System.out.printf("Absent Days: %d\n", absentDays);
        } else {
            System.out.println("Perfect Attendance for this period!");
        }
    }

    /**
     * Main method for running standalone report
     *
     * @param args Command-line arguments: employeeId, startDate, endDate
     */
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: WeeklyHoursReport <employeeId> <startDate MM/dd/yyyy> <endDate MM/dd/yyyy>");
            return;
        }

        String employeeId = args[0];
        LocalDate startDate = DateTimeUtil.parseDate(args[1]);
        LocalDate endDate = DateTimeUtil.parseDate(args[2]);

        if (startDate == null || endDate == null) {
            System.out.println("Invalid date format. Please use MM/dd/yyyy");
            return;
        }

        WeeklyHoursReport report = new WeeklyHoursReport(
                "resources/MotorPH Employee Data - Employee Details.csv",
                "resources/MotorPH Employee Data - Attendance Record.csv");

        report.generateReportForEmployee(employeeId, startDate, endDate);
    }
}