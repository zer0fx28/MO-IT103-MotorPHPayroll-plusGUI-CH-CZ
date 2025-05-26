package motorph.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginForm extends JFrame {
    // Simple components
    JPanel mainPanel;
    JLabel titleLabel;
    JLabel usernameLabel;
    JLabel passwordLabel;
    JTextField usernameField;
    JPasswordField passwordField;
    JButton loginButton;
    JButton cancelButton;
    JLabel messageLabel;

    public LoginForm() {
        // Window setup - professional styling
        setTitle("MotorPH Payroll System - Login");
        setSize(450, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center window
        setResizable(false);
        getContentPane().setBackground(new Color(245, 245, 245)); // Light gray background
        setLayout(new BorderLayout());

        // Create components
        createComponents();

        // Add button actions
        setupButtons();

        // Show window
        setVisible(true);
    }

    void createComponents() {
        // Main panel with white background
        mainPanel = new JPanel();
        mainPanel.setLayout(null); // Keep simple positioning for beginners
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title with professional styling
        titleLabel = new JLabel("MotorPH Payroll System");
        titleLabel.setBounds(90, 30, 280, 35);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(70, 130, 180)); // Professional blue
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(titleLabel);

        // Subtitle
        JLabel subtitleLabel = new JLabel("Employee Management System");
        subtitleLabel.setBounds(90, 65, 280, 20);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(102, 102, 102));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(subtitleLabel);

        // Username section
        usernameLabel = new JLabel("Username:");
        usernameLabel.setBounds(70, 120, 80, 25);
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 12));
        usernameLabel.setForeground(new Color(51, 51, 51));
        mainPanel.add(usernameLabel);

        usernameField = new JTextField();
        usernameField.setBounds(150, 120, 180, 30);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 12));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        mainPanel.add(usernameField);

        // Password section
        passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(70, 160, 80, 25);
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 12));
        passwordLabel.setForeground(new Color(51, 51, 51));
        mainPanel.add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(150, 160, 180, 30);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 12));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        mainPanel.add(passwordField);

        // Buttons with professional styling
        loginButton = createStyledButton("Login", new Color(46, 204, 113)); // Green
        loginButton.setBounds(110, 220, 100, 35);
        mainPanel.add(loginButton);

        cancelButton = createStyledButton("Cancel", new Color(231, 76, 60)); // Red
        cancelButton.setBounds(220, 220, 100, 35);
        mainPanel.add(cancelButton);

        // Message area
        messageLabel = new JLabel("");
        messageLabel.setBounds(70, 270, 280, 25);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(messageLabel);

        // Add main panel to frame
        add(mainPanel, BorderLayout.CENTER);
    }

    // Helper method to create professional buttons
    JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add simple hover effect
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

    void setupButtons() {
        // Login button action
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                checkLogin();
            }
        });

        // Cancel button action
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0); // Close program
            }
        });

        // Enter key on password field
        passwordField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                checkLogin();
            }
        });
    }

    void checkLogin() {
        // Get what user typed
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        // Check if empty
        if (username.equals("") || password.equals("")) {
            messageLabel.setText("Please fill in both fields!");
            messageLabel.setForeground(new Color(231, 76, 60)); // Red for error
            return;
        }

        // Simple check (you can change this later)
        if (username.equals("admin") && password.equals("password")) {
            messageLabel.setText("Login successful!");
            messageLabel.setForeground(new Color(46, 204, 113)); // Green for success

            // Small delay to show success message
            Timer timer = new Timer(1000, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    dispose(); // Close login window
                    new MainDashboard(); // Open dashboard
                }
            });
            timer.setRepeats(false);
            timer.start();

        } else {
            messageLabel.setText("Wrong username or password!");
            messageLabel.setForeground(new Color(231, 76, 60)); // Red for error
            passwordField.setText(""); // Clear password
            usernameField.requestFocus();
        }
    }

    // Test the login form
    public static void main(String[] args) {
        new LoginForm();
    }
}