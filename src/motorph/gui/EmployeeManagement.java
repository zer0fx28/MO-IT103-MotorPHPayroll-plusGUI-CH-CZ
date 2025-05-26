package motorph.gui;

import motorph.employee.Employee;
import motorph.employee.EmployeeDataReader;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.List;

public class EmployeeManagement extends JFrame {
    // Employee data management
    private EmployeeDataReader employeeDataReader;

    // Components
    JTable employeeTable;
    DefaultTableModel tableModel;
    JScrollPane scrollPane;
    JButton viewEmployeeButton;
    JButton newEmployeeButton;
    JButton updateEmployeeButton;
    JButton deleteEmployeeButton;
    JButton backButton;

    // Table columns as per Feature Change #2 + View Details button
    String[] columnNames = {
            "Employee Number", "Last Name", "First Name",
            "SSS Number", "PhilHealth Number", "TIN", "Pag-IBIG Number", "View Details"
    };

    public EmployeeManagement() {
        // Window setup - make it wider and more professional
        setTitle("MotorPH Employee Management System");
        setSize(1200, 700); // Much wider for better spacing
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout()); // Use BorderLayout for better organization
        getContentPane().setBackground(new Color(245, 245, 245)); // Light gray background

        // Create components
        createComponents();

        // Load employee data
        loadEmployeeData();

        // Setup button actions
        setupButtons();

