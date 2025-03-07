// File: motorph/deductions/PagIBIG.java
package motorph.deductions;

/**
 * Calculates Pag-IBIG Fund contributions
 */
public class PagIBIG {
    private static final double MIN_CONTRIBUTION = 100.0;
    private static final double MAX_CONTRIBUTION = 100.0;

    /**
     * Calculate Pag-IBIG contribution based on monthly salary
     * Currently implemented as a flat rate of 100.0 pesos
     *
     * @param monthlySalary Monthly basic salary
     * @return Pag-IBIG contribution amount (monthly)
     */
    public static double calculateContribution(double monthlySalary) {
        // Simplified Pag-IBIG contribution (flat rate)
        return MAX_CONTRIBUTION;

        // For a more accurate implementation, could use the following:
        // 2% or 3% of monthly compensation depending on salary bracket
        // double rate = (monthlySalary <= 1500.0) ? 0.02 : 0.03;
        // double contribution = monthlySalary * rate;
        // return Math.min(contribution, MAX_CONTRIBUTION);
    }
}