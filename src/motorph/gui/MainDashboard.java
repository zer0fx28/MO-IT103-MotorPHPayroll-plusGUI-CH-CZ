package motorph.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainDashboard extends JFrame {
    // Menu components
    JPanel mainPanel;
    JLabel welcomeLabel;
    JLabel subtitleLabel;
    JButton employeeButton;
    JButton payrollButton;
    JButton reportsButton;
    JButton logoutButton;

    // Constructor
    public MainDashboard() {
        // Window setup - professional styling
        setTitle("MotorPH Payroll System - Dashboard");
        setSize(600, 500);
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
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Welcome header with professional styling
        welcomeLabel = new JLabel("Welcome to MotorPH");
        welcomeLabel.setBounds(150, 40, 300, 35);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setForeground(new Color(70, 130, 180)); // Professional blue
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(welcomeLabel);

        // Subtitle
        subtitleLabel = new JLabel("Payroll Management System");
        subtitleLabel.setBounds(150, 75, 300, 20);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(102, 102, 102));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(subtitleLabel);

        // Menu buttons with professional styling and icons
        employeeButton = createMenuButton("👥 Employee Management", new Color(52, 152, 219));
        employeeButton.setBounds(175, 130, 250, 50);
        mainPanel.add(employeeButton);

        payrollButton = createMenuButton("💰 Payroll Processing", new Color(46, 204, 113));
        payrollButton.setBounds(175, 190, 250, 50);
        mainPanel.add(payrollButton);

        reportsButton = createMenuButton("📊 Reports & Analytics", new Color(155, 89, 182));
        reportsButton.setBounds(175, 250, 250, 50);
        mainPanel.add(reportsButton);

        logoutButton = createMenuButton("🔒 Logout", new Color(231, 76, 60));
        logoutButton.setBounds(175, 330, 250, 45);
        mainPanel.add(logoutButton);

        // Footer text
        JLabel footerLabel = new JLabel("Select an option to continue");
        footerLabel.setBounds(150, 390, 300, 20);
        footerLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        footerLabel.setForeground(new Color(149, 165, 166));
        footerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(footerLabel);

        // Add main panel to frame
        add(mainPanel, BorderLayout.CENTER);
    }

    // Helper method to create professional menu buttons
    JButton createMenuButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setHorizontalAlignment(SwingConstants.LEFT);

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
        // Employee Management button
        employeeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openEmployeeManagement();
            }
        });

        // Payroll Processing button
        payrollButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openPayrollProcessing();
            }
        });

        // Reports button
        reportsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openReports();
            }
        });

        // Logout button
        logoutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                logout();
            }
        });
    }

    void openEmployeeManagement() {
        dispose(); // Close dashboard
        new EmployeeManagement(); // Open employee management
    }

    void openPayrollProcessing() {
        JOptionPane.showMessageDialog(this,
                "Payroll Processing will open here!\n" +
                        "(This will use your existing payroll calculation logic)",
                "Payroll Processing",
                JOptionPane.INFORMATION_MESSAGE);

        // TODO: Later you'll open your payroll processing window
        // new PayrollProcessingWindow();
    }

    void openReports() {
        JOptionPane.showMessageDialog(this,
                "Reports will open here!\n" +
                        "(This will use your existing reports logic)",
                "Reports",
                JOptionPane.INFORMATION_MESSAGE);

        // TODO: Later you'll open your reports window
        // new ReportsWindow();
    }

    void logout() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            dispose(); // Close dashboard
            new LoginForm(); // Open login again
        }
    }

    // Test the dashboard
    public static void main(String[] args) {
        new MainDashboard();
    }
}