// File: motorph/ui/PayrollUI.java
package motorph.ui;

import motorph.employee.Employee;
import motorph.hours.AttendanceReader;
import motorph.output.PayrollOutputManager;
import motorph.process.PayrollDateManager;
import motorph.employee.EmployeeDataReader;
import motorph.process.PayrollProcessor; // <-- FIXED: Removed extra "essor"
import motorph.process.PayrollProcessor.PayrollResult;
import motorph.util.DateTimeUtil;
import motorph.util.InputValidator;

import java.time.LocalDate;
import java.util.Map;
import java.util.Scanner;

/**
 * Handles user interface for payroll processing
 * Manages interaction with the user for payroll operations
 */
public class PayrollUI {
    // Input/output components
    private final Scanner scanner;
    private final EmployeeDataReader employeeDataReader; // <-- FIXED: Lowercase variable name
    private final AttendanceReader attendanceReader;
    private final PayrollProcessor payrollProcessor;
    private final PayrollOutputManager outputManager;

    /**
     * Create a new PayrollUI
     *
     * @param employeeDataReader Reader for employee data
     * @param attendanceReader Reader for attendance data
     * @param payrollProcessor Processor for payroll calculations
     * @param outputManager Manager for payroll output
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
     * Process the payroll for an employee
     */
    public void processPayroll() {
        System.out.println("\n===== PROCESS PAYROLL =====");

        // Get payroll period information
        int year = getYear();
        int month = getMonth();
        int payrollType = getPayrollType();

        // Get payroll date and cutoff period
        LocalDate payrollDate = PayrollDateManager.getPayrollDate(year, month, payrollType);
        LocalDate[] cutoffDates = PayrollDateManager.getCutoffDateRange(payrollDate, payrollType);
        LocalDate startDate = cutoffDates[0];
        LocalDate endDate = cutoffDates[1];

        // Display payroll information
        System.out.println("\nPayroll Information:");
        System.out.println("Year: " + year);
        System.out.println("Month: " + PayrollDateManager.getMonthName(month));
        System.out.println("Payroll Type: " + (payrollType == PayrollDateManager.MID_MONTH ? "Mid-month" : "End-month"));
        System.out.println("Payroll Date: " + DateTimeUtil.formatDate(payrollDate));
        System.out.println("Cutoff Period: " + PayrollDateManager.getFormattedDateRange(startDate, endDate));

        // Get employee to process
        System.out.print("\nEnter Employee Full Name or ID: ");
        Employee employee = getEmployeeInput();

        if (employee == null) {
            return;
        }

        // Get attendance summary
        Map<String, Object> attendanceSummary = attendanceReader.getAttendanceSummary(
                employee.getEmployeeId(), startDate, endDate);

        if (attendanceSummary == null || (int)attendanceSummary.getOrDefault("recordCount", 0) == 0) {
            System.out.println("No attendance records found for this period.");
            return;
        }

        // Display attendance summary
        outputManager.displayAttendanceSummary(employee, attendanceSummary, startDate, endDate);

        // Get confirmation
        if (!getConfirmation("\nAre these records accurate? (Y/N): ")) {
            System.out.println("Please check the attendance file and try again.");
            return;
        }

        System.out.println("\nCalculating salary...");

        // Extract attendance data
        double totalHours = (double) attendanceSummary.get("hours");
        double overtimeHours = (double) attendanceSummary.get("overtimeHours");
        double lateMinutes = (double) attendanceSummary.get("lateMinutes");
        double undertimeMinutes = (double) attendanceSummary.get("undertimeMinutes");
        boolean isLateAnyDay = (boolean) attendanceSummary.get("isLateAnyDay");
        boolean hasUnpaidAbsences = (boolean) attendanceSummary.get("hasUnpaidAbsences");

        // Process payroll
        PayrollResult result = payrollProcessor.processPayroll(
                employee, totalHours, overtimeHours, lateMinutes, undertimeMinutes,
                isLateAnyDay, payrollType, startDate, endDate, year, month, hasUnpaidAbsences
        );

        // Display results
        displayPayrollResult(result);

        // Final confirmation
        if (getConfirmation("\nConfirm payroll for processing (Y/N): ")) {
            System.out.println("\nPayroll confirmed and processed.");
            System.out.println("Payment will be issued on " + DateTimeUtil.formatDate(payrollDate) + ".");
        } else {
            System.out.println("\nPayroll processing canceled.");
        }
    }

    /**
     * Display the result of payroll processing
     *
     * @param result The payroll result to display
     */
    private void displayPayrollResult(PayrollResult result) {
        if (result == null) {
            System.out.println("Error: No payroll result available.");
            return;
        }

        payrollProcessor.displaySalaryDetails(result.employee);
    }

