// File: motorph/test/PayrollCalculationTest.java
package motorph.test;

import motorph.deductions.StatutoryDeductions;
import motorph.employee.Employee;
import motorph.exceptions.ValidationException;
import motorph.hours.WorkHoursCalculator;
import motorph.process.PayrollDateManager;
import motorph.process.PayrollProcessor;
import motorph.util.ValidationUtils;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Test framework for payroll calculations
 *
 * This class provides a simple testing framework to validate the correctness
 * of the payroll calculation logic against known test cases.
 */
public class PayrollCalculationTest {

    // Updated file paths to match the main application
    private static final String EMPLOYEE_FILE = "C:\\Users\\ferna\\IdeaProjects\\MotorphPayrollSystem\\resources\\MotorPH Employee Data - Employee Details.csv";
    private static final String ATTENDANCE_FILE = "C:\\Users\\ferna\\IdeaProjects\\MotorphPayrollSystem\\resources\\MotorPH Employee Data - Attendance Record.csv";

    /**
     * Main entry point for test execution
     *
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("=== Running Payroll Calculation Tests ===");

        runWorkHoursTests();
        runDeductionTests();
        runPayrollProcessingTests();

        System.out.println("\n=== All Tests Completed ===");
    }

    /**
     * Run tests for work hour calculations
     */
    private static void runWorkHoursTests() {
        System.out.println("\n--- Work Hours Calculator Tests ---");

        WorkHoursCalculator calculator = new WorkHoursCalculator();

        // Test 1: Exactly 8 hours should return 8.0
        LocalTime timeIn1 = LocalTime.of(8, 0);
        LocalTime timeOut1 = LocalTime.of(16, 0);
        double result1 = calculator.calculateHoursWorked(timeIn1, timeOut1, false);
        assertEqualsWithTolerance(8.0, result1, 0.001, "Exact 8 hours calculation");

        // Test 2: Late arrival should still calculate correct hours
        LocalTime timeIn2 = LocalTime.of(8, 30);
        LocalTime timeOut2 = LocalTime.of(16, 30);
        double result2 = calculator.calculateHoursWorked(timeIn2, timeOut2, true);
        assertEqualsWithTolerance(8.0, result2, 0.001, "Late arrival hours calculation");

        // Test 3: Overtime eligible (not late)
        LocalTime timeIn3 = LocalTime.of(8, 0);
        LocalTime timeOut3 = LocalTime.of(18, 0);
        double result3 = calculator.calculateHoursWorked(timeIn3, timeOut3, false);
        assertEqualsWithTolerance(8.0, result3, 0.001, "Regular hours with potential overtime");
        double overtime3 = calculator.calculateOvertimeHours(timeOut3, false);
        assertEqualsWithTolerance(1.0, overtime3, 0.001, "Overtime calculation");

        // Test 4: Overtime not eligible (late)
        LocalTime timeIn4 = LocalTime.of(8, 30);
        LocalTime timeOut4 = LocalTime.of(18, 0);
        double overtime4 = calculator.calculateOvertimeHours(timeOut4, true);
        assertEqualsWithTolerance(0.0, overtime4, 0.001, "No overtime for late employees");

        // Test 5: Edge case - null time values
        double result5 = calculator.calculateHoursWorked(null, null, false);
        assertEqualsWithTolerance(0.0, result5, 0.001, "Null time values handling");
    }

