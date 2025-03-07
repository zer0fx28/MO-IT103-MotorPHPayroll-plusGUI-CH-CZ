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
 * Main processor for payroll calculations
 */
public class PayrollProcessor {
    private final EmployeeDataReader employeeDataReader;
    private final AttendanceReader attendanceReader;
    private final WorkHoursCalculator hoursCalculator;

    // Maps to store calculated values for each employee
    private final Map<String, Double> currentGrossPay = new HashMap<>();
    private final Map<String, Double> currentNetPay = new HashMap<>();
    private final Map<String, StatutoryDeductions.DeductionResult> currentDeductions = new HashMap<>();
    private final Map<String, Map<String, Object>> currentPayDetails = new HashMap<>();

    /**
     * Constructor initializes the processor with data sources
     * @param employeeFilePath Path to employee data CSV
     * @param attendanceFilePath Path to attendance data CSV
     */
    public PayrollProcessor(String employeeFilePath, String attendanceFilePath) {
        this.employeeDataReader = new EmployeeDataReader(employeeFilePath);
        this.attendanceReader = new AttendanceReader(attendanceFilePath);
        this.hoursCalculator = new WorkHoursCalculator();
    }

    /**
     * Calculate daily hours from time strings
     * @param timeInStr Time in string (e.g., "8:00 AM")
     * @param timeOutStr Time out string (e.g., "5:00 PM")
     * @return Hours worked
     */
    public double calculateDailyHoursFromStrings(String timeInStr, String timeOutStr) {
        return hoursCalculator.calculateDailyHoursFromStrings(timeInStr, timeOutStr);
    }

    /**
     * Calculate late minutes from time string
     * @param timeInStr Time in string (e.g., "8:15 AM")
     * @return Minutes late
     */
    public double calculateLateMinutesFromString(String timeInStr) {
        return hoursCalculator.calculateLateMinutesFromString(timeInStr);
    }

    /**
     * Calculate overtime from time string
     * @param timeOutStr Time out string (e.g., "6:30 PM")
     * @return Overtime hours
     * Note: This doesn't check for lateness - use other method when lateness info is available
     */
    public double calculateOvertimeFromString(String timeOutStr) {
        return hoursCalculator.calculateOvertimeFromString(timeOutStr);
    }

    /**
     * Calculate overtime from time string, considering lateness
     * @param timeOutStr Time out string (e.g., "6:30 PM")
     * @param isLate Whether employee is late
     * @return Overtime hours (0 if late)
     */
    public double calculateOvertimeFromStringWithLate(String timeOutStr, boolean isLate) {
        if (isLate) {
            return 0.0; // No overtime for late employees
        }
        return hoursCalculator.calculateOvertimeFromString(timeOutStr);
    }

    /**
     * Process individual payroll for a day
     */
    public void processIndividualPayroll(Employee employee, double hoursWorked,
                                         double overtimeHours, double lateMinutes,
                                         boolean isLate, int payPeriod) {

        processPayrollForPeriod(employee, hoursWorked, overtimeHours, lateMinutes,
                isLate, payPeriod, null, null);
    }

    /**
     * Process payroll for a specific period
     */
    public void processPayrollForPeriod(Employee employee, double hoursWorked,
                                        double overtimeHours, double lateMinutes,
                                        boolean isLate, int payPeriod,
                                        LocalDate startDate, LocalDate endDate) {

        String employeeId = employee.getEmployeeId();
        double hourlyRate = employee.getHourlyRate();

        // Apply policy: Late employees get no overtime but can complete 8 hours
        if (isLate) {
            // Late employees can still earn up to 8 hours
            hoursWorked = Math.min(hoursWorked, 8.0);
            // No overtime for late employees
            overtimeHours = 0.0;
        } else {
            // Regular employees are capped at 8 regular hours
            hoursWorked = Math.min(hoursWorked, 8.0);
        }

        // Calculate regular pay for the hours worked
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

            // Ensure late deduction doesn't exceed a reasonable amount
            // Cap it at 50% of regular pay as a safety measure
            lateDeduction = Math.min(lateDeduction, regularPay * 0.5);
        }

