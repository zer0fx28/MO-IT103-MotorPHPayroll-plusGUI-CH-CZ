// File: motorph/process/PayrollProcessor.java
package motorph.process;

import motorph.deductions.StatutoryDeductions;
import motorph.employee.Employee;
import motorph.employee.EmployeeDataReader;
import motorph.exceptions.PayrollException;
import motorph.exceptions.ValidationException;
import motorph.holidays.HolidayManager;
import motorph.hours.AttendanceReader;
import motorph.hours.WorkHoursCalculator;
import motorph.output.PayrollSummaryDisplay;
import motorph.util.TimeConverter;
import motorph.util.ValidationUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles all payroll calculations
 *
 * This class is responsible for processing employee payroll, calculating gross and net pay,
 * applying deductions, and storing the results for later display.
 */
public class PayrollProcessor {
    // Components for payroll processing
    private final EmployeeDataReader employeeDataReader;
    private final AttendanceReader attendanceReader;
    private final WorkHoursCalculator hoursCalculator;
    private final HolidayManager holidayManager;

    // Storage for calculated values
    private final Map<String, Double> currentGrossPay = new HashMap<>();
    private final Map<String, Double> currentNetPay = new HashMap<>();
    private final Map<String, StatutoryDeductions.DeductionResult> currentDeductions = new HashMap<>();
    private final Map<String, Map<String, Object>> currentPayDetails = new HashMap<>();

    // Constants
    private static final double OVERTIME_RATE = 1.25; // 25% overtime premium
    private static final double EPSILON = 0.001; // For floating point comparisons
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy");

    /**
     * Initialize the payroll processor with necessary components
     *
     * @param employeeFilePath   Path to employee data CSV file
     * @param attendanceFilePath Path to attendance data CSV file
     */
    public PayrollProcessor(String employeeFilePath, String attendanceFilePath) {
        this.employeeDataReader = new EmployeeDataReader(employeeFilePath);
        this.attendanceReader = new AttendanceReader(attendanceFilePath);
        this.hoursCalculator = new WorkHoursCalculator();
        this.holidayManager = new HolidayManager();
    }

    /**
     * Process payroll for an employee for a specific period
     * <p>
     * This is the primary method for handling payroll calculations. It computes
     * all pay components, deductions, and stores the results.
     *
     * @param employee          Employee object
     * @param hoursWorked       Total hours worked during the period
     * @param overtimeHours     Overtime hours worked
     * @param lateMinutes       Minutes late
     * @param undertimeMinutes  Minutes of undertime
     * @param isLate            Whether the employee was late during the period
     * @param payPeriodType     Period type (mid-month or end-month)
     * @param startDate         Start date of the pay period
     * @param endDate           End date of the pay period
     * @param year              Year of the payroll
     * @param month             Month of the payroll
     * @param hasUnpaidAbsences Whether the employee has unpaid absences
     * @throws ValidationException If input validation fails
     */
    public void processPayrollForPeriod(Employee employee, double hoursWorked,
                                        double overtimeHours, double lateMinutes,
                                        double undertimeMinutes, boolean isLate,
                                        int payPeriodType, LocalDate startDate, LocalDate endDate,
                                        int year, int month, boolean hasUnpaidAbsences)
            throws ValidationException {

        // Validate inputs
        validateEmployeePayrollInputs(employee, hoursWorked, overtimeHours, lateMinutes,
                undertimeMinutes, payPeriodType, startDate, endDate);

        // Normalize negative values to zero
        hoursWorked = Math.max(0, hoursWorked);
        overtimeHours = Math.max(0, overtimeHours);
        lateMinutes = Math.max(0, lateMinutes);
        undertimeMinutes = Math.max(0, undertimeMinutes);

        String employeeId = employee.getEmployeeId();
        double monthlySalary = employee.getBasicSalary();
        double semiMonthlySalary = monthlySalary / 2;  // Half of the monthly salary
        double hourlyRate = employee.getHourlyRate();
        double dailyRate = calculateDailyRate(monthlySalary, year);

        // Base compensation is half the monthly salary
        double basePay = semiMonthlySalary;

        // Calculate deductions for late, undertime, and absences
        double lateDeduction = calculateLateDeduction(lateMinutes, hourlyRate);
        double undertimeDeduction = calculateUndertimeDeduction(undertimeMinutes, hourlyRate);

        // Calculate working days and absence deduction
        WorkingDaysResult workingDaysResult = calculateWorkingDaysAndAbsences(
                startDate, endDate, hoursWorked, lateMinutes, undertimeMinutes,
                hasUnpaidAbsences, dailyRate);

        double absenceDeduction = workingDaysResult.absenceDeduction;

        // Calculate overtime pay for eligible employees
        double overtimePay = calculateOvertimePay(overtimeHours, hourlyRate, isLate);

        // Calculate holiday pay
        double holidayPay = calculateHolidayPay(employee, startDate, endDate);

        // Calculate gross pay: base pay + overtime + holiday - deductions
        double grossPay = calculateGrossPay(basePay, overtimePay, holidayPay,
                lateDeduction, undertimeDeduction, absenceDeduction);

        // Full monthly gross (for tax calculation)
        double fullMonthlyGross = monthlySalary + (overtimePay * 2) + holidayPay;

        // Calculate statutory deductions
        StatutoryDeductions.DeductionResult deductions =
                StatutoryDeductions.calculateDeductions(grossPay, payPeriodType, fullMonthlyGross);

        // Calculate net pay
        double netPay = Math.max(0, grossPay - deductions.totalDeductions);

        // Save calculations
        saveCalculationResults(employeeId, grossPay, netPay, deductions);

        // Save additional details for reports
        savePayrollDetails(employeeId, basePay, overtimePay, holidayPay, lateDeduction,
                undertimeDeduction, absenceDeduction, hoursWorked, overtimeHours,
                lateMinutes, undertimeMinutes, workingDaysResult.expectedHours,
                workingDaysResult.absentHours, dailyRate, startDate, endDate,
                payPeriodType, hourlyRate, year, month, hasUnpaidAbsences);
    }

