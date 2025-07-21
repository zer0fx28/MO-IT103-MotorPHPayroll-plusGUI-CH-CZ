package motorph.gui;

import motorph.gui.EmployeeCredentialsManager.EmployeeCredential;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Improved Password Change Dialog with better UI
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

        setupDialog();
        createComponents();
        setupEventHandlers();
        setVisible(true);
    }

    private void setupDialog() {
        setTitle("Password Change Required - " + credential.fullName);
        setSize(500, 600);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setModal(true);
        setResizable(false);
        setLayout(new BorderLayout());

        // Set background
        getContentPane().setBackground(new Color(245, 245, 245));
    }

    private void createComponents() {
        // Header Panel
        JPanel headerPanel = createHeaderPanel();

        // Main Content Panel
        JPanel mainPanel = createMainPanel();

        // Button Panel
        JPanel buttonPanel = createButtonPanel();

        // Add panels to dialog
        add(headerPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(52, 73, 94));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("🔒 Password Change Required");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Welcome, " + credential.fullName);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(236, 240, 241));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(10));
        headerPanel.add(subtitleLabel);

        return headerPanel;
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // Security message
        JPanel messagePanel = createMessagePanel();

        // Form fields
        JPanel formPanel = createFormPanel();

        // Password requirements
        JPanel requirementsPanel = createRequirementsPanel();

        mainPanel.add(messagePanel);
        mainPanel.add(Box.createVerticalStrut(25));
        mainPanel.add(formPanel);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(requirementsPanel);

        return mainPanel;
    }

    private JPanel createMessagePanel() {
        JPanel messagePanel = new JPanel();
        messagePanel.setBackground(Color.WHITE);
        messagePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(241, 196, 15), 2),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JLabel messageLabel = new JLabel("<html><div style='text-align: center;'>" +
                "<b>For security purposes, you must change your temporary password</b><br/>" +
                "This is your first login to the MotorPH Employee Portal.<br/>" +
                "Please create a strong, secure password for your account." +
                "</div></html>");
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        messageLabel.setForeground(new Color(52, 73, 94));
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        messagePanel.add(messageLabel);
        return messagePanel;
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel();
        formPanel.setBackground(Color.WHITE);
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));

        // Current Password
        JLabel currentLabel = new JLabel("Current Password (Temporary):");
        currentLabel.setFont(new Font("Arial", Font.BOLD, 13));
        currentLabel.setForeground(new Color(52, 73, 94));
        currentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        currentPasswordField = new JPasswordField();
        currentPasswordField.setFont(new Font("Arial", Font.PLAIN, 13));
        currentPasswordField.setPreferredSize(new Dimension(400, 35));
        currentPasswordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        currentPasswordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        // New Password
        JLabel newLabel = new JLabel("New Password:");
        newLabel.setFont(new Font("Arial", Font.BOLD, 13));
        newLabel.setForeground(new Color(52, 73, 94));
        newLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        newPasswordField = new JPasswordField();
        newPasswordField.setFont(new Font("Arial", Font.PLAIN, 13));
        newPasswordField.setPreferredSize(new Dimension(400, 35));
        newPasswordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        newPasswordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        // Confirm Password
        JLabel confirmLabel = new JLabel("Confirm New Password:");
        confirmLabel.setFont(new Font("Arial", Font.BOLD, 13));
        confirmLabel.setForeground(new Color(52, 73, 94));
        confirmLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setFont(new Font("Arial", Font.PLAIN, 13));
        confirmPasswordField.setPreferredSize(new Dimension(400, 35));
        confirmPasswordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        confirmPasswordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        // Message label for validation feedback
        messageLabel = new JLabel(" ");
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        messageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Add components with spacing
        formPanel.add(currentLabel);
        formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(currentPasswordField);
        formPanel.add(Box.createVerticalStrut(15));

        formPanel.add(newLabel);
        formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(newPasswordField);
        formPanel.add(Box.createVerticalStrut(15));

        formPanel.add(confirmLabel);
        formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(confirmPasswordField);
        formPanel.add(Box.createVerticalStrut(15));

        formPanel.add(messageLabel);

        return formPanel;
    }

    private JPanel createRequirementsPanel() {
        JPanel requirementsPanel = new JPanel();
        requirementsPanel.setBackground(new Color(248, 249, 250));
        requirementsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(52, 152, 219), 1),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JLabel requirementsTitle = new JLabel("📋 Password Requirements:");
        requirementsTitle.setFont(new Font("Arial", Font.BOLD, 13));
        requirementsTitle.setForeground(new Color(52, 152, 219));

        JLabel requirementsText = new JLabel("<html>" +
                "• At least 8 characters long<br/>" +
                "• At least one uppercase letter (A-Z)<br/>" +
                "• At least one lowercase letter (a-z)<br/>" +
                "• At least one number (0-9)<br/><br/>" +
                "<b>Examples:</b> MyPassword123, SecureLogin2024, NewPass1" +
                "</html>");
        requirementsText.setFont(new Font("Arial", Font.PLAIN, 12));
        requirementsText.setForeground(new Color(85, 85, 85));

        requirementsPanel.setLayout(new BoxLayout(requirementsPanel, BoxLayout.Y_AXIS));
        requirementsPanel.add(requirementsTitle);
        requirementsPanel.add(Box.createVerticalStrut(8));
        requirementsPanel.add(requirementsText);

        return requirementsPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(245, 245, 245));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 25, 30));
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 0));

        changeButton = createStyledButton("🔒 Change Password", new Color(46, 204, 113));
        cancelButton = createStyledButton("❌ Cancel", new Color(231, 76, 60));

        buttonPanel.add(changeButton);
        buttonPanel.add(cancelButton);

        return buttonPanel;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 13));
        button.setPreferredSize(new Dimension(160, 40));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Cross-platform styling
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

    private void setupEventHandlers() {
        changeButton.addActionListener(e -> handlePasswordChange());
        cancelButton.addActionListener(e -> handleCancel());

        // Allow Enter key to trigger password change
        newPasswordField.addActionListener(e -> handlePasswordChange());
        confirmPasswordField.addActionListener(e -> handlePasswordChange());
    }

    private void handlePasswordChange() {
        String currentPassword = new String(currentPasswordField.getPassword());
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        // Validate inputs
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showMessage("Please fill in all fields.", Color.RED);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showMessage("New passwords do not match!", Color.RED);
            confirmPasswordField.setText("");
            confirmPasswordField.requestFocus();
            return;
        }

        // Attempt password change
        boolean success = credentialsManager.changePassword(credential.username, currentPassword, newPassword);

        if (success) {
            showMessage("Password changed successfully!", new Color(46, 204, 113));

            // Close this dialog and open employee dashboard
            Timer timer = new Timer(1500, e -> {
                dispose();
                new EmployeeDashboard(credential);
            });
            timer.setRepeats(false);
            timer.start();

        } else {
            showMessage("Password change failed. Check current password and requirements.", Color.RED);
            currentPasswordField.setText("");
            newPasswordField.setText("");
            confirmPasswordField.setText("");
            currentPasswordField.requestFocus();
        }
    }

    private void handleCancel() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cancel?\nYou will be returned to the login screen.",
                "Confirm Cancel",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            new EmployeeLoginForm();
        }
    }

    private void showMessage(String message, Color color) {
        messageLabel.setText(message);
        messageLabel.setForeground(color);
    }
}