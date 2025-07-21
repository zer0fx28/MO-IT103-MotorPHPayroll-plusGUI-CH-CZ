package motorph.gui;

import java.io.File;
import java.util.Map;
import java.util.Scanner;

/**
 * Generate credentials using real employee CSV data
 */
public class RealEmployeeCredentials {

    public static void main(String[] args) {
        System.out.println("🏢 MotorPH Real Employee Credential Generator");
        System.out.println("═════════════════════════════════════════════");

        Scanner scanner = new Scanner(System.in);

        // Get CSV file path
        System.out.println("📁 Enter the full path to your employee CSV file:");
        System.out.println("(You can drag and drop the file here)");
        String csvPath = scanner.nextLine().trim();

        // Clean path (remove quotes)
        csvPath = csvPath.replaceAll("^['\"]|['\"]$", "");

        // Check if file exists
        File csvFile = new File(csvPath);
        if (!csvFile.exists()) {
            System.out.println("❌ File not found: " + csvPath);
            System.out.println("💡 Make sure to use the complete file path");
            return;
        }

        System.out.println("✅ Found CSV file: " + csvFile.getName());
        System.out.println("📊 File size: " + csvFile.length() + " bytes");

        // Preview the file
        System.out.println("\n🔍 Generating credentials for your real employees...");

        try {
            // Create credentials manager
            EmployeeCredentialsManager manager = new EmployeeCredentialsManager();

            // Clear existing credentials
            File credFile = new File("employee_credentials.txt");
            if (credFile.exists()) {
                System.out.println("🗑️  Removing existing credentials...");
                credFile.delete();
            }

            // Generate credentials
            System.out.println("\n🔐 Generating BCrypt-secured credentials...\n");
            manager.generateTemporaryCredentials(csvPath);

            // Show summary
            System.out.println("\n" + "=".repeat(60));
            System.out.println("✅ SUCCESS! All employee credentials generated!");
            System.out.println("=".repeat(60));

            // Display sample credentials
            Map<String, EmployeeCredentialsManager.EmployeeCredential> allCreds = manager.getAllCredentials();
            System.out.println("📊 Total employees: " + allCreds.size());
            System.out.println("\n📋 Sample credentials (first 3 employees):");

            int count = 0;
            for (var cred : allCreds.values()) {
                if (count >= 3) break;
                System.out.println("👤 " + cred.fullName + " (" + cred.employeeId + ")");
                System.out.println("   Username: " + cred.username);
                System.out.println("   Status: Temporary password (requires change)");
                System.out.println();
                count++;
            }

            if (allCreds.size() > 3) {
                System.out.println("... and " + (allCreds.size() - 3) + " more employees");
            }

            // Instructions
            System.out.println("\n📋 NEXT STEPS:");
            System.out.println("1. 🚀 Run: mvn exec:java -Dexec.mainClass=\"motorph.gui.EmployeeLoginForm\"");
            System.out.println("2. 🔑 Use any username from above with its temporary password");
            System.out.println("3. 🔒 Change the temporary password when prompted");
            System.out.println("4. 📊 Access the employee dashboard");

            System.out.println("\n💡 Password Requirements for new passwords:");
            System.out.println(manager.getPasswordRequirements());

            System.out.println("\n🎉 All your employees can now login to the system!");

        } catch (Exception e) {
            System.out.println("❌ Error generating credentials: " + e.getMessage());
            e.printStackTrace();
        }

        scanner.close();
    }
}