    /**
     * Run tests for deduction calculations
     */
    private static void runDeductionTests() {
        System.out.println("\n--- Statutory Deductions Tests ---");

        // Test Mid-month deductions
        double grossSalary = 25000.0;
        double fullMonthlyGross = 25000.0;

        StatutoryDeductions.DeductionResult midMonthResult =
                StatutoryDeductions.calculateDeductions(grossSalary, PayrollDateManager.MID_MONTH, fullMonthlyGross);

        assertNotNull(midMonthResult, "Mid-month deduction result");
        assertGreaterThan(0.0, midMonthResult.sssDeduction, "SSS deduction should be positive");
        assertGreaterThan(0.0, midMonthResult.philhealthDeduction, "PhilHealth deduction should be positive");
        assertGreaterThan(0.0, midMonthResult.pagibigDeduction, "Pag-IBIG deduction should be positive");
        assertEqualsWithTolerance(0.0, midMonthResult.withholdingTax, 0.001, "No tax deduction mid-month");

        // Test End-month deductions
        StatutoryDeductions.DeductionResult endMonthResult =
                StatutoryDeductions.calculateDeductions(grossSalary, PayrollDateManager.END_MONTH, fullMonthlyGross);

        assertNotNull(endMonthResult, "End-month deduction result");
        assertEqualsWithTolerance(0.0, endMonthResult.sssDeduction, 0.001, "No SSS deduction end-month");
        assertEqualsWithTolerance(0.0, endMonthResult.philhealthDeduction, 0.001, "No PhilHealth deduction end-month");
        assertEqualsWithTolerance(0.0, endMonthResult.pagibigDeduction, 0.001, "No Pag-IBIG deduction end-month");
        assertGreaterThan(0.0, endMonthResult.withholdingTax, "Tax deduction should be positive");
    }

    /**
     * Run tests for payroll processing logic
     */
    private static void runPayrollProcessingTests() {
        System.out.println("\n--- Payroll Processing Tests ---");

        // Create a dummy employee for testing
        String[] employeeData = new String[] {
                "EMP001", "Doe", "John", "01/15/1990", "Manila", "123456789",
                "SSS123", "PH123", "TIN123", "PI123", "Regular", "Developer",
                "Manager1", "25000", "1500", "1000", "1000", "12500", "142.05"
        };

        Employee testEmployee = new Employee(employeeData);

        try {
            // Create a test processor
            System.out.println("Creating test PayrollProcessor with files:");
            System.out.println("- Employee file: " + EMPLOYEE_FILE);
            System.out.println("- Attendance file: " + ATTENDANCE_FILE);

            PayrollProcessor processor = new PayrollProcessor(
                    EMPLOYEE_FILE, ATTENDANCE_FILE);

            // Test a regular payroll with no issues
            LocalDate startDate = LocalDate.of(2024, 5, 1);
            LocalDate endDate = LocalDate.of(2024, 5, 15);
            processor.processPayrollForPeriod(
                    testEmployee, 80.0, 5.0, 0.0, 0.0, false,
                    PayrollDateManager.MID_MONTH, startDate, endDate, 2024, 5, false);

            System.out.println("Regular payroll processing test passed");

            // Test payroll with late minutes
            processor.processPayrollForPeriod(
                    testEmployee, 78.0, 0.0, 60.0, 30.0, true,
                    PayrollDateManager.END_MONTH, startDate, endDate, 2024, 5, false);

            System.out.println("Late deduction payroll processing test passed");

            // Test payroll with absences
            processor.processPayrollForPeriod(
                    testEmployee, 64.0, 0.0, 0.0, 0.0, false,
                    PayrollDateManager.END_MONTH, startDate, endDate, 2024, 5, true);

            System.out.println("Absence deduction payroll processing test passed");

        } catch (ValidationException e) {
            System.out.println("Test failed with validation exception: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Test failed with exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Assert that two double values are equal within a tolerance
     *
     * @param expected Expected value
     * @param actual Actual value
     * @param tolerance Maximum allowed difference
     * @param message Test description message
     */
    private static void assertEqualsWithTolerance(double expected, double actual, double tolerance, String message) {
        if (Math.abs(expected - actual) <= tolerance) {
            System.out.println("PASS: " + message);
        } else {
            System.out.println("FAIL: " + message + " (Expected: " + expected + ", Actual: " + actual + ")");
        }
    }

    /**
     * Assert that a value is greater than a minimum
     *
     * @param minimum Minimum acceptable value
     * @param actual Actual value
     * @param message Test description message
     */
    private static void assertGreaterThan(double minimum, double actual, String message) {
        if (actual > minimum) {
            System.out.println("PASS: " + message);
        } else {
            System.out.println("FAIL: " + message + " (Expected > " + minimum + ", Actual: " + actual + ")");
        }
    }

    /**
     * Assert that an object is not null
     *
     * @param actual Object to check
     * @param message Test description message
     */
    private static void assertNotNull(Object actual, String message) {
        if (actual != null) {
            System.out.println("PASS: " + message);
        } else {
            System.out.println("FAIL: " + message + " (Object is null)");
        }
    }
}