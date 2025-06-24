package motorph.gui;

import motorph.gui.EmployeeCredentialsManager.EmployeeCredential;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Password change dialog for first-time login
 */
public class PasswordChangeDialog extends JDialog {
    private EmployeeCredential credential;
    private EmployeeCredentialsManager credentialsManager;

    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JLabel messageLabel;
    private JButton changeButton;
    private JButton cancelButton;

    public PasswordChangeDialog(EmployeeCredential credential, EmployeeCredentialsManager credentialsManager) {
        this.credential = credential;
        this.credentialsManager = credentialsManager;

        setTitle("Password Change Required - " + credential.fullName);
        setSize(500, 450);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setModal(true);
        setLayout(new BorderLayout());

        createComponents();
        setupButtons();
        setVisible(true);
    }

    void createComponents() {
        // Header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(52, 73, 94));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel headerLabel = new JLabel("🔒 Password Change Required");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);

        // Main content panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.anchor = GridBagConstraints.WEST;

        // Welcome message
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel welcomeLabel = new JLabel("<html><b>Welcome, " + credential.fullName + "!</b><br/>" +
                "For security, you must change your temporary password before continuing.</html>");
        welcomeLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        welcomeLabel.setForeground(new Color(52, 73, 94));
        mainPanel.add(welcomeLabel, gbc);

        // Current password
        gbc.gridwidth = 1; gbc.gridy++;
        JLabel currentLabel = new JLabel("Current Password:");
        currentLabel.setFont(new Font("Arial", Font.BOLD, 12));
        mainPanel.add(currentLabel, gbc);

        gbc.gridx = 1;
        currentPasswordField = new JPasswordField(20);
        currentPasswordField.setFont(new Font("Arial", Font.PLAIN, 12));
        stylePasswordField(currentPasswordField);
        mainPanel.add(currentPasswordField, gbc);

        // New password
        gbc.gridx = 0; gbc.gridy++;
        JLabel newLabel = new JLabel("New Password:");
        newLabel.setFont(new Font("Arial", Font.BOLD, 12));
        mainPanel.add(newLabel, gbc);

        gbc.gridx = 1;
        newPasswordField = new JPasswordField(20);
        newPasswordField.setFont(new Font("Arial", Font.PLAIN, 12));
        stylePasswordField(newPasswordField);
        mainPanel.add(newPasswordField, gbc);

        // Confirm password
        gbc.gridx = 0; gbc.gridy++;
        JLabel confirmLabel = new JLabel("Confirm Password:");
        confirmLabel.setFont(new Font("Arial", Font.BOLD, 12));
        mainPanel.add(confirmLabel, gbc);

        gbc.gridx = 1;
        confirmPasswordField = new JPasswordField(20);
        confirmPasswordField.setFont(new Font("Arial", Font.PLAIN, 12));
        stylePasswordField(confirmPasswordField);
        mainPanel.add(confirmPasswordField, gbc);

        // Password requirements
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        JLabel requirementsLabel = new JLabel("<html>" +
                credentialsManager.getPasswordRequirements().replace("\n", "<br/>") + "</html>");
        requirementsLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        requirementsLabel.setForeground(new Color(127, 140, 141));
        mainPanel.add(requirementsLabel, gbc);

        // Message label
        gbc.gridy++;
        messageLabel = new JLabel("");
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(messageLabel, gbc);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);

        changeButton = createStyledButton("✅ Change Password", new Color(46, 204, 113));
        cancelButton = createStyledButton("❌ Cancel", new Color(231, 76, 60));

        buttonPanel.add(changeButton);
        buttonPanel.add(cancelButton);

        add(headerPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    void stylePasswordField(JPasswordField field) {
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        field.setPreferredSize(new Dimension(250, 35));
    }

    void setupButtons() {
        changeButton.addActionListener(e -> handlePasswordChange());
        cancelButton.addActionListener(e -> handleCancel());

        // Allow Enter to submit
        confirmPasswordField.addActionListener(e -> handlePasswordChange());
    }

    void handlePasswordChange() {
        String currentPassword = new String(currentPasswordField.getPassword());
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        // Validate inputs
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showMessage("Please fill in all fields!", Color.RED);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showMessage("New passwords do not match!", Color.RED);
            return;
        }

        // Attempt password change
        boolean success = credentialsManager.changePassword(credential.username, currentPassword, newPassword);

        if (success) {
            showMessage("Password changed successfully!", new Color(46, 204, 113));

            // Clear password fields for security
            currentPasswordField.setText("");
            newPasswordField.setText("");
            confirmPasswordField.setText("");

            Timer timer = new Timer(1500, e -> {
                dispose();
                new EmployeeDashboard(credential);
            });
            timer.setRepeats(false);
            timer.start();
        } else {
            showMessage("Password change failed! Check current password and requirements.", Color.RED);
        }
    }

    void handleCancel() {
        int choice = JOptionPane.showConfirmDialog(this,
                "You must change your password to continue.\nDo you want to logout?",
                "Password Change Required",
                JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            new EmployeeLoginForm();
        }
    }

    void showMessage(String message, Color color) {
        messageLabel.setText(message);
        messageLabel.setForeground(color);
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.setOpaque(true);
        button.setBorderPainted(true);
        button.setContentAreaFilled(true);

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