    /**
     * Validate all inputs for payroll processing
     *
     * @param employee         Employee object
     * @param hoursWorked      Total hours worked
     * @param overtimeHours    Overtime hours
     * @param lateMinutes      Late minutes
     * @param undertimeMinutes Undertime minutes
     * @param payPeriodType    Pay period type
     * @param startDate        Start date
     * @param endDate          End date
     * @throws ValidationException If validation fails
     */
    private void validateEmployeePayrollInputs(Employee employee, double hoursWorked,
                                               double overtimeHours, double lateMinutes,
                                               double undertimeMinutes, int payPeriodType,
                                               LocalDate startDate, LocalDate endDate)
            throws ValidationException {
        // Check employee
        if (employee == null) {
            throw new ValidationException("Employee cannot be null", ValidationException.NULL_VALUE);
        }

        // Check date range
        if (startDate == null || endDate == null) {
            throw new ValidationException("Pay period dates cannot be null", ValidationException.NULL_VALUE);
        }

        if (endDate.isBefore(startDate)) {
            throw new ValidationException("End date cannot be before start date", ValidationException.INVALID_DATE);
        }

        // Check pay period type
        if (payPeriodType != PayrollDateManager.MID_MONTH && payPeriodType != PayrollDateManager.END_MONTH) {
            throw new ValidationException("Invalid pay period type", ValidationException.INVALID_VALUE);
        }
    }

    /**
     * Calculate daily rate based on monthly salary
     *
     * @param monthlySalary Monthly salary
     * @param year          Year for working days calculation
     * @return Daily rate
     */
    private double calculateDailyRate(double monthlySalary, int year) {
        return monthlySalary / PayrollDateManager.getWorkingDaysInYear(year);
    }

    /**
     * Calculate deduction for late arrivals
     *
     * @param lateMinutes Minutes late
     * @param hourlyRate  Hourly rate
     * @return Late deduction amount
     */
    private double calculateLateDeduction(double lateMinutes, double hourlyRate) {
        if (lateMinutes <= 0) {
            return 0.0;
        }
        double perMinuteRate = hourlyRate / 60.0;
        return perMinuteRate * lateMinutes;
    }

    /**
     * Calculate deduction for undertime (leaving early)
     *
     * @param undertimeMinutes Minutes of undertime
     * @param hourlyRate       Hourly rate
     * @return Undertime deduction amount
     */
    private double calculateUndertimeDeduction(double undertimeMinutes, double hourlyRate) {
        if (undertimeMinutes <= 0) {
            return 0.0;
        }
        double perMinuteRate = hourlyRate / 60.0;
        return perMinuteRate * undertimeMinutes;
    }