    /**
     * Get employee by name or ID from user input
     *
     * @return Employee object or null if not found
     */
    private Employee getEmployeeInput() {
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
     * Get confirmation from user (Y/N)
     *
     * @param message The message to display
     * @return true if confirmed, false otherwise
     */
    private boolean getConfirmation(String message) {
        System.out.print(message);
        String response = scanner.nextLine().trim().toUpperCase();
        return response.equals("Y") || response.equals("YES");
    }

    /**
     * Get year from user
     *
     * @return Valid year
     */
    private int getYear() {
        while (true) {
            System.out.print("Enter Year (YYYY): ");
            try {
                int year = Integer.parseInt(scanner.nextLine().trim());
                if (InputValidator.isValidYear(year)) {
                    return year;
                } else {
                    System.out.println("Invalid year. Please enter a year between 2020 and 2050.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid year. Please enter a number.");
            }
        }
    }

    /**
     * Get month from user
     *
     * @return Valid month (1-12)
     */
    private int getMonth() {
        while (true) {
            System.out.print("Enter Month (1-12): ");
            try {
                int month = Integer.parseInt(scanner.nextLine().trim());
                if (InputValidator.isValidMonth(month)) {
                    return month;
                } else {
                    System.out.println("Invalid month. Please enter a month between 1 and 12.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid month. Please enter a number.");
            }
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

        while (true) {
            System.out.print("Enter choice (1-2): ");
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                if (choice == 1 || choice == 2) {
                    return choice;
                } else {
                    System.out.println("Invalid choice. Please enter 1 or 2.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid choice. Please enter a number.");
            }
        }
    }

    /**
     * View payroll dates for a specific month and year
     */
    public void viewPayrollDates() {
        System.out.println("\n===== PAYROLL DATES =====");

        // Get year and month
        int year = getYear();
        int month = getMonth();

        // Calculate payroll dates
        LocalDate midMonth = PayrollDateManager.getPayrollDate(year, month, PayrollDateManager.MID_MONTH);
        LocalDate endMonth = PayrollDateManager.getPayrollDate(year, month, PayrollDateManager.END_MONTH);

        // Get cutoff periods
        LocalDate[] midCutoff = PayrollDateManager.getCutoffDateRange(midMonth, PayrollDateManager.MID_MONTH);
        LocalDate[] endCutoff = PayrollDateManager.getCutoffDateRange(endMonth, PayrollDateManager.END_MONTH);

        // Display information
        System.out.println("\nPayroll Dates for " + PayrollDateManager.getMonthName(month) + " " + year);

        System.out.println("\nMid-month:");
        System.out.println("  Payroll Date: " + DateTimeUtil.formatDate(midMonth));
        System.out.println("  Cutoff Period: " + DateTimeUtil.formatDate(midCutoff[0]) +
                " to " + DateTimeUtil.formatDate(midCutoff[1]));
        System.out.println("  Deductions: SSS, PhilHealth, Pag-IBIG");

        System.out.println("\nEnd-month:");
        System.out.println("  Payroll Date: " + DateTimeUtil.formatDate(endMonth));
        System.out.println("  Cutoff Period: " + DateTimeUtil.formatDate(endCutoff[0]) +
                " to " + DateTimeUtil.formatDate(endCutoff[1]));
        System.out.println("  Deductions: Withholding Tax");

        // Wait for user
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }

    /**
     * Find and display employee information
     */
    public void findEmployee() {
        System.out.println("\n===== FIND EMPLOYEE =====");

        System.out.print("Enter Employee Full Name or ID: ");
        Employee employee = getEmployeeInput();

        if (employee == null) {
            return;
        }

        // Display employee details
        System.out.println("\n===== EMPLOYEE DETAILS =====");
        System.out.println("ID: " + employee.getEmployeeId());
        System.out.println("Name: " + employee.getFullName());
        System.out.println("Position: " + employee.getPosition());
        System.out.println("Basic Salary: ₱" + String.format("%,.2f", employee.getBasicSalary()));
        System.out.println("Rice Subsidy: ₱" + String.format("%,.2f", employee.getRiceSubsidy()));
        System.out.println("Phone Allowance: ₱" + String.format("%,.2f", employee.getPhoneAllowance()));
        System.out.println("Clothing Allowance: ₱" + String.format("%,.2f", employee.getClothingAllowance()));
        System.out.println("Hourly Rate: ₱" + String.format("%.2f", employee.getHourlyRate()));

        // Show options
        System.out.println("\nOptions:");
        System.out.println("1. View Attendance");
        System.out.println("2. Process Payroll");
        System.out.println("3. Return to Main Menu");

        while (true) {
            System.out.print("Enter choice: ");
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());

                switch (choice) {
                    case 1:
                        viewEmployeeAttendance(employee);
                        return;
                    case 2:
                        processEmployeePayroll(employee);
                        return;
                    case 3:
                        return;
                    default:
                        System.out.println("Invalid choice. Please enter 1, 2, or 3.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid choice. Please enter a number.");
            }
        }
    }

    /**
     * View attendance for a specific employee
     *
     * @param employee The employee to view attendance for
     */
    private void viewEmployeeAttendance(Employee employee) {
        System.out.println("\n===== ATTENDANCE OPTIONS =====");
        System.out.println("Employee: " + employee.getFullName());
        System.out.println("\nSelect view type:");
        System.out.println("1. Daily Attendance");
        System.out.println("2. Weekly Summary");

        // Get view type
        int viewType;
        while (true) {
            System.out.print("Enter choice (1-2): ");
            try {
                viewType = Integer.parseInt(scanner.nextLine().trim());
                if (viewType == 1 || viewType == 2) {
                    break;
                } else {
                    System.out.println("Invalid choice. Please enter 1 or 2.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid choice. Please enter a number.");
            }
        }

        // Get date range
        LocalDate startDate = null;
        LocalDate endDate = null;

        while (startDate == null) {
            System.out.print("\nStart Date (MM/DD/YYYY): ");
            try {
                startDate = DateTimeUtil.parseDate(scanner.nextLine().trim());
                if (startDate == null) {
                    System.out.println("Invalid date format. Please use MM/DD/YYYY.");
                }
            } catch (Exception e) {
                System.out.println("Invalid date. Please try again.");
            }
        }

        while (endDate == null) {
            System.out.print("End Date (MM/DD/YYYY): ");
            try {
                endDate = DateTimeUtil.parseDate(scanner.nextLine().trim());
                if (endDate == null) {
                    System.out.println("Invalid date format. Please use MM/DD/YYYY.");
                    continue;
                }

                if (endDate.isBefore(startDate)) {
                    System.out.println("End date must be after start date.");
                    endDate = null;
                }
            } catch (Exception e) {
                System.out.println("Invalid date. Please try again.");
            }
        }

        // Display attendance
        if (viewType == 1) {
            outputManager.displayDailyAttendance(employee, startDate, endDate);
        } else {
            outputManager.displayWeeklyAttendance(employee, startDate, endDate);
        }
    }

    /**
     * Process payroll for a specific employee
     *
     * @param employee The employee to process payroll for
     */
    private void processEmployeePayroll(Employee employee) {
        System.out.println("\n===== PROCESS PAYROLL =====");

        // Get year and month
        int year = getYear();
        int month = getMonth();

        // Get payroll type
        int payrollType = getPayrollType();

        // Get cutoff dates
        LocalDate payrollDate = PayrollDateManager.getPayrollDate(year, month, payrollType);
        LocalDate[] cutoffDates = PayrollDateManager.getCutoffDateRange(payrollDate, payrollType);
        LocalDate startDate = cutoffDates[0];
        LocalDate endDate = cutoffDates[1];

        // Display payroll information
        System.out.println("\nPayroll Information:");
        System.out.println("Employee: " + employee.getFullName());
        System.out.println("Period: " + DateTimeUtil.formatDate(startDate) +
                " to " + DateTimeUtil.formatDate(endDate));
        System.out.println("Payroll Date: " + DateTimeUtil.formatDate(payrollDate));

        // Get attendance summary
        Map<String, Object> attendanceSummary = attendanceReader.getAttendanceSummary(
                employee.getEmployeeId(), startDate, endDate);

        if (attendanceSummary == null || (int)attendanceSummary.getOrDefault("recordCount", 0) == 0) {
            System.out.println("\nNo attendance records found for this period.");
            return;
        }

        // Display attendance summary
        outputManager.displayAttendanceSummary(employee, attendanceSummary, startDate, endDate);

        // Get confirmation
        if (!getConfirmation("\nAre these records accurate? (Y/N): ")) {
            System.out.println("Please check the attendance file and try again.");
            return;
        }

        System.out.println("\nCalculating salary...");

        // Extract attendance data
        double totalHours = (double) attendanceSummary.get("hours");
        double overtimeHours = (double) attendanceSummary.get("overtimeHours");
        double lateMinutes = (double) attendanceSummary.get("lateMinutes");
        double undertimeMinutes = (double) attendanceSummary.get("undertimeMinutes");
        boolean isLateAnyDay = (boolean) attendanceSummary.get("isLateAnyDay");
        boolean hasUnpaidAbsences = (boolean) attendanceSummary.get("hasUnpaidAbsences");

        // Process payroll
        PayrollResult result = payrollProcessor.processPayroll(
                employee, totalHours, overtimeHours, lateMinutes, undertimeMinutes,
                isLateAnyDay, payrollType, startDate, endDate, year, month, hasUnpaidAbsences
        );

        // Display results
        displayPayrollResult(result);

        // Final confirmation
        if (getConfirmation("\nConfirm payroll for processing (Y/N): ")) {
            System.out.println("\nPayroll confirmed and processed.");
            System.out.println("Payment will be issued on " + DateTimeUtil.formatDate(payrollDate) + ".");
        } else {
            System.out.println("\nPayroll processing canceled.");
        }
    }
}