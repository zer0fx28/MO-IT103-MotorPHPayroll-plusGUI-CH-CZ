package motorph.process;

import motorph.process.PayrollDateManager;
import motorph.deductions.StatutoryDeductions;
import motorph.employee.Employee;
import motorph.employee.EmployeeDataReader;
import motorph.hours.AttendanceReader;
import motorph.util.DateTimeUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles all payroll calculations
 * Core class for processing employee payroll
 */
public class PayrollProcessor {
    // Data readers
    private final EmployeeDataReader employeeDataReader;
    private final AttendanceReader attendanceReader;

    // Storage for calculated values
    private final Map<String, PayrollResult> payrollResults = new HashMap<>();

    /**
     * Create a new PayrollProcessor
     *
     * @param employeeFilePath Path to employee data CSV
     * @param attendanceFilePath Path to attendance data CSV
     */
    public PayrollProcessor(String employeeFilePath, String attendanceFilePath) {
        this.employeeDataReader = new EmployeeDataReader(employeeFilePath);
        this.attendanceReader = new AttendanceReader(attendanceFilePath);
    }

    /**
     * Process payroll for an employee
     *
     * @param employee The employee to process
     * @param hoursWorked Total hours worked
     * @param overtimeHours Overtime hours
     * @param lateMinutes Minutes late
     * @param undertimeMinutes Minutes undertime
     * @param isLateAnyDay Whether employee was late
     * @param payPeriodType Pay period type (MID_MONTH or END_MONTH)
     * @param startDate Start date of pay period
     * @param endDate End date of pay period
     * @param year Year of payroll
     * @param month Month of payroll
     * @param hasUnpaidAbsences Whether employee has unpaid absences
     * @return PayrollResult containing calculation results
     */
    public PayrollResult processPayroll(
            Employee employee,
            double hoursWorked,
            double overtimeHours,
            double lateMinutes,
            double undertimeMinutes,
            boolean isLateAnyDay,
            int payPeriodType,
            LocalDate startDate,
            LocalDate endDate,
            int year,
            int month,
            boolean hasUnpaidAbsences) {

        // Validate input
        if (employee == null) {
            throw new IllegalArgumentException("Employee cannot be null");
        }

        // Ensure non-negative values
        hoursWorked = Math.max(0, hoursWorked);
        overtimeHours = Math.max(0, overtimeHours);
        lateMinutes = Math.max(0, lateMinutes);
        undertimeMinutes = Math.max(0, undertimeMinutes);

        String employeeId = employee.getEmployeeId();
        double monthlySalary = employee.getBasicSalary();
        double semiMonthlySalary = employee.getSemiMonthlyRate();
        double hourlyRate = employee.getHourlyRate();
        double dailyRate = employee.getDailyRate();

        // Base computation is the semi-monthly rate
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

        // Calculate expected working hours for the period
        int workingDaysInPeriod = DateTimeUtil.daysBetween(startDate, endDate);
        workingDaysInPeriod = Math.min(workingDaysInPeriod, 12); // Maximum 12 working days per period
        double expectedHours = workingDaysInPeriod * 8.0; // 8 hours per day

        // Calculate absences (if any)
        double absentHours = Math.max(0, expectedHours - hoursWorked - (lateMinutes / 60.0) - (undertimeMinutes / 60.0));
        double absentDays = absentHours / 8.0; // Convert hours to days

        // Only apply absence deduction if the absence is unpaid
        double absenceDeduction = 0.0;
        if (hasUnpaidAbsences && absentDays > 0) {
            absenceDeduction = absentDays * dailyRate;
        }

        // Calculate overtime pay
        double overtimePay = 0.0;
        if (overtimeHours > 0 && !isLateAnyDay) {
            overtimePay = hourlyRate * overtimeHours * 1.25; // 25% overtime premium
        }

        // Calculate holiday pay
        double holidayPay = 0.0;
        // This would check if any of the days worked were holidays
        // Simplified implementation - would need to check each day in real system

        // Calculate gross pay: base pay + overtime - deductions
        double grossPay = basePay + overtimePay + holidayPay - lateDeduction - undertimeDeduction - absenceDeduction;

        // Ensure gross pay is not negative
        grossPay = Math.max(0, grossPay);

        // Full monthly gross (for tax calculation)
        double fullMonthlyGross = monthlySalary; // Use actual monthly salary

        // Calculate statutory deductions
        StatutoryDeductions.DeductionResult deductions =
                StatutoryDeductions.calculateDeductions(grossPay, payPeriodType, fullMonthlyGross);

        // Calculate net pay
        double netPay = Math.max(0, grossPay - deductions.totalDeductions);

        // Create result object
        PayrollResult result = new PayrollResult(
                employee, grossPay, netPay, deductions,
                basePay, overtimePay, holidayPay, lateDeduction,
                undertimeDeduction, absenceDeduction, hoursWorked,
                overtimeHours, lateMinutes, undertimeMinutes,
                expectedHours, absentHours, dailyRate, startDate,
                endDate, payPeriodType, hourlyRate, year, month,
                hasUnpaidAbsences
        );

        // Save result
        payrollResults.put(employeeId, result);

        return result;
    }

