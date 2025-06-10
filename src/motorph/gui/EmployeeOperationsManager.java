// File: motorph/gui/EmployeeOperationsManager.java
package motorph.gui;

import motorph.employee.Employee;
import motorph.employee.EmployeeDataReader;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all employee CRUD operations for the GUI system
 * Keeps the main EmployeeManagement class clean and focused on display
 */
public class EmployeeOperationsManager {
    private final String csvFilePath;
    private final JFrame parentFrame;
    private EmployeeDataReader employeeDataReader;

    public EmployeeOperationsManager(String csvFilePath, JFrame parentFrame) {
        this.csvFilePath = csvFilePath;
        this.parentFrame = parentFrame;
        this.employeeDataReader = new EmployeeDataReader(csvFilePath);
    }

    /**
     * Show update dialog for selected employee
     * @param employeeId The ID of employee to update
     * @param onSuccess Callback to run when update succeeds (like refreshing table)
     */
    public void showUpdateDialog(String employeeId, Runnable onSuccess) {
        Employee employee = employeeDataReader.getEmployee(employeeId);
        if (employee == null) {
            JOptionPane.showMessageDialog(parentFrame,
                    "Employee not found!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Simple approach: Use JOptionPane for quick updates
        showQuickUpdateDialog(employee, onSuccess);
    }

    /**
     * Show delete confirmation and perform deletion
     * @param employeeId The ID of employee to delete
     * @param firstName Employee first name for confirmation
     * @param lastName Employee last name for confirmation
     * @param onSuccess Callback to run when deletion succeeds
     */
    public void confirmAndDelete(String employeeId, String firstName, String lastName, Runnable onSuccess) {
        int result = JOptionPane.showConfirmDialog(parentFrame,
                "Are you sure you want to delete this employee?\n\n" +
                        "Name: " + firstName + " " + lastName + "\n" +
                        "ID: " + employeeId + "\n\n" +
                        "This action cannot be undone!",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            if (deleteEmployee(employeeId)) {
                JOptionPane.showMessageDialog(parentFrame,
                        "Employee deleted successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                onSuccess.run(); // Refresh the table
            } else {
                JOptionPane.showMessageDialog(parentFrame,
                        "Failed to delete employee from CSV file!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Show dialog to add new employee
     * @param onSuccess Callback to run when addition succeeds
     */
    public void showAddEmployeeDialog(Runnable onSuccess) {
        JDialog dialog = new JDialog((Frame) parentFrame, "Add New Employee", true);
        dialog.setSize(500, 600);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setLayout(new BorderLayout());

        // Create form
        JPanel formPanel = createEmployeeForm(null, false);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveButton = new JButton("Add Employee");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> {
            if (saveNewEmployeeFromForm(formPanel)) {
                dialog.dispose();
                onSuccess.run();
                JOptionPane.showMessageDialog(parentFrame, "Employee added successfully!");
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.add(new JScrollPane(formPanel), BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    /**
     * Quick update dialog using simple input dialogs
     */
    private void showQuickUpdateDialog(Employee employee, Runnable onSuccess) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Create input fields
        JTextField firstNameField = new JTextField(employee.getFirstName(), 20);
        JTextField lastNameField = new JTextField(employee.getLastName(), 20);
        JTextField positionField = new JTextField(employee.getPosition(), 20);
        JTextField phoneField = new JTextField(employee.getPhoneNumber(), 20);
        JTextField addressField = new JTextField(employee.getAddress(), 25);
        JTextField salaryField = new JTextField(String.valueOf(employee.getBasicSalary()), 15);

        // Layout form
        int row = 0;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("First Name:"), gbc);
        gbc.gridx = 1;
        panel.add(firstNameField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Last Name:"), gbc);
        gbc.gridx = 1;
        panel.add(lastNameField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Position:"), gbc);
        gbc.gridx = 1;
        panel.add(positionField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        panel.add(phoneField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1;
        panel.add(addressField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Basic Salary:"), gbc);
        gbc.gridx = 1;
        panel.add(salaryField, gbc);

        int result = JOptionPane.showConfirmDialog(parentFrame,
                panel,
                "Update Employee: " + employee.getFullName(),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                double salary = Double.parseDouble(salaryField.getText().trim());

                if (updateEmployee(employee.getEmployeeId(),
                        firstNameField.getText().trim(),
                        lastNameField.getText().trim(),
                        positionField.getText().trim(),
                        phoneField.getText().trim(),
                        addressField.getText().trim(),
                        salary)) {

                    JOptionPane.showMessageDialog(parentFrame,
                            "Employee updated successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    onSuccess.run(); // Refresh table
                } else {
                    JOptionPane.showMessageDialog(parentFrame,
                            "Failed to update employee!",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(parentFrame,
                        "Please enter a valid salary amount!",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Create a form panel for employee data
     */
    private JPanel createEmployeeForm(Employee employee, boolean isUpdate) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Add form fields here - basic implementation
        // You can expand this based on your needs

        return panel;
    }

    /**
     * Save new employee from form (placeholder for full implementation)
     */
    private boolean saveNewEmployeeFromForm(JPanel formPanel) {
        // Implementation depends on your form structure
        // For now, return true as placeholder
        return true;
    }

    /**
     * Update employee in CSV file
     */
    private boolean updateEmployee(String employeeId, String firstName, String lastName,
                                   String position, String phone, String address, double salary) {
        try {
            // Create backup first
            createBackup();

            List<String> lines = new ArrayList<>();
            boolean found = false;

            try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"); // Handle quoted fields

                    // Check if this is the employee to update
                    if (parts.length > 0 && cleanField(parts[0]).equals(employeeId)) {
                        // Update the fields (adjust indices based on your CSV structure)
                        if (parts.length >= 19) {
                            parts[1] = "\"" + lastName + "\"";
                            parts[2] = "\"" + firstName + "\"";
                            parts[4] = "\"" + address + "\"";
                            parts[5] = "\"" + phone + "\"";
                            parts[11] = "\"" + position + "\"";
                            parts[13] = String.valueOf(salary);

                            line = String.join(",", parts);
                            found = true;
                        }
                    }
                    lines.add(line);
                }
            }

            if (!found) {
                System.out.println("Employee not found: " + employeeId);
                return false;
            }

            // Write back to file
            try (PrintWriter writer = new PrintWriter(new FileWriter(csvFilePath))) {
                for (String line : lines) {
                    writer.println(line);
                }
            }

            // Refresh the employee data reader
            employeeDataReader = new EmployeeDataReader(csvFilePath);

            return true;

        } catch (Exception e) {
            System.err.println("Error updating employee: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete employee from CSV file
     */
    private boolean deleteEmployee(String employeeId) {
        try {
            // Create backup first
            createBackup();

            List<String> lines = new ArrayList<>();
            boolean found = false;

            try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                    // Skip the employee to delete
                    if (parts.length > 0 && cleanField(parts[0]).equals(employeeId)) {
                        found = true;
                        continue; // Skip this line (delete it)
                    }
                    lines.add(line);
                }
            }

            if (!found) {
                System.out.println("Employee not found for deletion: " + employeeId);
                return false;
            }

            // Write remaining lines back to file
            try (PrintWriter writer = new PrintWriter(new FileWriter(csvFilePath))) {
                for (String line : lines) {
                    writer.println(line);
                }
            }

            // Refresh the employee data reader
            employeeDataReader = new EmployeeDataReader(csvFilePath);

            return true;

        } catch (Exception e) {
            System.err.println("Error deleting employee: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Create backup of CSV file before making changes
     */
    private void createBackup() {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupPath = csvFilePath.replace(".csv", "_backup_" + timestamp + ".csv");

            try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath));
                 PrintWriter writer = new PrintWriter(new FileWriter(backupPath))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    writer.println(line);
                }
            }

            System.out.println("Backup created: " + backupPath);

        } catch (Exception e) {
            System.err.println("Warning: Could not create backup: " + e.getMessage());
        }
    }

    /**
     * Clean CSV field by removing quotes
     */
    private String cleanField(String field) {
        if (field == null) return "";
        return field.replace("\"", "").trim();
    }

    /**
     * Get current employee data reader (in case main class needs it)
     */
    public EmployeeDataReader getEmployeeDataReader() {
        return employeeDataReader;
    }

    /**
     * Refresh the employee data (call this after external changes)
     */
    public void refreshData() {
        employeeDataReader = new EmployeeDataReader(csvFilePath);
    }
}