// File: motorph/process/PayrollProcessor.java
package motorph.process;

import motorph.deductions.StatutoryDeductions;
import motorph.employee.Employee;
import motorph.employee.EmployeeDataReader;
import motorph.hours.AttendanceReader;
import motorph.hours.WorkHoursCalculator;
import motorph.util.TimeConverter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles all payroll calculations
 */
public class PayrollProcessor {
    private final EmployeeDataReader employeeDataReader;
    private final AttendanceReader attendanceReader;
    private final WorkHoursCalculator hoursCalculator;

    // Storage for calculated values
    private final Map<String, Double> currentGrossPay = new HashMap<>();
    private final Map<String, Double> currentNetPay = new HashMap<>();
    private final Map<String, StatutoryDeductions.DeductionResult> currentDeductions = new HashMap<>();
    private final Map<String, Map<String, Object>> currentPayDetails = new HashMap<>();

    /**
     * Set up processor with data files
     */
    public PayrollProcessor(String employeeFilePath, String attendanceFilePath) {
        this.employeeDataReader = new EmployeeDataReader(employeeFilePath);
        this.attendanceReader = new AttendanceReader(attendanceFilePath);
        this.hoursCalculator = new WorkHoursCalculator();
    }

    /**
     * Calculate hours worked from time strings
     */
    public double calculateDailyHoursFromStrings(String timeInStr, String timeOutStr) {
        return hoursCalculator.calculateDailyHoursFromStrings(timeInStr, timeOutStr);
    }

    /**
     * Calculate minutes late
     */
    public double calculateLateMinutesFromString(String timeInStr) {
        return hoursCalculator.calculateLateMinutesFromString(timeInStr);
    }

    /**
     * Calculate minutes undertime (left before 5 PM)
     */
    public double calculateUndertimeMinutesFromString(String timeOutStr) {
        return hoursCalculator.calculateUndertimeMinutesFromString(timeOutStr);
    }

    /**
     * Calculate overtime hours
     */
    public double calculateOvertimeFromString(String timeOutStr) {
        return hoursCalculator.calculateOvertimeFromString(timeOutStr);
    }

    /**
     * Calculate overtime with lateness considered
     */
    public double calculateOvertimeFromStringWithLate(String timeOutStr, boolean isLate) {
        return hoursCalculator.calculateOvertimeFromStringWithLate(timeOutStr, isLate);
    }

    /**
     * Process payroll for one employee day
     */
    public void processIndividualPayroll(Employee employee, double hoursWorked,
                                         double overtimeHours, double lateMinutes,
                                         double undertimeMinutes, boolean isLate,
                                         int payPeriod) {

        processPayrollForPeriod(employee, hoursWorked, overtimeHours, lateMinutes,
                undertimeMinutes, isLate, payPeriod, null, null);
    }

    /**
     * Process payroll for an entire period
     */
    public void processPayrollForPeriod(Employee employee, double hoursWorked,
                                        double overtimeHours, double lateMinutes,
                                        double undertimeMinutes, boolean isLate,
                                        int payPeriod, LocalDate startDate, LocalDate endDate) {

        String employeeId = employee.getEmployeeId();
        double hourlyRate = employee.getHourlyRate();

        // Calculate regular pay for the total hours worked
        double regularPay = hourlyRate * hoursWorked;

        // Calculate overtime pay (1.5x regular rate)
        double overtimePay = 0.0;
        if (overtimeHours > 0) {
            overtimePay = hourlyRate * overtimeHours * 1.5;
        }

        // Calculate late deduction (per minute rate * late minutes)
        double lateDeduction = 0.0;
        if (lateMinutes > 0) {
            double perMinuteRate = hourlyRate / 60.0;
            lateDeduction = perMinuteRate * lateMinutes;
        }

        // Calculate undertime deduction (per minute rate * undertime minutes)
        double undertimeDeduction = 0.0;
        if (undertimeMinutes > 0) {
            double perMinuteRate = hourlyRate / 60.0;
            undertimeDeduction = perMinuteRate * undertimeMinutes;
        }

        // Calculate gross pay (never less than zero)
        double grossPay = Math.max(0, regularPay + overtimePay - lateDeduction - undertimeDeduction);

        // Calculate government deductions
        StatutoryDeductions.DeductionResult deductions =
                StatutoryDeductions.calculateDeductions(grossPay, payPeriod);

        // Calculate net pay (never less than zero)
        double netPay = Math.max(0, grossPay - deductions.totalDeductions);

        // Save calculations
        currentGrossPay.put(employeeId, grossPay);
        currentNetPay.put(employeeId, netPay);
        currentDeductions.put(employeeId, deductions);

        // Save additional details for reports
        Map<String, Object> payDetails = new HashMap<>();
        payDetails.put("regularPay", regularPay);
        payDetails.put("overtimePay", overtimePay);
        payDetails.put("lateDeduction", lateDeduction);
        payDetails.put("undertimeDeduction", undertimeDeduction);
        payDetails.put("hoursWorked", hoursWorked);
        payDetails.put("overtimeHours", overtimeHours);
        payDetails.put("lateMinutes", lateMinutes);
        payDetails.put("undertimeMinutes", undertimeMinutes);
        payDetails.put("startDate", startDate);
        payDetails.put("endDate", endDate);
        payDetails.put("payPeriod", payPeriod);
        payDetails.put("hourlyRate", hourlyRate);

        currentPayDetails.put(employeeId, payDetails);
    }

