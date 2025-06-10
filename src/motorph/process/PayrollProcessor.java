package motorph.process;

import motorph.employee.EmployeeDataReader;
import motorph.deductions.StatutoryDeductions;
import motorph.employee.Employee;
import motorph.hours.AttendanceReader;
import motorph.holidays.HolidayManager;
import motorph.holidays.HolidayPayCalculator;
import motorph.util.DateTimeUtil;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles all payroll calculations
 * Core class for processing employee payroll
 */
public class PayrollProcessor {
    // Data readers
    private final EmployeeDataReader EmployeeDataReader;
    private final AttendanceReader attendanceReader;

    // Holiday handling
    private final HolidayManager holidayManager;
    private final HolidayPayCalculator holidayPayCalculator;

    // Storage for calculated values
    private final Map<String, PayrollResult> payrollResults = new HashMap<>();

    /**
     * Create a new PayrollProcessor
     *
     * @param employeeFilePath Path to employee data CSV
     * @param attendanceFilePath Path to attendance data CSV
     */
    public PayrollProcessor(String employeeFilePath, String attendanceFilePath) {
        this.EmployeeDataReader = new EmployeeDataReader(employeeFilePath);
        this.attendanceReader = new AttendanceReader(attendanceFilePath);
        this.holidayManager = new HolidayManager();
        this.holidayPayCalculator = new HolidayPayCalculator(holidayManager);
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
        double lateDeduction = lateMinutes > 0 ? (hourlyRate / 60.0) * lateMinutes : 0.0;
        double undertimeDeduction = undertimeMinutes > 0 ? (hourlyRate / 60.0) * undertimeMinutes : 0.0;

        // Calculate expected working hours for the period
        int workingDaysInPeriod = DateTimeUtil.daysBetween(startDate, endDate);
        workingDaysInPeriod = Math.min(workingDaysInPeriod, 12); // Maximum 12 working days per period
        double expectedHours = workingDaysInPeriod * 8.0; // 8 hours per day

        // Calculate absences (if any)
        double absentHours = Math.max(0, expectedHours - hoursWorked - (lateMinutes / 60.0) - (undertimeMinutes / 60.0));
        double absentDays = absentHours / 8.0; // Convert hours to days
        double absenceDeduction = hasUnpaidAbsences && absentDays > 0 ? absentDays * dailyRate : 0.0;

        // Calculate overtime pay
        double overtimePay = !isLateAnyDay && overtimeHours > 0 ? hourlyRate * overtimeHours * 1.25 : 0.0;

        // Calculate holiday pay for each day in the period
        double holidayPay = 0.0;
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            if (holidayManager.isHoliday(currentDate)) {
                Map<LocalDate, Map<String, Object>> dailyAttendance =
                        attendanceReader.getDailyAttendanceForEmployee(employee.getEmployeeId(), currentDate, currentDate);
                Map<String, Object> dayData = dailyAttendance.isEmpty() ? null : dailyAttendance.get(currentDate);

                double dayHoursWorked = dayData != null ? (double) dayData.getOrDefault("hours", 0.0) : 0.0;
                double dayOvertimeHours = dayData != null ? (double) dayData.getOrDefault("overtimeHours", 0.0) : 0.0;
                boolean dayIsLate = dayData != null ? (boolean) dayData.getOrDefault("isLate", false) : false;

                boolean isRestDay = currentDate.getDayOfWeek().getValue() == 7;
                double dayHolidayPay = holidayPayCalculator.calculateHolidayPay(
                        currentDate, dailyRate, dayHoursWorked, isRestDay, dayIsLate, dayOvertimeHours);

                holidayPay += dayHolidayPay;
            }
            currentDate = currentDate.plusDays(1);
        }

        // Calculate gross pay: base pay + overtime + holiday pay - deductions
        double grossPay = basePay + overtimePay + holidayPay - lateDeduction - undertimeDeduction - absenceDeduction;
        grossPay = Math.max(0, grossPay);

        // Full monthly gross (for tax calculation)
        double fullMonthlyGross = monthlySalary;

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

        // Display earnings and deductions
        System.out.println("\n=== EARNINGS ===");
        System.out.println("Base Pay: ₱" + String.format("%,.2f", result.basePay));

        if (result.holidayPay > 0) {
            System.out.println("Holiday Pay: ₱" + String.format("%,.2f", result.holidayPay));

            // Display holiday details if available
            LocalDate currentDate = result.startDate;
            while (!currentDate.isAfter(result.endDate)) {
                if (holidayManager.isHoliday(currentDate)) {
                    String holidayName = holidayManager.getHolidayName(currentDate);
                    System.out.println("  - " + DateTimeUtil.formatDate(currentDate) + ": " + holidayName);
                }
                currentDate = currentDate.plusDays(1);
            }
        }

        if (result.overtimePay > 0) {
            System.out.println("Overtime Pay: ₱" + String.format("%,.2f", result.overtimePay) +
                    " (" + String.format("%.2f", result.overtimeHours) + " hours)");
        }

        System.out.println("Gross Pay: ₱" + String.format("%,.2f", result.grossPay));

        // Display deductions
        System.out.println("\n=== DEDUCTIONS ===");

        if (result.lateDeduction > 0) {
            System.out.println("Late Deduction: ₱" + String.format("%,.2f", result.lateDeduction) +
                    " (" + String.format("%.0f", result.lateMinutes) + " minutes)");
        }

        if (result.undertimeDeduction > 0) {
            System.out.println("Undertime Deduction: ₱" + String.format("%,.2f", result.undertimeDeduction) +
                    " (" + String.format("%.0f", result.undertimeMinutes) + " minutes)");
        }

        if (result.absenceDeduction > 0) {
            System.out.println("Absence Deduction: ₱" + String.format("%,.2f", result.absenceDeduction));
        }

        // Display statutory deductions
        if (result.deductions.sssDeduction > 0) {
            System.out.println("SSS Contribution: ₱" + String.format("%,.2f", result.deductions.sssDeduction));
        }

        if (result.deductions.philhealthDeduction > 0) {
            System.out.println("PhilHealth Contribution: ₱" + String.format("%,.2f", result.deductions.philhealthDeduction));
        }

        if (result.deductions.pagibigDeduction > 0) {
            System.out.println("Pag-IBIG Contribution: ₱" + String.format("%,.2f", result.deductions.pagibigDeduction));
        }

        if (result.deductions.withholdingTax > 0) {
            System.out.println("Withholding Tax: ₱" + String.format("%,.2f", result.deductions.withholdingTax));
        }

        System.out.println("Total Deductions: ₱" + String.format("%,.2f", result.deductions.totalDeductions));

        // Display final pay
        System.out.println("\n=== NET PAY ===");
        System.out.println("Net Pay: ₱" + String.format("%,.2f", result.netPay));

        // Add holiday pay calculation explanation
        if (result.holidayPay > 0) {
            System.out.println("\n=== HOLIDAY PAY CALCULATION ===");
            System.out.println("Regular Holiday Pay Rules:");
            System.out.println("- Non-working employees: 100% of daily rate");
            System.out.println("- Working employees: 200% of daily rate for first 8 hours");
            System.out.println("- Overtime: Additional 30% + 25% (if not late)");
            System.out.println("- Rest day premium: Additional 30%");

            System.out.println("\nSpecial Non-Working Holiday Pay Rules:");
            System.out.println("- Non-working employees: No additional pay");
            System.out.println("- Working employees: Additional 30% of daily rate");
            System.out.println("- Overtime: 30% holiday premium + 25% (if not late)");
        }
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
