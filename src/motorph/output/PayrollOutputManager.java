// File: motorph/output/PayrollOutputManager.java
package motorph.output;

import motorph.employee.Employee;
import motorph.hours.AttendanceReader;
import motorph.process.PayrollDateManager;
import motorph.process.PayrollProcessor;
import motorph.util.DateTimeUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Handles display of payroll information
 * Manages all output formatting and display for the payroll system
 */
public class PayrollOutputManager {
    // For user input
    private final Scanner scanner;

    // Data sources
    private final AttendanceReader attendanceReader;
    private final PayrollProcessor payrollProcessor;

    /**
     * Create a new output manager
     *
     * @param scanner Scanner for user input
     * @param attendanceReader Reader for attendance data
     * @param payrollProcessor Processor for payroll calculations
     */
    public PayrollOutputManager(Scanner scanner, AttendanceReader attendanceReader, PayrollProcessor payrollProcessor) {
        this.scanner = scanner;
        this.attendanceReader = attendanceReader;
        this.payrollProcessor = payrollProcessor;
    }

    /**
     * Display main menu
     */
    public void displayMainMenu() {
        System.out.println("\nMAIN MENU:");
        System.out.println("1. Process Payroll");
        System.out.println("2. Find Employee");
        System.out.println("3. View Payroll Calendar");
        System.out.println("4. Exit");
        System.out.print("Enter choice (1-4): ");
    }

    /**
     * Display employee details
     *
     * @param employee Employee to display details for
     */
    public void displayEmployeeDetails(Employee employee) {
        if (employee == null) {
            return;
        }

        System.out.println("\n===== EMPLOYEE DETAILS =====");
        System.out.println("ID: " + employee.getEmployeeId());
        System.out.println("Name: " + employee.getFullName());
        System.out.println("Position: " + employee.getPosition());
        System.out.println("Basic Salary: ₱" + String.format("%,.2f", employee.getBasicSalary()));
        System.out.println("Rice Subsidy: ₱" + String.format("%,.2f", employee.getRiceSubsidy()));
        System.out.println("Phone Allowance: ₱" + String.format("%,.2f", employee.getPhoneAllowance()));
        System.out.println("Clothing Allowance: ₱" + String.format("%,.2f", employee.getClothingAllowance()));
        System.out.println("Hourly Rate: ₱" + String.format("%.2f", employee.getHourlyRate()));

        System.out.println("\nOptions:");
        System.out.println("1. View Attendance");
        System.out.println("2. Process Payroll");
        System.out.println("3. Return to Main Menu");
        System.out.print("Enter choice: ");
    }

    /**
     * Display attendance summary for payroll processing
     *
     * @param employee Employee to display summary for
     * @param attendanceSummary Summary data from attendance reader
     * @param startDate Start date of payroll period
     * @param endDate End date of payroll period
     */
    public void displayAttendanceSummary(Employee employee, Map<String, Object> attendanceSummary,
                                         LocalDate startDate, LocalDate endDate) {
        if (employee == null || attendanceSummary == null) {
            return;
        }

        System.out.println("\n===== ATTENDANCE SUMMARY =====");
        System.out.println("Employee: " + employee.getFullName() + " (ID: " + employee.getEmployeeId() + ")");
        System.out.println("Period: " + DateTimeUtil.formatDateStandard(startDate) +
                " to " + DateTimeUtil.formatDateStandard(endDate));

        // Extract attendance data
        double totalHours = (double) attendanceSummary.get("hours");
        double overtimeHours = (double) attendanceSummary.get("overtimeHours");
        double lateMinutes = (double) attendanceSummary.get("lateMinutes");
        double undertimeMinutes = (double) attendanceSummary.get("undertimeMinutes");
        boolean isLateAnyDay = (boolean) attendanceSummary.get("isLateAnyDay");
        int recordCount = (int) attendanceSummary.getOrDefault("recordCount", 0);

        // Display summary
        System.out.println("\nSUMMARY:");
        System.out.println("Total Days Worked: " + recordCount);
        System.out.println("Total Hours: " + String.format("%.2f", totalHours));
        System.out.println("Overtime Hours: " + String.format("%.2f", overtimeHours));

        if (lateMinutes > 0) {
            System.out.println("Late Minutes: " + String.format("%.0f", lateMinutes));
        }

        if (undertimeMinutes > 0) {
            System.out.println("Undertime Minutes: " + String.format("%.0f", undertimeMinutes));
        }

        // Show notices
        if (isLateAnyDay) {
            System.out.println("\nNOTE: Employee was late during this period.");
            System.out.println("      Late employees are not eligible for overtime pay.");
        }

        if (attendanceSummary.containsKey("hasUnpaidAbsences") &&
                (boolean) attendanceSummary.get("hasUnpaidAbsences")) {
            System.out.println("\nNOTE: Employee has unpaid absences during this period.");
        }
    }

