// File: motorph/output/PayrollSummaryDisplay.java
package motorph.output;

import motorph.employee.Employee;
import motorph.deductions.StatutoryDeductions;
import motorph.process.PayrollDateManager;
import motorph.hours.AttendanceReader;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Displays payroll summary information in a well-organized format
 */
public class PayrollSummaryDisplay {

    /**
     * Display detailed payroll summary for an employee
     */
    public static void displayPayrollSummary(
            Employee employee,
            Map<String, Object> attendanceSummary,
            LocalDate startDate,
            LocalDate endDate,
            int payPeriodType,
            StatutoryDeductions.DeductionResult deductions,
            Map<String, Object> payDetails,
            AttendanceReader attendanceReader) {

        if (employee == null || attendanceSummary == null || payDetails == null || attendanceReader == null) {
            System.out.println("Error: Cannot display payroll summary due to missing data");
            return;
        }

        // Extract values with proper null checks
        double standardHours = (double) attendanceSummary.getOrDefault("hours", 0.0);
        double actualHours = (double) attendanceSummary.getOrDefault("actualHours", 0.0);
        double overtimeHours = (double) attendanceSummary.getOrDefault("overtimeHours", 0.0);
        double lateMinutes = (double) attendanceSummary.getOrDefault("lateMinutes", 0.0);
        double undertimeMinutes = (double) attendanceSummary.getOrDefault("undertimeMinutes", 0.0);

        double regularPay = (double) payDetails.getOrDefault("regularPay", 0.0);
        double overtimePay = (double) payDetails.getOrDefault("overtimePay", 0.0);
        double holidayPay = (double) payDetails.getOrDefault("holidayPay", 0.0);
        double lateDeduction = (double) payDetails.getOrDefault("lateDeduction", 0.0);
        double undertimeDeduction = (double) payDetails.getOrDefault("undertimeDeduction", 0.0);
        double absenceDeduction = (double) payDetails.getOrDefault("absenceDeduction", 0.0);
        double hourlyRate = (double) payDetails.getOrDefault("hourlyRate", 0.0);
        double absentHours = (double) payDetails.getOrDefault("absentHours", 0.0);

        double grossPay = regularPay + overtimePay + holidayPay - lateDeduction - undertimeDeduction - absenceDeduction;
        double netPay = grossPay - deductions.totalDeductions;

        // Header
        System.out.println("\n===== PAYROLL SUMMARY =====");
        System.out.println("Employee: " + employee.getFullName() + " (ID: " + employee.getEmployeeId() + ")");
        System.out.println("Period: " + formatDate(startDate) + " to " + formatDate(endDate));
        System.out.println("Payroll Type: " + (payPeriodType == PayrollDateManager.MID_MONTH ? "Mid-month" : "End-month"));

        // Display attendance table first
        AttendanceDisplayFormatter.displayAttendanceReport(
                attendanceReader.getDailyAttendanceForEmployee(employee.getEmployeeId(), startDate, endDate));

        // Display work summary in a formatted table
        displayWorkSummaryTable(standardHours, actualHours, overtimeHours, lateMinutes, undertimeMinutes, absentHours);

        // Display earnings in a formatted table
        displayEarningsTable(regularPay, overtimePay, holidayPay);

        // Display deductions in a formatted table
        displayDeductionsTable(lateDeduction, undertimeDeduction, absenceDeduction,
                deductions, payPeriodType, lateMinutes, undertimeMinutes, hourlyRate);

        // Display net pay in a formatted table
        displayNetPayTable(grossPay, deductions.totalDeductions, netPay);
    }

    /**
     * Display work summary as a formatted table
     */
    private static void displayWorkSummaryTable(
            double standardHours, double actualHours, double overtimeHours,
            double lateMinutes, double undertimeMinutes, double absentHours) {

        // Table header
        System.out.println("\n┌───────────────────────────────────────────┐");
        System.out.println("│             WORK SUMMARY                  │");
        System.out.println("├───────────────────────┬───────────────────┤");
        System.out.println("│ Category              │ Hours/Minutes     │");
        System.out.println("├───────────────────────┼───────────────────┤");

        // Table content
        System.out.printf("│ Expected Work Hours   │ %-17.2f │\n", standardHours);
        System.out.printf("│ Actual Hours Worked   │ %-17.2f │\n", actualHours);
        System.out.printf("│ Overtime Hours        │ %-17.2f │\n", overtimeHours);
        System.out.printf("│ Late                  │ %-17s │\n", formatMinutes(lateMinutes));
        System.out.printf("│ Undertime             │ %-17s │\n", formatMinutes(undertimeMinutes));
        System.out.printf("│ Absent Hours          │ %-17.2f │\n", absentHours);

        // Table footer
        System.out.println("└───────────────────────┴───────────────────┘");
    }

