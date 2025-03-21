// File: motorph/deductions/PagIBIG.java
package motorph.deductions;

/**
 * Calculates Pag-IBIG Fund contributions based on salary range
 * Implements the DeductionProvider interface for uniform handling
 */
public class PagIBIG implements DeductionProvider {

    // Contribution rates
    private static final double RATE_LOWER = 0.01; // 1% for salaries from 1,000 to 1,500
    private static final double RATE_HIGHER = 0.02; // 2% for salaries over 1,500
    private static final double MIN_SALARY = 1000.0; // Minimum salary threshold
    private static final double MID_SALARY = 1500.0; // Middle salary threshold
    private static final double MAX_CONTRIBUTION = 100.0; // Maximum monthly contribution
    private static final double MIN_CONTRIBUTION = 100.0; // Minimum monthly contribution

    /**
     * Calculate Pag-IBIG contribution based on monthly salary
     *
     * @param monthlySalary Monthly basic salary
     * @return Pag-IBIG contribution amount (monthly)
     */
    @Override
    public double calculateContribution(double monthlySalary) {
        // If salary is below minimum threshold, no contribution
        if (monthlySalary < MIN_SALARY) {
            return 0.0;
        }

        // Calculate contribution based on salary range
        double contribution;
        if (monthlySalary <= MID_SALARY) {
            // 1% for salaries from 1,000 to 1,500
            contribution = monthlySalary * RATE_LOWER;
        } else {
            // 2% for salaries over 1,500
            contribution = monthlySalary * RATE_HIGHER;
        }

        // Ensure contribution is at least the minimum contribution amount
        contribution = Math.max(contribution, MIN_CONTRIBUTION);

        // Cap at maximum contribution amount
        contribution = Math.min(contribution, MAX_CONTRIBUTION);

        return contribution;
    }

    /**
     * Get the name of this deduction
     *
     * @return The name "Pag-IBIG"
     */
    @Override
    public String getName() {
        return "Pag-IBIG";
    }

    /**
     * Check if Pag-IBIG applies to this pay period
     * Pag-IBIG is only deducted on mid-month payroll
     *
     * @param payPeriodType The pay period type (MID_MONTH or END_MONTH)
     * @return true if this is a mid-month pay period, false otherwise
     */
    @Override
    public boolean appliesTo(int payPeriodType) {
        return payPeriodType == DeductionProvider.MID_MONTH;
    }
}