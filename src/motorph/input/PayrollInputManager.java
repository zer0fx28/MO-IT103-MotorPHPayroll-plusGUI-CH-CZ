// File: motorph/input/PayrollInputManager.java
package motorph.input;

import motorph.employee.Employee;
import motorph.employee.EmployeeDataReader;  // <-- ADDED THIS MISSING IMPORT
import motorph.process.PayrollDateManager;
import motorph.util.DateTimeUtil;
import motorph.util.InputValidator;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Handles user input for the payroll system
 * Manages all user interaction and input validation
 */
public class PayrollInputManager {
    // Scanner for reading user input
    private final Scanner scanner;

    // Employee data reader for finding employees
    private final EmployeeDataReader employeeDataReader;

    /**
     * Create a new PayrollInputManager
     *
     * @param scanner Scanner for reading user input
     * @param employeeDataReader Reader for employee data
     */
    public PayrollInputManager(Scanner scanner, EmployeeDataReader employeeDataReader) {
        this.scanner = scanner;
        this.employeeDataReader = employeeDataReader;
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
     * Get date range from user for reports
     *
     * @return Array with [startDate, endDate] or null if input was invalid
     */
    public LocalDate[] getDateRange() {
        System.out.println("\nEnter date range:");

        LocalDate startDate = null;
        while (startDate == null) {
            System.out.print("From (MM/DD/YYYY): ");
            String dateStr = scanner.nextLine().trim();

            try {
                startDate = DateTimeUtil.parseDate(dateStr);

                // Validate the date
                if (startDate == null || !InputValidator.isValidDate(startDate)) {
                    System.out.println("Invalid date. Please enter a valid date.");
                    startDate = null;
                }
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use MM/DD/YYYY.");
            }
        }

        LocalDate endDate = null;
        while (endDate == null) {
            System.out.print("To (MM/DD/YYYY): ");
            String dateStr = scanner.nextLine().trim();

            try {
                endDate = DateTimeUtil.parseDate(dateStr);

                // Validate the date
                if (endDate == null || !InputValidator.isValidDate(endDate)) {
                    System.out.println("Invalid date. Please enter a valid date.");
                    endDate = null;
                    continue;
                }

                // Make sure end date is after start date
                if (endDate.isBefore(startDate)) {
                    System.out.println("End date must be after start date.");
                    endDate = null;
                }
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use MM/DD/YYYY.");
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
        while (true) {
            System.out.print(prompt);
            String response = scanner.nextLine().trim();

            if (InputValidator.isValidYesNoResponse(response)) {
                return InputValidator.isAffirmative(response);
            } else {
                System.out.println("Please enter Y (Yes) or N (No).");
            }
        }
    }

    /**
     * Get year from user
     *
     * @return Valid year entered by user
     */
    public int getYear() {
        int year;

        while (true) {
            System.out.print("Enter Year (YYYY): ");

            try {
                String input = scanner.nextLine().trim();
                year = Integer.parseInt(input);

                if (InputValidator.isValidYear(year)) {
                    return year;
                } else {
                    System.out.println("Invalid year. Please enter a year between 2020 and 2050.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid year.");
            }
        }
    }

    /**
     * Get month from user
     *
     * @return Valid month (1-12) entered by user
     */
    public int getMonth() {
        int month;

        while (true) {
            System.out.print("Enter Month (1-12): ");

            try {
                String input = scanner.nextLine().trim();
                month = Integer.parseInt(input);

                if (InputValidator.isValidMonth(month)) {
                    return month;
                } else {
                    System.out.println("Invalid month. Please enter a month between 1 and 12.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid month.");
            }
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

        while (true) {
            System.out.print("Enter choice (1-2): ");
            String input = scanner.nextLine().trim();

            if (InputValidator.isValidMenuChoice(input, 1, 2)) {
                int choice = Integer.parseInt(input);

                if (choice == 1) {
                    return PayrollDateManager.MID_MONTH;
                } else {
                    return PayrollDateManager.END_MONTH;
                }
            } else {
                System.out.println("Invalid choice. Please enter 1 or 2.");
            }
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

    /**
     * Get a number from user within a specified range
     *
     * @param prompt Prompt to display
     * @param min Minimum valid value
     * @param max Maximum valid value
     * @return Valid number entered by user
     */
    public int getNumberInRange(String prompt, int min, int max) {
        int number;

        while (true) {
            System.out.print(prompt);

            try {
                String input = scanner.nextLine().trim();
                number = Integer.parseInt(input);

                if (number >= min && number <= max) {
                    return number;
                } else {
                    System.out.println("Please enter a number between " + min + " and " + max + ".");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
    }
}