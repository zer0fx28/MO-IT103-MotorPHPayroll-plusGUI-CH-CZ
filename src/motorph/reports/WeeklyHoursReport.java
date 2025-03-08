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
 */
public class WeeklyHoursReport {
    private final EmployeeDataReader employeeDataReader;
    private final AttendanceReader attendanceReader;
    private final WorkHoursCalculator hoursCalculator;
    private final DateTimeFormatter dateFormatter;

    /**
     * Create a new report generator
     */
    public WeeklyHoursReport(String employeeFilePath, String attendanceFilePath) {
        this.employeeDataReader = new EmployeeDataReader(employeeFilePath);
        this.attendanceReader = new AttendanceReader(attendanceFilePath);
        this.hoursCalculator = new WorkHoursCalculator();
        this.dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    }

    /**
     * Generate report for a specific employee
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

        System.out.println("\n===== WEEKLY HOURS REPORT =====");
        System.out.println("Employee: " + employee.getFullName() + " (ID: " + employeeId + ")");
        System.out.println("Period: " + startDate.format(dateFormatter) + " to " + endDate.format(dateFormatter));
        System.out.println("\nDAILY BREAKDOWN:");
        System.out.printf("%-12s %-10s %-10s %-12s %-12s %-10s %-10s\n",
                "Date", "Time In", "Time Out", "Regular Hrs", "Overtime", "Late", "Undertime");
        System.out.println("----------------------------------------------------------------------------");

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
            System.out.printf("%-12s %-10s %-10s %-12.2f %-12.2f %-10.0f %-10.0f\n",
                    date.format(dateFormatter),
                    formatTime(timeIn), formatTime(timeOut),
                    regularHours, overtimeHours,
                    lateMinutes, undertimeMinutes);
        }

        System.out.println("----------------------------------------------------------------------------");
        System.out.printf("%-34s %-12.2f %-12.2f %-10.0f %-10.0f\n",
                "TOTALS:", totalRegularHours, totalOvertimeHours,
                totalLateMinutes, totalUndertimeMinutes);

        // Calculate potential deductions
        double hourlyRate = employee.getHourlyRate();
        double lateDeduction = (hourlyRate / 60.0) * totalLateMinutes;
        double undertimeDeduction = (hourlyRate / 60.0) * totalUndertimeMinutes;
        double overtimePay = hourlyRate * 1.25 * totalOvertimeHours;

        System.out.println("\nSUMMARY FOR PAYROLL:");
        System.out.printf("Total Regular Hours: %.2f hours\n", totalRegularHours);
        System.out.printf("Total Overtime Hours: %.2f hours (₱%.2f)\n", totalOvertimeHours, overtimePay);
        System.out.printf("Late Deduction: ₱%.2f (%.0f minutes)\n", lateDeduction, totalLateMinutes);
        System.out.printf("Undertime Deduction: ₱%.2f (%.0f minutes)\n", undertimeDeduction, totalUndertimeMinutes);
    }

    /**
     * Format time for display
     */
    private String formatTime(LocalTime time) {
        if (time == null) {
            return "-";
        }
        return time.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    /**
     * Main method for running standalone report
     */
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: WeeklyHoursReport <employeeId> <startDate MM/dd/yyyy> <endDate MM/dd/yyyy>");
            return;
        }

        String employeeId = args[0];
        LocalDate startDate = LocalDate.parse(args[1], DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        LocalDate endDate = LocalDate.parse(args[2], DateTimeFormatter.ofPattern("MM/dd/yyyy"));

        WeeklyHoursReport report = new WeeklyHoursReport(
                "resources/MotorPH Employee Data - Employee Details.csv",
                "resources/MotorPH Employee Data - Attendance Record.csv");

        report.generateReportForEmployee(employeeId, startDate, endDate);
    }
}