    /**
     * Display earnings as a formatted table
     */
    private static void displayEarningsTable(double regularPay, double overtimePay, double holidayPay) {
        double totalEarnings = regularPay + overtimePay + holidayPay;

        // Table header
        System.out.println("\n┌───────────────────────────────────────────┐");
        System.out.println("│                EARNINGS                   │");
        System.out.println("├───────────────────────┬───────────────────┤");
        System.out.println("│ Category              │ Amount (₱)        │");
        System.out.println("├───────────────────────┼───────────────────┤");

        // Table content
        System.out.printf("│ Basic Pay             │ %-17s │\n", formatCurrency(regularPay));
        System.out.printf("│ Overtime Pay          │ %-17s │\n", formatCurrency(overtimePay));
        System.out.printf("│ Holiday Pay           │ %-17s │\n", formatCurrency(holidayPay));

        // Table footer with total
        System.out.println("├───────────────────────┼───────────────────┤");
        System.out.printf("│ Total Earnings        │ %-17s │\n", formatCurrency(totalEarnings));
        System.out.println("└───────────────────────┴───────────────────┘");
    }

    /**
     * Display deductions as a formatted table
     */
    private static void displayDeductionsTable(
            double lateDeduction, double undertimeDeduction, double absenceDeduction,
            StatutoryDeductions.DeductionResult deductions, int payPeriodType,
            double lateMinutes, double undertimeMinutes, double hourlyRate) {

        double totalAttendanceDeductions = lateDeduction + undertimeDeduction + absenceDeduction;
        double perMinuteRate = hourlyRate / 60.0;

        // Table header - fix width to ensure proper alignment
        System.out.println("\n┌───────────────────────┬───────────────────┬───────────────────┐");
        System.out.println("│        DEDUCTIONS     │                   │                   │");
        System.out.println("├───────────────────────┼───────────────────┼───────────────────┤");
        System.out.println("│ Category              │ Amount (₱)        │ Details           │");
        System.out.println("├───────────────────────┼───────────────────┼───────────────────┤");

        // Use consistent width for all cells
        String format = "│ %-21s │ %-17s │ %-17s │\n";

        // Attendance deductions
        System.out.printf(format,
                "Late Deduction",
                formatCurrency(lateDeduction),
                lateMinutes > 0 ? String.format("%.1f mins × ₱%.4f", lateMinutes, perMinuteRate) : "-");

        System.out.printf(format,
                "Undertime Deduction",
                formatCurrency(undertimeDeduction),
                undertimeMinutes > 0 ? String.format("%.1f mins × ₱%.4f", undertimeMinutes, perMinuteRate) : "-");

        System.out.printf(format,
                "Absence Deduction",
                formatCurrency(absenceDeduction),
                absenceDeduction > 0 ? "Unpaid absence" : "-");

        // Statutory deductions
        if (payPeriodType == PayrollDateManager.MID_MONTH) {
            System.out.printf(format, "SSS", formatCurrency(deductions.sssDeduction), "-");
            System.out.printf(format, "PhilHealth", formatCurrency(deductions.philhealthDeduction), "-");
            System.out.printf(format, "Pag-IBIG", formatCurrency(deductions.pagibigDeduction), "-");
            System.out.printf(format, "Withholding Tax", formatCurrency(deductions.withholdingTax), "End-month");
        } else {
            System.out.printf(format, "SSS", formatCurrency(0.0), "Mid-month");
            System.out.printf(format, "PhilHealth", formatCurrency(0.0), "Mid-month");
            System.out.printf(format, "Pag-IBIG", formatCurrency(0.0), "Mid-month");
            System.out.printf(format, "Withholding Tax", formatCurrency(deductions.withholdingTax), "-");
        }

        // Table footer - ensure consistent width
        System.out.println("├───────────────────────┼───────────────────┴───────────────────┤");
        System.out.printf("│ Total Deductions      │ %-35s │\n",
                formatCurrency(totalAttendanceDeductions + deductions.totalDeductions));
        System.out.println("└───────────────────────┴─────────────────────────────────────┘");
    }

    /**
     * Display net pay as a formatted table
     */
    private static void displayNetPayTable(double grossPay, double totalDeductions, double netPay) {
        // Table header
        System.out.println("\n┌───────────────────────────────────────────┐");
        System.out.println("│               NET PAY                     │");
        System.out.println("├───────────────────────┬───────────────────┤");
        System.out.println("│ Category              │ Amount (₱)        │");
        System.out.println("├───────────────────────┼───────────────────┤");

        // Table content
        System.out.printf("│ Gross Pay             │ %-17s │\n", formatCurrency(grossPay));
        System.out.printf("│ Total Deductions      │ %-17s │\n", formatCurrency(totalDeductions));
        System.out.println("├───────────────────────┼───────────────────┤");
        System.out.printf("│ NET PAY               │ %-17s │\n", formatCurrency(netPay));
        System.out.println("└───────────────────────┴───────────────────┘");
    }

    /**
     * Format date for display
     */
    private static String formatDate(LocalDate date) {
        if (date == null) return "";
        return date.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
    }

    /**
     * Format minutes as "HH:MM" or "XX min" for display
     */
    private static String formatMinutes(double minutes) {
        if (minutes <= 0) return "-";

        int hours = (int) (minutes / 60);
        int mins = (int) (minutes % 60);

        if (hours > 0) {
            return String.format("%d:%02d", hours, mins);
        } else {
            return String.format("%d min", mins);
        }
    }

    /**
     * Format currency values for display
     */
    private static String formatCurrency(double amount) {
        return String.format("%,.2f", amount);
    }
}