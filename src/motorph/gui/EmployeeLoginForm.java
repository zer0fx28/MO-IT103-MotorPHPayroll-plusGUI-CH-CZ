package motorph.gui;

import motorph.gui.EmployeeCredentialsManager.LoginResult;
import motorph.gui.EmployeeCredentialsManager.EmployeeCredential;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Employee Login Form for individual employee access
 * Handles initial login and password change requirements
 */
public class EmployeeLoginForm extends JFrame {
    private EmployeeCredentialsManager credentialsManager;

    // UI Components
    JPanel mainPanel;
    JLabel titleLabel;
    JLabel usernameLabel;
    JLabel passwordLabel;
    JTextField usernameField;
    JPasswordField passwordField;
    JButton loginButton;
    JButton adminButton;
    JButton exitButton;
    JLabel messageLabel;

    public EmployeeLoginForm() {
        credentialsManager = new EmployeeCredentialsManager();

        // Window setup
        setTitle("MotorPH Employee Portal - Login");
        setSize(500, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(new Color(245, 245, 245));
        setLayout(new BorderLayout());

        createComponents();
        setupButtons();
        setVisible(true);
    }

    void createComponents() {
        // Main panel
        mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Header
        titleLabel = new JLabel("MotorPH Employee Portal");
        titleLabel.setBounds(90, 30, 320, 35);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(new Color(52, 73, 94));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(titleLabel);

        JLabel subtitleLabel = new JLabel("Individual Employee Access");
        subtitleLabel.setBounds(90, 65, 320, 20);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(127, 140, 141));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(subtitleLabel);

        // Instructions
        JLabel instructionLabel = new JLabel("<html><center>Use your assigned username and temporary password<br/>You'll be prompted to change your password on first login</center></html>");
        instructionLabel.setBounds(50, 100, 400, 40);
        instructionLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        instructionLabel.setForeground(new Color(93, 109, 126));
        instructionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(instructionLabel);

        // Username field
        usernameLabel = new JLabel("Username:");
        usernameLabel.setBounds(80, 160, 100, 25);
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 13));
        usernameLabel.setForeground(new Color(52, 73, 94));
        mainPanel.add(usernameLabel);

        usernameField = new JTextField();
        usernameField.setBounds(80, 185, 340, 35);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 13));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        mainPanel.add(usernameField);

        // Password field
        passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(80, 235, 100, 25);
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 13));
        passwordLabel.setForeground(new Color(52, 73, 94));
        mainPanel.add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(80, 260, 340, 35);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 13));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        mainPanel.add(passwordField);

        // Buttons
        loginButton = createStyledButton("🔐 Employee Login", new Color(46, 204, 113));
        loginButton.setBounds(80, 320, 150, 40);
        mainPanel.add(loginButton);

        adminButton = createStyledButton("👨‍💼 Admin Login", new Color(52, 152, 219));
        adminButton.setBounds(270, 320, 150, 40);
        mainPanel.add(adminButton);

        exitButton = createStyledButton("❌ Exit", new Color(231, 76, 60));
        exitButton.setBounds(175, 370, 150, 35);
        mainPanel.add(exitButton);

        // Message area
        messageLabel = new JLabel("");
        messageLabel.setBounds(80, 285, 340, 25);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(messageLabel);

        add(mainPanel, BorderLayout.CENTER);
    }

    void setupButtons() {
        loginButton.addActionListener(e -> handleEmployeeLogin());
        adminButton.addActionListener(e -> openAdminLogin());
        exitButton.addActionListener(e -> System.exit(0));

        // Allow Enter key to login
        passwordField.addActionListener(e -> handleEmployeeLogin());
    }

    void handleEmployeeLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Please enter both username and password!", Color.RED);
            return;
        }

        LoginResult result = credentialsManager.validateLogin(username, password);

        if (result.success) {
            if (result.requiresPasswordChange) {
                showMessage("Login successful! Password change required.", new Color(255, 165, 0));
                // Show password change dialog
                Timer timer = new Timer(1000, e -> showPasswordChangeDialog(result.credential));
                timer.setRepeats(false);
                timer.start();
            } else {
                showMessage("Login successful! Welcome back " + result.credential.fullName, new Color(46, 204, 113));
                Timer timer = new Timer(1500, e -> openEmployeeDashboard(result.credential));
                timer.setRepeats(false);
                timer.start();
            }
        } else {
            showMessage(result.message, Color.RED);
            passwordField.setText("");
        }
    }

    void showPasswordChangeDialog(EmployeeCredential credential) {
        dispose(); // Close login form
        new PasswordChangeDialog(credential, credentialsManager);
    }

    void openEmployeeDashboard(EmployeeCredential credential) {
        dispose(); // Close login form
        new EmployeeDashboard(credential);
    }

    void openAdminLogin() {
        dispose(); // Close employee login
        new LoginForm(); // Open admin login
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
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Cross-platform compatibility
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

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch(Exception e) {
            System.err.println("Could not set Look and Feel: " + e.getMessage());
        }

        SwingUtilities.invokeLater(() -> new EmployeeLoginForm());
    }
}