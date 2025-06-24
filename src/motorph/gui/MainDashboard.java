package motorph.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainDashboard extends JFrame {
    // Menu components - these are the building blocks of our dashboard
    JPanel mainPanel;           // Main container panel
    JLabel welcomeLabel;        // Big welcome text
    JLabel subtitleLabel;       // Smaller text under welcome
    JButton employeeButton;     // Button to open employee management
    JButton payrollButton;      // Button for payroll processing
    JButton reportsButton;      // Button for reports
    JButton logoutButton;       // Button to logout

    // Constructor - this runs when we create a new MainDashboard
    public MainDashboard() {
        // Window setup - professional styling (keeping your exact design)
        setTitle("MotorPH Payroll System - Dashboard");
        setSize(600, 500); // Your original size
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
        // Main panel with white background (keeping your exact design)
        mainPanel = new JPanel();
        mainPanel.setLayout(null); // Manual positioning (beginner-friendly)
        mainPanel.setBackground(Color.WHITE); // White background
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30)); // Padding

        // Welcome header with professional styling (your exact design)
        welcomeLabel = new JLabel("Welcome to MotorPH");
        welcomeLabel.setBounds(150, 40, 300, 35); // Your exact positioning
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24)); // Your exact font
        welcomeLabel.setForeground(new Color(70, 130, 180)); // Professional blue
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(welcomeLabel);

        // Subtitle (your exact design)
        subtitleLabel = new JLabel("Payroll Management System");
        subtitleLabel.setBounds(150, 75, 300, 20); // Your exact positioning
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14)); // Your exact font
        subtitleLabel.setForeground(new Color(102, 102, 102)); // Gray color
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(subtitleLabel);

        // Menu buttons with professional styling and icons (your exact design)
        employeeButton = createMenuButton("👥 Employee Management", new Color(52, 152, 219));
        employeeButton.setBounds(175, 130, 250, 50); // Your exact positioning
        mainPanel.add(employeeButton);

        payrollButton = createMenuButton("💰 Payroll Processing", new Color(46, 204, 113));
        payrollButton.setBounds(175, 190, 250, 50); // Your exact positioning
        mainPanel.add(payrollButton);

        reportsButton = createMenuButton("📊 Reports & Analytics", new Color(155, 89, 182));
        reportsButton.setBounds(175, 250, 250, 50); // Your exact positioning
        mainPanel.add(reportsButton);

        logoutButton = createMenuButton("🔒 Logout", new Color(231, 76, 60));
        logoutButton.setBounds(175, 330, 250, 45); // Your exact positioning
        mainPanel.add(logoutButton);

        // Footer text (your exact design)
        JLabel footerLabel = new JLabel("Select an option to continue");
        footerLabel.setBounds(150, 390, 300, 20); // Your exact positioning
        footerLabel.setFont(new Font("Arial", Font.ITALIC, 11)); // Your exact font
        footerLabel.setForeground(new Color(149, 165, 166)); // Light gray
        footerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(footerLabel);

        // Add main panel to frame
        add(mainPanel, BorderLayout.CENTER);
    }

    // Helper method to create professional menu buttons - CROSS-PLATFORM COMPATIBLE
    JButton createMenuButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor); // Set background color
        button.setForeground(Color.WHITE); // White text
        button.setFont(new Font("Arial", Font.BOLD, 14)); // Your exact font
        button.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20)); // Your exact padding
        button.setFocusPainted(false); // Remove ugly focus border
        button.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Hand cursor on hover
        button.setHorizontalAlignment(SwingConstants.LEFT); // Align text to left

        // THESE 3 LINES MAKE BUTTONS WORK ON MAC:
        button.setOpaque(true);           // Show the background color
        button.setBorderPainted(true);    // Show the button border
        button.setContentAreaFilled(true); // Fill the button with color

        // Add simple hover effect (your exact design)
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
        // Employee Management button action
        employeeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openEmployeeManagement(); // Call our method to open employee window
            }
        });

        // Payroll Processing button action
        payrollButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openPayrollProcessing(); // Call our method for payroll
            }
        });

        // Reports button action
        reportsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openReports(); // Call our method for reports
            }
        });

        // Logout button action
        logoutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                logout(); // Call our logout method
            }
        });
    }

    void openEmployeeManagement() {
        // This method opens the Employee Management window
        try {
            System.out.println("Opening Employee Management..."); // Debug message
            dispose(); // Close the dashboard window
            new EmployeeManagement(); // Open employee management window

        } catch (Exception ex) {
            // If something goes wrong, show error message
            JOptionPane.showMessageDialog(this,
                    "Could not open Employee Management: " + ex.getMessage() +
                            "\n\nPlease check if all required files are available.",
                    "Error Opening Employee Management",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace(); // Print error details for debugging
        }
    }

    void openPayrollProcessing() {
        // This feature will be implemented later (your exact message)
        JOptionPane.showMessageDialog(this,
                "Payroll Processing will open here!\n" +
                        "(This will use your existing payroll calculation logic)",
                "Payroll Processing",
                JOptionPane.INFORMATION_MESSAGE);

        // TODO: Later you'll open your payroll processing window
        // new PayrollProcessingWindow();
    }

    void openReports() {
        // This feature will be implemented later (your exact message)
        JOptionPane.showMessageDialog(this,
                "Reports will open here!\n" +
                        "(This will use your existing reports logic)",
                "Reports",
                JOptionPane.INFORMATION_MESSAGE);

        // TODO: Later you'll open your reports window
        // new ReportsWindow();
    }

    void logout() {
        // Ask user if they really want to logout (your exact design)
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION);

        // If user clicked "Yes"
        if (choice == JOptionPane.YES_OPTION) {
            dispose(); // Close the dashboard
            new LoginForm(); // Open login form again
        }
        // If user clicked "No", do nothing (stay on dashboard)
    }

    // Test the dashboard - FIXED: Now works on all operating systems
    public static void main(String[] args) {
        // THIS ONE LINE MAKES IT WORK ON ALL COMPUTERS:
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch(Exception e) {
            System.err.println("Could not set Look and Feel: " + e.getMessage());
        }

        System.out.println("Testing MainDashboard..."); // Debug message

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new MainDashboard();
            }
        });
    }
}