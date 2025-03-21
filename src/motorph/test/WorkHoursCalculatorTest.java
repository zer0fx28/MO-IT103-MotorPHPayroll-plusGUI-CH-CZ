package motorph.test;

import motorph.hours.WorkHoursCalculator;
import motorph.util.DateTimeUtil;

import java.time.LocalTime;

/**
 * Tests for the WorkHoursCalculator class
 */
public class WorkHoursCalculatorTest {

    /**
     * Run all tests for WorkHoursCalculator
     */
    public void runTests() {
        System.out.println("--- Work Hours Calculator Tests ---");
        System.out.println(new java.util.Date());

        testNormalDay();
        testLateArrival();
        testEarlyDeparture();
        testOvertimeHours();
        testInvalidInput();

        System.out.println("=== All Tests Completed ===");
    }

    /**
     * Test calculation for a normal working day
     */
    private void testNormalDay() {
        System.out.println("\nTest: Normal working day");

        WorkHoursCalculator calculator = new WorkHoursCalculator();
        LocalTime timeIn = DateTimeUtil.parseTime("8:05 AM");
        LocalTime timeOut = DateTimeUtil.parseTime("5:00 PM");
        boolean isLate = false;

        double hoursWorked = calculator.calculateHoursWorked(timeIn, timeOut, isLate);
        double overtimeHours = calculator.calculateOvertimeHours(timeOut, isLate);

        System.out.println("Time In: 8:05 AM");
        System.out.println("Time Out: 5:00 PM");
        System.out.println("Is Late: No");
        System.out.println("Hours worked: " + hoursWorked + " (Expected: 7.92)");
        System.out.println("Overtime hours: " + overtimeHours + " (Expected: 0.0)");

        System.out.println("PASS: Normal day hours calculation");
    }

    /**
     * Test calculation for late arrival
     */
    private void testLateArrival() {
        System.out.println("\nTest: Late arrival");

        WorkHoursCalculator calculator = new WorkHoursCalculator();
        LocalTime timeIn = DateTimeUtil.parseTime("9:30 AM");
        LocalTime timeOut = DateTimeUtil.parseTime("5:30 PM");
        boolean isLate = true;

        double hoursWorked = calculator.calculateHoursWorked(timeIn, timeOut, isLate);
        double overtimeHours = calculator.calculateOvertimeHours(timeOut, isLate);

        System.out.println("Time In: 9:30 AM");
        System.out.println("Time Out: 5:30 PM");
        System.out.println("Is Late: Yes");
        System.out.println("Hours worked: " + hoursWorked + " (Expected: 6.5)");
        System.out.println("Overtime hours: " + overtimeHours + " (Expected: 0.0 - No overtime for late employees)");

        System.out.println("PASS: Late arrival hours calculation");
    }

    /**
     * Test calculation for early departure
     */
    private void testEarlyDeparture() {
        System.out.println("\nTest: Early departure");

        WorkHoursCalculator calculator = new WorkHoursCalculator();
        LocalTime timeIn = DateTimeUtil.parseTime("8:00 AM");
        LocalTime timeOut = DateTimeUtil.parseTime("4:30 PM");
        boolean isLate = false;

        double hoursWorked = calculator.calculateHoursWorked(timeIn, timeOut, isLate);
        double overtimeHours = calculator.calculateOvertimeHours(timeOut, isLate);

        System.out.println("Time In: 8:00 AM");
        System.out.println("Time Out: 4:30 PM");
        System.out.println("Is Late: No");
        System.out.println("Hours worked: " + hoursWorked + " (Expected: 7.5)");
        System.out.println("Overtime hours: " + overtimeHours + " (Expected: 0.0)");

        System.out.println("PASS: Early departure hours calculation");
    }

    /**
     * Test calculation for overtime hours
     */
    private void testOvertimeHours() {
        System.out.println("\nTest: Overtime hours");

        WorkHoursCalculator calculator = new WorkHoursCalculator();
        LocalTime timeIn = DateTimeUtil.parseTime("8:00 AM");
        LocalTime timeOut = DateTimeUtil.parseTime("6:30 PM");
        boolean isLate = false;

        double hoursWorked = calculator.calculateHoursWorked(timeIn, timeOut, isLate);
        double overtimeHours = calculator.calculateOvertimeHours(timeOut, isLate);

        System.out.println("Time In: 8:00 AM");
        System.out.println("Time Out: 6:30 PM");
        System.out.println("Is Late: No");
        System.out.println("Hours worked: " + hoursWorked + " (Expected: 8.0 - capped at 8 hours)");
        System.out.println("Overtime hours: " + overtimeHours + " (Expected: 1.5)");

        System.out.println("PASS: Overtime hours calculation");
    }

    /**
     * Test calculation with invalid input
     */
    private void testInvalidInput() {
        System.out.println("\nTest: Invalid input");

        WorkHoursCalculator calculator = new WorkHoursCalculator();

        // Test with null values
        double hoursWorked = calculator.calculateHoursWorked(null, null, false);
        double overtimeHours = calculator.calculateOvertimeHours(null, false);

        System.out.println("Time In: null");
        System.out.println("Time Out: null");
        System.out.println("WARNING: Invalid time values (null) provided for hours calculation");
        System.out.println("Hours worked: " + hoursWorked + " (Expected: 0.0)");
        System.out.println("Overtime hours: " + overtimeHours + " (Expected: 0.0)");

        System.out.println("PASS: Invalid input handling");
    }
}