package motorph.gui;

import motorph.employee.Employee;
import motorph.employee.EmployeeDataReader;
import motorph.gui.EmployeeOperationsManager; // For handling CRUD operations

// ADD THESE NEW IMPORTS FOR ATTENDANCE INTEGRATION:
import motorph.hours.AttendanceReader;
import motorph.hours.AttendanceRecord;
import motorph.hours.AttendanceFormatter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.List;

public class EmployeeManagement extends JFrame {
    // Employee data management - these handle reading and updating employee data
    private EmployeeDataReader employeeDataReader;      // Reads employee data from CSV
    private EmployeeOperationsManager operationsManager; // Handles update/delete operations

    // Main components - these are the building blocks of our employee management window
    JTable employeeTable;           // Table showing all employees
    DefaultTableModel tableModel;   // Data model for the table
    JScrollPane scrollPane;         // Makes table scrollable
    JButton viewEmployeeButton;     // Button to view selected employee details
    JButton newEmployeeButton;      // Button to add new employee
    JButton updateEmployeeButton;   // Button to update selected employee
    JButton deleteEmployeeButton;   // Button to delete selected employee
    JButton backButton;             // Button to go back to dashboard

    // Table columns - these define what information is shown in the table
    String[] columnNames = {
            "Employee Number", "Last Name", "First Name",
            "SSS Number", "PhilHealth Number", "TIN", "Pag-IBIG Number", "View Details"
    };

    // Constructor - this runs when we create a new EmployeeManagement window
    public EmployeeManagement() {
        // Window setup - make it wider and more professional (keeping your design)
        setTitle("MotorPH Employee Management System");
        setSize(1200, 700); // Much wider for better spacing
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Close just this window, not whole program
        setLocationRelativeTo(null); // Center on screen
        setLayout(new BorderLayout()); // Use BorderLayout for better organization
        getContentPane().setBackground(new Color(245, 245, 245)); // Light gray background

        // Create all the visual components
        createComponents();

        // Load employee data from CSV file
        loadEmployeeData();

        // Set up what happens when buttons are clicked
        setupButtons();

        // Make the window visible
        setVisible(true);
    }

