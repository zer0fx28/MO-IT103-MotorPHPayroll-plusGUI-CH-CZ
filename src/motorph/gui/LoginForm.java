package motorph.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginForm extends JFrame {
    // Simple components - these are the building blocks of our login form
    JPanel mainPanel;           // Main container panel
    JLabel titleLabel;          // Big title text
    JLabel usernameLabel;       // "Username:" label
    JLabel passwordLabel;       // "Password:" label
    JTextField usernameField;   // Where user types username
    JPasswordField passwordField; // Where user types password (hidden)
    JButton loginButton;        // Login button
    JButton cancelButton;       // Cancel button
    JLabel messageLabel;        // Shows success/error messages

    // Pre-hashed passwords for demo - these are for testing the login
    // In a real app, these would come from a database
    private final String ADMIN_USERNAME = "admin";
    private final String ADMIN_PASSWORD_HASH = "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8";

    private final String USER_USERNAME = "user";
    private final String USER_PASSWORD_HASH = "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92";

    // Constructor - this runs when we create a new LoginForm
    public LoginForm() {
        // Window setup - make it look professional
        setTitle("MotorPH Payroll System - Login");
        setSize(450, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Close program when X clicked
        setLocationRelativeTo(null); // Center window on screen
        setResizable(false); // Don't allow resizing
        getContentPane().setBackground(new Color(245, 245, 245)); // Light gray background
        setLayout(new BorderLayout()); // Use BorderLayout for organization

        // Create all the visual components
        createComponents();

        // Set up what happens when buttons are clicked
        setupButtons();

        // Make the window visible
        setVisible(true);
    }

    void createComponents() {
        // Main panel - white container inside the gray window
        mainPanel = new JPanel();
        mainPanel.setLayout(null); // Manual positioning (beginner-friendly)
        mainPanel.setBackground(Color.WHITE); // White background
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Add padding

        // Main title - big blue text saying "MotorPH Payroll System"
        titleLabel = new JLabel("MotorPH Payroll System");
        titleLabel.setBounds(90, 30, 280, 35); // x, y, width, height
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20)); // Big bold font
        titleLabel.setForeground(new Color(70, 130, 180)); // Professional blue color
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER); // Center the text
        mainPanel.add(titleLabel); // Add to panel

        // Subtitle - smaller text under main title
        JLabel subtitleLabel = new JLabel("Employee Management System");
        subtitleLabel.setBounds(90, 65, 280, 20);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 12)); // Smaller font
        subtitleLabel.setForeground(new Color(102, 102, 102)); // Gray color
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(subtitleLabel);

        // Demo credentials info - shows test accounts
        JLabel demoLabel = new JLabel("Demo: admin/password or user/123456");
        demoLabel.setBounds(90, 90, 280, 20);
        demoLabel.setFont(new Font("Arial", Font.ITALIC, 10)); // Small italic font
        demoLabel.setForeground(new Color(128, 128, 128)); // Light gray
        demoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(demoLabel);

        // Username section
        usernameLabel = new JLabel("Username:");
        usernameLabel.setBounds(70, 140, 80, 25);
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 12)); // Bold label
        usernameLabel.setForeground(new Color(51, 51, 51)); // Dark gray
        mainPanel.add(usernameLabel);

        // Username input field
        usernameField = new JTextField();
        usernameField.setBounds(150, 140, 180, 30);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 12));
        // Add border and padding to make it look professional
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        mainPanel.add(usernameField);

        // Password section
        passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(70, 180, 80, 25);
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 12));
        passwordLabel.setForeground(new Color(51, 51, 51));
        mainPanel.add(passwordLabel);

        // Password input field (hides what user types)
        passwordField = new JPasswordField();
        passwordField.setBounds(150, 180, 180, 30);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 12));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        mainPanel.add(passwordField);

        // Login button - green color for "go"
        loginButton = createStyledButton("Login", new Color(46, 204, 113));
        loginButton.setBounds(110, 240, 100, 35);
        mainPanel.add(loginButton);

        // Cancel button - red color for "stop"
        cancelButton = createStyledButton("Cancel", new Color(231, 76, 60));
        cancelButton.setBounds(220, 240, 100, 35);
        mainPanel.add(cancelButton);

        // Message area - shows success or error messages
        messageLabel = new JLabel("");
        messageLabel.setBounds(70, 290, 280, 25);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(messageLabel);

        // Security info - shows what type of password protection we use
        JLabel securityLabel = new JLabel("Passwords are securely hashed with SHA-256");
        securityLabel.setBounds(70, 320, 280, 20);
        securityLabel.setFont(new Font("Arial", Font.ITALIC, 9));
        securityLabel.setForeground(new Color(100, 150, 100)); // Green for security
        securityLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(securityLabel);

        // Add the main panel to the window
        add(mainPanel, BorderLayout.CENTER);
    }

    // Helper method to create nice-looking buttons
    // This saves us from writing the same styling code over and over
    JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor); // Set background color
        button.setForeground(Color.WHITE); // White text
        button.setFont(new Font("Arial", Font.BOLD, 12)); // Bold font
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15)); // Padding
        button.setFocusPainted(false); // Remove ugly focus border
        button.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Hand cursor on hover

        // Add hover effect - button gets darker when mouse is over it
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            Color originalColor = bgColor; // Remember original color

            // When mouse enters button area
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(originalColor.darker());
            }

            // When mouse leaves button area
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(originalColor);
            }
        });

        return button; // Return the finished button
    }

    void setupButtons() {
        // Login button action - what happens when user clicks "Login"
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                checkLogin(); // Call our login checking method
            }
        });

        // Cancel button action - what happens when user clicks "Cancel"
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0); // Close the entire program
            }
        });

        // Enter key on password field - allow login by pressing Enter
        passwordField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                checkLogin(); // Same as clicking login button
            }
        });
    }

    void checkLogin() {
        // Get what the user typed (remove extra spaces)
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        // Check if user left fields empty
        if (username.equals("") || password.equals("")) {
            showMessage("Please fill in both fields!", Color.RED);
            return; // Stop here, don't continue
        }

        // Debug output - helps us see what's happening
        System.out.println("Attempting login with username: '" + username + "'");

        try {
            boolean loginSuccess = false; // Start assuming login will fail

            // Simple credential check first - easy to understand
            if (username.equals("admin") && password.equals("password")) {
                loginSuccess = true;
            } else if (username.equals("user") && password.equals("123456")) {
                loginSuccess = true;
            }

            // Also try with hashed passwords (more secure method)
            if (!loginSuccess) {
                String hashedPassword = "";

                // Find which user is trying to login
                if (username.equals(ADMIN_USERNAME)) {
                    hashedPassword = ADMIN_PASSWORD_HASH;
                } else if (username.equals(USER_USERNAME)) {
                    hashedPassword = USER_PASSWORD_HASH;
                }

                // If we found a user, check their password
                if (!hashedPassword.equals("")) {
                    String enteredPasswordHash = hashPassword(password);
                    loginSuccess = hashedPassword.equals(enteredPasswordHash);
                }
            }

            // If login worked, show success and open dashboard
            if (loginSuccess) {
                showMessage("Login successful! Welcome " + username + "!", new Color(46, 204, 113));

                // Wait a bit to show success message, then open dashboard
                Timer timer = new Timer(1500, new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        dispose(); // Close login window
                        new MainDashboard(); // Open your dashboard
                    }
                });
                timer.setRepeats(false); // Only run once
                timer.start();

            } else {
                // Login failed - show error message
                showMessage("Invalid username or password!", Color.RED);
                passwordField.setText(""); // Clear password field for security
                usernameField.requestFocus(); // Put cursor back in username field
            }

        } catch (Exception ex) {
            // Something went wrong during login process
            showMessage("Login error: " + ex.getMessage(), Color.RED);
            ex.printStackTrace(); // Print error details for debugging
        }
    }

    // Helper method to show messages in different colors
    void showMessage(String message, Color color) {
        messageLabel.setText(message);
        messageLabel.setForeground(color);
    }

    // Method to create SHA-256 hash of password (for security)
    public static String hashPassword(String plainPassword) {
        try {
            // Create SHA-256 hash algorithm
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(plainPassword.getBytes());

            // Convert bytes to hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0'); // Add leading zero if needed
                }
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    // Test method - this runs when you test the login form by itself
    public static void main(String[] args) {
        // Show what the hashed passwords look like (for learning)
        System.out.println("SHA-256 hash for 'password': " + hashPassword("password"));
        System.out.println("SHA-256 hash for '123456': " + hashPassword("123456"));

        // Start the login form (proper way to start Swing applications)
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new LoginForm();
            }
        });
    }
}