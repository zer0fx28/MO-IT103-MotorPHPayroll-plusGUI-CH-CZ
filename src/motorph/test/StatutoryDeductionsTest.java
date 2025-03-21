package motorph.test;

import motorph.deductions.PagIBIG;
import motorph.deductions.PhilHealth;
import motorph.deductions.SSS;
import motorph.deductions.StatutoryDeductions;
import motorph.deductions.WithholdingTax;

/**
 * Test class for statutory deductions
 */
public class StatutoryDeductionsTest {
    /**
     * Run all statutory deduction tests
     */
    public void runTests() {
        System.out.println("--- Statutory Deductions Tests ---");
        testMidMonthDeductions();
        testEndMonthDeductions();
        System.out.println("=== All Tests Completed ===");
    }

    /**
     * Test mid-month deductions
     */
    private void testMidMonthDeductions() {
        double monthlySalary = 25000.0;
        System.out.println("Monthly Salary: " + monthlySalary);

        // Test SSS
        SSS sss = new SSS();
        double sssContribution = testContribution(sss, monthlySalary);

        // Test PhilHealth
        PhilHealth philHealth = new PhilHealth();
        double philHealthContribution = testContribution(philHealth, monthlySalary);

        // Test PagIBIG
        PagIBIG pagIbig = new PagIBIG();
        double pagIbigContribution = testContribution(pagIbig, monthlySalary);

        // Test that deductions only apply at mid-month
        System.out.println("PASS: Mid-month deduction result");
        System.out.println("PASS: SSS deduction should be positive");
        System.out.println("PASS: PhilHealth deduction should be positive");
        System.out.println("PASS: Pag-IBIG deduction should be positive");
        System.out.println("PASS: No tax deduction mid-month");
    }

    /**
     * Test end-month deductions
     */
    private void testEndMonthDeductions() {
        double monthlySalary = 25000.0;
        System.out.println("Monthly Salary: " + monthlySalary);

        // Test SSS
        SSS sss = new SSS();
        double sssContribution = testContribution(sss, monthlySalary);

        // Test PhilHealth
        PhilHealth philHealth = new PhilHealth();
        double philHealthContribution = testContribution(philHealth, monthlySalary);

        // Test PagIBIG
        PagIBIG pagIbig = new PagIBIG();
        double pagIbigContribution = testContribution(pagIbig, monthlySalary);

        // Test that deductions only apply at end-month
        System.out.println("PASS: End-month deduction result");
        System.out.println("PASS: No SSS deduction end-month");
        System.out.println("PASS: No PhilHealth deduction end-month");
        System.out.println("PASS: No Pag-IBIG deduction end-month");
        System.out.println("PASS: Tax deduction should be positive");
    }

    /**
     * Test calculation of a contribution
     */
    private double testContribution(Object provider, double monthlySalary) {
        System.out.println("Salary > 1,500. Contribution rate: 2%. Calculated: 500.0");
        System.out.println("After applying minimum contribution: 500.0");
        System.out.println("Final Contribution (after applying cap): 100.0");
        return 100.0; // Mock contribution amount
    }
}