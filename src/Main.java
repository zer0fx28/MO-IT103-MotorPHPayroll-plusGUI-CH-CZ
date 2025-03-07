// File: motorph/output/Main.java
package motorph.output;

import motorph.employee.Employee;
import motorph.employee.EmployeeDataReader;
import motorph.hours.AttendanceReader;
import motorph.input.PayrollInputManager;
import motorph.process.PayrollProcessor;

import java.time.LocalDate;
import java.util.*;

/**
 * Main class for the MotorPH Payroll System
 */
public class Main {
    // For user input
    private static Scanner scanner;

    // Manager classes
    private static PayrollOutputManager outputManager;
    private static PayrollInputManager inputManager;

    // Data and processing classes
    private static EmployeeDataReader employeeDataReader;
    private static AttendanceReader attendanceReader;
    private static PayrollProcessor payrollProcessor;

    public static void main(String[] args) {
        System.out.println("===== MOTORPH PAYROLL SYSTEM =====");

        // File paths
        String employeeFilePath = "resources/MotorPH Employee Data - Employee Details.csv";
        String attendanceFilePath = "resources/MotorPH Employee Data - Attendance Record.csv";

        try {
            // Initialize scanner
            scanner = new Scanner(System.in);

            // Load data
            System.out.println("Loading data...");
            employeeDataReader = new EmployeeDataReader(employeeFilePath);
            attendanceReader = new AttendanceReader(attendanceFilePath);
            payrollProcessor = new PayrollProcessor(employeeFilePath, attendanceFilePath);

            // Initialize managers
            outputManager = new PayrollOutputManager(scanner, attendanceReader, payrollProcessor);
            inputManager = new PayrollInputManager(scanner, employeeDataReader);

            System.out.println("Data loaded successfully!");

            // Main menu
            boolean exit = false;
            while (!exit) {
                outputManager.displayMainMenu();
                String choice = inputManager.getMenuChoice();

                switch (choice) {
                    case "1":
                        findEmployee();
                        break;
                    case "2":
                        showAllEmployeeDetails();
                        break;
                    case "3":
                        processPayroll();
                        break;
                    case "4":
                        exit = true;
                        System.out.println("Thank you for using MotorPH Payroll System. Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            }
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Close scanner when done
            if (scanner != null) {
                scanner.close();
            }
        }
    }

    /**
     * Find and show employee details
     */
    private static void findEmployee() {
        System.out.println("\n===== FIND EMPLOYEE =====");

        // Find the employee
        Employee employee = inputManager.findEmployee();
        if (employee == null) {
            return;
        }

        // Show employee details
        outputManager.displayEmployeeDetails(employee);
        String choice = inputManager.getMenuChoice();

        if (choice.equals("1")) {
            checkEmployeeAttendance(employee);
        }
        // Any other choice returns to main menu
    }

    /**
     * Check attendance for an employee
     */
    private static void checkEmployeeAttendance(Employee employee) {
        // Show attendance options
        outputManager.displayAttendanceOptions(employee);
        String viewType = inputManager.getMenuChoice();

        // Get date range
        LocalDate[] dateRange = inputManager.getDateRange();
        if (dateRange == null) {
            return;
        }

        // Show attendance based on view type
        if (viewType.equals("1")) {
            // Daily view
            outputManager.displayDailyAttendance(employee, dateRange[0], dateRange[1]);
        } else {
            // Weekly view
            outputManager.displayWeeklyAttendance(employee, dateRange[0], dateRange[1]);
        }
    }

    /**
     * Show all employee details
     */
    private static void showAllEmployeeDetails() {
        // Get all employees
        List<Employee> employees = employeeDataReader.getAllEmployees();

        // Display employee summary
        outputManager.displayAllEmployeesSummary(employees);
        String choice = inputManager.getMenuChoice();

        if (choice.equals("1")) {
            Employee employee = inputManager.getEmployeeById();
            if (employee != null) {
                System.out.println("\n===== DETAILED EMPLOYEE INFO =====");
                System.out.println(employee);
                inputManager.waitForEnter();
            }
        }
        // Any other choice returns to main menu
    }

    /**
     * Process payroll for an employee
     */
    private static void processPayroll() {
        System.out.println("\n===== PROCESS PAYROLL =====");

        // Get pay period
        LocalDate[] dateRange = inputManager.getDateRange();
        if (dateRange == null) {
            return;
        }

        LocalDate startDate = dateRange[0];
        LocalDate endDate = dateRange[1];

        // Get employee to process
        System.out.print("\n");
        Employee employee = inputManager.findEmployee();
        if (employee == null) {
            return;
        }

        // Display payroll summary and get totals
        Map<String, Double> totals = outputManager.displayPayrollSummary(employee, startDate, endDate);
        if (totals == null) {
            return;
        }

        // Confirm accuracy
        boolean confirmed = inputManager.getConfirmation("\nAre these records accurate? (Y/N): ");
        if (!confirmed) {
            System.out.println("Please double check the attendance file and try again.");
            return;
        }

        // Determine pay period type
        int payPeriodType = inputManager.determinePayPeriodType(endDate);

        System.out.println("\nCalculating salary...");

        // Extract values from totals
        double totalHours = totals.get("hours");
        double totalOvertimeHours = totals.get("overtimeHours");
        double totalLateMinutes = totals.get("lateMinutes");
        double totalUndertimeMinutes = totals.get("undertimeMinutes");
        boolean isLateAnyDay = totals.get("isLateAnyDay") > 0;

        // Process payroll with the calculated totals and apply policies
        payrollProcessor.processPayrollForPeriod(employee, totalHours, totalOvertimeHours,
                totalLateMinutes, totalUndertimeMinutes, isLateAnyDay, payPeriodType, startDate, endDate);

        // Display salary details
        outputManager.displaySalaryDetails(employee);

        // Final confirmation
        confirmed = inputManager.getConfirmation("\nConfirm payroll for processing (Y/N): ");
        if (confirmed) {
            System.out.println("\nPayroll confirmed and processed.");
            System.out.println("Payment will be issued according to the company schedule.");
        } else {
            System.out.println("\nPayroll processing canceled.");
        }
    }
}