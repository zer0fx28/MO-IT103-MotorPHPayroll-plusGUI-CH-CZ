// File: motorph/deductions/WithholdingTax.java
package motorph.deductions;

/**
 * Calculates withholding tax based on the Philippine tax brackets
 * Implements the DeductionProvider interface for uniform handling
 */
public class WithholdingTax implements DeductionProvider {

    // Tax bracket thresholds
    private static final double BRACKET_0 = 20833; // 0% tax bracket
    private static final double BRACKET_1 = 33332; // 20% tax bracket
    private static final double BRACKET_2 = 66666; // 25% tax bracket
    private static final double BRACKET_3 = 166666; // 30% tax bracket
    private static final double BRACKET_4 = 666666; // 32% tax bracket

    // Fixed tax amounts for each bracket
    private static final double TAX_BRACKET_1 = 0; // Base tax for bracket 1
    private static final double TAX_BRACKET_2 = 2500; // Base tax for bracket 2
    private static final double TAX_BRACKET_3 = 10833; // Base tax for bracket 3
    private static final double TAX_BRACKET_4 = 40833.33; // Base tax for bracket 4
    private static final double TAX_BRACKET_5 = 200833.33; // Base tax for bracket 5

    // Tax rates for each bracket
    private static final double RATE_BRACKET_1 = 0.0; // 0%
    private static final double RATE_BRACKET_2 = 0.20; // 20%
    private static final double RATE_BRACKET_3 = 0.25; // 25%
    private static final double RATE_BRACKET_4 = 0.30; // 30%
    private static final double RATE_BRACKET_5 = 0.32; // 32%
    private static final double RATE_BRACKET_6 = 0.35; // 35%

    /**
     * Calculate withholding tax based on monthly taxable income
     * Applies progressive tax rates according to Philippine tax brackets
     *
     * @param monthlySalary Monthly gross salary
     * @return Withholding tax amount
     */
    @Override
    public double calculateContribution(double monthlySalary) {
        // For withholding tax, we need to account for deductions first
        // This is handled in the StatutoryDeductions class
        // Here we assume the input is already the taxable income

        return calculateTax(monthlySalary);
    }

    /**
     * Calculate tax based on taxable income
     * This is the core tax calculation method
     *
     * @param taxableIncome The income amount after deductions
     * @return The calculated tax amount
     */
    public double calculateTax(double taxableIncome) {
        // Apply tax brackets (based on Philippine tax table)
        if (taxableIncome <= BRACKET_0) {
            return 0; // No tax for income up to ₱20,833
        } else if (taxableIncome <= BRACKET_1) {
            return (taxableIncome - BRACKET_0) * RATE_BRACKET_2;
        } else if (taxableIncome <= BRACKET_2) {
            return TAX_BRACKET_2 + (taxableIncome - BRACKET_1) * RATE_BRACKET_3;
        } else if (taxableIncome <= BRACKET_3) {
            return TAX_BRACKET_3 + (taxableIncome - BRACKET_2) * RATE_BRACKET_4;
        } else if (taxableIncome <= BRACKET_4) {
            return TAX_BRACKET_4 + (taxableIncome - BRACKET_3) * RATE_BRACKET_5;
        } else {
            return TAX_BRACKET_5 + (taxableIncome - BRACKET_4) * RATE_BRACKET_6;
        }
    }

    /**
     * Calculate withholding tax based on monthly salary and deductions
     * This method accounts for deductions before calculating tax
     *
     * @param monthlySalary Monthly gross salary
     * @param sssDeduction SSS contribution amount
     * @param philhealthDeduction PhilHealth contribution amount
     * @param pagibigDeduction Pag-IBIG contribution amount
     * @return Withholding tax amount
     */
    public double calculateTax(double monthlySalary, double sssDeduction,
                               double philhealthDeduction, double pagibigDeduction) {
        // Calculate taxable income
        double taxableIncome = monthlySalary - (sssDeduction + philhealthDeduction + pagibigDeduction);

        // Calculate tax based on taxable income
        return calculateTax(taxableIncome);
    }

    /**
     * Get the name of this deduction
     *
     * @return The name "Withholding Tax"
     */
    @Override
    public String getName() {
        return "Withholding Tax";
    }

    /**
     * Check if withholding tax applies to this pay period
     * Withholding tax is only deducted on end-month payroll
     *
     * @param payPeriodType The pay period type (MID_MONTH or END_MONTH)
     * @return true if this is an end-month pay period, false otherwise
     */
    @Override
    public boolean appliesTo(int payPeriodType) {
        return payPeriodType == DeductionProvider.END_MONTH;
    }
}