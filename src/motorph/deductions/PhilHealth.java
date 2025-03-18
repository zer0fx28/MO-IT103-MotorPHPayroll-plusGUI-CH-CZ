// File: motorph/deductions/PhilHealth.java
package motorph.deductions;

/**
 * Calculates PhilHealth contributions based on monthly salary
 *
 * This class implements the computation of PhilHealth contributions according to
 * the government-mandated rate of 3% of monthly salary, with a maximum contribution cap.
 * The contribution is automatically divided by 2 to provide the semi-monthly rate.
 */
public class PhilHealth {
    // PhilHealth contribution constants
    private static final double RATE = 0.03; // 3% of monthly basic salary
    private static final double MAX_CONTRIBUTION = 1800.0; // Maximum monthly contribution

    /**
     * Calculate PhilHealth contribution based on monthly salary
     *
     * @param monthlySalary Monthly basic salary (should be non-negative)
     * @return PhilHealth contribution (semi-monthly)
     */
    public static double calculateContribution(double monthlySalary) {
        // Validate input: salary can't be negative
        if (monthlySalary < 0) {
            System.out.println("Error: Negative salary provided for PhilHealth calculation. Using 0.0");
            monthlySalary = 0.0;
        }

        // Calculate contribution (3% of monthly salary)
        double monthlyContribution = monthlySalary * RATE;

        // Cap at maximum contribution
        monthlyContribution = Math.min(monthlyContribution, MAX_CONTRIBUTION);

        // Return semi-monthly rate (half of monthly contribution)
        return monthlyContribution / 2;
    }
}