        // Show window
        setVisible(true);
    }

    void createComponents() {
        // Create header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        JLabel titleLabel = new JLabel("Employee Database Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(51, 51, 51));
        headerPanel.add(titleLabel);

        // Create table with column names
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7; // Only the "View Details" button column is editable
            }
        };

        employeeTable = new JTable(tableModel);
        employeeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        employeeTable.setRowHeight(40); // Taller rows for better readability
        employeeTable.setFont(new Font("Arial", Font.PLAIN, 12));
        employeeTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        employeeTable.getTableHeader().setBackground(new Color(70, 130, 180));
        employeeTable.getTableHeader().setForeground(Color.WHITE);
        employeeTable.getTableHeader().setPreferredSize(new Dimension(0, 35));
        employeeTable.setGridColor(new Color(230, 230, 230));
        employeeTable.setBackground(Color.WHITE);
        employeeTable.setSelectionBackground(new Color(230, 240, 250));

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
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        scrollPane.getViewport().setBackground(Color.WHITE);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(new Color(245, 245, 245));
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Create button panel with better spacing
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 20));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 20, 30));

        // Style buttons
        viewEmployeeButton = createStyledButton("View Selected Employee", new Color(52, 152, 219));
        newEmployeeButton = createStyledButton("New Employee", new Color(46, 204, 113));
        updateEmployeeButton = createStyledButton("Update Employee", new Color(241, 196, 15));
        deleteEmployeeButton = createStyledButton("Delete Employee", new Color(231, 76, 60));
        backButton = createStyledButton("Back to Dashboard", new Color(149, 165, 166));

        buttonPanel.add(viewEmployeeButton);
        buttonPanel.add(newEmployeeButton);
        buttonPanel.add(updateEmployeeButton);
        buttonPanel.add(deleteEmployeeButton);
        buttonPanel.add(backButton);

        // Add components to main frame
        add(headerPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

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

    // Helper method to create styled buttons
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(160, 35));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 11));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            Color originalColor = bgColor;
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

        return button;
    }

    void loadEmployeeData() {
        // Clear existing data
        tableModel.setRowCount(0);

        try {
            // Initialize EmployeeDataReader with your CSV file path
            String csvFile = "C:\\Users\\ferna\\IdeaProjects\\MO-IT103-MotorPHPayroll-CH-CZ\\resources\\MotorPH Employee Data - Employee Details.csv";
            employeeDataReader = new EmployeeDataReader(csvFile);

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

            JOptionPane.showMessageDialog(this,
                    "Employee data loaded successfully!\n" +
                            "Total employees: " + employees.size(),
                    "Data Loaded",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            // If file not found or error, add some dummy data for testing
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
        // Sample data for testing (remove this when you have real CSV data)
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
        // View Employee button
        viewEmployeeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                viewSelectedEmployee();
            }
        });

        // New Employee button
        newEmployeeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                createNewEmployee();
            }
        });

        // Update Employee button
        updateEmployeeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateSelectedEmployee();
            }
        });

        // Delete Employee button
        deleteEmployeeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deleteSelectedEmployee();
            }
        });

        // Back button
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

    void createNewEmployee() {
        new NewEmployeeForm(this); // Open the new employee form
    }

    void updateSelectedEmployee() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow != -1) {
            String employeeNumber = (String) tableModel.getValueAt(selectedRow, 0);

            JOptionPane.showMessageDialog(this,
                    "Update Employee form will open here.\n" +
                            "Employee Number: " + employeeNumber +
                            "\nThis will populate form fields with current data.",
                    "Update Employee",
                    JOptionPane.INFORMATION_MESSAGE);

            // TODO: Open update employee form
            // new UpdateEmployeeForm(employeeNumber, this);
        }
    }

    void deleteSelectedEmployee() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow != -1) {
            String employeeNumber = (String) tableModel.getValueAt(selectedRow, 0);
            String lastName = (String) tableModel.getValueAt(selectedRow, 1);
            String firstName = (String) tableModel.getValueAt(selectedRow, 2);

            int choice = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this employee?\n\n" +
                            "Employee: " + firstName + " " + lastName +
                            "\nEmployee Number: " + employeeNumber,
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (choice == JOptionPane.YES_OPTION) {
                // Remove from table
                tableModel.removeRow(selectedRow);

                JOptionPane.showMessageDialog(this,
                        "Employee deleted successfully!\n" +
                                "(This will also remove from CSV file)",
                        "Delete Successful",
                        JOptionPane.INFORMATION_MESSAGE);

                // TODO: Also remove from CSV file
                // deleteFromCSV(employeeNumber);
            }
        }
    }

    // Method to refresh table data (call this after adding/updating employees)
    public void refreshTable() {
        loadEmployeeData();
    }

    // Method to show detailed employee information in popup - Using Employee object
    void showEmployeeDetails(int row) {
        // Get employee ID from table
        String employeeId = (String) tableModel.getValueAt(row, 0);

        // Get complete employee data using your existing class
        Employee employee = employeeDataReader.getEmployee(employeeId);
        if (employee == null) {
            JOptionPane.showMessageDialog(this, "Could not load employee data", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create main dialog - larger and taller for better UX
        JDialog detailsDialog = new JDialog(this, "Complete Employee Details", true);
        detailsDialog.setSize(850, 950); // Increased height for better UX
        detailsDialog.setLocationRelativeTo(this);
        detailsDialog.setLayout(new BorderLayout());
        detailsDialog.getContentPane().setBackground(Color.WHITE);

        // Header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        JLabel headerLabel = new JLabel(employee.getFullName());
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);

        // Main content panel - organized for better visibility
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 20);
        gbc.anchor = GridBagConstraints.WEST;

        int currentRow = 0;

        // Personal Information Section
        addSectionHeader(contentPanel, gbc, "PERSONAL INFORMATION", currentRow++);
        addDetailRow(contentPanel, gbc, "Employee Number:", employee.getEmployeeId(), currentRow++);
        addDetailRow(contentPanel, gbc, "Full Name:", employee.getFullName(), currentRow++);
        addDetailRow(contentPanel, gbc, "Birthday:", employee.getBirthday(), currentRow++);
        addDetailRow(contentPanel, gbc, "Address:", employee.getAddress(), currentRow++);
        addDetailRow(contentPanel, gbc, "Phone Number:", employee.getPhoneNumber(), currentRow++);

        currentRow++; // Add space

        // Government IDs Section
        addSectionHeader(contentPanel, gbc, "GOVERNMENT IDENTIFICATION", currentRow++);
        addDetailRow(contentPanel, gbc, "SSS Number:", employee.getSssNo(), currentRow++);
        addDetailRow(contentPanel, gbc, "PhilHealth Number:", employee.getPhilhealthNo(), currentRow++);
        addDetailRow(contentPanel, gbc, "TIN:", employee.getTinNo(), currentRow++);
        addDetailRow(contentPanel, gbc, "Pag-IBIG Number:", employee.getPagibigNo(), currentRow++);

        currentRow++; // Add space

        // Employment Information Section
        addSectionHeader(contentPanel, gbc, "EMPLOYMENT DETAILS", currentRow++);
        addDetailRow(contentPanel, gbc, "Status:", employee.getStatus(), currentRow++);
        addDetailRow(contentPanel, gbc, "Position:", employee.getPosition(), currentRow++);
        addDetailRow(contentPanel, gbc, "Immediate Supervisor:", employee.getSupervisor(), currentRow++);

        currentRow++; // Add space

        // Salary Information Section
        addSectionHeader(contentPanel, gbc, "COMPENSATION DETAILS", currentRow++);
        addDetailRow(contentPanel, gbc, "Basic Salary:", "₱" + String.format("%,.2f", employee.getBasicSalary()), currentRow++);
        addDetailRow(contentPanel, gbc, "Rice Subsidy:", "₱" + String.format("%,.2f", employee.getRiceSubsidy()), currentRow++);
        addDetailRow(contentPanel, gbc, "Phone Allowance:", "₱" + String.format("%,.2f", employee.getPhoneAllowance()), currentRow++);
        addDetailRow(contentPanel, gbc, "Clothing Allowance:", "₱" + String.format("%,.2f", employee.getClothingAllowance()), currentRow++);
        addDetailRow(contentPanel, gbc, "Gross Semi-monthly Rate:", "₱" + String.format("%,.2f", employee.getSemiMonthlyRate()), currentRow++);
        addDetailRow(contentPanel, gbc, "Hourly Rate:", "₱" + String.format("%.2f", employee.getHourlyRate()), currentRow++);

        currentRow++; // Add space

        // Salary computation section
        addSectionHeader(contentPanel, gbc, "SALARY COMPUTATION", currentRow++);

        // Month selection
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

        // Year selection
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

        // Pay Period selection
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

        // Minimal scroll capability as backup
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null); // Remove scroll pane border for cleaner look

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 20, 0));

        JButton computeButton = createStyledButton("Compute Salary", new Color(46, 204, 113));
        computeButton.addActionListener(e -> {
            String selectedMonth = (String) monthCombo.getSelectedItem();
            Integer selectedYear = (Integer) yearCombo.getSelectedItem();
            String selectedPayPeriod = (String) payPeriodCombo.getSelectedItem();

            JOptionPane.showMessageDialog(detailsDialog,
                    "Computing salary for " + employee.getFullName() +
                            "\nMonth: " + selectedMonth + " " + selectedYear +
                            "\nPay Period: " + selectedPayPeriod +
                            "\nEmployee ID: " + employee.getEmployeeId() +
                            "\nBasic Salary: ₱" + String.format("%,.2f", employee.getBasicSalary()) +
                            "\nGross Semi-monthly: ₱" + String.format("%,.2f", employee.getSemiMonthlyRate()) +
                            "\nTotal Benefits: ₱" + String.format("%,.2f", employee.getTotalBenefits()) +
                            "\n\n(This is a backup version - payroll calculation will be implemented later)",
                    "Salary Computation Preview",
                    JOptionPane.INFORMATION_MESSAGE);
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

    // Helper method to add section headers
    private void addSectionHeader(JPanel panel, GridBagConstraints gbc, String title, int row) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JLabel sectionLabel = new JLabel(title);
        sectionLabel.setFont(new Font("Arial", Font.BOLD, 14));
        sectionLabel.setForeground(new Color(70, 130, 180));
        panel.add(sectionLabel, gbc);
        gbc.gridwidth = 1; // Reset for normal rows
    }

    // Helper method to add detail rows with professional formatting
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

    // Test the employee management window
    public static void main(String[] args) {
        new EmployeeManagement();
    }

    // Button Renderer Class for table buttons
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

    // Button Editor Class for handling button clicks
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