    /**
     * Get the most recent payroll result for an employee
     *
     * @param employeeId The employee ID
     * @return PayrollResult or null if not found
     */
    public PayrollResult getPayrollResult(String employeeId) {
        return payrollResults.get(employeeId);
    }

    /**
     * Display salary details for an employee
     *
     * @param employee The employee to display details for
     */
    public void displaySalaryDetails(Employee employee) {
        if (employee == null) {
            System.out.println("No employee provided");
            return;
        }

        String employeeId = employee.getEmployeeId();
        PayrollResult result = payrollResults.get(employeeId);

        if (result == null) {
            System.out.println("No payroll data for this employee.");
            return;
        }

        // Display payroll details
        System.out.println("\n===== SALARY CALCULATION =====");
        System.out.println("Date: " + DateTimeUtil.formatDate(LocalDate.now()));
        System.out.println("Employee: " + employee.getFullName() + " (ID: " + employeeId + ")");
        System.out.println("Position: " + employee.getPosition());
        System.out.println("Basic Salary: ₱" + String.format("%,.2f", employee.getBasicSalary()) + "/month");
        System.out.println("Hourly Rate: ₱" + String.format("%.2f", result.hourlyRate) + "/hour");
        System.out.println("Daily Rate: ₱" + String.format("%.2f", result.dailyRate) + "/day");

        // Show cutoff period and payroll date
        System.out.println("Cutoff Period: " + DateTimeUtil.formatDate(result.startDate) +
                " to " + DateTimeUtil.formatDate(result.endDate));

        LocalDate payrollDate = PayrollDateManager.getPayrollDate(result.year, result.month, result.payPeriodType);
        System.out.println("Payroll Date: " + DateTimeUtil.formatDate(payrollDate) +
                (result.payPeriodType == PayrollDateManager.MID_MONTH ? " (Mid-month)" : " (End-month)"));

        // ... (rest of the existing display logic remains the same)
    }

    /**
     * Inner class to store payroll calculation results
     */
    public static class PayrollResult {
        // Basic information
        public final Employee employee;
        public final double grossPay;
        public final double netPay;
        public final StatutoryDeductions.DeductionResult deductions;

        // Earning components
        public final double basePay;
        public final double overtimePay;
        public final double holidayPay;

        // Deduction components
        public final double lateDeduction;
        public final double undertimeDeduction;
        public final double absenceDeduction;

        // Work details
        public final double hoursWorked;
        public final double overtimeHours;
        public final double lateMinutes;
        public final double undertimeMinutes;
        public final double expectedHours;
        public final double absentHours;

        // Rates
        public final double dailyRate;
        public final double hourlyRate;

        // Period information
        public final LocalDate startDate;
        public final LocalDate endDate;
        public final int payPeriodType;
        public final int year;
        public final int month;

        // Flags
        public final boolean hasUnpaidAbsences;

        /**
         * Create a new PayrollResult
         */
        public PayrollResult(
                Employee employee,
                double grossPay,
                double netPay,
                StatutoryDeductions.DeductionResult deductions,
                double basePay,
                double overtimePay,
                double holidayPay,
                double lateDeduction,
                double undertimeDeduction,
                double absenceDeduction,
                double hoursWorked,
                double overtimeHours,
                double lateMinutes,
                double undertimeMinutes,
                double expectedHours,
                double absentHours,
                double dailyRate,
                LocalDate startDate,
                LocalDate endDate,
                int payPeriodType,
                double hourlyRate,
                int year,
                int month,
                boolean hasUnpaidAbsences) {

            this.employee = employee;
            this.grossPay = grossPay;
            this.netPay = netPay;
            this.deductions = deductions;
            this.basePay = basePay;
            this.overtimePay = overtimePay;
            this.holidayPay = holidayPay;
            this.lateDeduction = lateDeduction;
            this.undertimeDeduction = undertimeDeduction;
            this.absenceDeduction = absenceDeduction;
            this.hoursWorked = hoursWorked;
            this.overtimeHours = overtimeHours;
            this.lateMinutes = lateMinutes;
            this.undertimeMinutes = undertimeMinutes;
            this.expectedHours = expectedHours;
            this.absentHours = absentHours;
            this.dailyRate = dailyRate;
            this.startDate = startDate;
            this.endDate = endDate;
            this.payPeriodType = payPeriodType;
            this.hourlyRate = hourlyRate;
            this.year = year;
            this.month = month;
            this.hasUnpaidAbsences = hasUnpaidAbsences;
        }
    }
}