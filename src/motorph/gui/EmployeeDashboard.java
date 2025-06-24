package motorph.gui;

import motorph.employee.Employee;
import motorph.employee.EmployeeDataReader;
import motorph.gui.EmployeeCredentialsManager.EmployeeCredential;
import motorph.deductions.StatutoryDeductions;
import motorph.process.PayPeriod;
import motorph.process.PayrollDateManager;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Individual Employee Dashboard
 * Shows personal information and allows payslip generation
 */
public class EmployeeDashboard extends JFrame {
    private EmployeeCredential credential;
    private Employee employeeData;
    private EmployeeDataReader dataReader;

    public EmployeeDashboard(EmployeeCredential credential) {
        this.credential = credential;
        loadEmployeeData();

        setTitle("MotorPH Employee Portal - " + credential.fullName);
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(245, 245, 245));

        createComponents();
        setVisible(true);
    }

    void loadEmployeeData() {
        try {
            String csvFile = "/Users/zer0fx28/IdeaProjects/MO-IT103-MotorPHPayroll-plusGUI-CH-CZ/resources/MotorPH Employee Data - Employee Details.csv";
            dataReader = new EmployeeDataReader(csvFile);
            employeeData = dataReader.getEmployee(credential.employeeId);

            if (employeeData == null) {
                JOptionPane.showMessageDialog(this,
                        "Could not load your employee data. Please contact HR.",
                        "Data Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading employee data: " + e.getMessage(),
                    "System Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    void createComponents() {
        // Header Panel
        JPanel headerPanel = createHeaderPanel();

        // Main Content Panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(10, 0, 10, 0);

        // Personal Information Panel
        gbc.gridy = 0;
        mainPanel.add(createPersonalInfoPanel(), gbc);

        // Employment Details Panel
        gbc.gridy = 1;
        mainPanel.add(createEmploymentPanel(), gbc);

        // Quick Actions Panel
        gbc.gridy = 2;
        mainPanel.add(createQuickActionsPanel(), gbc);

        // Footer Panel
        JPanel footerPanel = createFooterPanel();

        // Assemble
        add(headerPanel, BorderLayout.NORTH);
        add(new JScrollPane(mainPanel), BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);
    }

    JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(52, 73, 94));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        // Welcome section
        JPanel welcomePanel = new JPanel(new GridLayout(3, 1));
        welcomePanel.setBackground(new Color(52, 73, 94));

        JLabel welcomeLabel = new JLabel("Welcome to MotorPH Employee Portal");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 22));
        welcomeLabel.setForeground(Color.WHITE);

        JLabel nameLabel = new JLabel(credential.fullName + " (" + credential.employeeId + ")");
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        nameLabel.setForeground(new Color(236, 240, 241));

        JLabel dateLabel = new JLabel("Today: " + LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        dateLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        dateLabel.setForeground(new Color(189, 195, 199));

        welcomePanel.add(welcomeLabel);
        welcomePanel.add(nameLabel);
        welcomePanel.add(dateLabel);

        // Logout button
        JButton logoutButton = createStyledButton("🚪 Logout", new Color(231, 76, 60));
        logoutButton.addActionListener(e -> handleLogout());

        headerPanel.add(welcomePanel, BorderLayout.CENTER);
        headerPanel.add(logoutButton, BorderLayout.EAST);

        return headerPanel;
    }

    JPanel createPersonalInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
                "👤 Personal Information",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14),
                new Color(52, 152, 219)
        ));

        if (employeeData != null) {
            String[] columns = {"Field", "Information", "Government ID", "Details"};
            Object[][] data = {
                    {"Full Name", employeeData.getFullName(), "SSS Number", employeeData.getSssNo()},
                    {"Employee ID", employeeData.getEmployeeId(), "PhilHealth", employeeData.getPhilhealthNo()},
                    {"Position", employeeData.getPosition(), "TIN", employeeData.getTinNo()},
                    {"Phone", employeeData.getPhoneNumber(), "Pag-IBIG", employeeData.getPagibigNo()},
                    {"Address", employeeData.getAddress(), "Status", "Regular Employee"}
            };

            JTable infoTable = new JTable(data, columns);
            infoTable.setFont(new Font("Arial", Font.PLAIN, 12));
            infoTable.setRowHeight(28);
            infoTable.setEnabled(false);
            infoTable.setBackground(Color.WHITE);

            panel.add(new JScrollPane(infoTable), BorderLayout.CENTER);
        } else {
            JLabel errorLabel = new JLabel("Unable to load personal information");
            errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
            errorLabel.setForeground(Color.RED);
            panel.add(errorLabel, BorderLayout.CENTER);
        }

        return panel;
    }

    JPanel createEmploymentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(46, 204, 113), 2),
                "💼 Employment Details",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14),
                new Color(46, 204, 113)
        ));

        if (employeeData != null) {
            String[] columns = {"Detail", "Amount"};
            Object[][] data = {
                    {"Basic Salary", "₱" + String.format("%,.2f", employeeData.getBasicSalary())},
                    {"Semi-Monthly Rate", "₱" + String.format("%,.2f", employeeData.getSemiMonthlyRate())},
                    {"Rice Subsidy", "₱" + String.format("%,.2f", employeeData.getRiceSubsidy())},
                    {"Phone Allowance", "₱" + String.format("%,.2f", employeeData.getPhoneAllowance())},
                    {"Clothing Allowance", "₱" + String.format("%,.2f", employeeData.getClothingAllowance())}
            };

            JTable employmentTable = new JTable(data, columns);
            employmentTable.setFont(new Font("Arial", Font.PLAIN, 12));
            employmentTable.setRowHeight(28);
            employmentTable.setEnabled(false);
            employmentTable.setBackground(Color.WHITE);

            // Right-align amounts
            employmentTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                                                               boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    if (column == 1) {
                        setHorizontalAlignment(SwingConstants.RIGHT);
                    } else {
                        setHorizontalAlignment(SwingConstants.LEFT);
                    }
                    return c;
                }
            });

            panel.add(new JScrollPane(employmentTable), BorderLayout.CENTER);
        }

        return panel;
    }

    JPanel createQuickActionsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(155, 89, 182), 2),
                "⚡ Quick Actions",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14),
                new Color(155, 89, 182)
        ));

        JButton payslipButton = createActionButton("📄 Generate Payslip",
                "View and download your payslip", new Color(52, 152, 219));
        payslipButton.addActionListener(e -> generatePayslip());

        JButton profileButton = createActionButton("👤 View Profile",
                "View your complete profile", new Color(46, 204, 113));
        profileButton.addActionListener(e -> viewProfile());

        JButton passwordButton = createActionButton("🔒 Change Password",
                "Update your login password", new Color(241, 196, 15));
        passwordButton.addActionListener(e -> changePassword());

        JButton helpButton = createActionButton("❓ Help & Support",
                "Get help or contact HR", new Color(155, 89, 182));
        helpButton.addActionListener(e -> showHelp());

        panel.add(payslipButton);
        panel.add(profileButton);
        panel.add(passwordButton);
        panel.add(helpButton);

        return panel;
    }

    JButton createActionButton(String title, String description, Color color) {
        JButton button = new JButton();
        button.setLayout(new BorderLayout());
        button.setBackground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(color);

        JLabel descLabel = new JLabel("<html>" + description + "</html>");
        descLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        descLabel.setForeground(new Color(127, 140, 141));

        button.add(titleLabel, BorderLayout.NORTH);
        button.add(descLabel, BorderLayout.CENTER);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(248, 249, 250));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.WHITE);
            }
        });

        return button;
    }

    JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setBackground(new Color(236, 240, 241));
        footerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel footerLabel = new JLabel("MotorPH Employee Portal v2.0 | Secure Individual Access");
        footerLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        footerLabel.setForeground(new Color(127, 140, 141));
        footerPanel.add(footerLabel);

        return footerPanel;
    }

    void generatePayslip() {
        if (employeeData == null) {
            JOptionPane.showMessageDialog(this, "Unable to generate payslip. Employee data not available.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Show pay period selection dialog
        showEmployeePayPeriodDialog();
    }

    void showEmployeePayPeriodDialog() {
        JDialog periodDialog = new JDialog(this, "Select Pay Period", true);
        periodDialog.setSize(500, 400);
        periodDialog.setLocationRelativeTo(this);
        periodDialog.setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(52, 73, 94));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("📄 Generate Your Payslip");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(titleLabel);

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Year selection
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel yearLabel = new JLabel("Year:");
        yearLabel.setFont(new Font("Arial", Font.BOLD, 13));
        formPanel.add(yearLabel, gbc);

        gbc.gridx = 1;
        JComboBox<Integer> yearCombo = new JComboBox<>();
        int currentYear = LocalDate.now().getYear();
        for (int year = currentYear - 1; year <= currentYear; year++) {
            yearCombo.addItem(year);
        }
        yearCombo.setSelectedItem(currentYear);
        formPanel.add(yearCombo, gbc);

        // Month selection
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel monthLabel = new JLabel("Month:");
        monthLabel.setFont(new Font("Arial", Font.BOLD, 13));
        formPanel.add(monthLabel, gbc);

        gbc.gridx = 1;
        JComboBox<String> monthCombo = new JComboBox<>();
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        for (String month : months) {
            monthCombo.addItem(month);
        }
        monthCombo.setSelectedIndex(LocalDate.now().getMonthValue() - 1);
        formPanel.add(monthCombo, gbc);

        // Period selection
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel periodLabel = new JLabel("Pay Period:");
        periodLabel.setFont(new Font("Arial", Font.BOLD, 13));
        formPanel.add(periodLabel, gbc);

        gbc.gridx = 1;
        JComboBox<String> periodCombo = new JComboBox<>();
        periodCombo.addItem("1st Half (1st - 15th)");
        periodCombo.addItem("2nd Half (16th - End of Month)");
        formPanel.add(periodCombo, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);

        JButton generateButton = createStyledButton("📄 Generate", new Color(46, 204, 113));
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

                LocalDate payrollDate = PayrollDateManager.getPayrollDate(selectedYear, selectedMonth, selectedPeriod);
                LocalDate[] cutoffRange = PayrollDateManager.getCutoffDateRange(payrollDate, selectedPeriod);
                PayPeriod payPeriod = new PayPeriod(cutoffRange[0], cutoffRange[1], payrollDate, selectedPeriod);

                showEmployeePayslip(payPeriod, selectedYear, selectedMonthName, selectedPeriodName);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(periodDialog,
                        "Error generating payslip: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> periodDialog.dispose());

        buttonPanel.add(generateButton);
        buttonPanel.add(cancelButton);

        periodDialog.add(headerPanel, BorderLayout.NORTH);
        periodDialog.add(formPanel, BorderLayout.CENTER);
        periodDialog.add(buttonPanel, BorderLayout.SOUTH);

        periodDialog.setVisible(true);
    }

    void showEmployeePayslip(PayPeriod payPeriod, int year, String monthName, String periodName) {
        // Calculate payroll details
        double grossPay = employeeData.getSemiMonthlyRate();
        double riceSubsidy = employeeData.getRiceSubsidy() / 2;
        double phoneAllowance = employeeData.getPhoneAllowance() / 2;
        double clothingAllowance = employeeData.getClothingAllowance() / 2;
        double totalAllowances = riceSubsidy + phoneAllowance + clothingAllowance;
        double totalGrossPay = grossPay + totalAllowances;

        StatutoryDeductions.DeductionResult deductions = StatutoryDeductions.calculateDeductions(
                totalGrossPay, payPeriod.getPeriodType(), employeeData.getBasicSalary());

        double netPay = totalGrossPay - deductions.totalDeductions;

        // Create payslip dialog
        JDialog payslipDialog = new JDialog(this, "My Payslip - " + monthName + " " + year, true);
        payslipDialog.setSize(700, 600);
        payslipDialog.setLocationRelativeTo(this);
        payslipDialog.setLayout(new BorderLayout());

        // Create payslip content (simplified version for employees)
        JTextArea payslipArea = new JTextArea();
        payslipArea.setFont(new Font("Courier New", Font.PLAIN, 12));
        payslipArea.setEditable(false);
        payslipArea.setBackground(Color.WHITE);

        StringBuilder content = new StringBuilder();
        content.append("═══════════════════════════════════════════════════════════════════\n");
        content.append("                          MOTORPH CORPORATION                      \n");
        content.append("                         EMPLOYEE PAYSLIP                          \n");
        content.append("═══════════════════════════════════════════════════════════════════\n\n");

        content.append("Employee: ").append(employeeData.getFullName()).append("\n");
        content.append("ID: ").append(employeeData.getEmployeeId()).append("\n");
        content.append("Position: ").append(employeeData.getPosition()).append("\n");
        content.append("Pay Period: ").append(monthName).append(" ").append(year).append(" - ").append(periodName).append("\n");
        content.append("Pay Date: ").append(PayrollDateManager.formatDate(payPeriod.getPayDate())).append("\n\n");

        content.append("EARNINGS:\n");
        content.append("Basic Pay:              ₱").append(String.format("%,10.2f", grossPay)).append("\n");
        content.append("Rice Subsidy:           ₱").append(String.format("%,10.2f", riceSubsidy)).append("\n");
        content.append("Phone Allowance:        ₱").append(String.format("%,10.2f", phoneAllowance)).append("\n");
        content.append("Clothing Allowance:     ₱").append(String.format("%,10.2f", clothingAllowance)).append("\n");
        content.append("                        ─────────────\n");
        content.append("Gross Pay:              ₱").append(String.format("%,10.2f", totalGrossPay)).append("\n\n");

        content.append("DEDUCTIONS:\n");
        content.append("SSS:                    ₱").append(String.format("%,10.2f", deductions.sssDeduction)).append("\n");
        content.append("PhilHealth:             ₱").append(String.format("%,10.2f", deductions.philhealthDeduction)).append("\n");
        content.append("Pag-IBIG:               ₱").append(String.format("%,10.2f", deductions.pagibigDeduction)).append("\n");
        content.append("Withholding Tax:        ₱").append(String.format("%,10.2f", deductions.withholdingTax)).append("\n");
        content.append("                        ─────────────\n");
        content.append("Total Deductions:       ₱").append(String.format("%,10.2f", deductions.totalDeductions)).append("\n\n");

        content.append("NET PAY:                ₱").append(String.format("%,10.2f", netPay)).append("\n");
        content.append("═══════════════════════════════════════════════════════════════════\n");

        payslipArea.setText(content.toString());

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton downloadButton = createStyledButton("💾 Download", new Color(52, 152, 219));
        JButton closeButton = createStyledButton("✖️ Close", new Color(149, 165, 166));

        downloadButton.addActionListener(e -> downloadEmployeePayslip(content.toString(), year, monthName, periodName));
        closeButton.addActionListener(e -> payslipDialog.dispose());

        buttonPanel.add(downloadButton);
        buttonPanel.add(closeButton);

        payslipDialog.add(new JScrollPane(payslipArea), BorderLayout.CENTER);
        payslipDialog.add(buttonPanel, BorderLayout.SOUTH);
        payslipDialog.setVisible(true);
    }

    void downloadEmployeePayslip(String content, int year, String monthName, String periodName) {
        try {
            String fileName = "MyPayslip_" + employeeData.getEmployeeId() + "_" + monthName + "_" + year + ".txt";
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File(fileName));

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (!file.getName().endsWith(".txt")) {
                    file = new File(file.getAbsolutePath() + ".txt");
                }

                try (PrintWriter writer = new PrintWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
                    writer.print(content);
                }

                JOptionPane.showMessageDialog(this, "Payslip saved successfully!", "Download Complete", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    void viewProfile() {
        JOptionPane.showMessageDialog(this,
                "📋 Complete Profile View\n\n" +
                        "This would show your complete employee profile including:\n" +
                        "• Personal information\n" +
                        "• Emergency contacts\n" +
                        "• Employment history\n" +
                        "• Benefits information\n" +
                        "• Tax information",
                "Profile View",
                JOptionPane.INFORMATION_MESSAGE);
    }

    void changePassword() {
        new PasswordChangeDialog(credential, new EmployeeCredentialsManager());
    }

    void showHelp() {
        JOptionPane.showMessageDialog(this,
                "📞 Help & Support\n\n" +
                        "For assistance, please contact:\n\n" +
                        "🏢 HR Department:\n" +
                        "   Phone: (02) 8123-4567\n" +
                        "   Email: hr@motorph.com\n\n" +
                        "💻 IT Support:\n" +
                        "   Phone: (02) 8123-4568\n" +
                        "   Email: it@motorph.com\n\n" +
                        "⏰ Office Hours: Monday - Friday, 8:00 AM - 5:00 PM",
                "Help & Support",
                JOptionPane.INFORMATION_MESSAGE);
    }

    void handleLogout() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            new EmployeeLoginForm();
        }
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.setOpaque(true);
        button.setBorderPainted(true);
        button.setContentAreaFilled(true);

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
}
