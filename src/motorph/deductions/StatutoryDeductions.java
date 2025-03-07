package motorph.deductions;

/**
 * Calculates all government deductions
 */
public class StatutoryDeductions {

    // Pay period types
    public static final int FIRST_HALF = 1;    // 1st-15th
    public static final int SECOND_HALF = 2;   // 16th-end of month

    // Minimum deductions per pay period
    private static final double MIN_PAGIBIG_DEDUCTION = 100.0;

    /**
     * Calculate all deductions based on salary and pay period
     *
     * @param grossSalary The gross salary for the period
     * @param payPeriod Either FIRST_HALF or SECOND_HALF
     * @return DeductionResult containing all deduction values
     */
    public static DeductionResult calculateDeductions(double grossSalary, int payPeriod) {
        // Convert to monthly amount for calculations
        double monthlyEquivalent = grossSalary * 2;

        double sssDeduction = 0;
        double philhealthDeduction = 0;
        double pagibigDeduction = 0;

        // Calculate for this pay period
        if (payPeriod == FIRST_HALF || payPeriod == SECOND_HALF) {
            // SSS contribution (half of monthly amount)
            sssDeduction = SSS.calculateContribution(monthlyEquivalent) / 2;

            // PhilHealth (already calculated as semi-monthly)
            philhealthDeduction = PhilHealth.calculateContribution(monthlyEquivalent);

            // Pag-IBIG (half of monthly amount)
            double calculatedPagibigDeduction = PagIBIG.calculateContribution(monthlyEquivalent) / 2;
            // Ensure minimum Pag-IBIG deduction per pay period
            pagibigDeduction = Math.max(calculatedPagibigDeduction, MIN_PAGIBIG_DEDUCTION);
        }

        // For tax calculation, we need full monthly values
        double sssFullDeduction = SSS.calculateContribution(monthlyEquivalent);
        double philhealthFullDeduction = PhilHealth.calculateContribution(monthlyEquivalent) * 2;
        double pagibigFullDeduction = PagIBIG.calculateContribution(monthlyEquivalent);

        // Calculate withholding tax on monthly salary
        double withholdingTax = WithholdingTax.calculateTax(
                monthlyEquivalent,
                sssFullDeduction,
                philhealthFullDeduction,
                pagibigFullDeduction
        );

        // Split tax between pay periods
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
     * Class to store deduction amounts
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
