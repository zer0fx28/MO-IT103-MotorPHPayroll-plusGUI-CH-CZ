package motorph.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class NewEmployeeForm extends JDialog {
    // Parent reference to refresh table
    private EmployeeManagement parentWindow;

    // Form components
    JPanel mainPanel;
    JLabel titleLabel;

    // Employee information fields
    JTextField employeeNumberField;
    JTextField firstNameField;
    JTextField lastNameField;
    JTextField sssNumberField;
    JTextField philHealthNumberField;
    JTextField tinField;
    JTextField pagIbigNumberField;
    JTextField positionField;
    JTextField departmentField;
    JTextField salaryField;

    // Buttons
    JButton saveButton;
    JButton cancelButton;
    JLabel messageLabel;

    public NewEmployeeForm(EmployeeManagement parent) {
        super(parent, "Add New Employee", true); // Modal dialog
        this.parentWindow = parent;

        // Window setup - professional styling
        setSize(500, 600);
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(new Color(245, 245, 245));
        setLayout(new BorderLayout());

        // Create components
        createComponents();

        // Setup button actions
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
        titleLabel = new JLabel("Add New Employee");
        titleLabel.setBounds(150, 20, 200, 35);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(70, 130, 180)); // Professional blue
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(titleLabel);

        // Subtitle
        JLabel subtitleLabel = new JLabel("Please fill in all employee information");
        subtitleLabel.setBounds(100, 55, 300, 20);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(102, 102, 102));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(subtitleLabel);

        // Create form fields
        int yPosition = 90;
        int fieldSpacing = 40;

        // Employee Number
        createFieldRow("Employee Number:", employeeNumberField = new JTextField(), yPosition);
        yPosition += fieldSpacing;

        // First Name
        createFieldRow("First Name:", firstNameField = new JTextField(), yPosition);
        yPosition += fieldSpacing;

        // Last Name
        createFieldRow("Last Name:", lastNameField = new JTextField(), yPosition);
        yPosition += fieldSpacing;

        // Position
        createFieldRow("Position:", positionField = new JTextField(), yPosition);
        yPosition += fieldSpacing;

        // Department
        createFieldRow("Department:", departmentField = new JTextField(), yPosition);
        yPosition += fieldSpacing;

        // Salary
        createFieldRow("Salary:", salaryField = new JTextField(), yPosition);
        yPosition += fieldSpacing;

        // SSS Number
        createFieldRow("SSS Number:", sssNumberField = new JTextField(), yPosition);
        yPosition += fieldSpacing;

        // PhilHealth Number
        createFieldRow("PhilHealth Number:", philHealthNumberField = new JTextField(), yPosition);
        yPosition += fieldSpacing;

        // TIN
        createFieldRow("TIN:", tinField = new JTextField(), yPosition);
        yPosition += fieldSpacing;

        // Pag-IBIG Number
        createFieldRow("Pag-IBIG Number:", pagIbigNumberField = new JTextField(), yPosition);
        yPosition += fieldSpacing;

        // Buttons with professional styling
        saveButton = createStyledButton("Save Employee", new Color(46, 204, 113)); // Green
        saveButton.setBounds(120, yPosition + 20, 120, 35);
        mainPanel.add(saveButton);

        cancelButton = createStyledButton("Cancel", new Color(231, 76, 60)); // Red
        cancelButton.setBounds(260, yPosition + 20, 120, 35);
        mainPanel.add(cancelButton);

        // Message area
        messageLabel = new JLabel("");
        messageLabel.setBounds(50, yPosition + 70, 400, 25);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(messageLabel);

        // Add main panel to dialog
        add(mainPanel, BorderLayout.CENTER);
    }

    // Helper method to create field rows
    void createFieldRow(String labelText, JTextField field, int yPosition) {
        // Label
        JLabel label = new JLabel(labelText);
        label.setBounds(50, yPosition, 120, 25);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setForeground(new Color(51, 51, 51));
        mainPanel.add(label);

        // Text field with professional styling
        field.setBounds(180, yPosition, 250, 30);
        field.setFont(new Font("Arial", Font.PLAIN, 12));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        mainPanel.add(field);
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
        // Save button action
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveEmployee();
            }
        });

        // Cancel button action
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose(); // Close the form
            }
        });
    }

    void saveEmployee() {
        // Get all field values
        String employeeNumber = employeeNumberField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String position = positionField.getText().trim();
        String department = departmentField.getText().trim();
        String salary = salaryField.getText().trim();
        String sssNumber = sssNumberField.getText().trim();
        String philHealthNumber = philHealthNumberField.getText().trim();
        String tin = tinField.getText().trim();
        String pagIbigNumber = pagIbigNumberField.getText().trim();

        // Basic validation
        if (employeeNumber.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
            messageLabel.setText("Employee Number, First Name, and Last Name are required!");
            messageLabel.setForeground(new Color(231, 76, 60)); // Red for error
            return;
        }

        // Validate employee number format (should be numeric)
        try {
            Integer.parseInt(employeeNumber);
        } catch (NumberFormatException e) {
            messageLabel.setText("Employee Number must be a valid number!");
            messageLabel.setForeground(new Color(231, 76, 60)); // Red for error
            return;
        }

        // Validate salary if provided
        if (!salary.isEmpty()) {
            try {
                Double.parseDouble(salary);
            } catch (NumberFormatException e) {
                messageLabel.setText("Salary must be a valid number!");
                messageLabel.setForeground(new Color(231, 76, 60)); // Red for error
                return;
            }
        }

        // Save to CSV file
        try {
            saveToCSV(employeeNumber, firstName, lastName, position, department,
                    salary, sssNumber, philHealthNumber, tin, pagIbigNumber);

            messageLabel.setText("Employee saved successfully!");
            messageLabel.setForeground(new Color(46, 204, 113)); // Green for success

            // Refresh parent table after a short delay
            Timer timer = new Timer(1500, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    parentWindow.refreshTable(); // Refresh the employee table
                    dispose(); // Close this form
                }
            });
            timer.setRepeats(false);
            timer.start();

        } catch (IOException e) {
            messageLabel.setText("Error saving employee: " + e.getMessage());
            messageLabel.setForeground(new Color(231, 76, 60)); // Red for error
        }
    }

    void saveToCSV(String employeeNumber, String firstName, String lastName,
                   String position, String department, String salary,
                   String sssNumber, String philHealthNumber, String tin, String pagIbigNumber) throws IOException {

        // CSV file path (same as used in EmployeeManagement)
        String csvFile = "C:\\Users\\ferna\\IdeaProjects\\MO-IT103-MotorPHPayroll-CH-CZ\\resources\\MotorPH Employee Data - Employee Details.csv";

        // Create CSV line matching your 19-column structure:
        // Employee #,Last Name,First Name,Birthday,Address,Phone Number,SSS #,Philhealth #,TIN #,Pag-ibig #,Status,Position,Immediate Supervisor,Basic Salary,Rice Subsidy,Phone Allowance,Clothing Allowance,Gross Semi-monthly Rate,Hourly Rate
        String csvLine = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                employeeNumber,                           // Column 0: Employee #
                lastName,                                 // Column 1: Last Name
                firstName,                                // Column 2: First Name
                "",                                      // Column 3: Birthday (empty for now)
                "",                                      // Column 4: Address (empty for now)
                "",                                      // Column 5: Phone Number (empty for now)
                sssNumber.isEmpty() ? "" : sssNumber,    // Column 6: SSS #
                philHealthNumber.isEmpty() ? "" : philHealthNumber, // Column 7: Philhealth #
                tin.isEmpty() ? "" : tin,                // Column 8: TIN #
                pagIbigNumber.isEmpty() ? "" : pagIbigNumber, // Column 9: Pag-ibig #
                "Regular",                               // Column 10: Status (default to Regular)
                position.isEmpty() ? "Employee" : position, // Column 11: Position
                "",                                      // Column 12: Immediate Supervisor (empty for now)
                salary.isEmpty() ? "0" : salary,         // Column 13: Basic Salary
                "0",                                     // Column 14: Rice Subsidy (default to 0)
                "0",                                     // Column 15: Phone Allowance (default to 0)
                "0",                                     // Column 16: Clothing Allowance (default to 0)
                salary.isEmpty() ? "0" : salary,         // Column 17: Gross Semi-monthly Rate (same as basic salary for now)
                salary.isEmpty() ? "0" : String.valueOf(Double.parseDouble(salary) / 168) // Column 18: Hourly Rate (basic calculation)
        );

        // Append to CSV file
        BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile, true)); // true = append mode
        writer.newLine(); // Add new line
        writer.write(csvLine);
        writer.close();
    }

    // Test the form independently
    public static void main(String[] args) {
        // For testing only - normally opened from EmployeeManagement
        JFrame testFrame = new JFrame();
        testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        new NewEmployeeForm(null);
    }
}