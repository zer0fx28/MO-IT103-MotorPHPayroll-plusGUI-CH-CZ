// File: motorph/input/PayrollInputManager.java
package motorph.input;

import motorph.employee.Employee;
import motorph.employee.EmployeeDataReader;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Handles all user input for the payroll system
 */
public class PayrollInputManager {
    private final Scanner scanner;
    private final EmployeeDataReader employeeDataReader;
    private final DateTimeFormatter dateFormatter;

    /**
     * Constructor to initialize the input manager
     */
    public PayrollInputManager(Scanner scanner, EmployeeDataReader employeeDataReader) {
        this.scanner = scanner;
        this.employeeDataReader = employeeDataReader;
        this.dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    }

    /**
     * Get menu choice from user
     * @return The selected option as a string
     */
    public String getMenuChoice() {
        return scanner.nextLine().trim();
    }

    /**
     * Get employee by searching with name or ID
     * @return Found employee or null
     */
    public Employee findEmployee() {
        System.out.print("Enter Employee Full Name or ID: ");
        String searchTerm = scanner.nextLine().trim();

        Employee employee = employeeDataReader.findEmployee(searchTerm);

        if (employee == null) {
            System.out.println("Employee not found. Please try again.");
        }

        return employee;
    }

    /**
     * Get employee by ID for detailed view
     * @return Found employee or null
     */
    public Employee getEmployeeById() {
        System.out.print("\nEnter Employee ID to view details: ");
        String empId = scanner.nextLine().trim();

        Employee employee = employeeDataReader.getEmployee(empId);

        if (employee == null) {
            System.out.println("Employee not found.");
        }

        return employee;
    }

    /**
     * Get date range from user for reports
     * @return Array with [startDate, endDate] or null if input was invalid
     */
    public LocalDate[] getDateRange() {
        System.out.println("\nEnter date range:");

        LocalDate startDate = null;
        while (startDate == null) {
            System.out.print("From (MM/DD/YYYY): ");
            try {
                startDate = LocalDate.parse(scanner.nextLine().trim(), dateFormatter);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use MM/DD/YYYY.");
            }
        }

        LocalDate endDate = null;
        while (endDate == null) {
            System.out.print("To (MM/DD/YYYY): ");
            try {
                endDate = LocalDate.parse(scanner.nextLine().trim(), dateFormatter);

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
     * @param prompt The question to display
     * @return true if confirmed (Y), false otherwise
     */
    public boolean getConfirmation(String prompt) {
        System.out.print(prompt);
        String confirmation = scanner.nextLine().trim().toUpperCase();
        return confirmation.equals("Y");
    }

    /**
     * Determine pay period type based on end date
     * @param endDate The end date of the period
     * @return 1 for first half, 2 for second half of month
     */
    public int determinePayPeriodType(LocalDate endDate) {
        // If day is > 15, assume second half
        if (endDate.getDayOfMonth() > 15) {
            return 2; // Second half
        }
        return 1; // First half
    }

    /**
     * Wait for user to press Enter to continue
     */
    public void waitForEnter() {
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }
}