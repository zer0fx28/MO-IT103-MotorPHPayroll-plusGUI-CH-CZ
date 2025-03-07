// File: motorph/deductions/StatutoryDeductions.java
package motorph.deductions;

/**
 * Handles calculation of all statutory deductions
 */
public class StatutoryDeductions {

    // Constants for pay period options
    public static final int FIRST_HALF = 1;
    public static final int SECOND_HALF = 2;

    /**
     * Calculate all statutory deductions based on pay period
     * @param grossSalary The gross salary for the period
     * @param payPeriod Either FIRST_HALF or SECOND_HALF
     * @return DeductionResult containing all deduction values
     */
    public static DeductionResult calculateDeductions(double grossSalary, int payPeriod) {
        // For semi-monthly pay, we double the gross amount to get monthly equivalent for calculation
        double monthlyEquivalent = grossSalary * 2;

        double sssDeduction = 0;
        double philhealthDeduction = 0;
        double pagibigDeduction = 0;

        // Calculate half of each statutory deduction for the pay period
        if (payPeriod == FIRST_HALF || payPeriod == SECOND_HALF) {
            // SSS contribution
            sssDeduction = SSS.calculateContribution(monthlyEquivalent) / 2;

            // PhilHealth contribution - already calculates for semi-monthly
            philhealthDeduction = PhilHealth.calculateContribution(monthlyEquivalent);

            // Pag-IBIG contribution
            pagibigDeduction = PagIBIG.calculateContribution(monthlyEquivalent) / 2;
        }

        // For withholding tax, calculate based on monthly income less the full deductions
        double sssFullDeduction = SSS.calculateContribution(monthlyEquivalent);
        double philhealthFullDeduction = PhilHealth.calculateContribution(monthlyEquivalent) * 2; // Convert from semi-monthly to monthly
        double pagibigFullDeduction = PagIBIG.calculateContribution(monthlyEquivalent);

        // Calculate withholding tax on the monthly equivalent
        double withholdingTax = WithholdingTax.calculateTax(
                monthlyEquivalent,
                sssFullDeduction,
                philhealthFullDeduction,
                pagibigFullDeduction
        );

        // Split withholding tax equally between pay periods
        double withholdingTaxForPeriod = withholdingTax / 2;

        // Return all calculated deductions
        return new DeductionResult(
                sssDeduction,
                philhealthDeduction,
                pagibigDeduction,
                withholdingTaxForPeriod,
                sssDeduction + philhealthDeduction + pagibigDeduction + withholdingTaxForPeriod
        );
    }

    /**
     * Class to hold all deduction results
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