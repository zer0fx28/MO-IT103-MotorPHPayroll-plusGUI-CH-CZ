// File: motorph/deductions/WithholdingTax.java
package motorph.deductions;

/**
 * Calculates withholding tax based on the Philippine tax brackets
 */
public class WithholdingTax {

    /**
     * Calculate withholding tax based on monthly taxable income
     * Applies progressive tax rates according to Philippine tax brackets
     *
     * @param monthlySalary Monthly gross salary
     * @param sssDeduction SSS contribution amount
     * @param philhealthDeduction PhilHealth contribution amount
     * @param pagibigDeduction Pag-IBIG contribution amount
     * @return Withholding tax amount
     */
    public static double calculateTax(double monthlySalary, double sssDeduction,
                                      double philhealthDeduction, double pagibigDeduction) {
        // Calculate taxable income
        double taxableIncome = monthlySalary - (sssDeduction + philhealthDeduction + pagibigDeduction);

        // Apply tax brackets (based on Philippine tax table)
        if (taxableIncome <= 20833) {
            return 0; // No tax for income up to ₱20,833
        } else if (taxableIncome <= 33332) {
            return (taxableIncome - 20833) * 0.20;
        } else if (taxableIncome <= 66666) {
            return 2500 + (taxableIncome - 33333) * 0.25;
        } else if (taxableIncome <= 166666) {
            return 10833 + (taxableIncome - 66667) * 0.30;
        } else if (taxableIncome <= 666666) {
            return 40833.33 + (taxableIncome - 166667) * 0.32;
        } else {
            return 200833.33 + (taxableIncome - 666667) * 0.35;
        }
    }
}