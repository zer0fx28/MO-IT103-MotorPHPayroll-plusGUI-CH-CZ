// File: motorph/input/PayrollInputManager.java
package motorph.input;

import motorph.employee.Employee;
import motorph.employee.EmployeeDataReader;
import motorph.process.PayrollDateManager;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Handles all user input for the payroll system
 *
 * This class is responsible for getting and validating user inputs
 * for various operations like finding employees, selecting dates,
 * and choosing payroll options.
 */
public class PayrollInputManager {
    private final Scanner scanner;
    private final EmployeeDataReader employeeDataReader;
    private final DateTimeFormatter dateFormatter;

    /**
     * Constructor to initialize the input manager
     *
     * @param scanner Scanner for user input
     * @param employeeDataReader EmployeeDataReader for employee lookup
     */
    public PayrollInputManager(Scanner scanner, EmployeeDataReader employeeDataReader) {
        this.scanner = scanner;
        this.employeeDataReader = employeeDataReader;
        this.dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    }

    /**
     * Get menu choice from user
     *
     * @return The selected option as a string
     */
    public String getMenuChoice() {
        return scanner.nextLine().trim();
    }

    /**
     * Get employee by searching with name or ID
     *
     * @return Found employee or null
     */
    public Employee findEmployee() {
        System.out.print("Enter Employee Full Name or ID: ");
        String searchTerm = scanner.nextLine().trim();

        if (searchTerm.isEmpty()) {
            System.out.println("No search term entered. Please try again.");
            return null;
        }

        Employee employee = employeeDataReader.findEmployee(searchTerm);

        if (employee == null) {
            System.out.println("Employee not found. Please try again.");
        }

        return employee;
    }

    /**
     * Get employee by ID for detailed view
     *
     * @return Found employee or null
     */
    public Employee getEmployeeById() {
        System.out.print("\nEnter Employee ID to view details: ");
        String empId = scanner.nextLine().trim();

        if (empId.isEmpty()) {
            System.out.println("No employee ID entered. Please try again.");
            return null;
        }

        Employee employee = employeeDataReader.getEmployee(empId);

        if (employee == null) {
            System.out.println("Employee not found.");
        }

        return employee;
    }

    /**
     * Get date range from user for reports
     *
     * @return Array with [startDate, endDate] or null if input was invalid
     */
    public LocalDate[] getDateRange() {
        System.out.println("\nEnter date range:");

        LocalDate startDate = null;
        int startAttempts = 0;
        final int MAX_ATTEMPTS = 3;

        while (startDate == null && startAttempts < MAX_ATTEMPTS) {
            System.out.print("From (MM/DD/YYYY): ");
            try {
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    System.out.println("No date entered. Please try again.");
                    startAttempts++;
                    continue;
                }

                startDate = LocalDate.parse(input, dateFormatter);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use MM/DD/YYYY.");
                startAttempts++;
            }
        }

        if (startDate == null) {
            System.out.println("Too many invalid attempts. Using current date.");
            startDate = LocalDate.now();
        }

        LocalDate endDate = null;
        int endAttempts = 0;

        while (endDate == null && endAttempts < MAX_ATTEMPTS) {
            System.out.print("To (MM/DD/YYYY): ");
            try {
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    System.out.println("No date entered. Please try again.");
                    endAttempts++;
                    continue;
                }

                endDate = LocalDate.parse(input, dateFormatter);

                // Make sure end date is after start date
                if (endDate.isBefore(startDate)) {
                    System.out.println("End date must be after start date.");
                    endDate = null;
                    endAttempts++;
                }
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use MM/DD/YYYY.");
                endAttempts++;
            }
        }

        if (endDate == null) {
            System.out.println("Too many invalid attempts. Using current date.");
            endDate = LocalDate.now();

            // Ensure end date is not before start date
            if (endDate.isBefore(startDate)) {
                endDate = startDate;
            }
        }

        return new LocalDate[] {startDate, endDate};
    }

    /**
     * Get confirmation from user (Y/N)
     *
     * @param prompt The question to display
     * @return true if confirmed (Y), false otherwise
     */
    public boolean getConfirmation(String prompt) {
        System.out.print(prompt);
        String confirmation = scanner.nextLine().trim().toUpperCase();
        return confirmation.equals("Y") || confirmation.equals("YES");
    }

    /**
     * Get year from user
     *
     * @return Year selected by user
     */
    public int getYear() {
        System.out.print("Enter Year (YYYY): ");
        try {
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("No year entered. Using current year.");
                return LocalDate.now().getYear();
            }

            int year = Integer.parseInt(input);
            if (year < 2000 || year > 2100) { // Basic validation
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
     * Get month from user
     *
     * @return Month selected by user (1-12)
     */
    public int getMonth() {
        System.out.print("Enter Month (1-12): ");
        try {
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("No month entered. Using current month.");
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
     * Get pay period type from user
     *
     * @return PayrollDateManager.MID_MONTH or PayrollDateManager.END_MONTH
     */
    public int getPayPeriodType() {
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
     * Get cutoff date range based on year, month, and pay period type
     *
     * @param year Year
     * @param month Month (1-12)
     * @param payPeriodType PayrollDateManager.MID_MONTH or PayrollDateManager.END_MONTH
     * @return Array with [startDate, endDate] for the cutoff period
     */
    public LocalDate[] getCutoffDateRange(int year, int month, int payPeriodType) {
        // Validate inputs
        if (month < 1 || month > 12) {
            System.out.println("Invalid month. Using current month.");
            month = LocalDate.now().getMonthValue();
        }

        if (payPeriodType != PayrollDateManager.MID_MONTH && payPeriodType != PayrollDateManager.END_MONTH) {
            System.out.println("Invalid pay period type. Using mid-month.");
            payPeriodType = PayrollDateManager.MID_MONTH;
        }

        LocalDate payrollDate = PayrollDateManager.getPayrollDate(year, month, payPeriodType);
        return PayrollDateManager.getCutoffDateRange(payrollDate, payPeriodType);
    }

    /**
     * Wait for user to press Enter to continue
     */
    public void waitForEnter() {
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }
}