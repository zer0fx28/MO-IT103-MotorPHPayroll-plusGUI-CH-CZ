// File: motorph/process/PayrollProcessor.java
package motorph.process;

import motorph.deductions.StatutoryDeductions;
import motorph.employee.Employee;
import motorph.employee.EmployeeDataReader;
import motorph.holidays.HolidayManager;
import motorph.hours.AttendanceReader;
import motorph.hours.WorkHoursCalculator;
import motorph.util.TimeConverter;

import java.time.LocalDate;
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
    private final HolidayManager holidayManager;

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
        this.holidayManager = new HolidayManager();
    }

    /**
     * Process payroll for a period
     */
    public void processPayrollForPeriod(Employee employee, double hoursWorked,
                                        double overtimeHours, double lateMinutes,
                                        double undertimeMinutes, boolean isLate,
                                        int payPeriodType, LocalDate startDate, LocalDate endDate,
                                        int year, int month, boolean hasUnpaidAbsences) {

        String employeeId = employee.getEmployeeId();
        double monthlySalary = employee.getBasicSalary();
        double semiMonthlySalary = monthlySalary / 2;  // Half of the monthly salary
        double hourlyRate = employee.getHourlyRate();
        double dailyRate = monthlySalary / PayrollDateManager.getWorkingDaysInYear(year);

        // Base computation is half the monthly salary
        double basePay = semiMonthlySalary;

        // Calculate deductions for late, undertime, and absences

        // Late deduction: per minute rate × late minutes
        double lateDeduction = 0.0;
        if (lateMinutes > 0) {
            double perMinuteRate = hourlyRate / 60.0;
            lateDeduction = perMinuteRate * lateMinutes;
        }

        // Undertime deduction: per minute rate × undertime minutes
        double undertimeDeduction = 0.0;
        if (undertimeMinutes > 0) {
            double perMinuteRate = hourlyRate / 60.0;
            undertimeDeduction = perMinuteRate * undertimeMinutes;
        }

        // Calculate number of working days in the cutoff period
        LocalDate[] cutoffRange = PayrollDateManager.getCutoffDateRange(
                PayrollDateManager.getPayrollDate(year, month, payPeriodType),
                payPeriodType);

        // Calculate the expected working hours for the period
        int workingDaysInPeriod = 10; // Simplified: Assume 10 working days in a period
        double expectedHours = workingDaysInPeriod * 8.0; // 8 hours per day

        // Calculate absences (if any)
        double absentHours = Math.max(0, expectedHours - hoursWorked - (lateMinutes / 60.0) - (undertimeMinutes / 60.0));
        double absentDays = absentHours / 8.0; // Convert hours to days

        // Only apply absence deduction if the absence is unpaid (based on CSV data)
        double absenceDeduction = 0.0;
        if (hasUnpaidAbsences) {
            absenceDeduction = absentDays * dailyRate;
        }

        // Calculate overtime pay
        double overtimePay = 0.0;
        if (overtimeHours > 0 && !isLate) {
            overtimePay = hourlyRate * overtimeHours * 1.25; // 25% overtime premium
        }

        // Calculate holiday pay (simplified - would need to check each day in real implementation)
        double holidayPay = 0.0;
        // Not implementing full holiday check here to keep it simple

        // Calculate gross pay: base pay + overtime - deductions
        double grossPay = basePay + overtimePay + holidayPay - lateDeduction - undertimeDeduction - absenceDeduction;

        // Ensure gross pay is not negative
        grossPay = Math.max(0, grossPay);

        // Full monthly gross (for tax calculation)
        double fullMonthlyGross = grossPay * 2; // Simplified approximation

        // Calculate statutory deductions
        StatutoryDeductions.DeductionResult deductions =
                StatutoryDeductions.calculateDeductions(grossPay, payPeriodType, fullMonthlyGross);

        // Calculate net pay
        double netPay = Math.max(0, grossPay - deductions.totalDeductions);

        // Save calculations
        currentGrossPay.put(employeeId, grossPay);
        currentNetPay.put(employeeId, netPay);
        currentDeductions.put(employeeId, deductions);

        // Save additional details for reports
        Map<String, Object> payDetails = new HashMap<>();
        payDetails.put("regularPay", basePay);
        payDetails.put("overtimePay", overtimePay);
        payDetails.put("holidayPay", holidayPay);
        payDetails.put("lateDeduction", lateDeduction);
        payDetails.put("undertimeDeduction", undertimeDeduction);
        payDetails.put("absenceDeduction", absenceDeduction);
        payDetails.put("hoursWorked", hoursWorked);
        payDetails.put("overtimeHours", overtimeHours);
        payDetails.put("lateMinutes", lateMinutes);
        payDetails.put("undertimeMinutes", undertimeMinutes);
        payDetails.put("expectedHours", expectedHours);
        payDetails.put("absentHours", absentHours);
        payDetails.put("dailyRate", dailyRate);
        payDetails.put("startDate", startDate);
        payDetails.put("endDate", endDate);
        payDetails.put("payPeriod", payPeriodType);
        payDetails.put("hourlyRate", hourlyRate);
        payDetails.put("year", year);
        payDetails.put("month", month);
        payDetails.put("hasUnpaidAbsences", hasUnpaidAbsences);

        currentPayDetails.put(employeeId, payDetails);
    }

    /**
     * Process payroll with the original signature (for backward compatibility)
     */
    public void processPayrollForPeriod(Employee employee, double hoursWorked,
                                        double overtimeHours, double lateMinutes,
                                        double undertimeMinutes, boolean isLate,
                                        int payPeriodType, LocalDate startDate, LocalDate endDate,
                                        int year, int month) {
        // Default to not having unpaid absences when not specified
        processPayrollForPeriod(employee, hoursWorked, overtimeHours, lateMinutes,
                undertimeMinutes, isLate, payPeriodType, startDate,
                endDate, year, month, false);
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
        double holidayPay = (double) payDetails.getOrDefault("holidayPay", 0.0);
        double lateDeduction = (double) payDetails.get("lateDeduction");
        double undertimeDeduction = (double) payDetails.getOrDefault("undertimeDeduction", 0.0);
        double absenceDeduction = (double) payDetails.getOrDefault("absenceDeduction", 0.0);
        double hourlyRate = (double) payDetails.get("hourlyRate");
        double expectedHours = (double) payDetails.getOrDefault("expectedHours", 0.0);
        double absentHours = (double) payDetails.getOrDefault("absentHours", 0.0);
        double dailyRate = (double) payDetails.getOrDefault("dailyRate", 0.0);
        int payPeriod = (int) payDetails.get("payPeriod");
        int year = (int) payDetails.getOrDefault("year", LocalDate.now().getYear());
        int month = (int) payDetails.getOrDefault("month", LocalDate.now().getMonthValue());
        boolean hasUnpaidAbsences = (boolean) payDetails.getOrDefault("hasUnpaidAbsences", false);

        // Display payroll details
        System.out.println("\n===== SALARY CALCULATION =====");
        System.out.println("Date: " + formattedDate);
        System.out.println("Employee: " + employee.getFullName() + " (ID: " + employeeId + ")");
        System.out.println("Position: " + employee.getPosition());
        System.out.println("Basic Salary: ₱" + String.format("%,.2f", employee.getBasicSalary()) + "/month");
        System.out.println("Hourly Rate: ₱" + String.format("%.2f", hourlyRate) + "/hour");
        System.out.println("Daily Rate: ₱" + String.format("%.2f", dailyRate) + "/day");

        // Show cutoff period and payroll date
        if (startDate != null && endDate != null) {
            System.out.println("Cutoff Period: " + startDate.format(dateFormatter) +
                    " to " + endDate.format(dateFormatter));

            LocalDate payrollDate = PayrollDateManager.getPayrollDate(year, month, payPeriod);
            System.out.println("Payroll Date: " + payrollDate.format(dateFormatter) +
                    (payPeriod == PayrollDateManager.MID_MONTH ? " (Mid-month)" : " (End-month)"));
        }

        System.out.println("\n--- WORK SUMMARY ---");
        System.out.println("Expected Work Hours: " + String.format("%.2f", expectedHours) + " hours");
        System.out.println("Actual Hours Worked: " + String.format("%.2f", hoursWorked) + " hours");
        System.out.println("Overtime Hours: " + String.format("%.2f", overtimeHours) + " hours");
        System.out.println("Late: " + String.format("%.0f", lateMinutes) + " minutes");
        System.out.println("Undertime: " + String.format("%.0f", undertimeMinutes) + " minutes");
        System.out.println("Absent Hours: " + String.format("%.2f", absentHours) +
                " hours (" + String.format("%.2f", absentHours/8) + " days)");
        if (absentHours > 0) {
            System.out.println("Unpaid Absences: " + (hasUnpaidAbsences ? "Yes" : "No"));
        }

        System.out.println("\n--- EARNINGS ---");
        System.out.println("Basic Pay: ₱" + String.format("%,.2f", regularPay));

        if (overtimePay > 0) {
            System.out.println("Overtime Pay: ₱" + String.format("%,.2f", overtimePay) +
                    " (" + String.format("%.2f", overtimeHours) + " hrs × ₱" +
                    String.format("%.2f", hourlyRate * 1.25) + ")");
        } else {
            System.out.println("Overtime Pay: ₱0.00");
        }

        if (holidayPay > 0) {
            System.out.println("Holiday Pay: ₱" + String.format("%,.2f", holidayPay));
        }

        System.out.println("\n--- DEDUCTIONS ---");
        System.out.println("Late Deduction: ₱" + String.format("%,.2f", lateDeduction) +
                (lateMinutes > 0 ? " (" + String.format("%.0f", lateMinutes) +
                        " mins × ₱" + String.format("%.4f", hourlyRate/60) + ")" : ""));

        System.out.println("Undertime Deduction: ₱" + String.format("%,.2f", undertimeDeduction) +
                (undertimeMinutes > 0 ? " (" + String.format("%.0f", undertimeMinutes) +
                        " mins × ₱" + String.format("%.4f", hourlyRate/60) + ")" : ""));

        System.out.println("Absence Deduction: ₱" + String.format("%,.2f", absenceDeduction) +
                (absentHours > 0 && hasUnpaidAbsences ? " (" + String.format("%.2f", absentHours/8) +
                        " days × ₱" + String.format("%.2f", dailyRate) + ")" : ""));

        // Show statutory deductions based on period
        if (payPeriod == PayrollDateManager.MID_MONTH) {
            System.out.println("SSS: ₱" + String.format("%.2f", deductions.sssDeduction));
            System.out.println("PhilHealth: ₱" + String.format("%.2f", deductions.philhealthDeduction));
            System.out.println("Pag-IBIG: ₱" + String.format("%.2f", deductions.pagibigDeduction));
            System.out.println("Withholding Tax: ₱" + String.format("%.2f", deductions.withholdingTax) + " (Deducted on End-month)");
        } else {
            System.out.println("SSS: ₱0.00 (Deducted on Mid-month)");
            System.out.println("PhilHealth: ₱0.00 (Deducted on Mid-month)");
            System.out.println("Pag-IBIG: ₱0.00 (Deducted on Mid-month)");
            System.out.println("Withholding Tax: ₱" + String.format("%.2f", deductions.withholdingTax));
        }

        System.out.println("Total Deductions: ₱" + String.format("%.2f", deductions.totalDeductions));

        System.out.println("\n--- NET PAY ---");
        System.out.println("Gross Pay: ₱" + String.format("%,.2f", grossPay));
        System.out.println("Net Pay: ₱" + String.format("%,.2f", netPay));

        // Show notes about policies
        System.out.println("\n--- POLICIES ---");
        if (payPeriod == PayrollDateManager.MID_MONTH) {
            System.out.println("- SSS, PhilHealth, and Pag-IBIG are deducted on Mid-month payroll.");
        } else {
            System.out.println("- Withholding Tax is deducted on End-month payroll.");
        }

        if (lateMinutes > 0) {
            System.out.println("- Late employees cannot earn overtime pay.");
        }

        if (undertimeMinutes > 0) {
            System.out.println("- Early departure deductions apply for leaving before 5:00 PM.");
        }

        if (absentHours > 0) {
            System.out.println("- Only unpaid, unauthorized, or unapproved absences are deducted from salary.");
        }
    }
}