    /**
     * Calculate working days, expected hours, and absence deduction
     *
     * @param startDate         Start date of period
     * @param endDate           End date of period
     * @param hoursWorked       Hours worked
     * @param lateMinutes       Minutes late
     * @param undertimeMinutes  Minutes undertime
     * @param hasUnpaidAbsences Whether there are unpaid absences
     * @param dailyRate         Daily rate
     * @return WorkingDaysResult with calculation results
     */
    private WorkingDaysResult calculateWorkingDaysAndAbsences(
            LocalDate startDate, LocalDate endDate, double hoursWorked,
            double lateMinutes, double undertimeMinutes, boolean hasUnpaidAbsences,
            double dailyRate) {

        // Calculate working days in the cutoff period
        int workingDaysInPeriod = calculateWorkingDays(startDate, endDate);

        // Calculate the expected working hours for the period
        double expectedHours = workingDaysInPeriod * 8.0; // 8 hours per day

        // Calculate absences (if any)
        double absentHours = Math.max(0, expectedHours - hoursWorked -
                (lateMinutes / 60.0) - (undertimeMinutes / 60.0));
        double absentDays = absentHours / 8.0; // Convert hours to days

        // Only apply absence deduction if the absence is unpaid
        double absenceDeduction = 0.0;
        if (hasUnpaidAbsences && absentDays > 0) {
            absenceDeduction = absentDays * dailyRate;
        }

        return new WorkingDaysResult(
                workingDaysInPeriod, expectedHours, absentHours, absenceDeduction);
    }

    /**
     * Result class for working days calculation
     */
    private static class WorkingDaysResult {
        public final int workingDays;
        public final double expectedHours;
        public final double absentHours;
        public final double absenceDeduction;

        public WorkingDaysResult(int workingDays, double expectedHours,
                                 double absentHours, double absenceDeduction) {
            this.workingDays = workingDays;
            this.expectedHours = expectedHours;
            this.absentHours = absentHours;
            this.absenceDeduction = absenceDeduction;
        }
    }

    /**
     * Calculate overtime pay based on hours and rate
     *
     * @param overtimeHours Hours of overtime
     * @param hourlyRate    Hourly rate
     * @param isLate        Whether employee was late (ineligible for overtime)
     * @return Overtime pay amount
     */
    private double calculateOvertimePay(double overtimeHours, double hourlyRate, boolean isLate) {
        // Late employees don't get overtime
        if (isLate || overtimeHours <= 0) {
            return 0.0;
        }

        // 25% overtime premium on regular hourly rate
        double overtimeRate = hourlyRate * OVERTIME_RATE;
        return overtimeHours * overtimeRate;
    }

    /**
     * Calculate gross pay with all components
     *
     * @param basePay            Base pay amount
     * @param overtimePay        Overtime pay
     * @param holidayPay         Holiday pay
     * @param lateDeduction      Late deduction
     * @param undertimeDeduction Undertime deduction
     * @param absenceDeduction   Absence deduction
     * @return Gross pay amount
     */
    private double calculateGrossPay(
            double basePay, double overtimePay, double holidayPay,
            double lateDeduction, double undertimeDeduction, double absenceDeduction) {

        double grossPay = basePay + overtimePay + holidayPay -
                lateDeduction - undertimeDeduction - absenceDeduction;

        // Ensure gross pay is not negative
        return Math.max(0, grossPay);
    }

    /**
     * Save calculation results to storage maps
     *
     * @param employeeId Employee ID
     * @param grossPay   Gross pay amount
     * @param netPay     Net pay amount
     * @param deductions Deduction results
     */
    private void saveCalculationResults(
            String employeeId, double grossPay, double netPay,
            StatutoryDeductions.DeductionResult deductions) {

        currentGrossPay.put(employeeId, grossPay);
        currentNetPay.put(employeeId, netPay);
        currentDeductions.put(employeeId, deductions);
    }

    /**
     * Save detailed payroll information for reporting
     *
     * @param employeeId         Employee ID
     * @param regularPay         Regular pay amount
     * @param overtimePay        Overtime pay amount
     * @param holidayPay         Holiday pay amount
     * @param lateDeduction      Late deduction amount
     * @param undertimeDeduction Undertime deduction amount
     * @param absenceDeduction   Absence deduction amount
     * @param hoursWorked        Hours worked
     * @param overtimeHours      Overtime hours
     * @param lateMinutes        Minutes late
     * @param undertimeMinutes   Minutes undertime
     * @param expectedHours      Expected hours
     * @param absentHours        Hours absent
     * @param dailyRate          Daily rate
     * @param startDate          Start date
     * @param endDate            End date
     * @param payPeriod          Pay period type
     * @param hourlyRate         Hourly rate
     * @param year               Year
     * @param month              Month
     * @param hasUnpaidAbsences  Whether there are unpaid absences
     */
    private void savePayrollDetails(
            String employeeId, double regularPay, double overtimePay, double holidayPay,
            double lateDeduction, double undertimeDeduction, double absenceDeduction,
            double hoursWorked, double overtimeHours, double lateMinutes, double undertimeMinutes,
            double expectedHours, double absentHours, double dailyRate, LocalDate startDate,
            LocalDate endDate, int payPeriod, double hourlyRate, int year, int month,
            boolean hasUnpaidAbsences) {

        Map<String, Object> payDetails = new HashMap<>();
        payDetails.put("regularPay", regularPay);
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
        payDetails.put("payPeriod", payPeriod);
        payDetails.put("hourlyRate", hourlyRate);
        payDetails.put("year", year);
        payDetails.put("month", month);
        payDetails.put("hasUnpaidAbsences", hasUnpaidAbsences);

        currentPayDetails.put(employeeId, payDetails);
    }

