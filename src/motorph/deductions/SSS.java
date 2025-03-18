package motorph.deductions;

/**
 * Calculates Social Security System (SSS) contributions
 *
 * This class implements the SSS contribution calculation based on
 * the Philippine SSS contribution table. The calculation returns the
 * proper contribution amount based on the employee's monthly compensation.
 */
public class SSS {
    /**
     * Calculate SSS contribution based on monthly compensation
     *
     * @param compensation Monthly compensation amount (should be non-negative)
     * @return The appropriate SSS contribution amount
     */
    public static double calculateContribution(double compensation) {
        // Input validation
        if (compensation < 0) {
            System.out.println("Error: Negative compensation provided for SSS calculation. Using 0.0");
            compensation = 0.0;
        }

        // Contribution calculation based on SSS contribution table
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
        else return 1125.00; // Maximum contribution amount
    }
}