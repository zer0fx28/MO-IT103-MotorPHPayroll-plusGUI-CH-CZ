package motorph.gui;

import motorph.employee.Employee;
import motorph.employee.EmployeeDataReader;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.List;

/**
 * Manages employee login credentials
 * Creates temporary passwords and handles password changes
 */
public class EmployeeCredentialsManager {

    private static final String CREDENTIALS_FILE = "employee_credentials.txt";
    private Map<String, EmployeeCredential> credentials;

    // Inner class to store credential information
    public static class EmployeeCredential {
        public String employeeId;
        public String username;
        public String passwordHash;
        public boolean isTemporary;
        public String fullName;

        public EmployeeCredential(String employeeId, String username, String passwordHash,
                                  boolean isTemporary, String fullName) {
            this.employeeId = employeeId;
            this.username = username;
            this.passwordHash = passwordHash;
            this.isTemporary = isTemporary;
            this.fullName = fullName;
        }
    }

    public EmployeeCredentialsManager() {
        credentials = new HashMap<>();
        loadCredentials();
    }

    /**
     * Generate temporary credentials for all employees
     */
    public void generateTemporaryCredentials(String csvFilePath) {
        try {
            EmployeeDataReader reader = new EmployeeDataReader(csvFilePath);
            List<Employee> employees = reader.getAllEmployees();

            System.out.println("🔐 Generating temporary credentials for " + employees.size() + " employees...");

            for (Employee employee : employees) {
                String username = generateUsername(employee);
                String tempPassword = generateTemporaryPassword();
                String passwordHash = hashPassword(tempPassword);

                EmployeeCredential credential = new EmployeeCredential(
                        employee.getEmployeeId(),
                        username,
                        passwordHash,
                        true, // Temporary password
                        employee.getFullName()
                );

                credentials.put(username, credential);

                // Print for admin reference
                System.out.println(String.format("👤 %s (%s) → Username: %s, Temp Password: %s",
                        employee.getFullName(), employee.getEmployeeId(), username, tempPassword));
            }

            saveCredentials();
            System.out.println("✅ Temporary credentials generated and saved!");

        } catch (Exception e) {
            System.err.println("❌ Error generating credentials: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Generate username from employee data
     */
    private String generateUsername(Employee employee) {
        // Format: firstname.lastname (lowercase, no spaces)
        String firstName = employee.getFirstName().toLowerCase().replaceAll("[^a-z]", "");
        String lastName = employee.getLastName().toLowerCase().replaceAll("[^a-z]", "");

        String baseUsername = firstName + "." + lastName;

        // Check if username already exists, add number if needed
        String username = baseUsername;
        int counter = 1;
        while (credentials.containsKey(username)) {
            username = baseUsername + counter;
            counter++;
        }

        return username;
    }

    /**
     * Generate a temporary password
     */
    private String generateTemporaryPassword() {
        // Format: MotorPH + 4 random digits (e.g., MotorPH2024)
        SecureRandom random = new SecureRandom();
        int randomNum = 1000 + random.nextInt(9000); // 4-digit number
        return "MotorPH" + randomNum;
    }

    /**
     * Validate login credentials
     */
    public LoginResult validateLogin(String username, String password) {
        EmployeeCredential credential = credentials.get(username.toLowerCase());

        if (credential == null) {
            return new LoginResult(false, null, "Invalid username or password", false);
        }

        String enteredPasswordHash = hashPassword(password);
        if (!credential.passwordHash.equals(enteredPasswordHash)) {
            return new LoginResult(false, null, "Invalid username or password", false);
        }

        // Login successful
        return new LoginResult(true, credential, "Login successful", credential.isTemporary);
    }

    /**
     * Change employee password (for first-time login)
     */
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        // Validate old password first
        LoginResult loginResult = validateLogin(username, oldPassword);
        if (!loginResult.success) {
            return false;
        }

        // Validate new password strength
        if (!isPasswordValid(newPassword)) {
            return false;
        }

        // Update password
        EmployeeCredential credential = credentials.get(username.toLowerCase());
        credential.passwordHash = hashPassword(newPassword);
        credential.isTemporary = false; // No longer temporary

        saveCredentials();
        return true;
    }

    /**
     * Validate password strength
     */
    private boolean isPasswordValid(String password) {
        if (password.length() < 8) {
            return false; // Too short
        }

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            if (Character.isLowerCase(c)) hasLower = true;
            if (Character.isDigit(c)) hasDigit = true;
        }

        return hasUpper && hasLower && hasDigit;
    }

    /**
     * Get password requirements message
     */
    public String getPasswordRequirements() {
        return "Password must be at least 8 characters long and contain:\n" +
                "• At least one uppercase letter\n" +
                "• At least one lowercase letter\n" +
                "• At least one number";
    }

    /**
     * Hash password using SHA-256
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    /**
     * Save credentials to file
     */
    private void saveCredentials() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(CREDENTIALS_FILE))) {
            for (EmployeeCredential credential : credentials.values()) {
                writer.println(String.format("%s|%s|%s|%s|%s",
                        credential.employeeId,
                        credential.username,
                        credential.passwordHash,
                        credential.isTemporary,
                        credential.fullName));
            }
        } catch (IOException e) {
            System.err.println("Error saving credentials: " + e.getMessage());
        }
    }

    /**
     * Load credentials from file
     */
    private void loadCredentials() {
        File file = new File(CREDENTIALS_FILE);
        if (!file.exists()) {
            return; // No existing credentials
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 5) {
                    EmployeeCredential credential = new EmployeeCredential(
                            parts[0], // employeeId
                            parts[1], // username
                            parts[2], // passwordHash
                            Boolean.parseBoolean(parts[3]), // isTemporary
                            parts[4]  // fullName
                    );
                    credentials.put(credential.username, credential);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading credentials: " + e.getMessage());
        }
    }

    /**
     * Get all credentials (for admin reference)
     */
    public Map<String, EmployeeCredential> getAllCredentials() {
        return new HashMap<>(credentials);
    }

    /**
     * Login result class
     */
    public static class LoginResult {
        public final boolean success;
        public final EmployeeCredential credential;
        public final String message;
        public final boolean requiresPasswordChange;

        public LoginResult(boolean success, EmployeeCredential credential, String message, boolean requiresPasswordChange) {
            this.success = success;
            this.credential = credential;
            this.message = message;
            this.requiresPasswordChange = requiresPasswordChange;
        }
    }

    /**
     * Show credentials generation dialog for admin
     */
    public void showCredentialsGenerationDialog(JFrame parentFrame, String csvFilePath) {
        JDialog dialog = new JDialog(parentFrame, "Generate Employee Credentials", true);
        dialog.setSize(700, 500);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(52, 73, 94));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel headerLabel = new JLabel("🔐 Employee Credentials Generator");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);

        // Content
        JTextArea outputArea = new JTextArea();
        outputArea.setFont(new Font("Courier New", Font.PLAIN, 12));
        outputArea.setEditable(false);
        outputArea.setBackground(new Color(248, 249, 250));

        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton generateButton = createStyledButton("Generate Credentials", new Color(46, 204, 113));
        JButton showExistingButton = createStyledButton("Show Existing", new Color(52, 152, 219));
        JButton closeButton = createStyledButton("Close", new Color(149, 165, 166));

        generateButton.addActionListener(e -> {
            outputArea.setText("Generating credentials...\n\n");

            SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
                @Override
                protected Void doInBackground() throws Exception {
                    // Capture System.out to show in dialog
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    PrintStream originalOut = System.out;
                    System.setOut(new PrintStream(baos));

                    try {
                        generateTemporaryCredentials(csvFilePath);
                    } finally {
                        System.setOut(originalOut);
                    }

                    publish(baos.toString());
                    return null;
                }

                @Override
                protected void process(List<String> chunks) {
                    for (String chunk : chunks) {
                        outputArea.append(chunk);
                    }
                }

                @Override
                protected void done() {
                    outputArea.append("\n✅ Credentials generation complete!\n");
                    outputArea.append("\n💡 Give these credentials to employees for their first login.\n");
                    outputArea.append("⚠️  They will be required to change their password on first login.\n");
                }
            };
            worker.execute();
        });

        showExistingButton.addActionListener(e -> {
            outputArea.setText("📋 EXISTING EMPLOYEE CREDENTIALS:\n");
            outputArea.append("══════════════════════════════════════════════════════════════\n\n");

            Map<String, EmployeeCredential> allCreds = getAllCredentials();
            if (allCreds.isEmpty()) {
                outputArea.append("No credentials found. Generate them first.\n");
            } else {
                for (EmployeeCredential cred : allCreds.values()) {
                    outputArea.append(String.format("👤 %s (%s)\n", cred.fullName, cred.employeeId));
                    outputArea.append(String.format("   Username: %s\n", cred.username));
                    outputArea.append(String.format("   Status: %s\n",
                            cred.isTemporary ? "⚠️  Temporary (needs password change)" : "✅ Password set"));
                    outputArea.append("\n");
                }
            }
        });

        closeButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(generateButton);
        buttonPanel.add(showExistingButton);
        buttonPanel.add(closeButton);

        dialog.add(headerPanel, BorderLayout.NORTH);
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    /**
     * Create styled button (cross-platform compatible)
     */
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Cross-platform compatibility
        button.setOpaque(true);
        button.setBorderPainted(true);
        button.setContentAreaFilled(true);

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            Color originalColor = bgColor;

            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(originalColor.darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(originalColor);
            }
        });

        return button;
    }
}