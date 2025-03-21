package motorph.deductions;

import motorph.util.ValidationUtils;

/**
 * Calculates Social Security System (SSS) contributions
 * This class implements the SSS contribution calculation based on
 * the Philippine SSS contribution table. The calculation returns the
 * proper contribution amount based on the employee's monthly compensation.
 * The SSS contribution is structured as a tiered system based on monthly
 * compensation ranges. Each range has a corresponding fixed contribution amount.
 */
public class SSS {

    /**
     * Minimum compensation threshold for SSS contributions
     */
    private static final double MIN_COMPENSATION = 0.0;

    /**
     * Maximum compensation threshold for SSS contributions
     * Earnings above this threshold still contribute the maximum amount
     */
    private static final double MAX_COMPENSATION = 24750.0;

    /**
     * Maximum SSS contribution amount
     */
    private static final double MAX_CONTRIBUTION = 1125.00;

    /**
     * Calculate SSS contribution based on monthly compensation
     * This method applies the SSS contribution table to determine the
     * appropriate contribution amount based on the employee's monthly
     * compensation. The contribution follows a progressive structure
     * with different amounts for different salary ranges.
     *
     * @param compensation Monthly compensation amount (should be non-negative)
     * @return The appropriate SSS contribution amount based on the compensation tier
     */
    public static double calculateContribution(double compensation) {
        // Input validation with the utility class
        compensation = ValidationUtils.validateNonNegative(
                compensation, "compensation", 0.0);

        // Early return for zero compensation
        if (compensation == 0.0) {
            return 0.0;
        }

        // Apply the SSS contribution table
        // Each range has a corresponding fixed contribution amount
        if (compensation < 3250) return 135.00;
        else if (compensation < 3750) return 157.50;
        else if (compensation < 4250) return 180.00;
        else if (compensation < 4750) return 202.50;
        else if (compensation < 5250) return 225.00;
        else if (compensation < 5750) return 247.50;
        else if (compensation < 6250) return 270.00;
        else if (compensation < 6750) return 292.50;
        else if (compensation < 7250) return 315.00;
        else if (compensation < 7750) return 337.50;
        else if (compensation < 8250) return 360.00;
        else if (compensation < 8750) return 382.50;
        else if (compensation < 9250) return 405.00;
        else if (compensation < 9750) return 427.50;
        else if (compensation < 10250) return 450.00;
        else if (compensation < 10750) return 472.50;
        else if (compensation < 11250) return 495.00;
        else if (compensation < 11750) return 517.50;
        else if (compensation < 12250) return 540.00;
        else if (compensation < 12750) return 562.50;
        else if (compensation < 13250) return 585.00;
        else if (compensation < 13750) return 607.50;
        else if (compensation < 14250) return 630.00;
        else if (compensation < 14750) return 652.50;
        else if (compensation < 15250) return 675.00;
        else if (compensation < 15750) return 697.50;
        else if (compensation < 16250) return 720.00;
        else if (compensation < 16750) return 742.50;
        else if (compensation < 17250) return 765.00;
        else if (compensation < 17750) return 787.50;
        else if (compensation < 18250) return 810.00;
        else if (compensation < 18750) return 832.50;
        else if (compensation < 19250) return 855.00;
        else if (compensation < 19750) return 877.50;
        else if (compensation < 20250) return 900.00;
        else if (compensation < 20750) return 922.50;
        else if (compensation < 21250) return 945.00;
        else if (compensation < 21750) return 967.50;
        else if (compensation < 22250) return 990.00;
        else if (compensation < 22750) return 1012.50;
        else if (compensation < 23250) return 1035.00;
        else if (compensation < 23750) return 1057.50;
        else if (compensation < 24250) return 1080.00;
        else if (compensation < 24750) return 1102.50;
        else return MAX_CONTRIBUTION; // Maximum contribution amount
    }

    /**
     * Calculate SSS contribution with a more efficient algorithm
     * This alternative implementation uses a more maintainable approach
     * with arrays instead of multiple if-else statements. The function
     * is semantically equivalent to the original implementation.
     *
     * @param compensation Monthly compensation amount
     * @return The appropriate SSS contribution amount
     */
    public static double calculateContributionAlternative(double compensation) {
        // Input validation
        compensation = ValidationUtils.validateNonNegative(
                compensation, "compensation", 0.0);

        // Early return for zero compensation
        if (compensation == 0.0) {
            return 0.0;
        }

        // Define compensation brackets and corresponding contribution amounts
        double[] bracketLimits = {
                3250, 3750, 4250, 4750, 5250, 5750, 6250, 6750, 7250, 7750,
                8250, 8750, 9250, 9750, 10250, 10750, 11250, 11750, 12250, 12750,
                13250, 13750, 14250, 14750, 15250, 15750, 16250, 16750, 17250, 17750,
                18250, 18750, 19250, 19750, 20250, 20750, 21250, 21750, 22250, 22750,
                23250, 23750, 24250, 24750
        };

        double[] contributionAmounts = {
                135.00, 157.50, 180.00, 202.50, 225.00, 247.50, 270.00, 292.50, 315.00, 337.50,
                360.00, 382.50, 405.00, 427.50, 450.00, 472.50, 495.00, 517.50, 540.00, 562.50,
                585.00, 607.50, 630.00, 652.50, 675.00, 697.50, 720.00, 742.50, 765.00, 787.50,
                810.00, 832.50, 855.00, 877.50, 900.00, 922.50, 945.00, 967.50, 990.00, 1012.50,
                1035.00, 1057.50, 1080.00, 1102.50
        };

        // Find the appropriate bracket
        for (int i = 0; i < bracketLimits.length; i++) {
            if (compensation < bracketLimits[i]) {
                return contributionAmounts[i];
            }
        }

        // Return maximum contribution if compensation exceeds the highest bracket
        return MAX_CONTRIBUTION;
    }
}