    /**
     * Display payroll summary before processing
     *
     * @param employee Employee to display summary for
     * @param startDate Start date of payroll period
     * @param endDate End date of payroll period
     * @param payPeriodType Pay period type (MID_MONTH or END_MONTH)
     * @return Attendance summary map or null if no records found
     */
    public Map<String, Object> displayPayrollSummary(Employee employee, LocalDate startDate,
                                                     LocalDate endDate, int payPeriodType) {
        if (employee == null) {
            return null;
        }

        System.out.println("\n===== PAYROLL SUMMARY =====");
        System.out.println("Employee: " + employee.getFullName() + " (ID: " + employee.getEmployeeId() + ")");
        System.out.println("Period: " + DateTimeUtil.formatDateStandard(startDate) +
                " to " + DateTimeUtil.formatDateStandard(endDate));
        System.out.println("Payroll Type: " +
                (payPeriodType == PayrollDateManager.MID_MONTH ? "Mid-month" : "End-month"));

        // Get daily attendance records
        Map<LocalDate, Map<String, Object>> dailyAttendance =
                attendanceReader.getDailyAttendanceForEmployee(employee.getEmployeeId(), startDate, endDate);

        if (dailyAttendance.isEmpty()) {
            System.out.println("\nNo attendance records found for this period.");
            return null;
        }

        // Get attendance summary
        Map<String, Object> attendanceSummary = attendanceReader.getAttendanceSummary(
                employee.getEmployeeId(), startDate, endDate);

        // Display daily breakdown
        System.out.println("\n--- ATTENDANCE DETAILS ---");
        System.out.printf("%-12s %-10s %-10s %-10s %-10s %-10s %-15s\n",
                "Date", "Time In", "Time Out", "Hours", "OT Hours", "Late", "Absence Type");
        System.out.println("--------------------------------------------------------------------------------");

        // Display daily records sorted by date
        dailyAttendance.keySet().stream().sorted().forEach(date -> {
            Map<String, Object> dayData = dailyAttendance.get(date);

            String timeIn = (String) dayData.get("timeIn");
            String timeOut = (String) dayData.get("timeOut");
            double hours = (double) dayData.get("hours");
            double overtimeHours = (double) dayData.get("overtimeHours");
            double lateMinutes = (double) dayData.get("lateMinutes");
            String absenceType = (String) dayData.getOrDefault("absenceType", "");

            // Format the line
            System.out.printf("%-12s %-10s %-10s %-10.2f %-10.2f %-10s %-15s\n",
                    DateTimeUtil.formatDateStandard(date),
                    timeIn, timeOut, hours, overtimeHours,
                    (lateMinutes > 0 ? lateMinutes + " min" : "-"),
                    absenceType != null ? absenceType : "-");
        });

        System.out.println("--------------------------------------------------------------------------------");

        // Extract totals from summary
        double totalHours = (double) attendanceSummary.get("hours");
        double totalOvertimeHours = (double) attendanceSummary.get("overtimeHours");
        double totalLateMinutes = (double) attendanceSummary.get("lateMinutes");

        System.out.printf("%-34s %-10.2f %-10.2f %-10.2f\n",
                "TOTALS:", totalHours, totalOvertimeHours, totalLateMinutes);

        return attendanceSummary;
    }

