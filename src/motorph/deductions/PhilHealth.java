// File: motorph/deductions/PhilHealth.java
package motorph.deductions;

/**
 * Calculates PhilHealth contributions
 */
public class PhilHealth {
    private static final double RATE = 0.03; // 3% of monthly basic salary
    private static final double MAX_CONTRIBUTION = 1800.0; // Maximum monthly contribution

    /**
     * Calculate PhilHealth contribution based on monthly salary
     * Note: This already returns the semi-monthly rate
     *
     * @param monthlySalary Monthly basic salary
     * @return PhilHealth contribution (semi-monthly)
     */
    public static double calculateContribution(double monthlySalary) {
        // Calculate contribution (3% of monthly salary)
        double monthlyContribution = monthlySalary * RATE;

        // Cap at maximum contribution
        monthlyContribution = Math.min(monthlyContribution, MAX_CONTRIBUTION);

        // Return semi-monthly rate (half of monthly contribution)
        return monthlyContribution / 2;
    }
}