// File: motorph/test/HolidayPayTest.java
package motorph.test;

import motorph.holidays.HolidayManager;
import motorph.holidays.HolidayPayCalculator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Test class for holiday pay calculations
 * Verifies the implementation of the holiday pay rules
 */
public class HolidayPayTest {
    private HolidayManager holidayManager;
    private HolidayPayCalculator holidayPayCalculator;

    /**
     * Initialize the test components
     */
    public HolidayPayTest() {
        holidayManager = new HolidayManager();
        holidayPayCalculator = new HolidayPayCalculator(holidayManager);
    }

    /**
     * Run all tests
     */
    public void runTests() {
        System.out.println("=== Holiday Pay Tests ===");
        testRegularHolidayNonWorking();
        testRegularHolidayWorking();
        testRegularHolidayWorkingWithOvertime();
        testRegularHolidayOnRestDay();
        testSpecialHolidayWorking();
        testSpecialHolidayWithOvertime();
        testEmployeeLate();
        System.out.println("=== All Tests Completed ===");
    }

    /**
     * Test regular holiday pay for non-working employees
     */
    private void testRegularHolidayNonWorking() {
        System.out.println("\nTest: Regular Holiday Pay (Non-Working)");

        // Find a known regular holiday
        LocalDate christmasDay = LocalDate.of(2024, 12, 25); // Christmas Day
        double dailyRate = 1000.0;

        // Employee doesn't work (0 hours)
        double holidayPay = holidayPayCalculator.calculateHolidayPay(
                christmasDay, dailyRate, 0.0, false, false, 0.0);

        System.out.println("Date: " + formatDate(christmasDay));
        System.out.println("Is Regular Holiday: " + holidayManager.isRegularHoliday(christmasDay));
        System.out.println("Daily Rate: ₱" + dailyRate);
        System.out.println("Hours Worked: 0.0");
        System.out.println("Holiday Pay: ₱" + holidayPay);
        System.out.println("Expected: ₱" + dailyRate + " (100% of daily rate)");

        assert holidayPay == dailyRate : "Holiday pay should be 100% of daily rate";
        System.out.println("PASS: Regular holiday pay for non-working employees");
    }

    /**
     * Test regular holiday pay for working employees
     */
    private void testRegularHolidayWorking() {
        System.out.println("\nTest: Regular Holiday Pay (Working)");

        // Find a known regular holiday
        LocalDate laborDay = LocalDate.of(2024, 5, 1); // Labor Day
        double dailyRate = 1000.0;

        // Employee works 8 hours
        double holidayPay = holidayPayCalculator.calculateHolidayPay(
                laborDay, dailyRate, 8.0, false, false, 0.0);

        System.out.println("Date: " + formatDate(laborDay));
        System.out.println("Is Regular Holiday: " + holidayManager.isRegularHoliday(laborDay));
        System.out.println("Daily Rate: ₱" + dailyRate);
        System.out.println("Hours Worked: 8.0");
        System.out.println("Holiday Pay: ₱" + holidayPay);
        System.out.println("Expected: ₱" + (dailyRate * 2) + " (200% of daily rate)");

        assert holidayPay == dailyRate * 2 : "Holiday pay should be 200% of daily rate";
        System.out.println("PASS: Regular holiday pay for working employees");
    }

    /**
     * Test regular holiday pay with overtime
     */
    private void testRegularHolidayWorkingWithOvertime() {
        System.out.println("\nTest: Regular Holiday Pay with Overtime");

        // Find a known regular holiday
        LocalDate independenceDay = LocalDate.of(2024, 6, 12); // Independence Day
        double dailyRate = 1000.0;
        double hoursWorked = 8.0;
        double overtimeHours = 2.0;

        // Employee works 8 hours + 2 hours overtime
        double holidayPay = holidayPayCalculator.calculateHolidayPay(
                independenceDay, dailyRate, hoursWorked, false, false, overtimeHours);

        double hourlyRate = dailyRate / 8;
        double regularPay = hoursWorked * hourlyRate * 2; // 200% for regular hours
        double overtimePay = overtimeHours * hourlyRate * 1.3 * 1.25; // 30% + 25% premium
        double expectedTotal = regularPay + overtimePay;

        System.out.println("Date: " + formatDate(independenceDay));
        System.out.println("Is Regular Holiday: " + holidayManager.isRegularHoliday(independenceDay));
        System.out.println("Daily Rate: ₱" + dailyRate);
        System.out.println("Hours Worked: " + hoursWorked);
        System.out.println("Overtime Hours: " + overtimeHours);
        System.out.println("Holiday Pay: ₱" + holidayPay);
        System.out.println("Expected: ₱" + expectedTotal + " (200% regular + overtime premiums)");

        assert Math.abs(holidayPay - expectedTotal) < 0.01 : "Holiday pay calculation incorrect";
        System.out.println("PASS: Regular holiday pay with overtime");
    }