    /**
     * Show salary breakdown for an employee
     */
    public void displaySalaryDetails(Employee employee) {
        String employeeId = employee.getEmployeeId();

        // Get saved calculations
        double grossPay = currentGrossPay.getOrDefault(employeeId, 0.0);
        double netPay = currentNetPay.getOrDefault(employeeId, 0.0);
        StatutoryDeductions.DeductionResult deductions = currentDeductions.get(employeeId);
        Map<String, Object> payDetails = currentPayDetails.get(employeeId);

        if (deductions == null || payDetails == null) {
            System.out.println("No payroll data for this employee.");
            return;
        }

        // Get current date for the pay slip
        LocalDate today = LocalDate.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        String formattedDate = today.format(dateFormatter);

        // Get period dates if available
        LocalDate startDate = (LocalDate) payDetails.get("startDate");
        LocalDate endDate = (LocalDate) payDetails.get("endDate");
        String periodStr = "";

        if (startDate != null && endDate != null) {
            periodStr = "Period: " + startDate.format(dateFormatter) +
                    " to " + endDate.format(dateFormatter);
        }

        // Get work details
        double hoursWorked = (double) payDetails.get("hoursWorked");
        double overtimeHours = (double) payDetails.get("overtimeHours");
        double lateMinutes = (double) payDetails.get("lateMinutes");
        double undertimeMinutes = (double) payDetails.getOrDefault("undertimeMinutes", 0.0);
        double regularPay = (double) payDetails.get("regularPay");
        double overtimePay = (double) payDetails.get("overtimePay");
        double lateDeduction = (double) payDetails.get("lateDeduction");
        double undertimeDeduction = (double) payDetails.getOrDefault("undertimeDeduction", 0.0);
        double hourlyRate = (double) payDetails.get("hourlyRate");

        System.out.println("\n===== SALARY CALCULATION =====");
        System.out.println("Date: " + formattedDate);
        System.out.println("Employee: " + employee.getFullName() + " (ID: " + employeeId + ")");
        System.out.println("Position: " + employee.getPosition());
        System.out.println("Hourly Rate: ₱" + String.format("%.2f", hourlyRate) + "/hour" +
                " (₱" + String.format("%.4f", hourlyRate/60) + "/minute)");

        if (!periodStr.isEmpty()) {
            System.out.println(periodStr);
        }

        System.out.println("\n--- WORK SUMMARY ---");
        System.out.println("Regular Hours: " + String.format("%.2f", hoursWorked) + " hours");
        System.out.println("Overtime Hours: " + String.format("%.2f", overtimeHours) + " hours");
        System.out.println("Late: " + String.format("%.0f", lateMinutes) + " minutes");
        System.out.println("Undertime: " + String.format("%.0f", undertimeMinutes) + " minutes");

        System.out.println("\n--- EARNINGS ---");
        System.out.println("Basic Pay: ₱" + String.format("%,.2f", regularPay) +
                " (" + String.format("%.2f", hoursWorked) + " hrs × ₱" +
                String.format("%.2f", hourlyRate) + ")");
        System.out.println("Overtime Pay: ₱" + String.format("%,.2f", overtimePay) +
                (overtimeHours > 0 ? " (" + String.format("%.2f", overtimeHours) +
                        " hrs × ₱" + String.format("%.2f", hourlyRate * 1.5) + ")" : ""));

        System.out.println("\n--- DEDUCTIONS ---");
        System.out.println("Late Deduction: ₱" + String.format("%,.2f", lateDeduction) +
                (lateMinutes > 0 ? " (" + String.format("%.0f", lateMinutes) +
                        " mins × ₱" + String.format("%.4f", hourlyRate/60) + ")" : ""));
        System.out.println("Undertime Deduction: ₱" + String.format("%,.2f", undertimeDeduction) +
                (undertimeMinutes > 0 ? " (" + String.format("%.0f", undertimeMinutes) +
                        " mins × ₱" + String.format("%.4f", hourlyRate/60) + ")" : ""));
        System.out.println("SSS: ₱" + String.format("%.2f", deductions.sssDeduction));
        System.out.println("PhilHealth: ₱" + String.format("%.2f", deductions.philhealthDeduction));
        System.out.println("Pag-IBIG: ₱" + String.format("%.2f", deductions.pagibigDeduction));
        System.out.println("Withholding Tax: ₱" + String.format("%.2f", deductions.withholdingTax));
        System.out.println("Total Deductions: ₱" + String.format("%.2f", deductions.totalDeductions));

        System.out.println("\n--- NET PAY ---");
        System.out.println("Gross Pay: ₱" + String.format("%,.2f", grossPay));
        System.out.println("Net Pay: ₱" + String.format("%,.2f", netPay));

        // Show notes about policies
        System.out.println("\n--- NOTES ---");
        if (lateMinutes > 0) {
            System.out.println("- Employee was late. No overtime allowed.");
        }
        if (undertimeMinutes > 0) {
            System.out.println("- Employee left before 5:00 PM. Undertime deduction applied.");
        }
    }
}