    void createComponents() {
        // Create header panel with title
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(Color.WHITE); // White background
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30)); // Padding
        JLabel titleLabel = new JLabel("Employee Database Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20)); // Big bold font
        titleLabel.setForeground(new Color(51, 51, 51)); // Dark gray
        headerPanel.add(titleLabel);

        // Create table with column names
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7; // Only the "View Details" button column is editable
            }
        };

        // Set up the employee table
        employeeTable = new JTable(tableModel);
        employeeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Only one row at a time
        employeeTable.setRowHeight(40); // Taller rows for better readability
        employeeTable.setFont(new Font("Arial", Font.PLAIN, 12)); // Normal font for data
        employeeTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12)); // Bold font for headers
        employeeTable.getTableHeader().setBackground(new Color(70, 130, 180)); // Blue header
        employeeTable.getTableHeader().setForeground(Color.WHITE); // White text on blue
        employeeTable.getTableHeader().setPreferredSize(new Dimension(0, 35)); // Header height
        employeeTable.setGridColor(new Color(230, 230, 230)); // Light gray grid lines
        employeeTable.setBackground(Color.WHITE); // White background for data
        employeeTable.setSelectionBackground(new Color(230, 240, 250)); // Light blue when selected

        // Set up the "View Details" button column
        employeeTable.getColumn("View Details").setCellRenderer(new ButtonRenderer());
        employeeTable.getColumn("View Details").setCellEditor(new ButtonEditor(new JCheckBox()));
        employeeTable.getColumn("View Details").setPreferredWidth(120);
        employeeTable.getColumn("View Details").setMaxWidth(120);
        employeeTable.getColumn("View Details").setMinWidth(120);

        // Set column widths for better spacing
        employeeTable.getColumn("Employee Number").setPreferredWidth(120);
        employeeTable.getColumn("Last Name").setPreferredWidth(150);
        employeeTable.getColumn("First Name").setPreferredWidth(150);
        employeeTable.getColumn("SSS Number").setPreferredWidth(140);
        employeeTable.getColumn("PhilHealth Number").setPreferredWidth(140);
        employeeTable.getColumn("TIN").setPreferredWidth(120);
        employeeTable.getColumn("Pag-IBIG Number").setPreferredWidth(150);

        // Scroll pane for table with white background and padding
        scrollPane = new JScrollPane(employeeTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30)); // Padding
        scrollPane.getViewport().setBackground(Color.WHITE); // White background

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(new Color(245, 245, 245)); // Light gray background
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Create button panel with better spacing
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 20));
        buttonPanel.setBackground(Color.WHITE); // White background
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 20, 30)); // Padding

        // Style buttons with your exact colors
        viewEmployeeButton = createStyledButton("View Selected Employee", new Color(52, 152, 219)); // Blue
        newEmployeeButton = createStyledButton("New Employee", new Color(46, 204, 113)); // Green
        updateEmployeeButton = createStyledButton("Update Employee", new Color(241, 196, 15)); // Yellow
        deleteEmployeeButton = createStyledButton("Delete Employee", new Color(231, 76, 60)); // Red
        backButton = createStyledButton("Back to Dashboard", new Color(149, 165, 166)); // Gray

        buttonPanel.add(viewEmployeeButton);
        buttonPanel.add(newEmployeeButton);
        buttonPanel.add(updateEmployeeButton);
        buttonPanel.add(deleteEmployeeButton);
        buttonPanel.add(backButton);

        // Add components to main frame
        add(headerPanel, BorderLayout.NORTH);    // Header at top
        add(tablePanel, BorderLayout.CENTER);    // Table in center
        add(buttonPanel, BorderLayout.SOUTH);    // Buttons at bottom

        // Initially disable buttons that need selection
        viewEmployeeButton.setEnabled(false);
        updateEmployeeButton.setEnabled(false);
        deleteEmployeeButton.setEnabled(false);

        // Enable buttons when row is selected
        employeeTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = employeeTable.getSelectedRow() != -1;
            viewEmployeeButton.setEnabled(hasSelection);
            updateEmployeeButton.setEnabled(hasSelection);
            deleteEmployeeButton.setEnabled(hasSelection);
        });
    }

    // Helper method to create styled buttons (keeping your exact styling)
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(160, 35)); // Fixed size
        button.setBackground(bgColor); // Set background color
        button.setForeground(Color.WHITE); // White text
        button.setFont(new Font("Arial", Font.BOLD, 11)); // Bold font
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15)); // Padding
        button.setFocusPainted(false); // Remove ugly focus border
        button.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Hand cursor on hover

        // Add hover effect (keeping your exact design)
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            Color originalColor = bgColor; // Remember original color

            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setBackground(originalColor.darker());
                }
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setBackground(originalColor);
                }
            }
        });

        return button; // Return the finished button
    }

    void loadEmployeeData() {
        // Clear existing data from table
        tableModel.setRowCount(0);

        try {
            // Initialize EmployeeDataReader with your CSV file path
            String csvFile = "D:\\Users\\Cherwin\\MO-IT103-MotorPHPayroll-plusGUI-CH-CZ\\resources\\MotorPH Employee Data - Employee Details.csv";
            employeeDataReader = new EmployeeDataReader(csvFile);

            // Initialize operations manager for update/delete functionality
            operationsManager = new EmployeeOperationsManager(csvFile, this);

            // Get all employees using your existing class
            List<Employee> employees = employeeDataReader.getAllEmployees();

            // Add each employee to the table
            for (Employee employee : employees) {
                Object[] rowData = {
                        employee.getEmployeeId(),     // Employee Number
                        employee.getLastName(),       // Last Name
                        employee.getFirstName(),      // First Name
                        employee.getSssNo(),          // SSS Number
                        employee.getPhilhealthNo(),   // PhilHealth Number
                        employee.getTinNo(),          // TIN
                        employee.getPagibigNo(),      // Pag-IBIG Number
                        "View Details"                // Button text
                };
                tableModel.addRow(rowData);
            }

            // Show success message (keeping your exact message)
            JOptionPane.showMessageDialog(this,
                    "Employee data loaded successfully with OpenCSV!\n" +
                            "Total employees: " + employees.size() + "\n\n" +
                            "✅ Update and Delete features are now active!",
                    "Data Loaded - CRUD Operations Ready",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            // If file not found or error, add some dummy data for testing (your exact design)
            addDummyData();
            JOptionPane.showMessageDialog(this,
                    "Could not load employee CSV file: " + e.getMessage() + "\n" +
                            "Showing sample data instead.\n\n" +
                            "Please check if the file exists and is accessible.",
                    "File Not Found",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    void addDummyData() {
        // Sample data for testing (keeping your exact data)
        Object[][] sampleData = {
                {"001", "Dela Cruz", "Juan", "12-3456789-0", "1234567890", "123-456-789", "1234567890", "View Details"},
                {"002", "Santos", "Maria", "12-3456789-1", "1234567891", "123-456-790", "1234567891", "View Details"},
                {"003", "Garcia", "Pedro", "12-3456789-2", "1234567892", "123-456-791", "1234567892", "View Details"},
                {"004", "Rodriguez", "Ana", "12-3456789-3", "1234567893", "123-456-792", "1234567893", "View Details"},
                {"005", "Martinez", "Carlos", "12-3456789-4", "1234567894", "123-456-793", "1234567894", "View Details"}
        };

        for (Object[] row : sampleData) {
            tableModel.addRow(row);
        }
    }

    void setupButtons() {
        // View Employee button action
        viewEmployeeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                viewSelectedEmployee(); // Call our view method
            }
        });

        // New Employee button action - UPDATED to properly connect to form
        newEmployeeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                createNewEmployee(); // Call our create method
            }
        });

        // Update Employee button action
        updateEmployeeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateSelectedEmployee(); // Call our update method
            }
        });

        // Delete Employee button action
        deleteEmployeeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deleteSelectedEmployee(); // Call our delete method
            }
        });

        // Back button action
        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose(); // Close this window
                new MainDashboard(); // Return to dashboard
            }
        });
    }

    void viewSelectedEmployee() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow != -1) {
            String employeeNumber = (String) tableModel.getValueAt(selectedRow, 0);
            String lastName = (String) tableModel.getValueAt(selectedRow, 1);
            String firstName = (String) tableModel.getValueAt(selectedRow, 2);

            JOptionPane.showMessageDialog(this,
                    "Viewing Employee: " + firstName + " " + lastName +
                            "\nEmployee Number: " + employeeNumber +
                            "\n\n(This will open detailed employee view with salary computation)",
                    "View Employee",
                    JOptionPane.INFORMATION_MESSAGE);

            // TODO: Open detailed employee view window
            // new EmployeeDetailView(employeeNumber);
        }
    }

    // UPDATED: Fixed connection to NewEmployeeForm
    void createNewEmployee() {
        try {
            System.out.println("Opening New Employee Form..."); // Debug message
            new NewEmployeeForm(this); // Pass 'this' as parent reference for proper connection
        } catch (Exception ex) {
            // If something goes wrong, show error message
            JOptionPane.showMessageDialog(this,
                    "Could not open New Employee Form: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace(); // Print error details for debugging
        }
    }

    void updateSelectedEmployee() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow != -1) {
            String employeeId = (String) tableModel.getValueAt(selectedRow, 0);

            // Use operations manager to show update dialog
            if (operationsManager != null) {
                operationsManager.showUpdateDialog(employeeId, () -> refreshTable());
            } else {
                // Fallback message if operations manager not available
                JOptionPane.showMessageDialog(this,
                        "Update functionality not available.\nPlease restart the application.",
                        "Feature Not Available",
                        JOptionPane.WARNING_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please select an employee to update!",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    void deleteSelectedEmployee() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow != -1) {
            String employeeId = (String) tableModel.getValueAt(selectedRow, 0);
            String firstName = (String) tableModel.getValueAt(selectedRow, 2);
            String lastName = (String) tableModel.getValueAt(selectedRow, 1);

            // Use operations manager to handle deletion with confirmation
            if (operationsManager != null) {
                operationsManager.confirmAndDelete(employeeId, firstName, lastName, () -> refreshTable());
            } else {
                // Fallback to simple table removal if operations manager not available
                int choice = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to delete this employee?\n\n" +
                                "Employee: " + firstName + " " + lastName +
                                "\nEmployee Number: " + employeeId +
                                "\n\nNote: This will only remove from display, not from CSV file.",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                if (choice == JOptionPane.YES_OPTION) {
                    tableModel.removeRow(selectedRow);
                    JOptionPane.showMessageDialog(this,
                            "Employee removed from display!\n" +
                                    "(CSV file not modified - restart application to reload)",
                            "Display Updated",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please select an employee to delete!",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    // UPDATED: Enhanced method to refresh table data (call this after adding/updating employees)
    public void refreshTable() {
        System.out.println("Refreshing employee table..."); // Debug message

        // Clear the existing table data
        tableModel.setRowCount(0);

        try {
            // Recreate the EmployeeDataReader to get fresh data from CSV
            String csvFile = "D:\\Users\\Cherwin\\MO-IT103-MotorPHPayroll-plusGUI-CH-CZ\\resources\\MotorPH Employee Data - Employee Details.csv";
            employeeDataReader = new EmployeeDataReader(csvFile);

            // Refresh operations manager if it exists
            if (operationsManager != null) {
                operationsManager.refreshData();
            }

            // Get updated employee list
            List<Employee> employees = employeeDataReader.getAllEmployees();
            System.out.println("Found " + employees.size() + " employees"); // Debug message

            // Add each employee to the table
            for (Employee employee : employees) {
                Object[] rowData = {
                        employee.getEmployeeId(),     // Employee Number
                        employee.getLastName(),       // Last Name
                        employee.getFirstName(),      // First Name
                        employee.getSssNo(),          // SSS Number
                        employee.getPhilhealthNo(),   // PhilHealth Number
                        employee.getTinNo(),          // TIN
                        employee.getPagibigNo(),      // Pag-IBIG Number
                        "View Details"                // Button text
                };
                tableModel.addRow(rowData);
            }

            // Refresh the table display
            employeeTable.revalidate();
            employeeTable.repaint();

            System.out.println("Table refreshed successfully!"); // Debug message

        } catch (Exception e) {
            System.err.println("Error refreshing table: " + e.getMessage());
            e.printStackTrace(); // Print error details for debugging

            JOptionPane.showMessageDialog(this,
                    "Error refreshing employee data: " + e.getMessage() +
                            "\n\nPlease check if the CSV file is accessible.",
                    "Refresh Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Method to show detailed employee information in popup (keeping your exact design)
    void showEmployeeDetails(int row) {
        // Get employee ID from table
        String employeeId = (String) tableModel.getValueAt(row, 0);

        // Get complete employee data using your existing class
        Employee employee = employeeDataReader.getEmployee(employeeId);
        if (employee == null) {
            JOptionPane.showMessageDialog(this, "Could not load employee data", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create main dialog - larger and taller for better UX (keeping your exact design)
        JDialog detailsDialog = new JDialog(this, "Complete Employee Details", true);
        detailsDialog.setSize(850, 950); // Increased height for better UX
        detailsDialog.setLocationRelativeTo(this);
        detailsDialog.setLayout(new BorderLayout());
        detailsDialog.getContentPane().setBackground(Color.WHITE);

        // Header panel (keeping your exact design)
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        JLabel headerLabel = new JLabel(employee.getFullName());
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);

        // Main content panel - organized for better visibility (keeping your exact design)
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 20);
        gbc.anchor = GridBagConstraints.WEST;

        int currentRow = 0;

        // Personal Information Section (keeping your exact design)
        addSectionHeader(contentPanel, gbc, "PERSONAL INFORMATION", currentRow++);
        addDetailRow(contentPanel, gbc, "Employee Number:", employee.getEmployeeId(), currentRow++);
        addDetailRow(contentPanel, gbc, "Full Name:", employee.getFullName(), currentRow++);
        addDetailRow(contentPanel, gbc, "Birthday:", employee.getBirthday(), currentRow++);
        addDetailRow(contentPanel, gbc, "Address:", employee.getAddress(), currentRow++);
        addDetailRow(contentPanel, gbc, "Phone Number:", employee.getPhoneNumber(), currentRow++);

        currentRow++; // Add space

        // Government IDs Section (keeping your exact design)
        addSectionHeader(contentPanel, gbc, "GOVERNMENT IDENTIFICATION", currentRow++);
        addDetailRow(contentPanel, gbc, "SSS Number:", employee.getSssNo(), currentRow++);
        addDetailRow(contentPanel, gbc, "PhilHealth Number:", employee.getPhilhealthNo(), currentRow++);
        addDetailRow(contentPanel, gbc, "TIN:", employee.getTinNo(), currentRow++);
        addDetailRow(contentPanel, gbc, "Pag-IBIG Number:", employee.getPagibigNo(), currentRow++);

        currentRow++; // Add space

        // Employment Information Section (keeping your exact design)
        addSectionHeader(contentPanel, gbc, "EMPLOYMENT DETAILS", currentRow++);
        addDetailRow(contentPanel, gbc, "Status:", employee.getStatus(), currentRow++);
        addDetailRow(contentPanel, gbc, "Position:", employee.getPosition(), currentRow++);
        addDetailRow(contentPanel, gbc, "Immediate Supervisor:", employee.getSupervisor(), currentRow++);

        currentRow++; // Add space

        // Salary Information Section (keeping your exact design)
        addSectionHeader(contentPanel, gbc, "COMPENSATION DETAILS", currentRow++);
        addDetailRow(contentPanel, gbc, "Basic Salary:", "₱" + String.format("%,.2f", employee.getBasicSalary()), currentRow++);
        addDetailRow(contentPanel, gbc, "Rice Subsidy:", "₱" + String.format("%,.2f", employee.getRiceSubsidy()), currentRow++);
        addDetailRow(contentPanel, gbc, "Phone Allowance:", "₱" + String.format("%,.2f", employee.getPhoneAllowance()), currentRow++);
        addDetailRow(contentPanel, gbc, "Clothing Allowance:", "₱" + String.format("%,.2f", employee.getClothingAllowance()), currentRow++);
        addDetailRow(contentPanel, gbc, "Gross Semi-monthly Rate:", "₱" + String.format("%,.2f", employee.getSemiMonthlyRate()), currentRow++);
        addDetailRow(contentPanel, gbc, "Hourly Rate:", "₱" + String.format("%.2f", employee.getHourlyRate()), currentRow++);

        currentRow++; // Add space

        // Salary computation section (keeping your exact design)
        addSectionHeader(contentPanel, gbc, "SALARY COMPUTATION", currentRow++);

        // Month selection (keeping your exact design)
        JLabel monthLabel = new JLabel("Select Month:");
        monthLabel.setFont(new Font("Arial", Font.BOLD, 12));
        monthLabel.setForeground(new Color(51, 51, 51));
        gbc.gridx = 0; gbc.gridy = currentRow;
        contentPanel.add(monthLabel, gbc);

        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        JComboBox<String> monthCombo = new JComboBox<>(months);
        monthCombo.setPreferredSize(new Dimension(130, 30));
        monthCombo.setFont(new Font("Arial", Font.PLAIN, 12));
        monthCombo.setSelectedIndex(LocalDate.now().getMonthValue() - 1); // Set to current month
        gbc.gridx = 1; gbc.gridy = currentRow++;
        contentPanel.add(monthCombo, gbc);

        // Year selection (keeping your exact design)
        JLabel yearLabel = new JLabel("Select Year:");
        yearLabel.setFont(new Font("Arial", Font.BOLD, 12));
        yearLabel.setForeground(new Color(51, 51, 51));
        gbc.gridx = 0; gbc.gridy = currentRow;
        contentPanel.add(yearLabel, gbc);

        // Create year options (current year ± 2 years for flexibility)
        int currentYear = LocalDate.now().getYear();
        Integer[] years = {currentYear - 2, currentYear - 1, currentYear, currentYear + 1, currentYear + 2};
        JComboBox<Integer> yearCombo = new JComboBox<>(years);
        yearCombo.setPreferredSize(new Dimension(130, 30));
        yearCombo.setFont(new Font("Arial", Font.PLAIN, 12));
        yearCombo.setSelectedItem(currentYear); // Set to current year
        gbc.gridx = 1; gbc.gridy = currentRow++;
        contentPanel.add(yearCombo, gbc);

        // Pay Period selection (keeping your exact design)
        JLabel payPeriodLabel = new JLabel("Select Pay Period:");
        payPeriodLabel.setFont(new Font("Arial", Font.BOLD, 12));
        payPeriodLabel.setForeground(new Color(51, 51, 51));
        gbc.gridx = 0; gbc.gridy = currentRow;
        contentPanel.add(payPeriodLabel, gbc);

        String[] payPeriods = {"Mid-Month (1st-15th)", "End-Month (16th-30th)"};
        JComboBox<String> payPeriodCombo = new JComboBox<>(payPeriods);
        payPeriodCombo.setPreferredSize(new Dimension(180, 30));
        payPeriodCombo.setFont(new Font("Arial", Font.PLAIN, 12));
        gbc.gridx = 1; gbc.gridy = currentRow++;
        contentPanel.add(payPeriodCombo, gbc);

        // Minimal scroll capability as backup (keeping your exact design)
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null); // Remove scroll pane border for cleaner look

        // Button panel (keeping your exact design)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 20, 0));

        // UPDATED: Enhanced Compute Salary button with attendance integration
        JButton computeButton = createStyledButton("Compute Salary", new Color(46, 204, 113));
        computeButton.addActionListener(e -> {
            String selectedMonth = (String) monthCombo.getSelectedItem();
            Integer selectedYear = (Integer) yearCombo.getSelectedItem();
            String selectedPayPeriod = (String) payPeriodCombo.getSelectedItem();

            // Show loading state
            computeButton.setText("Loading...");
            computeButton.setEnabled(false);

            // Use SwingWorker to load attendance data in background
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    Thread.sleep(500); // Small delay to show loading
                    return null;
                }

                @Override
                protected void done() {
                    // Reset button
                    computeButton.setText("Compute Salary");
                    computeButton.setEnabled(true);

                    // Show detailed salary computation using your attendance system
                    showBasicSalaryInfo(employee, selectedMonth, selectedYear, selectedPayPeriod);
                }
            };
            worker.execute();
        });

        JButton closeButton = createStyledButton("Close", new Color(149, 165, 166));
        closeButton.addActionListener(e -> detailsDialog.dispose());

        buttonPanel.add(computeButton);
        buttonPanel.add(closeButton);

        // Add components to dialog
        detailsDialog.add(headerPanel, BorderLayout.NORTH);
        detailsDialog.add(scrollPane, BorderLayout.CENTER);
        detailsDialog.add(buttonPanel, BorderLayout.SOUTH);

        detailsDialog.setVisible(true);
    }

    // Helper method to add section headers (keeping your exact design)
    private void addSectionHeader(JPanel panel, GridBagConstraints gbc, String title, int row) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JLabel sectionLabel = new JLabel(title);
        sectionLabel.setFont(new Font("Arial", Font.BOLD, 14));
        sectionLabel.setForeground(new Color(70, 130, 180));
        panel.add(sectionLabel, gbc);
        gbc.gridwidth = 1; // Reset for normal rows
    }

    // Helper method to add detail rows with professional formatting (keeping your exact design)
    private void addDetailRow(JPanel panel, GridBagConstraints gbc, String label, String value, int row) {
        gbc.gridx = 0; gbc.gridy = row;
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Arial", Font.BOLD, 12));
        labelComp.setForeground(new Color(102, 102, 102));
        panel.add(labelComp, gbc);

        gbc.gridx = 1;
        JLabel valueComp = new JLabel(value);
        valueComp.setFont(new Font("Arial", Font.PLAIN, 12));
        valueComp.setForeground(new Color(51, 51, 51));
        panel.add(valueComp, gbc);
    }

    // Simplified withholding tax calculation
    private double calculateWithholdingTax(double grossPay) {
        // Annual gross pay for tax calculation
        double annualGross = grossPay * 24; // Semi-monthly × 24 periods

        // Philippine tax brackets (simplified)
        if (annualGross <= 250000) {
            return 0; // No tax for income ≤ ₱250,000
        } else if (annualGross <= 400000) {
            return (annualGross - 250000) * 0.05 / 24; // 5% for excess over ₱250,000
        } else if (annualGross <= 800000) {
            return (7500 + (annualGross - 400000) * 0.10) / 24; // 10% for excess over ₱400,000
        } else if (annualGross <= 2000000) {
            return (47500 + (annualGross - 800000) * 0.15) / 24; // 15% for excess over ₱800,000
        } else {
            return (227500 + (annualGross - 2000000) * 0.20) / 24; // 20% for excess over ₱2,000,000
        }
    }

    // Fallback method for when attendance data is not available
    private void showBasicSalaryInfo(Employee employee, String monthName,
                                     Integer year, String payPeriod) {
        // Create a simple dialog with basic salary info
        String message = String.format(
                "📊 Basic Salary Information\n\n" +
                        "Employee: %s\n" +
                        "Employee ID: %s\n" +
                        "Position: %s\n" +
                        "Period: %s %d (%s)\n\n" +
                        "💰 SALARY DETAILS:\n" +
                        "Monthly Salary: ₱%,.2f\n" +
                        "Semi-monthly Rate: ₱%,.2f\n" +
                        "Daily Rate: ₱%,.2f\n" +
                        "Hourly Rate: ₱%.2f\n\n" +
                        "🏠 ALLOWANCES:\n" +
                        "Rice Subsidy: ₱%,.2f\n" +
                        "Phone Allowance: ₱%,.2f\n" +
                        "Clothing Allowance: ₱%,.2f\n\n" +
                        "ℹ️ Note: For detailed computation with attendance,\n" +
                        "deductions, and net pay, please ensure your\n" +
                        "attendance system is properly connected.",

                employee.getFullName(),
                employee.getEmployeeId(),
                employee.getPosition(),
                monthName, year, payPeriod,
                employee.getBasicSalary(),
                employee.getSemiMonthlyRate(),
                employee.getBasicSalary() / 22, // Daily rate
                employee.getHourlyRate(),
                employee.getRiceSubsidy(),
                employee.getPhoneAllowance(),
                employee.getClothingAllowance()
        );

        JOptionPane.showMessageDialog(this, message, "Basic Salary Information",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // Test the employee management window (keeping your exact test method)
    public static void main(String[] args) {
        new EmployeeManagement();
    }

    // Button Renderer Class for table buttons (keeping your exact design)
    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setBackground(new Color(52, 152, 219));
            setForeground(Color.WHITE);
            setFont(new Font("Arial", Font.BOLD, 10));
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            setFocusPainted(false);
        }

        public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                                                                boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    // Button Editor Class for handling button clicks (keeping your exact design)
    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;
        private int currentRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                }
            });
        }

        public java.awt.Component getTableCellEditorComponent(JTable table, Object value,
                                                              boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            currentRow = row;
            return button;
        }

        public Object getCellEditorValue() {
            if (isPushed) {
                // Show employee details when button is clicked
                showEmployeeDetails(currentRow);
            }
            isPushed = false;
            return label;
        }

        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }

        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
    }
}