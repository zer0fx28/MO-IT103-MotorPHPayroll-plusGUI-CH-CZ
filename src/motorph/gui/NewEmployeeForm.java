package motorph.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;

public class NewEmployeeForm extends JDialog {
    // Parent reference - this connects our form to the main EmployeeManagement window
    // When we save an employee, we can tell the parent to refresh its table
    private EmployeeManagement parentWindow;

    // Form components - all the visual parts of our form
    JPanel mainPanel;           // Main container that holds everything
    JLabel titleLabel;          // The blue header title

    // Employee information fields - these are where users type information
    // Each JTextField is like a text box where users can enter data
    JTextField employeeNumberField;     // For employee ID number
    JTextField firstNameField;          // For first name
    JTextField lastNameField;           // For last name
    JTextField birthdayField;           // For birth date
    JTextField addressField;            // For home address
    JTextField phoneField;              // For phone number
    JTextField sssNumberField;          // For SSS government ID
    JTextField philHealthNumberField;   // For PhilHealth government ID
    JTextField tinField;                // For TIN tax ID
    JTextField pagIbigNumberField;      // For Pag-IBIG government ID
    JTextField positionField;           // For job position
    JTextField supervisorField;         // For immediate supervisor name
    JTextField salaryField;             // For monthly salary

    // Buttons - these perform actions when clicked
    JButton saveButton;         // Green button to save the employee
    JButton cancelButton;       // Red button to cancel and close form
    JLabel messageLabel;        // Shows success/error messages to user

    // Constructor - this runs when we create a new NewEmployeeForm
    // The 'parent' parameter is the EmployeeManagement window that opened this form
    public NewEmployeeForm(EmployeeManagement parent) {
        super(parent, "Add New Employee", true); // Modal dialog = blocks other windows until closed
        this.parentWindow = parent; // Remember who opened us so we can refresh their table

        // Window setup - make it look professional
        setSize(550, 700); // Width x Height in pixels
        setLocationRelativeTo(parent); // Center on parent window
        setResizable(false); // Don't allow user to resize (keeps layout neat)
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // Just close this window, don't exit program
        getContentPane().setBackground(new Color(245, 245, 245)); // Light gray background

        // Create all the visual components (text fields, buttons, etc.)
        createComponents();

        // Set up what happens when buttons are clicked
        setupButtons();

        // Make the window visible to user
        setVisible(true);
    }