        // Calculate gross pay (ensure it's not negative)
        double grossPay = Math.max(0, regularPay + overtimePay - lateDeduction);

        // Calculate statutory deductions
        StatutoryDeductions.DeductionResult deductions =
                StatutoryDeductions.calculateDeductions(grossPay, payPeriod);

        // Calculate net pay (ensure it's not negative)
        double netPay = Math.max(0, grossPay - deductions.totalDeductions);

        // Store calculations for this employee
        currentGrossPay.put(employeeId, grossPay);
        currentNetPay.put(employeeId, netPay);
        currentDeductions.put(employeeId, deductions);

        // Store additional details for reporting
        Map<String, Object> payDetails = new HashMap<>();
        payDetails.put("regularPay", regularPay);
        payDetails.put("overtimePay", overtimePay);
        payDetails.put("lateDeduction", lateDeduction);
        payDetails.put("hoursWorked", hoursWorked);
        payDetails.put("overtimeHours", overtimeHours);
        payDetails.put("lateMinutes", lateMinutes);
        payDetails.put("startDate", startDate);
        payDetails.put("endDate", endDate);
        payDetails.put("payPeriod", payPeriod);
        payDetails.put("hourlyRate", hourlyRate);

        currentPayDetails.put(employeeId, payDetails);
    }

    /**
     * Display salary details for an employee
     */
    public void displaySalaryDetails(Employee employee) {
        String employeeId = employee.getEmployeeId();

        // Get stored calculations
        double grossPay = currentGrossPay.getOrDefault(employeeId, 0.0);
        double netPay = currentNetPay.getOrDefault(employeeId, 0.0);
        StatutoryDeductions.DeductionResult deductions = currentDeductions.get(employeeId);
        Map<String, Object> payDetails = currentPayDetails.get(employeeId);

        if (deductions == null || payDetails == null) {
            System.out.println("No payroll data available for this employee.");
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
        double regularPay = (double) payDetails.get("regularPay");
        double overtimePay = (double) payDetails.get("overtimePay");
        double lateDeduction = (double) payDetails.get("lateDeduction");
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

        System.out.println("\n--- EARNINGS ---");
        System.out.println("Basic Pay: ₱" + String.format("%,.2f", regularPay) +
                " (" + String.format("%.2f", hoursWorked) + " hrs × ₱" +
                String.format("%.2f", hourlyRate) + ")");
        System.out.println("Overtime Pay: ₱" + String.format("%,.2f", overtimePay) +
                (overtimeHours > 0 ? " (" + String.format("%.2f", overtimeHours) +
                        " hrs × ₱" + String.format("%.2f", hourlyRate * 1.5) + ")" : ""));
        System.out.println("Late Deduction: ₱" + String.format("%,.2f", lateDeduction) +
                (lateMinutes > 0 ? " (" + String.format("%.0f", lateMinutes) +
                        " mins × ₱" + String.format("%.4f", hourlyRate/60) + ")" : ""));
        System.out.println("Gross Pay: ₱" + String.format("%,.2f", grossPay));

        System.out.println("\n--- DEDUCTIONS ---");
        System.out.println("SSS: ₱" + String.format("%.2f", deductions.sssDeduction));
        System.out.println("PhilHealth: ₱" + String.format("%.2f", deductions.philhealthDeduction));
        System.out.println("Pag-IBIG: ₱" + String.format("%.2f", deductions.pagibigDeduction));
        System.out.println("Withholding Tax: ₱" + String.format("%.2f", deductions.withholdingTax));
        System.out.println("Total Deductions: ₱" + String.format("%.2f", deductions.totalDeductions));

        System.out.println("\n--- NET PAY ---");
        System.out.println("Net Pay: ₱" + String.format("%,.2f", netPay));

        // Display note about late employees' pay
        if (lateMinutes > 0) {
            System.out.println("\nNote: Employee was late but still able to complete up to 8 hours of work.");
            System.out.println("Late deductions are calculated separately from regular pay.");
        }
    }
}