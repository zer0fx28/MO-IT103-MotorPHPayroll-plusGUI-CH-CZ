// File: motorph/ui/PayrollUI.java
package motorph.ui;

import motorph.employee.Employee;
import motorph.employee.EmployeeDataReader;
import motorph.hours.AttendanceReader;
import motorph.output.PayrollOutputManager;
import motorph.process.PayrollDateManager;
import motorph.process.PayrollProcessor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Scanner;

/**
 * Handles user interface for payroll processing
 *
 * This class provides a text-based user interface for the payroll system,
 * handling user interactions and delegating operations to the appropriate
 * components.
 */
public class PayrollUI {
    private final Scanner scanner;
    private final EmployeeDataReader employeeDataReader;
    private final AttendanceReader attendanceReader;
    private final PayrollProcessor payrollProcessor;
    private final PayrollOutputManager outputManager;
    private final DateTimeFormatter dateFormatter;

    /**
     * Create the payroll UI
     *
     * @param employeeDataReader EmployeeDataReader for employee data
     * @param attendanceReader AttendanceReader for attendance data
     * @param payrollProcessor PayrollProcessor for calculations
     * @param outputManager PayrollOutputManager for display
     * @param scanner Scanner for user input
     */
    public PayrollUI(EmployeeDataReader employeeDataReader,
                     AttendanceReader attendanceReader,
                     PayrollProcessor payrollProcessor,
                     PayrollOutputManager outputManager,
                     Scanner scanner) {
        this.employeeDataReader = employeeDataReader;
        this.attendanceReader = attendanceReader;
        this.payrollProcessor = payrollProcessor;
        this.outputManager = outputManager;
        this.scanner = scanner;
        this.dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    }

    /**
     * Show the main menu
     */
    public void showMainMenu() {
        System.out.println("\nMAIN MENU:");
        System.out.println("1. Process Payroll");
        System.out.println("2. Find Employee");
        System.out.println("3. View Payroll Dates");
        System.out.println("4. Exit");
        System.out.print("Enter choice (1-4): ");
    }

    /**
     * Process the payroll
     */
    public void processPayroll() {
        System.out.println("\n===== PROCESS PAYROLL =====");

        // Get year and month
        int year = getCurrentYear();
        int month = getCurrentMonth();

        // Get payroll type
        int payrollType = getPayrollType();

        // Get payroll date
        LocalDate payrollDate = PayrollDateManager.getPayrollDate(year, month, payrollType);

        // Get cutoff dates
        LocalDate[] cutoffDates = PayrollDateManager.getCutoffDateRange(payrollDate, payrollType);
        LocalDate startDate = cutoffDates[0];
        LocalDate endDate = cutoffDates[1];

        // Display payroll info
        displayPayrollInfo(year, month, payrollType, payrollDate, startDate, endDate);

        // Get employee to process
        System.out.print("\nEnter Employee Full Name or ID: ");
        Employee employee = findEmployee();

        if (employee == null) {
            return;
        }

        // Get attendance summary
        Map<String, Object> attendanceSummary = outputManager.displayPayrollSummary(
                employee, startDate, endDate, payrollType);

        if (attendanceSummary == null) {
            System.out.println("No attendance records found for this period.");
            return;
        }

        // Get confirmation
        if (!getConfirmation("\nAre these records accurate? (Y/N): ")) {
            System.out.println("Please check the attendance file and try again.");
            return;
        }

        // Process payroll
        processEmployeePayroll(employee, attendanceSummary, payrollType, startDate, endDate,
                year, month, payrollDate);
    }

    /**
     * Display payroll information
     */
    private void displayPayrollInfo(int year, int month, int payrollType,
                                    LocalDate payrollDate, LocalDate startDate, LocalDate endDate) {
        System.out.println("\nPayroll Information:");
        System.out.println("Year: " + year);
        System.out.println("Month: " + PayrollDateManager.getMonthName(month));
        System.out.println("Payroll Type: " + (payrollType == PayrollDateManager.MID_MONTH ? "Mid-month" : "End-month"));
        System.out.println("Payroll Date: " + PayrollDateManager.formatDate(payrollDate));
        System.out.println("Cutoff Period: " + PayrollDateManager.getFormattedDateRange(startDate, endDate));
    }

    /**
     * Process payroll for an employee
     */
    private void processEmployeePayroll(Employee employee, Map<String, Object> attendanceSummary,
                                        int payrollType, LocalDate startDate, LocalDate endDate,
                                        int year, int month, LocalDate payrollDate) {
        System.out.println("\nCalculating salary...");

        // Extract attendance data
        double totalHours = (double) attendanceSummary.get("hours");
        double overtimeHours = (double) attendanceSummary.get("overtimeHours");
        double lateMinutes = (double) attendanceSummary.get("lateMinutes");
        double undertimeMinutes = (double) attendanceSummary.get("undertimeMinutes");
        boolean isLateAnyDay = (boolean) attendanceSummary.get("isLateAnyDay");
        boolean hasUnpaidAbsences = false;

        if (attendanceSummary.containsKey("hasUnpaidAbsences")) {
            hasUnpaidAbsences = (boolean) attendanceSummary.get("hasUnpaidAbsences");
        }

        // Process payroll
        payrollProcessor.processPayrollForPeriod(
                employee, totalHours, overtimeHours, lateMinutes, undertimeMinutes,
                isLateAnyDay, payrollType, startDate, endDate, year, month, hasUnpaidAbsences
        );

        // Display results
        outputManager.displaySalaryDetails(employee);

        // Final confirmation
        if (getConfirmation("\nConfirm payroll for processing (Y/N): ")) {
            System.out.println("\nPayroll confirmed and processed.");
            System.out.println("Payment will be issued on " + PayrollDateManager.formatDate(payrollDate) + ".");
        } else {
            System.out.println("\nPayroll processing canceled.");
        }
    }