    void createComponents() {
        // Use BorderLayout - divides window into North, South, East, West, Center
        setLayout(new BorderLayout());

        // Header panel - the blue section at top with title
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(70, 130, 180)); // Professional blue color
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20)); // Add padding

        titleLabel = new JLabel("Add New Employee");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20)); // Big bold text
        titleLabel.setForeground(Color.WHITE); // White text on blue background
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER); // Center the text
        headerPanel.add(titleLabel);

        // Main form panel - this holds all the input fields
        // GridBagLayout gives us precise control over where everything goes
        mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE); // White background looks clean
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30)); // Add padding around edges

        // GridBagConstraints controls how components are positioned
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 15); // Space between components
        gbc.anchor = GridBagConstraints.WEST; // Align everything to the left

        int row = 0; // Keep track of which row we're adding components to

        // Subtitle - instruction text under the main title
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; // Span across 2 columns
        JLabel subtitleLabel = new JLabel("Please fill in all employee information");
        subtitleLabel.setFont(new Font("Arial", Font.ITALIC, 12)); // Smaller italic text
        subtitleLabel.setForeground(new Color(102, 102, 102)); // Gray color
        mainPanel.add(subtitleLabel, gbc);
        gbc.gridwidth = 1; // Reset back to 1 column for other components
        row++; // Move to next row

        // Add some vertical space
        gbc.gridx = 0; gbc.gridy = row++;
        mainPanel.add(Box.createVerticalStrut(10), gbc); // 10 pixels of empty space

        // PERSONAL INFORMATION SECTION
        // This creates a blue header to organize related fields together
        addSectionHeader("PERSONAL INFORMATION", row++);

        // Employee Number field - required field
        addFormRow("Employee Number:", employeeNumberField = new JTextField(20), row++);

        // First Name field - required field
        addFormRow("First Name:", firstNameField = new JTextField(20), row++);

        // Last Name field - required field
        addFormRow("Last Name:", lastNameField = new JTextField(20), row++);

        // Birthday field - optional but helpful
        addFormRow("Birthday (MM/DD/YYYY):", birthdayField = new JTextField(20), row++);

        // Address field - optional
        addFormRow("Address:", addressField = new JTextField(20), row++);

        // Phone Number field - optional
        addFormRow("Phone Number:", phoneField = new JTextField(20), row++);

        // Add some space between sections
        gbc.gridx = 0; gbc.gridy = row++;
        mainPanel.add(Box.createVerticalStrut(10), gbc);

        // GOVERNMENT IDENTIFICATION SECTION
        addSectionHeader("GOVERNMENT IDENTIFICATION", row++);

        // SSS Number field - important for Philippines employment
        addFormRow("SSS Number:", sssNumberField = new JTextField(20), row++);

        // PhilHealth Number field - health insurance ID
        addFormRow("PhilHealth Number:", philHealthNumberField = new JTextField(20), row++);

        // TIN field - tax identification number
        addFormRow("TIN:", tinField = new JTextField(20), row++);

        // Pag-IBIG Number field - housing fund ID
        addFormRow("Pag-IBIG Number:", pagIbigNumberField = new JTextField(20), row++);

        // Add space between sections
        gbc.gridx = 0; gbc.gridy = row++;
        mainPanel.add(Box.createVerticalStrut(10), gbc);

        // EMPLOYMENT DETAILS SECTION
        addSectionHeader("EMPLOYMENT DETAILS", row++);

        // Position field - job title
        addFormRow("Position:", positionField = new JTextField(20), row++);

        // Immediate Supervisor field - who is their boss
        addFormRow("Immediate Supervisor:", supervisorField = new JTextField(20), row++);

        // Basic Salary field - monthly pay amount
        addFormRow("Basic Salary (₱):", salaryField = new JTextField(20), row++);

        // Make the form scrollable in case it's too tall for the screen
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null); // Remove ugly border around scroll pane

        // Button panel - holds the Save and Cancel buttons at bottom
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        // Create the Save and Cancel buttons with nice styling
        saveButton = createStyledButton("💾 Save Employee", new Color(46, 204, 113)); // Green
        cancelButton = createStyledButton("❌ Cancel", new Color(231, 76, 60)); // Red

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // Message area - shows success or error messages
        messageLabel = new JLabel(" "); // Start with empty space
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setBackground(Color.WHITE);
        messagePanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 10, 20));
        messagePanel.add(messageLabel, BorderLayout.CENTER);

        // Put everything together in the window
        add(headerPanel, BorderLayout.NORTH);    // Blue title at top
        add(scrollPane, BorderLayout.CENTER);    // Form fields in middle

        // Bottom section with buttons and messages
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);   // Buttons
        bottomPanel.add(messagePanel, BorderLayout.SOUTH);   // Messages
        add(bottomPanel, BorderLayout.SOUTH);
    }

    // Helper method to add blue section headers like "PERSONAL INFORMATION"
    void addSectionHeader(String title, int row) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; // Span across both columns
        gbc.insets = new Insets(15, 0, 10, 0); // Extra space above and below

        JLabel sectionLabel = new JLabel(title);
        sectionLabel.setFont(new Font("Arial", Font.BOLD, 14)); // Bigger bold text
        sectionLabel.setForeground(new Color(70, 130, 180)); // Same blue as header
        mainPanel.add(sectionLabel, gbc);
    }

    // Helper method to add a label and text field pair (like "First Name: [____]")
    void addFormRow(String labelText, JTextField field, int row) {
        GridBagConstraints gbc = new GridBagConstraints();

        // Add the label (like "First Name:")
        gbc.gridx = 0; gbc.gridy = row; // Column 0, specified row
        gbc.anchor = GridBagConstraints.WEST; // Align to left
        gbc.insets = new Insets(5, 0, 5, 15); // Space around the label

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 12)); // Bold text for labels
        label.setForeground(new Color(51, 51, 51)); // Dark gray color
        label.setPreferredSize(new Dimension(180, 25)); // Fixed width for consistent alignment
        mainPanel.add(label, gbc);

        // Add the text field (the input box)
        gbc.gridx = 1; gbc.gridy = row; // Column 1, same row
        gbc.fill = GridBagConstraints.HORIZONTAL; // Stretch horizontally
        gbc.insets = new Insets(5, 0, 5, 0); // Space around the field

        field.setFont(new Font("Arial", Font.PLAIN, 12)); // Normal text for input
        // Create a nice border with padding inside
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)), // Gray border
                BorderFactory.createEmptyBorder(8, 12, 8, 12))); // Padding inside
        field.setPreferredSize(new Dimension(250, 35)); // Fixed size

        // Add helpful tooltips for some fields
        if (labelText.contains("Birthday")) {
            field.setToolTipText("Format: MM/DD/YYYY (e.g., 01/15/1990)");
        } else if (labelText.contains("Salary")) {
            field.setToolTipText("Enter monthly salary amount (numbers only)");
        } else if (labelText.contains("Phone")) {
            field.setToolTipText("Format: 09XX-XXX-XXXX");
        }

        mainPanel.add(field, gbc);
    }

    // Helper method to create nice-looking buttons with hover effects
    // UPDATED: Now works on all operating systems (Windows, Mac, Linux)
    JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(150, 40)); // Fixed size
        button.setBackground(bgColor); // Set the background color
        button.setForeground(Color.WHITE); // White text
        button.setFont(new Font("Arial", Font.BOLD, 12)); // Bold font
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // Padding
        button.setFocusPainted(false); // Remove ugly focus border
        button.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Hand cursor when hovering

        // THESE 3 LINES MAKE BUTTONS WORK ON MAC:
        button.setOpaque(true);           // Show the background color
        button.setBorderPainted(true);    // Show the button border
        button.setContentAreaFilled(true); // Fill the button with color

        // Add hover effect - button gets darker when mouse is over it
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            Color originalColor = bgColor; // Remember the original color

            // When mouse enters button area
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(originalColor.darker()); // Make it darker
            }

            // When mouse leaves button area
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(originalColor); // Restore original color
            }
        });

        return button;
    }

    void setupButtons() {
        // Save button action - what happens when user clicks "Save Employee"
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveEmployee(); // Call our method to save the employee
            }
        });

        // Cancel button action - what happens when user clicks "Cancel"
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Ask user if they really want to cancel (in case they have unsaved work)
                int choice = JOptionPane.showConfirmDialog(NewEmployeeForm.this,
                        "Are you sure you want to cancel?\nAny unsaved changes will be lost.",
                        "Confirm Cancel",
                        JOptionPane.YES_NO_OPTION);

                if (choice == JOptionPane.YES_OPTION) {
                    dispose(); // Close this window
                }
            }
        });
    }

    void saveEmployee() {
        // Get all the text that user typed into the fields
        // .trim() removes extra spaces at beginning and end
        String employeeNumber = employeeNumberField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String birthday = birthdayField.getText().trim();
        String address = addressField.getText().trim();
        String phone = phoneField.getText().trim();
        String position = positionField.getText().trim();
        String supervisor = supervisorField.getText().trim();
        String salary = salaryField.getText().trim();
        String sssNumber = sssNumberField.getText().trim();
        String philHealthNumber = philHealthNumberField.getText().trim();
        String tin = tinField.getText().trim();
        String pagIbigNumber = pagIbigNumberField.getText().trim();

        // Print to console what we're trying to save (helpful for debugging)
        System.out.println("Attempting to save employee: " + firstName + " " + lastName);

        // Check if required fields are filled in
        if (employeeNumber.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
            showMessage("Employee Number, First Name, and Last Name are required!", Color.RED);
            return; // Stop here, don't save
        }

        // Check if employee number is a valid number
        try {
            Integer.parseInt(employeeNumber); // Try to convert to number
        } catch (NumberFormatException e) {
            showMessage("Employee Number must be a valid number!", Color.RED);
            return; // Stop here, don't save
        }

        // Check if salary is a valid number (if user entered one)
        if (!salary.isEmpty()) {
            try {
                Double.parseDouble(salary); // Try to convert to number
            } catch (NumberFormatException e) {
                showMessage("Salary must be a valid number!", Color.RED);
                return; // Stop here, don't save
            }
        }

        // Prevent user from clicking Save button multiple times
        saveButton.setEnabled(false);
        saveButton.setText("Saving...");

        try {
            // Save the employee data to the CSV file
            saveToCSV(employeeNumber, firstName, lastName, birthday, address, phone,
                    position, supervisor, salary, sssNumber, philHealthNumber, tin, pagIbigNumber);

            // Show success message in green
            showMessage("Employee saved successfully! ✓", new Color(46, 204, 113));

            // Wait 1.5 seconds, then refresh the parent table and close this form
            Timer timer = new Timer(1500, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        // Tell the parent window (EmployeeManagement) to refresh its table
                        if (parentWindow != null) {
                            System.out.println("Calling refreshTable on parent window...");
                            parentWindow.refreshTable();

                            // Show a nice confirmation message in the parent window
                            JOptionPane.showMessageDialog(parentWindow,
                                    "New employee '" + firstName + " " + lastName + "' has been added!\n" +
                                            "Employee ID: " + employeeNumber,
                                    "Employee Added Successfully",
                                    JOptionPane.INFORMATION_MESSAGE);
                        }
                    } catch (Exception ex) {
                        System.err.println("Error refreshing parent: " + ex.getMessage());
                    } finally {
                        dispose(); // Close this form
                    }
                }
            });
            timer.setRepeats(false); // Only run once
            timer.start();

        } catch (IOException e) {
            // If something went wrong saving to file, show error message
            showMessage("Error saving employee: " + e.getMessage(), Color.RED);
            System.err.println("Save error: " + e.getMessage());
            e.printStackTrace(); // Print full error details to console

            // Re-enable the save button so user can try again
            saveButton.setEnabled(true);
            saveButton.setText("💾 Save Employee");
        }
    }

    // Helper method to show messages to the user
    void showMessage(String message, Color color) {
        messageLabel.setText(message);
        messageLabel.setForeground(color);
    }

    // This method saves the employee data to the CSV file
    void saveToCSV(String employeeNumber, String firstName, String lastName,
                   String birthday, String address, String phone, String position,
                   String supervisor, String salary, String sssNumber,
                   String philHealthNumber, String tin, String pagIbigNumber) throws IOException {

        // IMPORTANT: This path must match the one in EmployeeManagement.java exactly!
        String csvFile = "D:\\Users\\Cherwin\\MO-IT103-MotorPHPayroll-plusGUI-CH-CZ\\resources\\MotorPH Employee Data - Employee Details.csv";

        // Print to console so we can see what file we're trying to save to
        System.out.println("Attempting to save to: " + csvFile);

        // Check if the CSV file actually exists
        File file = new File(csvFile);
        if (!file.exists()) {
            throw new IOException("CSV file not found at: " + csvFile +
                    "\nPlease check the file path!");
        }

        // Calculate hourly rate from monthly salary
        String hourlyRate = "0";
        if (!salary.isEmpty()) {
            try {
                double monthlySalary = Double.parseDouble(salary);
                // Formula: Monthly salary ÷ (22 working days × 8 hours per day)
                hourlyRate = String.format("%.2f", monthlySalary / (22 * 8));
            } catch (NumberFormatException e) {
                hourlyRate = "0"; // Use 0 if calculation fails
            }
        }

        // Calculate semi-monthly rate (half of monthly salary)
        String semiMonthlyRate = salary.isEmpty() ? "0" : String.format("%.2f", Double.parseDouble(salary) / 2);

        // Create a line of CSV data with all 19 columns
        // Each value is wrapped in quotes to handle commas and special characters
        String csvLine = String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"",
                employeeNumber,                           // Column 0: Employee #
                lastName,                                 // Column 1: Last Name
                firstName,                                // Column 2: First Name
                birthday.isEmpty() ? "" : birthday,       // Column 3: Birthday
                address.isEmpty() ? "" : address,         // Column 4: Address
                phone.isEmpty() ? "" : phone,             // Column 5: Phone Number
                sssNumber.isEmpty() ? "" : sssNumber,     // Column 6: SSS #
                philHealthNumber.isEmpty() ? "" : philHealthNumber, // Column 7: Philhealth #
                tin.isEmpty() ? "" : tin,                 // Column 8: TIN #
                pagIbigNumber.isEmpty() ? "" : pagIbigNumber, // Column 9: Pag-ibig #
                "Regular",                                // Column 10: Status (always "Regular" for new employees)
                position.isEmpty() ? "Employee" : position, // Column 11: Position
                supervisor.isEmpty() ? "" : supervisor,   // Column 12: Immediate Supervisor
                salary.isEmpty() ? "0" : salary,          // Column 13: Basic Salary
                "1500.00",                                // Column 14: Rice Subsidy (default amount)
                "2000.00",                                // Column 15: Phone Allowance (default amount)
                "1000.00",                                // Column 16: Clothing Allowance (default amount)
                semiMonthlyRate,                          // Column 17: Gross Semi-monthly Rate
                hourlyRate                                // Column 18: Hourly Rate
        );

        // Print the CSV line to console so we can see what we're saving
        System.out.println("CSV Line to add: " + csvLine);

        // Append the new employee to the end of the CSV file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile, true))) {
            writer.newLine(); // Add a new line first
            writer.write(csvLine); // Write the employee data
            writer.flush(); // Make sure data is written to file immediately
        }

        System.out.println("Successfully saved new employee to CSV!");
    }

    // Test method - FIXED: Now works on all operating systems
    public static void main(String[] args) {
        // THIS ONE LINE MAKES IT WORK ON ALL COMPUTERS:
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch(Exception e) {
            System.err.println("Could not set Look and Feel: " + e.getMessage());
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // For testing only - normally this form is opened by EmployeeManagement
                new NewEmployeeForm(null);
            }
        });
    }
}