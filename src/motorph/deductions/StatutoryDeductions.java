// File: motorph/deductions/StatutoryDeductions.java
package motorph.deductions;

import java.util.ArrayList;
import java.util.List;

/**
 * Calculates government deductions (SSS, PhilHealth, Pag-IBIG, Tax)
 * Uses the DeductionProvider interface to handle deductions uniformly
 */
public class StatutoryDeductions {
    // Constants for pay period types
    public static final int MID_MONTH = DeductionProvider.MID_MONTH;
    public static final int END_MONTH = DeductionProvider.END_MONTH;

    // Deduction instances
    private static final SSS sssDeduction = new SSS();
    private static final PhilHealth philHealthDeduction = new PhilHealth();
    private static final PagIBIG pagIbigDeduction = new PagIBIG();
    private static final WithholdingTax taxDeduction = new WithholdingTax();

    // List of all deductions for easy iteration
    private static final List<DeductionProvider> allDeductions = new ArrayList<>();

    // Static initializer to populate the deductions list
    static {
        allDeductions.add(sssDeduction);
        allDeductions.add(philHealthDeduction);
        allDeductions.add(pagIbigDeduction);
        allDeductions.add(taxDeduction);
    }

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
            sssDeduction = StatutoryDeductions.sssDeduction.calculateContribution(fullMonthlyGross);
            philhealthDeduction = StatutoryDeductions.philHealthDeduction.calculateContribution(fullMonthlyGross) / 2; // Get semi-monthly
            pagibigDeduction = StatutoryDeductions.pagIbigDeduction.calculateContribution(fullMonthlyGross);
        }

        // For tax, deduct only on end-month payroll
        if (payPeriod == END_MONTH) {
            // Calculate statutory deductions for the month
            double monthlySSS = StatutoryDeductions.sssDeduction.calculateContribution(fullMonthlyGross);
            double monthlyPhilHealth = StatutoryDeductions.philHealthDeduction.calculateContribution(fullMonthlyGross);
            double monthlyPagIBIG = StatutoryDeductions.pagIbigDeduction.calculateContribution(fullMonthlyGross);

            // Calculate tax based on full monthly income minus the deductions
            withholdingTax = StatutoryDeductions.taxDeduction.calculateTax(
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