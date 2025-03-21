// File: motorph/deductions/WithholdingTax.java
package motorph.deductions;

/**
 * Calculates withholding tax based on the Philippine tax brackets
 * This class implements withholding tax calculation based on the progressive
 * tax structure of the Philippines, taking into account the taxable income
 * after statutory deductions.
 */
public class WithholdingTax {

    /**
     * Calculate withholding tax based on monthly taxable income
     * Applies progressive tax rates according to Philippine tax brackets
     *
     * @param monthlySalary Monthly gross salary (should be non-negative)
     * @param sssDeduction SSS contribution amount
     * @param philhealthDeduction PhilHealth contribution amount
     * @param pagibigDeduction Pag-IBIG contribution amount
     * @return Withholding tax amount
     */
    public static double calculateTax(double monthlySalary, double sssDeduction,
                                      double philhealthDeduction, double pagibigDeduction) {
        // Input validation
        if (monthlySalary < 0) {
            System.out.println("Error: Negative salary provided for tax calculation. Using 0.0");
            monthlySalary = 0.0;
        }

        if (sssDeduction < 0) {
            System.out.println("Error: Negative SSS deduction provided. Using 0.0");
            sssDeduction = 0.0;
        }

        if (philhealthDeduction < 0) {
            System.out.println("Error: Negative PhilHealth deduction provided. Using 0.0");
            philhealthDeduction = 0.0;
        }

        if (pagibigDeduction < 0) {
            System.out.println("Error: Negative Pag-IBIG deduction provided. Using 0.0");
            pagibigDeduction = 0.0;
        }

        // Calculate taxable income after deductions
        double taxableIncome = monthlySalary - (sssDeduction + philhealthDeduction + pagibigDeduction);

        // Ensure taxable income is not negative
        taxableIncome = Math.max(0, taxableIncome);

        // Apply tax brackets (based on Philippine tax table)
        // Returns the appropriate tax amount based on the income bracket
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