// File: motorph/deductions/PhilHealth.java
package motorph.deductions;

/**
 * Calculates PhilHealth contributions based on monthly salary
 * Implements the DeductionProvider interface for uniform handling
 */
public class PhilHealth implements DeductionProvider {

    // PhilHealth contribution constants
    private static final double RATE = 0.03; // 3% of monthly basic salary
    private static final double MAX_CONTRIBUTION = 1800.0; // Maximum monthly contribution

    /**
     * Calculate PhilHealth contribution based on monthly salary
     * Note: This returns the MONTHLY contribution, not semi-monthly
     *
     * @param monthlySalary Monthly basic salary
     * @return PhilHealth contribution (monthly)
     */
    @Override
    public double calculateContribution(double monthlySalary) {
        // Calculate contribution (3% of monthly salary)
        double monthlyContribution = monthlySalary * RATE;

        // Cap at maximum contribution
        monthlyContribution = Math.min(monthlyContribution, MAX_CONTRIBUTION);

        return monthlyContribution;
    }

    /**
     * Get the name of this deduction
     *
     * @return The name "PhilHealth"
     */
    @Override
    public String getName() {
        return "PhilHealth";
    }

    /**
     * Check if PhilHealth applies to this pay period
     * PhilHealth is only deducted on mid-month payroll
     *
     * @param payPeriodType The pay period type (MID_MONTH or END_MONTH)
     * @return true if this is a mid-month pay period, false otherwise
     */
    @Override
    public boolean appliesTo(int payPeriodType) {
        return payPeriodType == DeductionProvider.MID_MONTH;
    }

    /**
     * Get the semi-monthly contribution amount
     * This is a helper method specific to PhilHealth
     *
     * @param monthlySalary Monthly basic salary
     * @return PhilHealth contribution (semi-monthly)
     */
    public double calculateSemiMonthlyContribution(double monthlySalary) {
        return calculateContribution(monthlySalary) / 2;
    }
}