    /**
     * Process payroll with original signature (for backward compatibility)
     *
     * @param employee         Employee object
     * @param hoursWorked      Total hours worked during the period
     * @param overtimeHours    Overtime hours worked
     * @param lateMinutes      Minutes late
     * @param undertimeMinutes Minutes of undertime
     * @param isLate           Whether the employee was late during the period
     * @param payPeriodType    Period type (mid-month or end-month)
     * @param startDate        Start date of the pay period
     * @param endDate          End date of the pay period
     * @param year             Year of the payroll
     * @param month            Month of the payroll
     */
    public void processPayrollForPeriod(Employee employee, double hoursWorked,
                                        double overtimeHours, double lateMinutes,
                                        double undertimeMinutes, boolean isLate,
                                        int payPeriodType, LocalDate startDate, LocalDate endDate,
                                        int year, int month) {
        try {
            // Default to not having unpaid absences when not specified
            processPayrollForPeriod(employee, hoursWorked, overtimeHours, lateMinutes,
                    undertimeMinutes, isLate, payPeriodType, startDate,
                    endDate, year, month, false);
        } catch (ValidationException e) {
            System.out.println("Validation error: " + e.getMessage());
        }
    }

    /**
     * Calculate the number of working days between two dates
     * Excludes weekends (Saturday and Sunday)
     *
     * @param startDate Start date
     * @param endDate   End date
     * @return Number of working days
     */
    private int calculateWorkingDays(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            return 0;
        }

        int workingDays = 0;
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            int dayOfWeek = currentDate.getDayOfWeek().getValue();
            // Skip weekends (6 = Saturday, 7 = Sunday)
            if (dayOfWeek < 6) {
                workingDays++;
            }
            currentDate = currentDate.plusDays(1);
        }

        return workingDays;
    }

    /**
     * Calculate holiday pay for the period
     *
     * @param employee  Employee object
     * @param startDate Start date of period
     * @param endDate   End date of period
     * @return Holiday pay amount
     */
    private double calculateHolidayPay(Employee employee, LocalDate startDate, LocalDate endDate) {
        double holidayPay = 0.0;
        double dailyRate = employee.getBasicSalary() / 22; // Assume 22 working days per month

        // Check each day in the period for holidays
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            if (holidayManager.isRegularHoliday(currentDate)) {
                // Regular holiday pay is typically 100% of daily rate
                holidayPay += dailyRate;
            } else if (holidayManager.isSpecialNonWorkingHoliday(currentDate)) {
                // Special non-working holiday pay is typically 30% of daily rate
                holidayPay += dailyRate * 0.3;
            }
            currentDate = currentDate.plusDays(1);
        }

        return holidayPay;
    }

    /**
     * Show salary breakdown for an employee
     *
     * @param employee Employee object
     */
    public void displaySalaryDetails(Employee employee) {
        if (employee == null) {
            System.out.println("Error: Cannot display salary details for null employee");
            return;
        }

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

        // Get period information
        LocalDate startDate = (LocalDate) payDetails.get("startDate");
        LocalDate endDate = (LocalDate) payDetails.get("endDate");
        int payPeriod = (int) payDetails.getOrDefault("payPeriod", PayrollDateManager.MID_MONTH);
        int year = (int) payDetails.getOrDefault("year", LocalDate.now().getYear());
        int month = (int) payDetails.getOrDefault("month", LocalDate.now().getMonthValue());

        // Create attendance summary
        Map<String, Object> attendanceSummary = new HashMap<>();
        attendanceSummary.put("hours", payDetails.getOrDefault("expectedHours", 0.0));
        attendanceSummary.put("actualHours", payDetails.getOrDefault("hoursWorked", 0.0));
        attendanceSummary.put("overtimeHours", payDetails.getOrDefault("overtimeHours", 0.0));
        attendanceSummary.put("lateMinutes", payDetails.getOrDefault("lateMinutes", 0.0));
        attendanceSummary.put("undertimeMinutes", payDetails.getOrDefault("undertimeMinutes", 0.0));
        attendanceSummary.put("hasUnpaidAbsences", payDetails.getOrDefault("hasUnpaidAbsences", false));

        // Use the new tabular format display
        PayrollSummaryDisplay.displayPayrollSummary(
                employee,
                attendanceSummary,
                startDate,
                endDate,
                payPeriod,
                deductions,
                payDetails,
                attendanceReader);
    }
}