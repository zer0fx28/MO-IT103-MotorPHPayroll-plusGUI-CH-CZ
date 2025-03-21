// File: motorph/deductions/StatutoryDeductions.java
package motorph.deductions;

import motorph.process.PayrollDateManager;
import motorph.util.ValidationUtils;

/**
 * Calculates government statutory deductions (SSS, PhilHealth, Pag-IBIG, Tax)
 * This class handles the calculation of all mandated government deductions based on
 * the following schedule:
 * - SSS, PhilHealth, Pag-IBIG: deducted on mid-month payroll
 * - Tax: deducted on end-month payroll
 * The class manages the timing of deductions and ensures the correct application
 * of each statutory requirement according to Philippine regulations.
 */
public class StatutoryDeductions {
    // Constants for pay period types
    public static final int MID_MONTH = PayrollDateManager.MID_MONTH;
    public static final int END_MONTH = PayrollDateManager.END_MONTH;

    /**
     * Calculate all deductions based on the schedule:
     * - SSS, PhilHealth, Pag-IBIG: deducted on mid-month payroll
     * - Tax: deducted on end-month payroll
     * This method orchestrates the calculation of all required government deductions
     * based on the pay period. It ensures that deductions are applied in the correct
     * pay period according to company policy.
     *
     * @param grossSalary The gross salary for the period (should be non-negative)
     * @param payPeriod Either MID_MONTH or END_MONTH
     * @param fullMonthlyGross The total monthly gross (both periods combined)
     * @return DeductionResult with all calculated deductions
     */
    public static DeductionResult calculateDeductions(double grossSalary, int payPeriod, double fullMonthlyGross) {
        // Validate inputs using the ValidationUtils class
        grossSalary = ValidationUtils.validateNonNegative(grossSalary, "gross salary", 0.0);
        fullMonthlyGross = ValidationUtils.validateNonNegative(fullMonthlyGross, "monthly gross", 0.0);
        payPeriod = ValidationUtils.validatePayPeriodType(payPeriod, MID_MONTH, END_MONTH, MID_MONTH);

        // By default, set all deductions to zero
        double sssDeduction = 0;
        double philhealthDeduction = 0;
        double pagibigDeduction = 0;
        double withholdingTax = 0;

        // For SSS, PhilHealth, and Pag-IBIG, deduct only on mid-month payroll
        if (payPeriod == MID_MONTH) {
            // Calculate based on full monthly salary
            sssDeduction = calculateSSSContribution(fullMonthlyGross);
            philhealthDeduction = calculatePhilHealthContribution(fullMonthlyGross);
            pagibigDeduction = calculatePagIBIGContribution(fullMonthlyGross);
        }

        // For tax, deduct only on end-month payroll
        if (payPeriod == END_MONTH) {
            // Calculate monthly deductions for tax computation
            double monthlySSS = calculateSSSContribution(fullMonthlyGross);
            double monthlyPhilHealth = calculatePhilHealthContribution(fullMonthlyGross);
            double monthlyPagIBIG = calculatePagIBIGContribution(fullMonthlyGross);

            // Calculate tax based on full monthly income minus the deductions
            withholdingTax = calculateWithholdingTax(
                    fullMonthlyGross,
                    monthlySSS,
                    monthlyPhilHealth,
                    monthlyPagIBIG
            );
        }

        // Calculate total deductions
        double totalDeductions = sssDeduction + philhealthDeduction + pagibigDeduction + withholdingTax;

        // Return the deduction result
        return new DeductionResult(
                sssDeduction,
                philhealthDeduction,
                pagibigDeduction,
                withholdingTax,
                totalDeductions
        );
    }

    /**
     * Calculate SSS contribution with proper error handling
     *
     * @param fullMonthlyGross Monthly gross salary
     * @return SSS contribution amount
     */
    private static double calculateSSSContribution(double fullMonthlyGross) {
        try {
            return SSS.calculateContribution(fullMonthlyGross);
        } catch (Exception e) {
            System.out.println("Error calculating SSS contribution: " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * Calculate PhilHealth contribution with proper error handling
     *
     * @param fullMonthlyGross Monthly gross salary
     * @return PhilHealth contribution amount
     */
    private static double calculatePhilHealthContribution(double fullMonthlyGross) {
        try {
            return PhilHealth.calculateContribution(fullMonthlyGross);
        } catch (Exception e) {
            System.out.println("Error calculating PhilHealth contribution: " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * Calculate Pag-IBIG contribution with proper error handling
     *
     * @param fullMonthlyGross Monthly gross salary
     * @return Pag-IBIG contribution amount
     */
    private static double calculatePagIBIGContribution(double fullMonthlyGross) {
        try {
            return PagIBIG.calculateContribution(fullMonthlyGross);
        } catch (Exception e) {
            System.out.println("Error calculating Pag-IBIG contribution: " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * Calculate withholding tax with proper error handling
     *
     * @param fullMonthlyGross Monthly gross salary
     * @param sssDeduction SSS contribution amount
     * @param philhealthDeduction PhilHealth contribution amount
     * @param pagibigDeduction Pag-IBIG contribution amount
     * @return Withholding tax amount
     */
    private static double calculateWithholdingTax(double fullMonthlyGross,
                                                  double sssDeduction,
                                                  double philhealthDeduction,
                                                  double pagibigDeduction) {
        try {
            return WithholdingTax.calculateTax(
                    fullMonthlyGross,
                    sssDeduction,
                    philhealthDeduction,
                    pagibigDeduction
            );
        } catch (Exception e) {
            System.out.println("Error calculating withholding tax: " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * Simple class to hold all deduction amounts for an employee
     * This immutable data class stores the results of all deduction calculations
     * for an employee, providing a structured way to return multiple values.
     */
    public static class DeductionResult {
        public final double sssDeduction;
        public final double philhealthDeduction;
        public final double pagibigDeduction;
        public final double withholdingTax;
        public final double totalDeductions;

        /**
         * Create a new deduction result container
         *
         * @param sssDeduction SSS contribution amount
         * @param philhealthDeduction PhilHealth contribution amount
         * @param pagibigDeduction Pag-IBIG contribution amount
         * @param withholdingTax Withholding tax amount
         * @param totalDeductions Sum of all deductions
         */
        public DeductionResult(
                double sssDeduction,
                double philhealthDeduction,
                double pagibigDeduction,
                double withholdingTax,
                double totalDeductions
        ) {
            this.sssDeduction = sssDeduction;
            this.philhealthDeduction = philhealthDeduction;
            this.pagibigDeduction = pagibigDeduction;
            this.withholdingTax = withholdingTax;
            this.totalDeductions = totalDeductions;
        }

        /**
         * Get a string representation of deduction results
         *
         * @return Formatted string with all deduction amounts
         */
        @Override
        public String toString() {
            return String.format(
                    "Deductions: [SSS: ₱%.2f, PhilHealth: ₱%.2f, Pag-IBIG: ₱%.2f, Tax: ₱%.2f, Total: ₱%.2f]",
                    sssDeduction, philhealthDeduction, pagibigDeduction, withholdingTax, totalDeductions
            );
        }
    }
}