    /**
     * View payroll dates for a year and month
     */
    public void viewPayrollDates() {
        System.out.println("\n===== PAYROLL DATES =====");

        // Get year and month
        int year = getCurrentYear();
        int month = getCurrentMonth();

        // Calculate payroll dates
        LocalDate midMonth = PayrollDateManager.getPayrollDate(year, month, PayrollDateManager.MID_MONTH);
        LocalDate endMonth = PayrollDateManager.getPayrollDate(year, month, PayrollDateManager.END_MONTH);

        // Get cutoff periods
        LocalDate[] midCutoff = PayrollDateManager.getCutoffDateRange(midMonth, PayrollDateManager.MID_MONTH);
        LocalDate[] endCutoff = PayrollDateManager.getCutoffDateRange(endMonth, PayrollDateManager.END_MONTH);

        // Display information
        System.out.println("\nPayroll Dates for " + PayrollDateManager.getMonthName(month) + " " + year);

        System.out.println("\nMid-month:");
        System.out.println("  Payroll Date: " + PayrollDateManager.formatDate(midMonth));
        System.out.println("  Cutoff Period: " + PayrollDateManager.getFormattedDateRange(midCutoff[0], midCutoff[1]));
        System.out.println("  Deductions: SSS, PhilHealth, Pag-IBIG");

        System.out.println("\nEnd-month:");
        System.out.println("  Payroll Date: " + PayrollDateManager.formatDate(endMonth));
        System.out.println("  Cutoff Period: " + PayrollDateManager.getFormattedDateRange(endCutoff[0], endCutoff[1]));
        System.out.println("  Deductions: Withholding Tax");

        // Wait for user
        waitForEnter();
    }

    /**
     * Find an employee by name or ID
     *
     * @return Employee object if found, null otherwise
     */
    public Employee findEmployee() {
        String searchTerm = scanner.nextLine().trim();

        if (searchTerm.isEmpty()) {
            System.out.println("No search term entered.");
            return null;
        }

        Employee employee = employeeDataReader.findEmployee(searchTerm);

        if (employee == null) {
            System.out.println("Employee not found. Please try again.");
        }

        return employee;
    }

    /**
     * Get confirmation from user
     *
     * @param message Confirmation message to display
     * @return true if confirmed, false otherwise
     */
    private boolean getConfirmation(String message) {
        System.out.print(message);
        String response = scanner.nextLine().trim().toUpperCase();
        return response.equals("Y") || response.equals("YES");
    }

    /**
     * Get the current year or let user enter a year
     *
     * @return Selected year
     */
    private int getCurrentYear() {
        System.out.print("Enter Year (YYYY): ");
        try {
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return LocalDate.now().getYear();
            }

            int year = Integer.parseInt(input);
            if (year < 2000 || year > 2100) {
                System.out.println("Year must be between 2000 and 2100. Using current year.");
                return LocalDate.now().getYear();
            }
            return year;
        } catch (NumberFormatException e) {
            System.out.println("Invalid year. Using current year.");
            return LocalDate.now().getYear();
        }
    }

    /**
     * Get the current month or let user enter a month
     *
     * @return Selected month (1-12)
     */
    private int getCurrentMonth() {
        System.out.print("Enter Month (1-12): ");
        try {
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return LocalDate.now().getMonthValue();
            }

            int month = Integer.parseInt(input);
            if (month < 1 || month > 12) {
                System.out.println("Month must be between 1 and 12. Using current month.");
                return LocalDate.now().getMonthValue();
            }
            return month;
        } catch (NumberFormatException e) {
            System.out.println("Invalid month. Using current month.");
            return LocalDate.now().getMonthValue();
        }
    }

    /**
     * Get payroll type from user
     *
     * @return PayrollDateManager.MID_MONTH or PayrollDateManager.END_MONTH
     */
    private int getPayrollType() {
        System.out.println("\nSelect payroll type:");
        System.out.println("1. Mid-month (15th)");
        System.out.println("2. End-month");
        System.out.print("Enter choice (1-2): ");

        try {
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("No choice entered. Using Mid-month.");
                return PayrollDateManager.MID_MONTH;
            }

            int choice = Integer.parseInt(input);
            if (choice == 1) {
                return PayrollDateManager.MID_MONTH;
            } else if (choice == 2) {
                return PayrollDateManager.END_MONTH;
            } else {
                System.out.println("Invalid choice. Using Mid-month.");
                return PayrollDateManager.MID_MONTH;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid choice. Using Mid-month.");
            return PayrollDateManager.MID_MONTH;
        }
    }

    /**
     * Wait for user to press Enter
     */
    private void waitForEnter() {
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }
}