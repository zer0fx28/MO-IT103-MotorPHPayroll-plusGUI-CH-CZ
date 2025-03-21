// File: motorph/deductions/StatutoryDeductions.java
package motorph.deductions;

import motorph.process.PayrollDateManager;

/**
 * Calculates government deductions (SSS, PhilHealth, Pag-IBIG, Tax)
 */
public class StatutoryDeductions {
    // Constants for pay period types
    public static final int MID_MONTH = PayrollDateManager.MID_MONTH;
    public static final int END_MONTH = PayrollDateManager.END_MONTH;

    /**
     * Calculate all deductions based on the new schedule:
     * - SSS, PhilHealth, Pag-IBIG: deducted on mid-month payroll
     * - Tax: deducted on end-month payroll
     *
     * @param grossSalary The gross salary for the period
     * @param payPeriod Either MID_MONTH or END_MONTH
     * @param fullMonthlyGross The total monthly gross (both periods)
     * @return DeductionResult with all the calculated deductions
     */
    public static DeductionResult calculateDeductions(double grossSalary, int payPeriod, double fullMonthlyGross) {
        // By default, set all deductions to zero
        double sssDeduction = 0;
        double philhealthDeduction = 0;
        double pagibigDeduction = 0;
        double withholdingTax = 0;

        // For SSS, PhilHealth, and Pag-IBIG, deduct only on mid-month payroll
        if (payPeriod == MID_MONTH) {
            // Calculate based on full monthly salary
            sssDeduction = SSS.calculateContribution(fullMonthlyGross);
            philhealthDeduction = PhilHealth.calculateContribution(fullMonthlyGross);
            pagibigDeduction = PagIBIG.calculateContribution(fullMonthlyGross);
        }

        // For tax, deduct only on end-month payroll
        if (payPeriod == END_MONTH) {
            // Calculate statutory deductions for the month
            double monthlySSS = SSS.calculateContribution(fullMonthlyGross);
            double monthlyPhilHealth = PhilHealth.calculateContribution(fullMonthlyGross);
            double monthlyPagIBIG = PagIBIG.calculateContribution(fullMonthlyGross);

            // Calculate tax based on full monthly income minus the deductions
            withholdingTax = WithholdingTax.calculateTax(
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
     * Simple class to hold all deduction amounts
     */
    public static class DeductionResult {
        public final double sssDeduction;
        public final double philhealthDeduction;
        public final double pagibigDeduction;
        public final double withholdingTax;
        public final double totalDeductions;

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
    }
}