    /**
     * Display salary details
     *
     * @param employee Employee to display salary details for
     */
    public void displaySalaryDetails(Employee employee) {
        payrollProcessor.displaySalaryDetails(employee);
    }

    /**
     * Display attendance options
     *
     * @param employee Employee to display options for
     */
    public void displayAttendanceOptions(Employee employee) {
        System.out.println("\n===== ATTENDANCE OPTIONS =====");
        System.out.println("Employee: " + employee.getFullName());
        System.out.println("\nSelect view type:");
        System.out.println("1. Daily Attendance");
        System.out.println("2. Weekly Summary");
        System.out.print("Enter choice (1-2): ");
    }

    /**
     * Display daily attendance
     *
     * @param employee Employee to display attendance for
     * @param startDate Start date of range
     * @param endDate End date of range
     */
    public void displayDailyAttendance(Employee employee, LocalDate startDate, LocalDate endDate) {
        System.out.println("\n===== DAILY ATTENDANCE =====");
        System.out.println("Employee: " + employee.getFullName());
        System.out.println("Period: " + DateTimeUtil.formatDateStandard(startDate) +
                " to " + DateTimeUtil.formatDateStandard(endDate));

        // Get daily attendance records
        Map<LocalDate, Map<String, Object>> dailyAttendance =
                attendanceReader.getDailyAttendanceForEmployee(employee.getEmployeeId(), startDate, endDate);

        if (dailyAttendance.isEmpty()) {
            System.out.println("\nNo attendance records found for this period.");
            return;
        }

        // Display daily breakdown
        System.out.println("\n--- ATTENDANCE DETAILS ---");
        System.out.printf("%-12s %-10s %-10s %-10s %-10s %-10s %-10s %-15s\n",
                "Date", "Time In", "Time Out", "Hours", "OT Hours", "Late", "Undertime", "Absence Type");
        System.out.println("----------------------------------------------------------------------------------------");

        double totalHours = 0;
        double totalOvertimeHours = 0;
        double totalLateMinutes = 0;
        double totalUndertimeMinutes = 0;
        int unpaidAbsenceCount = 0;

        // Display records sorted by date
        for (LocalDate date : dailyAttendance.keySet().stream().sorted().toList()) {
            Map<String, Object> dayData = dailyAttendance.get(date);

            String timeIn = (String) dayData.get("timeIn");
            String timeOut = (String) dayData.get("timeOut");
            double hours = (double) dayData.get("hours");
            double overtimeHours = (double) dayData.get("overtimeHours");
            double lateMinutes = (double) dayData.get("lateMinutes");
            double undertimeMinutes = (double) dayData.getOrDefault("undertimeMinutes", 0.0);
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

            // Update totals
            totalHours += hours;
            totalOvertimeHours += overtimeHours;
            totalLateMinutes += lateMinutes;
            totalUndertimeMinutes += undertimeMinutes;

            if (isUnpaidAbsence) {
                unpaidAbsenceCount++;
            }

            // Format the line
            System.out.printf("%-12s %-10s %-10s %-10.2f %-10.2f %-10s %-10s %-15s\n",
                    DateTimeUtil.formatDateStandard(date),
                    timeIn, timeOut, hours, overtimeHours,
                    (lateMinutes > 0 ? lateMinutes + " min" : "-"),
                    (undertimeMinutes > 0 ? undertimeMinutes + " min" : "-"),
                    (absenceType != null && !absenceType.isEmpty() ? absenceType : "-"));
        }

        System.out.println("----------------------------------------------------------------------------------------");
        System.out.printf("%-34s %-10.2f %-10.2f %-10.2f %-10.2f\n",
                "TOTALS:", totalHours, totalOvertimeHours, totalLateMinutes, totalUndertimeMinutes);

        if (unpaidAbsenceCount > 0) {
            System.out.println("Unpaid Absences: " + unpaidAbsenceCount + " day(s)");
        }

        // Wait for user
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }

