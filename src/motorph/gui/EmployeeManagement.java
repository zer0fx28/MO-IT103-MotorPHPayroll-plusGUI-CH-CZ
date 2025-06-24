package motorph.gui;

import motorph.employee.Employee;
import motorph.employee.EmployeeDataReader;
import motorph.gui.EmployeeOperationsManager;
import motorph.deductions.StatutoryDeductions;
import motorph.hours.AttendanceReader;
import motorph.hours.AttendanceRecord;
import motorph.hours.AttendanceFormatter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.List;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class EmployeeManagement extends JFrame {
    // Employee data management
    private EmployeeDataReader employeeDataReader;
    private EmployeeOperationsManager operationsManager;

    // Main components
    JTable employeeTable;
    DefaultTableModel tableModel;
    JScrollPane scrollPane;
    JButton viewEmployeeButton;
    JButton newEmployeeButton;
    JButton updateEmployeeButton;
    JButton deleteEmployeeButton;
    JButton backButton;

    // Table columns
    String[] columnNames = {
            "Employee Number", "Last Name", "First Name",
            "SSS Number", "PhilHealth Number", "TIN", "Pag-IBIG Number", "View Details"
    };

    public EmployeeManagement() {
        setTitle("MotorPH Employee Management System");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(245, 245, 245));

        createComponents();
        loadEmployeeData();
        setupButtons();
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

        // Create table
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7;
            }
        };

        employeeTable = new JTable(tableModel);
        employeeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        employeeTable.setRowHeight(40);
        employeeTable.setFont(new Font("Arial", Font.PLAIN, 12));
        employeeTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        employeeTable.getTableHeader().setBackground(new Color(70, 130, 180));
        employeeTable.getTableHeader().setForeground(Color.WHITE);
        employeeTable.getTableHeader().setPreferredSize(new Dimension(0, 35));
        employeeTable.setGridColor(new Color(230, 230, 230));
        employeeTable.setBackground(Color.WHITE);
        employeeTable.setSelectionBackground(new Color(230, 240, 250));

        // Set up button column
        employeeTable.getColumn("View Details").setCellRenderer(new ButtonRenderer());
        employeeTable.getColumn("View Details").setCellEditor(new ButtonEditor(new JCheckBox()));
        employeeTable.getColumn("View Details").setPreferredWidth(120);
        employeeTable.getColumn("View Details").setMaxWidth(120);
        employeeTable.getColumn("View Details").setMinWidth(120);

        // Set column widths
        employeeTable.getColumn("Employee Number").setPreferredWidth(120);
        employeeTable.getColumn("Last Name").setPreferredWidth(150);
        employeeTable.getColumn("First Name").setPreferredWidth(150);
        employeeTable.getColumn("SSS Number").setPreferredWidth(140);
        employeeTable.getColumn("PhilHealth Number").setPreferredWidth(140);
        employeeTable.getColumn("TIN").setPreferredWidth(120);
        employeeTable.getColumn("Pag-IBIG Number").setPreferredWidth(150);

        // Scroll pane
        scrollPane = new JScrollPane(employeeTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        scrollPane.getViewport().setBackground(Color.WHITE);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(new Color(245, 245, 245));
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 20));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 20, 30));

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

        // Add components to frame
        add(headerPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Initially disable buttons
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

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(160, 35));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 11));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

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
        tableModel.setRowCount(0);

        try {
            String csvFile = "D:\\Users\\Cherwin\\MO-IT103-MotorPHPayroll-plusGUI-CH-CZ\\resources\\MotorPH Employee Data - Employee Details.csv";
            employeeDataReader = new EmployeeDataReader(csvFile);
            operationsManager = new EmployeeOperationsManager(csvFile, this);

            List<Employee> employees = employeeDataReader.getAllEmployees();

            for (Employee employee : employees) {
                Object[] rowData = {
                        employee.getEmployeeId(),
                        employee.getLastName(),
                        employee.getFirstName(),
                        employee.getSssNo(),
                        employee.getPhilhealthNo(),
                        employee.getTinNo(),
                        employee.getPagibigNo(),
                        "View Details"
                };
                tableModel.addRow(rowData);
            }

            JOptionPane.showMessageDialog(this,
                    "Employee data loaded successfully!\n" +
                            "Total employees: " + employees.size() + "\n\n" +
                            "✅ Statutory deductions system integrated!\n" +
                            "✅ Download payslip functionality ready!",
                    "Data Loaded Successfully",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            addDummyData();
            JOptionPane.showMessageDialog(this,
                    "Could not load employee CSV file: " + e.getMessage() + "\n" +
                            "Showing sample data instead.",
                    "File Not Found",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    void addDummyData() {
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
        viewEmployeeButton.addActionListener(e -> viewSelectedEmployee());
        newEmployeeButton.addActionListener(e -> createNewEmployee());
        updateEmployeeButton.addActionListener(e -> updateSelectedEmployee());
        deleteEmployeeButton.addActionListener(e -> deleteSelectedEmployee());
        backButton.addActionListener(e -> {
            dispose();
            new MainDashboard();
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
                            "\n\n(Opening detailed employee view with payroll computation)",
                    "View Employee",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    void createNewEmployee() {
        try {
            new NewEmployeeForm(this);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not open New Employee Form: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    void updateSelectedEmployee() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow != -1) {
            String employeeId = (String) tableModel.getValueAt(selectedRow, 0);

            if (operationsManager != null) {
                operationsManager.showUpdateDialog(employeeId, () -> refreshTable());
            } else {
                JOptionPane.showMessageDialog(this,
                        "Update functionality not available.\nPlease restart the application.",
                        "Feature Not Available",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    void deleteSelectedEmployee() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow != -1) {
            String employeeId = (String) tableModel.getValueAt(selectedRow, 0);
            String firstName = (String) tableModel.getValueAt(selectedRow, 2);
            String lastName = (String) tableModel.getValueAt(selectedRow, 1);

            if (operationsManager != null) {
                operationsManager.confirmAndDelete(employeeId, firstName, lastName, () -> refreshTable());
            } else {
                int choice = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to delete this employee?\n\n" +
                                "Employee: " + firstName + " " + lastName +
                                "\nEmployee Number: " + employeeId,
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                if (choice == JOptionPane.YES_OPTION) {
                    tableModel.removeRow(selectedRow);
                    JOptionPane.showMessageDialog(this,
                            "Employee removed from display!",
                            "Display Updated",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }
    }

    public void refreshTable() {
        tableModel.setRowCount(0);

        try {
            String csvFile = "D:\\Users\\Cherwin\\MO-IT103-MotorPHPayroll-plusGUI-CH-CZ\\resources\\MotorPH Employee Data - Employee Details.csv";
            employeeDataReader = new EmployeeDataReader(csvFile);

            if (operationsManager != null) {
                operationsManager.refreshData();
            }

            List<Employee> employees = employeeDataReader.getAllEmployees();

            for (Employee employee : employees) {
                Object[] rowData = {
                        employee.getEmployeeId(),
                        employee.getLastName(),
                        employee.getFirstName(),
                        employee.getSssNo(),
                        employee.getPhilhealthNo(),
                        employee.getTinNo(),
                        employee.getPagibigNo(),
                        "View Details"
                };
                tableModel.addRow(rowData);
            }

            employeeTable.revalidate();
            employeeTable.repaint();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error refreshing employee data: " + e.getMessage(),
                    "Refresh Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    void showEmployeeDetails(int row) {
        String employeeId = (String) tableModel.getValueAt(row, 0);
        Employee employee = employeeDataReader.getEmployee(employeeId);

        if (employee == null) {
            JOptionPane.showMessageDialog(this, "Could not load employee data", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog detailsDialog = new JDialog(this, "Complete Employee Details", true);
        detailsDialog.setSize(850, 950);
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

        // Content panel
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 20);
        gbc.anchor = GridBagConstraints.WEST;

        int currentRow = 0;

        // Personal Information
        addSectionHeader(contentPanel, gbc, "PERSONAL INFORMATION", currentRow++);
        addDetailRow(contentPanel, gbc, "Employee Number:", employee.getEmployeeId(), currentRow++);
        addDetailRow(contentPanel, gbc, "Full Name:", employee.getFullName(), currentRow++);
        addDetailRow(contentPanel, gbc, "Birthday:", employee.getBirthday(), currentRow++);
        addDetailRow(contentPanel, gbc, "Address:", employee.getAddress(), currentRow++);
        addDetailRow(contentPanel, gbc, "Phone Number:", employee.getPhoneNumber(), currentRow++);
        currentRow++;

        // Government IDs
        addSectionHeader(contentPanel, gbc, "GOVERNMENT IDENTIFICATION", currentRow++);
        addDetailRow(contentPanel, gbc, "SSS Number:", employee.getSssNo(), currentRow++);
        addDetailRow(contentPanel, gbc, "PhilHealth Number:", employee.getPhilhealthNo(), currentRow++);
        addDetailRow(contentPanel, gbc, "TIN:", employee.getTinNo(), currentRow++);
        addDetailRow(contentPanel, gbc, "Pag-IBIG Number:", employee.getPagibigNo(), currentRow++);
        currentRow++;

        // Employment Information
        addSectionHeader(contentPanel, gbc, "EMPLOYMENT DETAILS", currentRow++);
        addDetailRow(contentPanel, gbc, "Status:", employee.getStatus(), currentRow++);
        addDetailRow(contentPanel, gbc, "Position:", employee.getPosition(), currentRow++);
        addDetailRow(contentPanel, gbc, "Immediate Supervisor:", employee.getSupervisor(), currentRow++);
        currentRow++;

        // Salary Information
        addSectionHeader(contentPanel, gbc, "COMPENSATION DETAILS", currentRow++);
        addDetailRow(contentPanel, gbc, "Basic Salary:", "₱" + String.format("%,.2f", employee.getBasicSalary()), currentRow++);
        addDetailRow(contentPanel, gbc, "Rice Subsidy:", "₱" + String.format("%,.2f", employee.getRiceSubsidy()), currentRow++);
        addDetailRow(contentPanel, gbc, "Phone Allowance:", "₱" + String.format("%,.2f", employee.getPhoneAllowance()), currentRow++);
        addDetailRow(contentPanel, gbc, "Clothing Allowance:", "₱" + String.format("%,.2f", employee.getClothingAllowance()), currentRow++);
        addDetailRow(contentPanel, gbc, "Gross Semi-monthly Rate:", "₱" + String.format("%,.2f", employee.getSemiMonthlyRate()), currentRow++);
        addDetailRow(contentPanel, gbc, "Hourly Rate:", "₱" + String.format("%.2f", employee.getHourlyRate()), currentRow++);
        currentRow++;

        // Statutory deductions info
        addSectionHeader(contentPanel, gbc, "STATUTORY DEDUCTIONS INFO", currentRow++);
        JLabel scheduleLabel = new JLabel("<html><body style='width: 400px'>" +
                "<b>📅 Deduction Schedule:</b><br>" +
                "• <b>Mid-Month:</b> SSS, PhilHealth, Pag-IBIG<br>" +
                "• <b>End-Month:</b> Withholding Tax<br><br>" +
                "<i>Use payroll computation below for calculations.</i>" +
                "</body></html>");
        scheduleLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        scheduleLabel.setForeground(new Color(102, 102, 102));
        gbc.gridx = 0; gbc.gridy = currentRow++; gbc.gridwidth = 2;
        contentPanel.add(scheduleLabel, gbc);
        gbc.gridwidth = 1;
        currentRow++;

        // Payroll computation section
        addSectionHeader(contentPanel, gbc, "PAYROLL COMPUTATION", currentRow++);

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
        monthCombo.setSelectedIndex(LocalDate.now().getMonthValue() - 1);
        gbc.gridx = 1; gbc.gridy = currentRow++;
        contentPanel.add(monthCombo, gbc);

        // Year selection
        JLabel yearLabel = new JLabel("Select Year:");
        yearLabel.setFont(new Font("Arial", Font.BOLD, 12));
        yearLabel.setForeground(new Color(51, 51, 51));
        gbc.gridx = 0; gbc.gridy = currentRow;
        contentPanel.add(yearLabel, gbc);

        int currentYear = LocalDate.now().getYear();
        Integer[] years = {currentYear - 2, currentYear - 1, currentYear, currentYear + 1, currentYear + 2};
        JComboBox<Integer> yearCombo = new JComboBox<>(years);
        yearCombo.setPreferredSize(new Dimension(130, 30));
        yearCombo.setFont(new Font("Arial", Font.PLAIN, 12));
        yearCombo.setSelectedItem(currentYear);
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

        // Scroll pane
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 20, 0));

        // Enhanced payroll computation button
        JButton computeButton = createStyledButton("Complete Payroll", new Color(46, 204, 113));
        computeButton.addActionListener(e -> {
            String selectedMonth = (String) monthCombo.getSelectedItem();
            Integer selectedYear = (Integer) yearCombo.getSelectedItem();
            String selectedPayPeriod = (String) payPeriodCombo.getSelectedItem();

            computeButton.setText("Computing...");
            computeButton.setEnabled(false);

            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    Thread.sleep(300);
                    return null;
                }

                @Override
                protected void done() {
                    computeButton.setText("Complete Payroll");
                    computeButton.setEnabled(true);
                    showCompletePayrollComputation(employee, selectedMonth, selectedYear, selectedPayPeriod);
                }
            };
            worker.execute();
        });

        // Monthly summary button
        JButton monthlySummaryButton = createStyledButton("Monthly Summary", new Color(52, 152, 219));
        monthlySummaryButton.addActionListener(e -> {
            showMonthlySummary(employee, (String) monthCombo.getSelectedItem(), (Integer) yearCombo.getSelectedItem());
        });

        JButton closeButton = createStyledButton("Close", new Color(149, 165, 166));
        closeButton.addActionListener(e -> detailsDialog.dispose());

        buttonPanel.add(computeButton);
        buttonPanel.add(monthlySummaryButton);
        buttonPanel.add(closeButton);

        detailsDialog.add(headerPanel, BorderLayout.NORTH);
        detailsDialog.add(scrollPane, BorderLayout.CENTER);
        detailsDialog.add(buttonPanel, BorderLayout.SOUTH);

        detailsDialog.setVisible(true);
    }

    // Enhanced payroll computation with download functionality
    private void showCompletePayrollComputation(Employee employee, String monthName,
                                                Integer year, String payPeriod) {
        int payPeriodType = payPeriod.contains("Mid-Month") ?
                StatutoryDeductions.MID_MONTH : StatutoryDeductions.END_MONTH;

        double monthlySalary = employee.getBasicSalary();
        double semiMonthlyRate = employee.getSemiMonthlyRate();
        double grossPay = semiMonthlyRate;

        double totalAllowances = employee.getRiceSubsidy() +
                employee.getPhoneAllowance() +
                employee.getClothingAllowance();
        grossPay += (totalAllowances / 2);

        StatutoryDeductions.DeductionResult deductions = StatutoryDeductions.calculateDeductions(
                grossPay, payPeriodType, monthlySalary);

        double netPay = grossPay - deductions.totalDeductions;

        JDialog payrollDialog = new JDialog(this, "Complete Payroll Computation", true);
        payrollDialog.setSize(700, 800);
        payrollDialog.setLocationRelativeTo(this);
        payrollDialog.setLayout(new BorderLayout());
        payrollDialog.getContentPane().setBackground(Color.WHITE);

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(46, 204, 113));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        JLabel headerLabel = new JLabel("💰 OFFICIAL PAYROLL COMPUTATION");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);

        // Enhanced formatted content
        JTextArea payrollContent = new JTextArea();
        payrollContent.setFont(new Font("Courier New", Font.PLAIN, 12));
        payrollContent.setEditable(false);
        payrollContent.setBackground(Color.WHITE);
        payrollContent.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        StringBuilder content = new StringBuilder();

        // Professional header
        content.append("═══════════════════════════════════════════════════════════\n");
        content.append("                     MOTORPH PAYROLL SYSTEM                   \n");
        content.append("═══════════════════════════════════════════════════════════\n\n");

        // Employee details
        content.append("📋 PAYROLL DETAILS\n");
        content.append("─────────────────────────────────────────────────────────\n");
        content.append(String.format("Employee Name    : %-30s\n", employee.getFullName()));
        content.append(String.format("Employee ID      : %-30s\n", employee.getEmployeeId()));
        content.append(String.format("Position         : %-30s\n", employee.getPosition()));
        content.append(String.format("Pay Period       : %-30s\n", monthName + " " + year + " (" + payPeriod.split(" ")[0] + ")"));
        content.append(String.format("Payroll Type     : %-30s\n",
                payPeriodType == StatutoryDeductions.MID_MONTH ? "Mid-Month (1st-15th)" : "End-Month (16th-30th)"));
        content.append("\n");

        // Enhanced gross pay with perfect alignment
        content.append("💵 GROSS PAY BREAKDOWN\n");
        content.append("─────────────────────────────────────────────────────────\n");
        content.append(formatPayrollLine("Basic Semi-monthly Rate", semiMonthlyRate));
        content.append(formatPayrollLine("Rice Subsidy (50%)", employee.getRiceSubsidy() / 2));
        content.append(formatPayrollLine("Phone Allowance (50%)", employee.getPhoneAllowance() / 2));
        content.append(formatPayrollLine("Clothing Allowance (50%)", employee.getClothingAllowance() / 2));
        content.append("─────────────────────────────────────────────────────────\n");
        content.append(formatPayrollLine("TOTAL GROSS PAY", grossPay, true));
        content.append("\n");

        // Enhanced deductions with perfect alignment
        content.append("🏛️ STATUTORY DEDUCTIONS\n");
        content.append("─────────────────────────────────────────────────────────\n");

        if (deductions.totalDeductions > 0) {
            if (deductions.sssDeduction > 0) {
                content.append(formatPayrollLine("SSS Contribution", deductions.sssDeduction));
            }
            if (deductions.philhealthDeduction > 0) {
                content.append(formatPayrollLine("PhilHealth Contribution", deductions.philhealthDeduction));
            }
            if (deductions.pagibigDeduction > 0) {
                content.append(formatPayrollLine("Pag-IBIG Contribution", deductions.pagibigDeduction));
            }
            if (deductions.withholdingTax > 0) {
                content.append(formatPayrollLine("Withholding Tax", deductions.withholdingTax));
            }
            content.append("─────────────────────────────────────────────────────────\n");
            content.append(formatPayrollLine("TOTAL DEDUCTIONS", deductions.totalDeductions, true));
        } else {
            content.append("No statutory deductions for this pay period.\n");
            content.append("(Deductions applied on ");
            content.append(payPeriodType == StatutoryDeductions.MID_MONTH ? "mid-month" : "end-month");
            content.append(" payroll cycle)\n");
            content.append("─────────────────────────────────────────────────────────\n");
            content.append(formatPayrollLine("TOTAL DEDUCTIONS", 0.0, true));
        }
        content.append("\n");

        // Enhanced net pay
        content.append("💰 NET PAY CALCULATION\n");
        content.append("═════════════════════════════════════════════════════════\n");
        content.append(formatPayrollLine("Gross Pay", grossPay));
        content.append(formatPayrollLine("Less: Total Deductions", deductions.totalDeductions));
        content.append("═════════════════════════════════════════════════════════\n");
        content.append(formatPayrollLine("NET PAY", netPay, true, true));
        content.append("═════════════════════════════════════════════════════════\n\n");

        // Footer info
        content.append("ℹ️  DEDUCTION SCHEDULE INFORMATION\n");
        content.append("─────────────────────────────────────────────────────────\n");
        content.append("• Mid-Month (1st-15th): SSS, PhilHealth, Pag-IBIG\n");
        content.append("• End-Month (16th-30th): Withholding Tax\n");
        content.append("• All amounts in Philippine Peso (₱)\n");
        content.append("─────────────────────────────────────────────────────────\n");
        content.append("Generated: " + java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm:ss a")) + "\n");

        payrollContent.setText(content.toString());

        JScrollPane scrollPane = new JScrollPane(payrollContent);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);

        // Enhanced button panel with download functionality
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 20, 0));

        // Download Payslip button (main feature)
        JButton downloadButton = createStyledButton("Download Payslip", new Color(155, 89, 182));
        downloadButton.addActionListener(e -> {
            downloadButton.setText("Downloading...");
            downloadButton.setEnabled(false);

            try {
                // Create suggested filename
                String suggestedFileName = String.format("Payslip_%s_%s_%s_%s.txt",
                        employee.getEmployeeId(),
                        employee.getLastName().replaceAll("[^a-zA-Z0-9]", ""),
                        monthName,
                        year);

                // Create file chooser
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Save Payslip");
                fileChooser.setSelectedFile(new File(suggestedFileName));

                // Add file filters
                fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Text Files (*.txt)", "txt"));
                fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("All Files (*.*)", "*"));
                fileChooser.setFileFilter(fileChooser.getChoosableFileFilters()[0]);

                // Show save dialog
                int userSelection = fileChooser.showSaveDialog(payrollDialog);

                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    File fileToSave = fileChooser.getSelectedFile();

                    // Ensure .txt extension
                    String filePath = fileToSave.getAbsolutePath();
                    if (!filePath.toLowerCase().endsWith(".txt")) {
                        fileToSave = new File(filePath + ".txt");
                    }

                    // Write content to file
                    try (PrintWriter writer = new PrintWriter(
                            new FileWriter(fileToSave, StandardCharsets.UTF_8))) {

                        writer.print(content.toString());

                        // Show success message
                        JOptionPane.showMessageDialog(payrollDialog,
                                String.format("Payslip downloaded successfully!\n\nSaved to: %s\n\nFile size: %s",
                                        fileToSave.getAbsolutePath(),
                                        formatFileSize(fileToSave.length())),
                                "Download Complete",
                                JOptionPane.INFORMATION_MESSAGE);

                        // Ask to open file
                        int openChoice = JOptionPane.showConfirmDialog(payrollDialog,
                                "Would you like to open the downloaded payslip?",
                                "Open File?",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE);

                        if (openChoice == JOptionPane.YES_OPTION) {
                            try {
                                Desktop.getDesktop().open(fileToSave);
                            } catch (Exception openEx) {
                                JOptionPane.showMessageDialog(payrollDialog,
                                        "File saved successfully, but couldn't open it automatically.\n" +
                                                "Please navigate to the file location to open it.",
                                        "File Saved",
                                        JOptionPane.INFORMATION_MESSAGE);
                            }
                        }

                    } catch (IOException ioEx) {
                        JOptionPane.showMessageDialog(payrollDialog,
                                "Error saving payslip file:\n" + ioEx.getMessage() +
                                        "\n\nPlease try again or choose a different location.",
                                "Save Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(payrollDialog,
                        "Unexpected error occurred:\n" + ex.getMessage(),
                        "Download Error",
                        JOptionPane.ERROR_MESSAGE);
            } finally {
                downloadButton.setText("Download Payslip");
                downloadButton.setEnabled(true);
            }
        });

        // Copy to clipboard button (secondary feature)
        JButton copyButton = createStyledButton("Copy to Clipboard", new Color(52, 152, 219));
        copyButton.addActionListener(e -> {
            try {
                java.awt.datatransfer.StringSelection stringSelection =
                        new java.awt.datatransfer.StringSelection(content.toString());
                java.awt.datatransfer.Clipboard clipboard =
                        java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(stringSelection, null);

                JOptionPane.showMessageDialog(payrollDialog,
                        "Payroll details copied to clipboard!\nYou can paste it into any document or email.",
                        "Copied Successfully",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(payrollDialog,
                        "Error copying to clipboard: " + ex.getMessage(),
                        "Copy Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton closeButton = createStyledButton("Close", new Color(149, 165, 166));
        closeButton.addActionListener(e -> payrollDialog.dispose());

        buttonPanel.add(downloadButton);  // Primary action
        buttonPanel.add(copyButton);      // Secondary action
        buttonPanel.add(closeButton);     // Close dialog

        payrollDialog.add(headerPanel, BorderLayout.NORTH);
        payrollDialog.add(scrollPane, BorderLayout.CENTER);
        payrollDialog.add(buttonPanel, BorderLayout.SOUTH);

        payrollDialog.setVisible(true);
    }

    // Enhanced monthly summary with download functionality
    private void showMonthlySummary(Employee employee, String monthName, Integer year) {
        double monthlySalary = employee.getBasicSalary();
        double semiMonthlyRate = employee.getSemiMonthlyRate();
        double totalAllowances = employee.getRiceSubsidy() +
                employee.getPhoneAllowance() +
                employee.getClothingAllowance();
        double grossPayPerPeriod = semiMonthlyRate + (totalAllowances / 2);

        StatutoryDeductions.DeductionResult midMonthDeductions = StatutoryDeductions.calculateDeductions(
                grossPayPerPeriod, StatutoryDeductions.MID_MONTH, monthlySalary);

        StatutoryDeductions.DeductionResult endMonthDeductions = StatutoryDeductions.calculateDeductions(
                grossPayPerPeriod, StatutoryDeductions.END_MONTH, monthlySalary);

        double monthlyGrossPay = grossPayPerPeriod * 2;
        double monthlyTotalDeductions = midMonthDeductions.totalDeductions + endMonthDeductions.totalDeductions;
        double monthlyNetPay = monthlyGrossPay - monthlyTotalDeductions;

        JDialog summaryDialog = new JDialog(this, "Monthly Payroll Summary", true);
        summaryDialog.setSize(800, 700);
        summaryDialog.setLocationRelativeTo(this);
        summaryDialog.setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(52, 152, 219));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        JLabel headerLabel = new JLabel("📊 MONTHLY PAYROLL SUMMARY");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);

        // Enhanced table format content
        JTextArea summaryContent = new JTextArea();
        summaryContent.setFont(new Font("Courier New", Font.PLAIN, 11));
        summaryContent.setEditable(false);
        summaryContent.setBackground(Color.WHITE);
        summaryContent.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        StringBuilder content = new StringBuilder();

        content.append("═══════════════════════════════════════════════════════════════════\n");
        content.append("                      MONTHLY PAYROLL SUMMARY                       \n");
        content.append("═══════════════════════════════════════════════════════════════════\n\n");

        content.append(String.format("Employee: %-25s  Month: %s %d\n",
                employee.getFullName(), monthName, year));
        content.append(String.format("ID: %-30s  Position: %s\n\n",
                employee.getEmployeeId(), employee.getPosition()));

        // Perfect table formatting
        content.append("┌─────────────────────────────┬─────────────┬─────────────┬─────────────┐\n");
        content.append("│ DESCRIPTION                 │  MID-MONTH  │  END-MONTH  │    TOTAL    │\n");
        content.append("├─────────────────────────────┼─────────────┼─────────────┼─────────────┤\n");

        content.append(String.format("│ %-27s │ %11s │ %11s │ %11s │\n",
                "Gross Pay", formatCurrency(grossPayPerPeriod),
                formatCurrency(grossPayPerPeriod), formatCurrency(monthlyGrossPay)));

        content.append("├─────────────────────────────┼─────────────┼─────────────┼─────────────┤\n");
        content.append("│ STATUTORY DEDUCTIONS:       │             │             │             │\n");

        content.append(String.format("│ %-27s │ %11s │ %11s │ %11s │\n",
                "  • SSS Contribution", formatCurrency(midMonthDeductions.sssDeduction),
                formatCurrency(endMonthDeductions.sssDeduction),
                formatCurrency(midMonthDeductions.sssDeduction + endMonthDeductions.sssDeduction)));

        content.append(String.format("│ %-27s │ %11s │ %11s │ %11s │\n",
                "  • PhilHealth Contribution", formatCurrency(midMonthDeductions.philhealthDeduction),
                formatCurrency(endMonthDeductions.philhealthDeduction),
                formatCurrency(midMonthDeductions.philhealthDeduction + endMonthDeductions.philhealthDeduction)));

        content.append(String.format("│ %-27s │ %11s │ %11s │ %11s │\n",
                "  • Pag-IBIG Contribution", formatCurrency(midMonthDeductions.pagibigDeduction),
                formatCurrency(endMonthDeductions.pagibigDeduction),
                formatCurrency(midMonthDeductions.pagibigDeduction + endMonthDeductions.pagibigDeduction)));

        content.append(String.format("│ %-27s │ %11s │ %11s │ %11s │\n",
                "  • Withholding Tax", formatCurrency(midMonthDeductions.withholdingTax),
                formatCurrency(endMonthDeductions.withholdingTax),
                formatCurrency(midMonthDeductions.withholdingTax + endMonthDeductions.withholdingTax)));

        content.append("├─────────────────────────────┼─────────────┼─────────────┼─────────────┤\n");

        content.append(String.format("│ %-27s │ %11s │ %11s │ %11s │\n",
                "TOTAL DEDUCTIONS", formatCurrency(midMonthDeductions.totalDeductions),
                formatCurrency(endMonthDeductions.totalDeductions), formatCurrency(monthlyTotalDeductions)));

        content.append("├─────────────────────────────┼─────────────┼─────────────┼─────────────┤\n");

        content.append(String.format("│ %-27s │ %11s │ %11s │ %11s │\n",
                "NET PAY", formatCurrency(grossPayPerPeriod - midMonthDeductions.totalDeductions),
                formatCurrency(grossPayPerPeriod - endMonthDeductions.totalDeductions),
                formatCurrency(monthlyNetPay)));

        content.append("└─────────────────────────────┴─────────────┴─────────────┴─────────────┘\n\n");

        // Summary statistics
        content.append("📈 MONTHLY SUMMARY STATISTICS\n");
        content.append("─────────────────────────────────────────────────────────────────────\n");
        content.append(String.format("Monthly Gross Income        : ₱%,.2f\n", monthlyGrossPay));
        content.append(String.format("Total Monthly Deductions    : ₱%,.2f\n", monthlyTotalDeductions));
        content.append(String.format("Net Monthly Take-Home       : ₱%,.2f\n", monthlyNetPay));
        content.append(String.format("Effective Deduction Rate    : %.2f%%\n",
                (monthlyTotalDeductions / monthlyGrossPay) * 100));
        content.append("\n");

        content.append("📅 DEDUCTION SCHEDULE\n");
        content.append("─────────────────────────────────────────────────────────────────────\n");
        content.append("Mid-Month Payroll (1st-15th): SSS, PhilHealth, Pag-IBIG\n");
        content.append("End-Month Payroll (16th-30th): Withholding Tax\n");
        content.append("─────────────────────────────────────────────────────────────────────\n");
        content.append("Report generated: " + java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm:ss a")) + "\n");

        summaryContent.setText(content.toString());

        JScrollPane scrollPane = new JScrollPane(summaryContent);
        scrollPane.setBorder(null);

        // Enhanced button panel with download
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 20, 0));

        JButton exportButton = createStyledButton("Download Summary", new Color(230, 126, 34));
        exportButton.addActionListener(e -> {
            exportButton.setText("Downloading...");
            exportButton.setEnabled(false);

            try {
                String suggestedFileName = String.format("Monthly_Summary_%s_%s_%s.txt",
                        employee.getEmployeeId(), monthName, year);

                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Save Monthly Summary");
                fileChooser.setSelectedFile(new File(suggestedFileName));
                fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files (*.txt)", "txt"));

                if (fileChooser.showSaveDialog(summaryDialog) == JFileChooser.APPROVE_OPTION) {
                    File fileToSave = fileChooser.getSelectedFile();

                    if (!fileToSave.getName().toLowerCase().endsWith(".txt")) {
                        fileToSave = new File(fileToSave.getAbsolutePath() + ".txt");
                    }

                    try (PrintWriter writer = new PrintWriter(
                            new FileWriter(fileToSave, StandardCharsets.UTF_8))) {
                        writer.print(content.toString());

                        JOptionPane.showMessageDialog(summaryDialog,
                                String.format("Monthly summary downloaded successfully!\n\nSaved to: %s\nFile size: %s",
                                        fileToSave.getAbsolutePath(), formatFileSize(fileToSave.length())),
                                "Download Complete", JOptionPane.INFORMATION_MESSAGE);

                        if (JOptionPane.showConfirmDialog(summaryDialog,
                                "Would you like to open the downloaded summary?",
                                "Open File?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                            try {
                                Desktop.getDesktop().open(fileToSave);
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(summaryDialog,
                                        "File saved successfully, but couldn't open it automatically.",
                                        "File Saved", JOptionPane.INFORMATION_MESSAGE);
                            }
                        }
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(summaryDialog,
                                "Error saving file: " + ex.getMessage(),
                                "Save Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } finally {
                exportButton.setText("Download Summary");
                exportButton.setEnabled(true);
            }
        });

        JButton closeButton = createStyledButton("Close", new Color(149, 165, 166));
        closeButton.addActionListener(e -> summaryDialog.dispose());

        buttonPanel.add(exportButton);
        buttonPanel.add(closeButton);

        summaryDialog.add(headerPanel, BorderLayout.NORTH);
        summaryDialog.add(scrollPane, BorderLayout.CENTER);
        summaryDialog.add(buttonPanel, BorderLayout.SOUTH);

        summaryDialog.setVisible(true);
    }

    // Helper methods for formatting
    private void addSectionHeader(JPanel panel, GridBagConstraints gbc, String title, int row) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JLabel sectionLabel = new JLabel(title);
        sectionLabel.setFont(new Font("Arial", Font.BOLD, 14));
        sectionLabel.setForeground(new Color(70, 130, 180));
        panel.add(sectionLabel, gbc);
        gbc.gridwidth = 1;
    }

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

    private String formatPayrollLine(String description, double amount) {
        return formatPayrollLine(description, amount, false, false);
    }

    private String formatPayrollLine(String description, double amount, boolean isTotal) {
        return formatPayrollLine(description, amount, isTotal, false);
    }

    private String formatPayrollLine(String description, double amount, boolean isTotal, boolean isNetPay) {
        String formattedAmount = String.format("₱%,12.2f", amount);

        if (isNetPay) {
            return String.format("%-35s : %s ◄◄◄\n", description.toUpperCase(), formattedAmount);
        } else if (isTotal) {
            return String.format("%-35s : %s\n", description.toUpperCase(), formattedAmount);
        } else {
            return String.format("%-35s : %s\n", description, formattedAmount);
        }
    }

    private String formatCurrency(double amount) {
        if (amount == 0) {
            return "     -     ";
        }
        return String.format("₱%,8.2f", amount);
    }

    /**
     * Helper method to format file size for display
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " bytes";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
    }

    public static void main(String[] args) {
        new EmployeeManagement();
    }

    // Button Renderer and Editor Classes
    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setBackground(new Color(52, 152, 219));
            setForeground(Color.WHITE);
            setFont(new Font("Arial", Font.BOLD, 10));
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            setFocusPainted(false);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;
        private int currentRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            currentRow = row;
            return button;
        }

        public Object getCellEditorValue() {
            if (isPushed) {
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