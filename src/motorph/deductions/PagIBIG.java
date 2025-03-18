package motorph.deductions;

/**
 * Calculates Pag-IBIG Fund contributions based on salary range
 *
 * This class handles the computation of monthly Pag-IBIG Fund contributions
 * based on the government-mandated rates:
 * - 1% for salaries between ₱1,000 to ₱1,500
 * - 2% for salaries over ₱1,500
 * - Minimum contribution: ₱100
 * - Maximum contribution: ₱100
 */
public class PagIBIG {
    // Contribution rates and thresholds
    private static final double RATE_LOWER = 0.01; // 1% for salaries from 1,000 to 1,500
    private static final double RATE_HIGHER = 0.02; // 2% for salaries over 1,500
    private static final double MIN_SALARY = 1000.0; // Minimum salary threshold
    private static final double MID_SALARY = 1500.0; // Middle salary threshold
    private static final double MAX_CONTRIBUTION = 100.0; // Maximum monthly contribution
    private static final double MIN_CONTRIBUTION = 100.0; // Minimum monthly contribution

    /**
     * Calculate Pag-IBIG contribution based on monthly salary
     *
     * @param monthlySalary Monthly basic salary (should be non-negative)
     * @return Pag-IBIG contribution amount (monthly)
     */
    public static double calculateContribution(double monthlySalary) {
        // Validate input: salary can't be negative
        if (monthlySalary < 0) {
            System.out.println("Error: Negative salary provided. Using 0.0");
            monthlySalary = 0.0;
        }

        // For debugging purposes
        System.out.println("Monthly Salary: " + monthlySalary);

        // If salary is below minimum threshold, no contribution
        if (monthlySalary < MIN_SALARY) {
            System.out.println("Salary below minimum threshold. Contribution: 0.0");
            return 0.0;
        }

        // Calculate contribution based on salary range
        double contribution;
        if (monthlySalary <= MID_SALARY) {
            // 1% for salaries from 1,000 to 1,500
            contribution = monthlySalary * RATE_LOWER;
            System.out.println("Salary <= 1,500. Contribution rate: 1%. Calculated: " + contribution);
        } else {
            // 2% for salaries over 1,500
            contribution = monthlySalary * RATE_HIGHER;
            System.out.println("Salary > 1,500. Contribution rate: 2%. Calculated: " + contribution);
        }

        // Ensure contribution is at least the minimum contribution amount
        contribution = Math.max(contribution, MIN_CONTRIBUTION);
        System.out.println("After applying minimum contribution: " + contribution);

        // Cap at maximum contribution amount
        contribution = Math.min(contribution, MAX_CONTRIBUTION);
        System.out.println("Final Contribution (after applying cap): " + contribution);

        return contribution;
    }
}