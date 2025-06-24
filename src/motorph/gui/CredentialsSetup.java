// File: motorph/gui/CredentialsSetup.java
package motorph.gui;

/**
 * Utility class to set up employee credentials
 * Run this once to generate all employee login credentials
 */
public class CredentialsSetup {
    public static void main(String[] args) {
        try {
            // Set look and feel
            javax.swing.UIManager.setLookAndFeel(
                    javax.swing.UIManager.getCrossPlatformLookAndFeelClassName());
        } catch(Exception e) {
            System.err.println("Could not set Look and Feel: " + e.getMessage());
        }

        javax.swing.SwingUtilities.invokeLater(() -> {
            EmployeeCredentialsManager manager = new EmployeeCredentialsManager();

            // Show credentials generation dialog
            javax.swing.JFrame tempFrame = new javax.swing.JFrame();
            tempFrame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);

            String csvPath = "/Users/zer0fx28/IdeaProjects/MO-IT103-MotorPHPayroll-plusGUI-CH-CZ/resources/MotorPH Employee Data - Employee Details.csv";

            manager.showCredentialsGenerationDialog(tempFrame, csvPath);
        });
    }
}