    /**
     * Test regular holiday falling on a rest day
     */
    private void testRegularHolidayOnRestDay() {
        System.out.println("\nTest: Regular Holiday on Rest Day");

        // Find a known regular holiday
        LocalDate rizalDay = LocalDate.of(2024, 12, 30); // Rizal Day
        double dailyRate = 1000.0;

        // Employee works 8 hours on a rest day holiday
        double holidayPay = holidayPayCalculator.calculateHolidayPay(
                rizalDay, dailyRate, 8.0, true, false, 0.0);

        double regularHolidayPay = dailyRate * 2; // 200% for regular holiday
        double expectedTotal = regularHolidayPay * 1.3; // Additional 30% for rest day

        System.out.println("Date: " + formatDate(rizalDay));
        System.out.println("Is Regular Holiday: " + holidayManager.isRegularHoliday(rizalDay));
        System.out.println("Is Rest Day: Yes");
        System.out.println("Daily Rate: ₱" + dailyRate);
        System.out.println("Hours Worked: 8.0");
        System.out.println("Holiday Pay: ₱" + holidayPay);
        System.out.println("Expected: ₱" + expectedTotal + " (200% + 30% rest day premium)");

        assert Math.abs(holidayPay - expectedTotal) < 0.01 : "Holiday pay calculation incorrect";
        System.out.println("PASS: Regular holiday pay on rest day");
    }

    /**
     * Test special non-working holiday pay
     */
    private void testSpecialHolidayWorking() {
        System.out.println("\nTest: Special Non-Working Holiday Pay");

        // Find a known special non-working holiday
        LocalDate allSaintsDay = LocalDate.of(2024, 11, 1); // All Saints' Day
        double dailyRate = 1000.0;

        // Employee works 8 hours
        double holidayPay = holidayPayCalculator.calculateHolidayPay(
                allSaintsDay, dailyRate, 8.0, false, false, 0.0);

        double expectedPay = dailyRate * 1.3; // Additional 30%

        System.out.println("Date: " + formatDate(allSaintsDay));
        System.out.println("Is Special Holiday: " + holidayManager.isSpecialNonWorkingHoliday(allSaintsDay));
        System.out.println("Daily Rate: ₱" + dailyRate);
        System.out.println("Hours Worked: 8.0");
        System.out.println("Holiday Pay: ₱" + holidayPay);
        System.out.println("Expected: ₱" + expectedPay + " (130% of daily rate)");

        assert Math.abs(holidayPay - expectedPay) < 0.01 : "Special holiday pay calculation incorrect";
        System.out.println("PASS: Special holiday pay for working employees");
    }

    /**
     * Test special holiday pay with overtime
     */
    private void testSpecialHolidayWithOvertime() {
        System.out.println("\nTest: Special Holiday Pay with Overtime");

        // Find a known special non-working holiday
        LocalDate ninoyAquinoDay = LocalDate.of(2024, 8, 23); // Ninoy Aquino Day
        double dailyRate = 1000.0;
        double hoursWorked = 8.0;
        double overtimeHours = 2.0;

        // Employee works 8 hours + 2 hours overtime
        double holidayPay = holidayPayCalculator.calculateHolidayPay(
                ninoyAquinoDay, dailyRate, hoursWorked, false, false, overtimeHours);

        double hourlyRate = dailyRate / 8;
        double regularPay = hoursWorked * hourlyRate * 1.3; // 30% premium for special holiday
        double overtimePay = overtimeHours * hourlyRate * 1.3 * 1.25; // 30% holiday + 25% overtime
        double expectedTotal = regularPay + overtimePay;

        System.out.println("Date: " + formatDate(ninoyAquinoDay));
        System.out.println("Is Special Holiday: " + holidayManager.isSpecialNonWorkingHoliday(ninoyAquinoDay));
        System.out.println("Daily Rate: ₱" + dailyRate);
        System.out.println("Hours Worked: " + hoursWorked);
        System.out.println("Overtime Hours: " + overtimeHours);
        System.out.println("Holiday Pay: ₱" + holidayPay);
        System.out.println("Expected: ₱" + expectedTotal + " (130% regular + overtime premiums)");

        assert Math.abs(holidayPay - expectedTotal) < 0.01 : "Special holiday pay with overtime incorrect";
        System.out.println("PASS: Special holiday pay with overtime");
    }

    /**
     * Test holiday pay for late employees
     */
    private void testEmployeeLate() {
        System.out.println("\nTest: Holiday Pay for Late Employee");

        // Find a known regular holiday
        LocalDate newYearDay = LocalDate.of(2024, 1, 1); // New Year's Day
        double dailyRate = 1000.0;
        double hoursWorked = 8.0;
        double overtimeHours = 2.0;
        boolean isLate = true;

        // Late employee works 8 hours + 2 hours overtime
        double holidayPay = holidayPayCalculator.calculateHolidayPay(
                newYearDay, dailyRate, hoursWorked, false, isLate, overtimeHours);

        double hourlyRate = dailyRate / 8;
        double regularPay = hoursWorked * hourlyRate * 2; // 200% for regular holiday
        double overtimePay = overtimeHours * hourlyRate * 1.3; // 30% premium only (no 25% for late)
        double expectedTotal = regularPay + overtimePay;

        System.out.println("Date: " + formatDate(newYearDay));
        System.out.println("Is Regular Holiday: " + holidayManager.isRegularHoliday(newYearDay));
        System.out.println("Daily Rate: ₱" + dailyRate);
        System.out.println("Hours Worked: " + hoursWorked);
        System.out.println("Overtime Hours: " + overtimeHours);
        System.out.println("Is Late: " + isLate);
        System.out.println("Holiday Pay: ₱" + holidayPay);
        System.out.println("Expected: ₱" + expectedTotal + " (no 25% overtime premium due to lateness)");

        assert Math.abs(holidayPay - expectedTotal) < 0.01 : "Holiday pay for late employee incorrect";
        System.out.println("PASS: Holiday pay for late employee");
    }

    /**
     * Format date for display
     */
    private String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));
    }

    /**
     * Main method to run tests directly
     */
    public static void main(String[] args) {
        HolidayPayTest test = new HolidayPayTest();
        test.runTests();
    }
}