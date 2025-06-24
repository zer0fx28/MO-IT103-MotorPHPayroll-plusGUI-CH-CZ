package motorph.gui;

import motorph.employee.Employee;
import motorph.employee.EmployeeDataReader;
import motorph.gui.EmployeeOperationsManager;
import motorph.deductions.StatutoryDeductions;
import motorph.process.PayPeriod;
import motorph.process.PayrollDateManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.Month;
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
        setTitle("MotorPH Employee Management System - Enhanced Payslip Edition");
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

        JLabel titleLabel = new JLabel("Employee Database Management - Enhanced Payslip System");
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

    // Cross-platform compatible button creation
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(160, 35));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 11));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // These 3 lines make buttons work on Mac:
        button.setOpaque(true);
        button.setBorderPainted(true);
        button.setContentAreaFilled(true);

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
            // UPDATE THIS PATH TO MATCH YOUR SYSTEM:
            String csvFile = "/Users/zer0fx28/IdeaProjects/MO-IT103-MotorPHPayroll-plusGUI-CH-CZ/resources/MotorPH Employee Data - Employee Details.csv";
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
                            "✅ Enhanced payslip system ready!\n" +
                            "✅ Pay period selection enabled!\n" +
                            "✅ Updated deduction calculations!",
                    "Enhanced Payslip System Ready",
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
                            "\n\nEnhanced payslip system with pay period selection!",
                    "Enhanced Employee View",
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
            // UPDATE THIS PATH TO MATCH YOUR SYSTEM:
            String csvFile = "/Users/zer0fx28/IdeaProjects/MO-IT103-MotorPHPayroll-plusGUI-CH-CZ/resources/MotorPH Employee Data - Employee Details.csv";
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

        // Employee details dialog
        JDialog detailsDialog = new JDialog(this, "Employee Details - " + employee.getFullName(), true);
        detailsDialog.setSize(650, 550);
        detailsDialog.setLocationRelativeTo(this);
        detailsDialog.setLayout(new BorderLayout());

        // Header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel headerLabel = new JLabel("Employee Information & Enhanced Payslip Generation");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(headerLabel);

        // Content panel
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        int row2 = 0;
        addDetailRow(contentPanel, gbc, "Employee ID:", employee.getEmployeeId(), row2++);
        addDetailRow(contentPanel, gbc, "Full Name:", employee.getFullName(), row2++);
        addDetailRow(contentPanel, gbc, "Position:", employee.getPosition(), row2++);
        addDetailRow(contentPanel, gbc, "Basic Salary:", "₱" + String.format("%,.2f", employee.getBasicSalary()), row2++);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);

        JButton payrollButton = createStyledButton("📄 Generate Enhanced Payslip", new Color(46, 204, 113));
        JButton closeButton = createStyledButton("Close", new Color(149, 165, 166));

        payrollButton.addActionListener(e -> {
            detailsDialog.dispose();
            showPayPeriodSelectionDialog(employee);
        });
        closeButton.addActionListener(e -> detailsDialog.dispose());

        buttonPanel.add(payrollButton);
        buttonPanel.add(closeButton);

        detailsDialog.add(headerPanel, BorderLayout.NORTH);
        detailsDialog.add(new JScrollPane(contentPanel), BorderLayout.CENTER);
        detailsDialog.add(buttonPanel, BorderLayout.SOUTH);
        detailsDialog.setVisible(true);
    }

    private void showPayPeriodSelectionDialog(Employee employee) {
        JDialog periodDialog = new JDialog(this, "Select Pay Period - " + employee.getFullName(), true);
        periodDialog.setSize(580, 500);
        periodDialog.setLocationRelativeTo(this);
        periodDialog.setLayout(new BorderLayout());
        periodDialog.getContentPane().setBackground(new Color(245, 245, 245));

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("Enhanced Payslip - Select Pay Period");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(titleLabel);

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.anchor = GridBagConstraints.WEST;

        // Year selection
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel yearLabel = new JLabel("Year:");
        yearLabel.setFont(new Font("Arial", Font.BOLD, 14));
        formPanel.add(yearLabel, gbc);

        gbc.gridx = 1;
        JComboBox<Integer> yearCombo = new JComboBox<>();
        int currentYear = LocalDate.now().getYear();
        for (int year = currentYear - 2; year <= currentYear + 1; year++) {
            yearCombo.addItem(year);
        }
        yearCombo.setSelectedItem(currentYear);
        yearCombo.setFont(new Font("Arial", Font.PLAIN, 12));
        yearCombo.setPreferredSize(new Dimension(150, 35));
        formPanel.add(yearCombo, gbc);

        // Month selection
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel monthLabel = new JLabel("Month:");
        monthLabel.setFont(new Font("Arial", Font.BOLD, 14));
        formPanel.add(monthLabel, gbc);

        gbc.gridx = 1;
        JComboBox<String> monthCombo = new JComboBox<>();
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        for (String month : months) {
            monthCombo.addItem(month);
        }
        monthCombo.setSelectedIndex(LocalDate.now().getMonthValue() - 1);
        monthCombo.setFont(new Font("Arial", Font.PLAIN, 12));
        monthCombo.setPreferredSize(new Dimension(150, 35));
        formPanel.add(monthCombo, gbc);

        // Pay period selection
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel periodLabel = new JLabel("Pay Period:");
        periodLabel.setFont(new Font("Arial", Font.BOLD, 14));
        formPanel.add(periodLabel, gbc);

        gbc.gridx = 1;
        JComboBox<String> periodCombo = new JComboBox<>();
        periodCombo.addItem("1st Half (1st - 15th)");
        periodCombo.addItem("2nd Half (16th - End of Month)");
        periodCombo.setFont(new Font("Arial", Font.PLAIN, 12));
        periodCombo.setPreferredSize(new Dimension(220, 35));
        formPanel.add(periodCombo, gbc);

        // Info panel
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(new Color(240, 248, 255));
        infoPanel.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 1));
        infoPanel.setLayout(new BorderLayout());

        JLabel infoLabel = new JLabel("<html><center><b>Enhanced Deduction Schedule:</b><br/>" +
                "• <b>1st Half:</b> SSS + PhilHealth (3%) + Pag-IBIG<br/>" +
                "• <b>2nd Half:</b> Withholding Tax only<br/>" +
                "• <b>Updated rates:</b> PhilHealth 3%, max ₱1,800</center></html>");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        infoLabel.setForeground(new Color(70, 130, 180));
        infoLabel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        infoPanel.add(infoLabel, BorderLayout.CENTER);
        formPanel.add(infoPanel, gbc);

        // Preview panel
        gbc.gridy = 4;
        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBackground(new Color(240, 255, 240));
        previewPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(46, 204, 113), 1),
                "Pay Period Preview",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 12),
                new Color(46, 204, 113)
        ));

        JLabel previewLabel = new JLabel();
        previewLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        previewLabel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        previewPanel.add(previewLabel, BorderLayout.CENTER);
        formPanel.add(previewPanel, gbc);

        // Update preview when selections change
        ActionListener updatePreview = e -> {
            try {
                int selectedYear = (Integer) yearCombo.getSelectedItem();
                int selectedMonth = monthCombo.getSelectedIndex() + 1;
                int selectedPeriod = periodCombo.getSelectedIndex() == 0 ?
                        PayrollDateManager.MID_MONTH : PayrollDateManager.END_MONTH;

                LocalDate payrollDate = PayrollDateManager.getPayrollDate(selectedYear, selectedMonth, selectedPeriod);
                LocalDate[] cutoffRange = PayrollDateManager.getCutoffDateRange(payrollDate, selectedPeriod);

                String previewText = String.format(
                        "<html><b>Payroll Date:</b> %s<br/>" +
                                "<b>Coverage Period:</b> %s<br/>" +
                                "<b>Working Days:</b> %d days</html>",
                        PayrollDateManager.formatDate(payrollDate),
                        PayrollDateManager.getFormattedDateRange(cutoffRange[0], cutoffRange[1]),
                        PayrollDateManager.getWorkingDaysInPeriod(cutoffRange[0], cutoffRange[1])
                );
                previewLabel.setText(previewText);
            } catch (Exception ex) {
                previewLabel.setText("<html><b>Error calculating dates</b></html>");
            }
        };

        yearCombo.addActionListener(updatePreview);
        monthCombo.addActionListener(updatePreview);
        periodCombo.addActionListener(updatePreview);

        // Initial preview update
        updatePreview.actionPerformed(null);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(Color.WHITE);

        JButton generateButton = createStyledButton("📄 Generate Enhanced Payslip", new Color(46, 204, 113));
        JButton cancelButton = createStyledButton("❌ Cancel", new Color(149, 165, 166));

        generateButton.addActionListener(e -> {
            try {
                int selectedYear = (Integer) yearCombo.getSelectedItem();
                int selectedMonth = monthCombo.getSelectedIndex() + 1;
                int selectedPeriod = periodCombo.getSelectedIndex() == 0 ?
                        PayrollDateManager.MID_MONTH : PayrollDateManager.END_MONTH;
                String selectedMonthName = (String) monthCombo.getSelectedItem();
                String selectedPeriodName = (String) periodCombo.getSelectedItem();

                periodDialog.dispose();

                // Create PayPeriod object
                LocalDate payrollDate = PayrollDateManager.getPayrollDate(selectedYear, selectedMonth, selectedPeriod);
                LocalDate[] cutoffRange = PayrollDateManager.getCutoffDateRange(payrollDate, selectedPeriod);
                PayPeriod payPeriod = new PayPeriod(cutoffRange[0], cutoffRange[1], payrollDate, selectedPeriod);

                // Generate enhanced payslip with selected pay period
                showEnhancedPayslipWithPeriod(employee, payPeriod, selectedYear, selectedMonthName, selectedPeriodName);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(periodDialog,
                        "Error generating payslip: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        cancelButton.addActionListener(e -> periodDialog.dispose());

        buttonPanel.add(generateButton);
        buttonPanel.add(cancelButton);

        // Assemble dialog
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        periodDialog.add(mainPanel);
        periodDialog.setVisible(true);
    }

    private void showEnhancedPayslipWithPeriod(Employee employee, PayPeriod payPeriod,
                                               int year, String monthName, String periodName) {
        // Calculate payroll details
        double grossPay = employee.getSemiMonthlyRate();
        double riceSubsidy = employee.getRiceSubsidy() / 2;
        double phoneAllowance = employee.getPhoneAllowance() / 2;
        double clothingAllowance = employee.getClothingAllowance() / 2;
        double totalAllowances = riceSubsidy + phoneAllowance + clothingAllowance;
        double totalGrossPay = grossPay + totalAllowances;

        // Calculate statutory deductions
        StatutoryDeductions.DeductionResult deductions = StatutoryDeductions.calculateDeductions(
                totalGrossPay, payPeriod.getPeriodType(), employee.getBasicSalary());

        double netPay = totalGrossPay - deductions.totalDeductions;

        // Create enhanced payslip dialog
        JDialog payslipDialog = new JDialog(this, "Enhanced Payslip - " + employee.getFullName() + " (" + monthName + " " + year + ")", true);
        payslipDialog.setSize(850, 800);
        payslipDialog.setLocationRelativeTo(this);
        payslipDialog.setLayout(new BorderLayout());
        payslipDialog.getContentPane().setBackground(new Color(245, 245, 245));

        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create all panels
        JPanel headerPanel = createEnhancedPayslipHeader(employee, payPeriod, year, monthName, periodName);
        JPanel employeeInfoPanel = createEmployeeInfoPanel(employee);
        JPanel payPeriodPanel = createEnhancedPayPeriodDetailsPanel(payPeriod);
        JPanel earningsPanel = createEarningsPanel(grossPay, riceSubsidy, phoneAllowance, clothingAllowance, totalGrossPay);
        JPanel deductionsPanel = createEnhancedDeductionsPanel(deductions, payPeriod.getPeriodType());
        JPanel summaryPanel = createSummaryPanel(totalGrossPay, deductions.totalDeductions, netPay);

        // Combine all panels
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(8, 0, 8, 0);

        gbc.gridy = 0; contentPanel.add(headerPanel, gbc);
        gbc.gridy = 1; contentPanel.add(employeeInfoPanel, gbc);
        gbc.gridy = 2; contentPanel.add(payPeriodPanel, gbc);
        gbc.gridy = 3; contentPanel.add(earningsPanel, gbc);
        gbc.gridy = 4; contentPanel.add(deductionsPanel, gbc);
        gbc.gridy = 5; contentPanel.add(summaryPanel, gbc);

        // Scroll pane
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        buttonPanel.setBackground(Color.WHITE);

        JButton downloadButton = createStyledButton("📄 Download", new Color(155, 89, 182));
        JButton printButton = createStyledButton("🖨️ Print", new Color(52, 152, 219));
        JButton closeButton = createStyledButton("✖️ Close", new Color(149, 165, 166));

        downloadButton.addActionListener(e -> downloadEnhancedPayslipWithPeriod(employee, payPeriod,
                totalGrossPay, deductions.totalDeductions, netPay, year, monthName, periodName));
        printButton.addActionListener(e -> printEnhancedPayslip(contentPanel));
        closeButton.addActionListener(e -> payslipDialog.dispose());

        buttonPanel.add(downloadButton);
        buttonPanel.add(printButton);
        buttonPanel.add(closeButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        payslipDialog.add(mainPanel);
        payslipDialog.setVisible(true);
    }

    private JPanel createEnhancedPayslipHeader(Employee employee, PayPeriod payPeriod,
                                               int year, String monthName, String periodName) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        JPanel companyPanel = new JPanel(new GridLayout(4, 1));
        companyPanel.setBackground(new Color(70, 130, 180));

        JLabel companyName = new JLabel("MOTORPH CORPORATION");
        companyName.setFont(new Font("Arial", Font.BOLD, 26));
        companyName.setForeground(Color.WHITE);
        companyName.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel payslipTitle = new JLabel("ENHANCED EMPLOYEE PAYSLIP");
        payslipTitle.setFont(new Font("Arial", Font.BOLD, 16));
        payslipTitle.setForeground(Color.WHITE);
        payslipTitle.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel periodLabel = new JLabel(monthName + " " + year + " - " + periodName);
        periodLabel.setFont(new Font("Arial", Font.BOLD, 15));
        periodLabel.setForeground(Color.WHITE);
        periodLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel payDateLabel = new JLabel("Pay Date: " + PayrollDateManager.formatDate(payPeriod.getPayDate()));
        payDateLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        payDateLabel.setForeground(Color.WHITE);
        payDateLabel.setHorizontalAlignment(SwingConstants.CENTER);

        companyPanel.add(companyName);
        companyPanel.add(payslipTitle);
        companyPanel.add(periodLabel);
        companyPanel.add(payDateLabel);

        headerPanel.add(companyPanel, BorderLayout.CENTER);
        return headerPanel;
    }

    private JPanel createEmployeeInfoPanel(Employee employee) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                "Employee Information",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14),
                new Color(70, 130, 180)
        ));

        String[] columns = {"Field", "Information", "Government ID", "Number"};
        Object[][] data = {
                {"Employee ID:", employee.getEmployeeId(), "SSS Number:", employee.getSssNo()},
                {"Full Name:", employee.getFullName(), "PhilHealth:", employee.getPhilhealthNo()},
                {"Position:", employee.getPosition(), "TIN:", employee.getTinNo()},
                {"Status:", "Regular Employee", "Pag-IBIG:", employee.getPagibigNo()}
        };

        JTable infoTable = new JTable(data, columns);
        infoTable.setFont(new Font("Arial", Font.PLAIN, 12));
        infoTable.setRowHeight(28);
        infoTable.setGridColor(new Color(230, 230, 230));
        infoTable.setBackground(Color.WHITE);
        infoTable.setEnabled(false);

        infoTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        infoTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        infoTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        infoTable.getColumnModel().getColumn(3).setPreferredWidth(150);

        infoTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        infoTable.getTableHeader().setBackground(new Color(240, 248, 255));
        infoTable.getTableHeader().setForeground(new Color(70, 130, 180));

        panel.add(new JScrollPane(infoTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createEnhancedPayPeriodDetailsPanel(PayPeriod payPeriod) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(241, 196, 15), 2),
                "Pay Period Details",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14),
                new Color(241, 196, 15)
        ));

        String deductionType = payPeriod.getPeriodType() == PayrollDateManager.MID_MONTH ?
                "Government Contributions (SSS + PhilHealth 3% + Pag-IBIG)" :
                "Withholding Tax";

        String[] columns = {"Detail", "Information"};
        Object[][] data = {
                {"Coverage Period", PayrollDateManager.getFormattedDateRange(payPeriod.getStartDate(), payPeriod.getEndDate())},
                {"Pay Date", PayrollDateManager.formatDate(payPeriod.getPayDate())},
                {"Working Days", String.valueOf(PayrollDateManager.getWorkingDaysInPeriod(payPeriod.getStartDate(), payPeriod.getEndDate()))},
                {"Period Type", payPeriod.getPeriodType() == PayrollDateManager.MID_MONTH ? "Mid-Month (1st-15th)" : "End-Month (16th-30th/31st)"},
                {"Deductions Applied", deductionType}
        };

        JTable periodTable = new JTable(data, columns);
        periodTable.setFont(new Font("Arial", Font.PLAIN, 12));
        periodTable.setRowHeight(28);
        periodTable.setGridColor(new Color(230, 230, 230));
        periodTable.setBackground(Color.WHITE);
        periodTable.setEnabled(false);

        periodTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        periodTable.getColumnModel().getColumn(1).setPreferredWidth(350);

        periodTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        periodTable.getTableHeader().setBackground(new Color(255, 250, 235));
        periodTable.getTableHeader().setForeground(new Color(241, 196, 15));

        panel.add(new JScrollPane(periodTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createEarningsPanel(double grossPay, double riceSubsidy, double phoneAllowance,
                                       double clothingAllowance, double totalGrossPay) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(46, 204, 113), 2),
                "Earnings Breakdown",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14),
                new Color(46, 204, 113)
        ));

        String[] columns = {"Description", "Amount (₱)"};
        Object[][] data = {
                {"Basic Salary (Semi-monthly)", String.format("%,.2f", grossPay)},
                {"Rice Subsidy", String.format("%,.2f", riceSubsidy)},
                {"Phone Allowance", String.format("%,.2f", phoneAllowance)},
                {"Clothing Allowance", String.format("%,.2f", clothingAllowance)},
                {"", ""},
                {"TOTAL GROSS PAY", String.format("%,.2f", totalGrossPay)}
        };

        JTable earningsTable = new JTable(data, columns);
        earningsTable.setFont(new Font("Arial", Font.PLAIN, 12));
        earningsTable.setRowHeight(28);
        earningsTable.setGridColor(new Color(230, 230, 230));
        earningsTable.setBackground(Color.WHITE);
        earningsTable.setEnabled(false);

        earningsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (row == 5) {
                    c.setBackground(new Color(46, 204, 113));
                    c.setForeground(Color.WHITE);
                    setFont(new Font("Arial", Font.BOLD, 13));
                } else if (row == 4) {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                    setFont(new Font("Arial", Font.PLAIN, 12));
                }

                if (column == 1) {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }

                return c;
            }
        });

        earningsTable.getColumnModel().getColumn(0).setPreferredWidth(350);
        earningsTable.getColumnModel().getColumn(1).setPreferredWidth(150);

        earningsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        earningsTable.getTableHeader().setBackground(new Color(240, 255, 240));
        earningsTable.getTableHeader().setForeground(new Color(46, 204, 113));

        panel.add(new JScrollPane(earningsTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createEnhancedDeductionsPanel(StatutoryDeductions.DeductionResult deductions, int periodType) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(231, 76, 60), 2),
                "Statutory Deductions",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14),
                new Color(231, 76, 60)
        ));

        String[] columns = {"Description", "Amount (₱)"};
        Object[][] data = {
                {"SSS Contribution", String.format("%,.2f", deductions.sssDeduction)},
                {"PhilHealth Contribution", String.format("%,.2f", deductions.philhealthDeduction)},
                {"Pag-IBIG Contribution", String.format("%,.2f", deductions.pagibigDeduction)},
                {"Withholding Tax", String.format("%,.2f", deductions.withholdingTax)},
                {"", ""},
                {"TOTAL DEDUCTIONS", String.format("%,.2f", deductions.totalDeductions)}
        };

        JTable deductionsTable = new JTable(data, columns);
        deductionsTable.setFont(new Font("Arial", Font.PLAIN, 12));
        deductionsTable.setRowHeight(28);
        deductionsTable.setGridColor(new Color(230, 230, 230));
        deductionsTable.setBackground(Color.WHITE);
        deductionsTable.setEnabled(false);

        deductionsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (row == 5) {
                    c.setBackground(new Color(231, 76, 60));
                    c.setForeground(Color.WHITE);
                    setFont(new Font("Arial", Font.BOLD, 13));
                } else if (row == 4) {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                    setFont(new Font("Arial", Font.PLAIN, 12));
                }

                if (column == 1) {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }

                return c;
            }
        });

        deductionsTable.getColumnModel().getColumn(0).setPreferredWidth(350);
        deductionsTable.getColumnModel().getColumn(1).setPreferredWidth(150);

        deductionsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        deductionsTable.getTableHeader().setBackground(new Color(255, 240, 240));
        deductionsTable.getTableHeader().setForeground(new Color(231, 76, 60));

        panel.add(new JScrollPane(deductionsTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSummaryPanel(double totalGrossPay, double totalDeductions, double netPay) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(155, 89, 182), 2),
                "Pay Summary",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14),
                new Color(155, 89, 182)
        ));

        String[] columns = {"Description", "Amount (₱)"};
        Object[][] data = {
                {"Total Gross Pay", String.format("%,.2f", totalGrossPay)},
                {"Total Deductions", String.format("(%,.2f)", totalDeductions)},
                {"", ""},
                {"NET PAY", String.format("%,.2f", netPay)}
        };

        JTable summaryTable = new JTable(data, columns);
        summaryTable.setFont(new Font("Arial", Font.PLAIN, 15));
        summaryTable.setRowHeight(35);
        summaryTable.setGridColor(new Color(230, 230, 230));
        summaryTable.setBackground(Color.WHITE);
        summaryTable.setEnabled(false);

        summaryTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (row == 3) {
                    c.setBackground(new Color(155, 89, 182));
                    c.setForeground(Color.WHITE);
                    setFont(new Font("Arial", Font.BOLD, 18));
                } else if (row == 2) {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                    setFont(new Font("Arial", Font.PLAIN, 15));
                }

                if (column == 1) {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }

                return c;
            }
        });

        summaryTable.getColumnModel().getColumn(0).setPreferredWidth(350);
        summaryTable.getColumnModel().getColumn(1).setPreferredWidth(150);

        summaryTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        summaryTable.getTableHeader().setBackground(new Color(248, 240, 255));
        summaryTable.getTableHeader().setForeground(new Color(155, 89, 182));

        panel.add(new JScrollPane(summaryTable), BorderLayout.CENTER);
        return panel;
    }

    private void downloadEnhancedPayslipWithPeriod(Employee employee, PayPeriod payPeriod,
                                                   double totalGrossPay, double totalDeductions, double netPay,
                                                   int year, String monthName, String periodName) {
        try {
            String fileName = "Enhanced_Payslip_" + employee.getEmployeeId() + "_" + employee.getLastName() +
                    "_" + monthName + "_" + year + "_" + (payPeriod.getPeriodType() == PayrollDateManager.MID_MONTH ? "1stHalf" : "2ndHalf") + ".txt";

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File(fileName));

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (!file.getName().endsWith(".txt")) {
                    file = new File(file.getAbsolutePath() + ".txt");
                }

                StringBuilder content = new StringBuilder();
                content.append("═══════════════════════════════════════════════════════════════════\n");
                content.append("                          MOTORPH CORPORATION                      \n");
                content.append("                    ENHANCED EMPLOYEE PAYSLIP                     \n");
                content.append("═══════════════════════════════════════════════════════════════════\n\n");

                content.append("PAY PERIOD INFORMATION:\n");
                content.append("Month/Year: ").append(monthName).append(" ").append(year).append("\n");
                content.append("Period: ").append(periodName).append("\n");
                content.append("Coverage: ").append(PayrollDateManager.getFormattedDateRange(payPeriod.getStartDate(), payPeriod.getEndDate())).append("\n");
                content.append("Pay Date: ").append(PayrollDateManager.formatDate(payPeriod.getPayDate())).append("\n");
                content.append("Working Days: ").append(PayrollDateManager.getWorkingDaysInPeriod(payPeriod.getStartDate(), payPeriod.getEndDate())).append(" days\n\n");

                content.append("EMPLOYEE INFORMATION:\n");
                content.append("Employee ID: ").append(employee.getEmployeeId()).append("\n");
                content.append("Full Name: ").append(employee.getFullName()).append("\n");
                content.append("Position: ").append(employee.getPosition()).append("\n");
                content.append("SSS Number: ").append(employee.getSssNo()).append("\n");
                content.append("PhilHealth: ").append(employee.getPhilhealthNo()).append("\n");
                content.append("TIN: ").append(employee.getTinNo()).append("\n");
                content.append("Pag-IBIG: ").append(employee.getPagibigNo()).append("\n\n");

                content.append("EARNINGS:\n");
                content.append("Basic Salary (Semi-monthly): ₱").append(String.format("%,.2f", employee.getSemiMonthlyRate())).append("\n");
                content.append("Rice Subsidy: ₱").append(String.format("%,.2f", employee.getRiceSubsidy()/2)).append("\n");
                content.append("Phone Allowance: ₱").append(String.format("%,.2f", employee.getPhoneAllowance()/2)).append("\n");
                content.append("Clothing Allowance: ₱").append(String.format("%,.2f", employee.getClothingAllowance()/2)).append("\n");
                content.append("                                      ─────────────────\n");
                content.append("TOTAL GROSS PAY: ₱").append(String.format("%,.2f", totalGrossPay)).append("\n\n");

                StatutoryDeductions.DeductionResult deductions = StatutoryDeductions.calculateDeductions(
                        totalGrossPay, payPeriod.getPeriodType(), employee.getBasicSalary());

                content.append("STATUTORY DEDUCTIONS:\n");
                content.append("SSS Contribution: ₱").append(String.format("%,.2f", deductions.sssDeduction)).append("\n");
                content.append("PhilHealth Contribution: ₱").append(String.format("%,.2f", deductions.philhealthDeduction)).append("\n");
                content.append("Pag-IBIG Contribution: ₱").append(String.format("%,.2f", deductions.pagibigDeduction)).append("\n");
                content.append("Withholding Tax: ₱").append(String.format("%,.2f", deductions.withholdingTax)).append("\n");
                content.append("                                      ─────────────────\n");
                content.append("TOTAL DEDUCTIONS: ₱").append(String.format("%,.2f", totalDeductions)).append("\n\n");

                content.append("═══════════════════════════════════════════════════════════════════\n");
                content.append("NET PAY: ₱").append(String.format("%,.2f", netPay)).append("\n");
                content.append("═══════════════════════════════════════════════════════════════════\n\n");

                content.append("Generated on: ").append(LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("MMMM dd, yyyy"))).append("\n");
                content.append("This is a computer-generated payslip. No signature required.\n");

                try (PrintWriter writer = new PrintWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
                    writer.print(content.toString());
                }

                JOptionPane.showMessageDialog(this,
                        "Enhanced payslip saved successfully!\nFile: " + file.getName(),
                        "Download Complete",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error saving file: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void printEnhancedPayslip(JPanel contentPanel) {
        JOptionPane.showMessageDialog(this,
                "Print functionality would be implemented here.\n" +
                        "This would send the enhanced payslip to the default printer.",
                "Print Payslip",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void addDetailRow(JPanel panel, GridBagConstraints gbc, String label, String value, int row) {
        gbc.gridx = 0; gbc.gridy = row;
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(labelComp, gbc);

        gbc.gridx = 1;
        JLabel valueComp = new JLabel(value);
        valueComp.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(valueComp, gbc);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch(Exception e) {
            System.err.println("Could not set Look and Feel: " + e.getMessage());
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new EmployeeManagement();
            }
        });
    }

    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setBackground(new Color(52, 152, 219));
            setForeground(Color.WHITE);
            setFont(new Font("Arial", Font.BOLD, 10));
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            setFocusPainted(false);

            setOpaque(true);
            setBorderPainted(true);
            setContentAreaFilled(true);
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
            button.setBorderPainted(true);
            button.setContentAreaFilled(true);

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