    /**
     * Display weekly attendance
     *
     * @param employee Employee to display attendance for
     * @param startDate Start date of range
     * @param endDate End date of range
     */
    public void displayWeeklyAttendance(Employee employee, LocalDate startDate, LocalDate endDate) {
        System.out.println("\n===== WEEKLY ATTENDANCE =====");
        System.out.println("Employee: " + employee.getFullName());
        System.out.println("Period: " + DateTimeUtil.formatDateStandard(startDate) +
                " to " + DateTimeUtil.formatDateStandard(endDate));

        // Get weekly attendance records
        Map<String, Map<String, Double>> weeklyAttendance =
                attendanceReader.getWeeklyAttendanceForEmployee(employee.getEmployeeId(), startDate, endDate);

        if (weeklyAttendance.isEmpty()) {
            System.out.println("\nNo attendance records found for this period.");
            return;
        }

        // Display weekly summary
        System.out.println("\n--- WEEKLY SUMMARY ---");
        System.out.printf("%-30s %-10s %-12s %-12s %-10s\n",
                "Week", "Hours", "Overtime", "Late (min)", "Unpaid Abs");
        System.out.println("--------------------------------------------------------------------------------");

        double totalHours = 0;
        double totalOvertimeHours = 0;
        double totalLateMinutes = 0;
        int totalUnpaidAbsences = 0;

        for (String weekLabel : weeklyAttendance.keySet()) {
            Map<String, Double> weekData = weeklyAttendance.get(weekLabel);

            double hours = weekData.getOrDefault("hours", 0.0);
            double overtimeHours = weekData.getOrDefault("overtimeHours", 0.0);
            double lateMinutes = weekData.getOrDefault("lateMinutes", 0.0);
            int unpaidAbsences = weekData.getOrDefault("unpaidAbsenceCount", 0.0).intValue();

            // Add to totals
            totalHours += hours;
            totalOvertimeHours += overtimeHours;
            totalLateMinutes += lateMinutes;
            totalUnpaidAbsences += unpaidAbsences;

            // Format the line
            System.out.printf("%-30s %-10.2f %-12.2f %-12.2f %-10d\n",
                    weekLabel, hours, overtimeHours, lateMinutes, unpaidAbsences);
        }

        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("%-30s %-10.2f %-12.2f %-12.2f %-10d\n",
                "TOTALS:", totalHours, totalOvertimeHours, totalLateMinutes, totalUnpaidAbsences);

        // Wait for user
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }

    /**
     * Display payroll calendar
     *
     * @param year Year to display calendar for
     * @param month Month to display calendar for
     */
    public void displayPayrollCalendar(int year, int month) {
        System.out.println("\n===== PAYROLL CALENDAR =====");
        System.out.println("Year: " + year);
        System.out.println("Month: " + PayrollDateManager.getMonthName(month));

        // Calculate payroll dates
        LocalDate midMonth = PayrollDateManager.getPayrollDate(year, month, PayrollDateManager.MID_MONTH);
        LocalDate endMonth = PayrollDateManager.getPayrollDate(year, month, PayrollDateManager.END_MONTH);

        // Get cutoff periods
        LocalDate[] midCutoff = PayrollDateManager.getCutoffDateRange(midMonth, PayrollDateManager.MID_MONTH);
        LocalDate[] endCutoff = PayrollDateManager.getCutoffDateRange(endMonth, PayrollDateManager.END_MONTH);

        // Display payroll information
        System.out.println("\n--- MID-MONTH PAYROLL ---");
        System.out.println("Payroll Date: " + DateTimeUtil.formatDate(midMonth));
        System.out.println("Cutoff Period: " + DateTimeUtil.formatDate(midCutoff[0]) +
                " to " + DateTimeUtil.formatDate(midCutoff[1]));
        System.out.println("Deductions: SSS, PhilHealth, Pag-IBIG");

        System.out.println("\n--- END-MONTH PAYROLL ---");
        System.out.println("Payroll Date: " + DateTimeUtil.formatDate(endMonth));
        System.out.println("Cutoff Period: " + DateTimeUtil.formatDate(endCutoff[0]) +
                " to " + DateTimeUtil.formatDate(endCutoff[1]));
        System.out.println("Deductions: Withholding Tax");
    }
}