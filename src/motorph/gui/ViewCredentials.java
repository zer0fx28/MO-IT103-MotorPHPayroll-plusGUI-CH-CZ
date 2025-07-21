import motorph.gui.EmployeeCredentialsManager;
import java.io.*;

/**
 * View all generated employee credentials
 * Shows usernames and status for all your real employees
 */
public class ViewCredentials {
    public static void main(String[] args) {
        System.out.println("👀 MotorPH Employee Credential Viewer");
        System.out.println("═════════════════════════════════════");

        // Check if credentials file exists
        File credFile = new File("employee_credentials.txt");
        if (!credFile.exists()) {
            System.out.println("❌ No credentials file found!");
            System.out.println("💡 Run RealEmployeeCredentials first to generate credentials.");
            return;
        }

        // Load and display credentials
        EmployeeCredentialsManager manager = new EmployeeCredentialsManager();
        var allCreds = manager.getAllCredentials();

        if (allCreds.isEmpty()) {
            System.out.println("❌ No credentials loaded!");
            return;
        }

        System.out.println("📊 Found " + allCreds.size() + " employee credentials:");
        System.out.println();

        // Sort by employee ID for better organization
        allCreds.values().stream()
                .sorted((a, b) -> a.employeeId.compareTo(b.employeeId))
                .forEach(cred -> {
                    System.out.printf("👤 %-20s (ID: %s) → Username: %-25s Status: %s%n",
                            cred.fullName,
                            cred.employeeId,
                            cred.username,
                            cred.isTemporary ? "🔒 Temporary" : "✅ Set"
                    );
                });

        // Show summary
        long tempCount = allCreds.values().stream()
                .mapToLong(cred -> cred.isTemporary ? 1 : 0)
                .sum();

        System.out.println();
        System.out.println("📈 Summary:");
        System.out.println("   Total employees: " + allCreds.size());
        System.out.println("   Temporary passwords: " + tempCount);
        System.out.println("   Permanent passwords: " + (allCreds.size() - tempCount));

        System.out.println();
        System.out.println("🚀 Ready to test! Run: java motorph.gui.EmployeeLoginForm");
        System.out.println("🔑 Use any username above with the temporary password from generation");
    }
}