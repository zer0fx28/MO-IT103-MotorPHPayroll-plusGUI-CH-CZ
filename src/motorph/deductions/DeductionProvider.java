// File: motorph/deductions/DeductionProvider.java
package motorph.deductions;

import motorph.process.PayrollDateManager;

/**
 * Interface for deduction providers
 * This interface represents components that can provide deduction calculations
 */
public interface DeductionProvider {

    /**
     * Constants for pay period types
     */
    public static final int MID_MONTH = PayrollDateManager.MID_MONTH;
    public static final int END_MONTH = PayrollDateManager.END_MONTH;

    /**
     * Calculate the contribution amount based on monthly salary
     *
     * @param monthlySalary Monthly basic salary
     * @return Calculated contribution amount
     */
    double calculateContribution(double monthlySalary);

    /**
     * Get the name of this deduction
     *
     * @return Deduction name (e.g., "SSS", "PhilHealth")
     */
    String getName();

    /**
     * Determine if this deduction applies to the given pay period
     *
     * @param payPeriodType Pay period type (MID_MONTH or END_MONTH)
     * @return true if applicable, false otherwise
     */
    boolean appliesTo(int payPeriodType);
}