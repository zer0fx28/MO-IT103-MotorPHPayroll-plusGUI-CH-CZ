package motorph;

import motorph.employee.Employee;
import motorph.employee.EmployeeDataReader;
import motorph.hours.AttendanceReader;
import motorph.input.PayrollInputManager;
import motorph.output.PayrollOutputManager;
import motorph.process.PayrollProcessor;
import motorph.ui.MainMenu;

import java.time.LocalDate;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class for the MotorPH Payroll System
 * Entry point for the application
 */
public class Main {
    // Logger for error handling
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    /**
     * Main method - application entry point
     */
    public static void main(String... args) {
        // Create MainMenu and display it
        try {
            MainMenu mainMenu = new MainMenu();
            mainMenu.displayMainMenu();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unhandled exception in main application", e);
        }
    }

    /**
     * Run the main payroll system
     * This method is called from the MainMenu
     */
    public static void runPayrollSystem() {
        // File paths
        String employeeFilePath = "resources/MotorPH Employee Data - Employee Details.csv";
        String attendanceFilePath = "resources/MotorPH Employee Data - Attendance Record.csv";

        try (Scanner scanner = new Scanner(System.in)) {
            // Initialize components
            EmployeeDataReader employeeDataReader = new EmployeeDataReader(employeeFilePath);
            AttendanceReader attendanceReader = new AttendanceReader(attendanceFilePath);
            PayrollProcessor payrollProcessor = new PayrollProcessor(employeeFilePath, attendanceFilePath);

            // Managers
            PayrollOutputManager outputManager = new PayrollOutputManager(scanner, attendanceReader, payrollProcessor);
            PayrollInputManager inputManager = new PayrollInputManager(scanner, employeeDataReader);

            // Run main application
            runApplication(inputManager, outputManager, payrollProcessor);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unhandled exception in payroll system", e);
        }
    }

    /**
     * Run the main application
     */
    private static void runApplication(
            PayrollInputManager inputManager,
            PayrollOutputManager outputManager,
            PayrollProcessor payrollProcessor) {
        while (true) {
            displayMainMenu();
            String choice = inputManager.getMenuChoice();

            switch (choice) {
                case "1":
                    processPayroll(inputManager, outputManager, payrollProcessor);
                    break;
                case "2":
                    findEmployee(inputManager, outputManager, payrollProcessor);
                    break;
                case "3":
                    viewPayrollCalendar(inputManager, outputManager);
                    break;
                case "4":
                    System.out.println("Thank you for using MotorPH Payroll System. Goodbye!");
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    /**
     * Display main menu options
     */
    private static void displayMainMenu() {
        System.out.println("\nPAYROLL SYSTEM MENU:");
        System.out.println("1. Process Payroll");
        System.out.println("2. Find Employee");
        System.out.println("3. View Payroll Calendar");
        System.out.println("4. Exit");
        System.out.print("Enter choice (1-4): ");
    }

    /**
     * Process payroll workflow
     */
    private static void processPayroll(
            PayrollInputManager inputManager,
            PayrollOutputManager outputManager,
            PayrollProcessor payrollProcessor) {
        // Get payroll details
        int year = inputManager.getYear();
        int month = inputManager.getMonth();
        int payrollType = inputManager.getPayPeriodType();

        // Get cutoff dates
        LocalDate[] cutoffDates = inputManager.getCutoffDateRange(year, month, payrollType);
        LocalDate startDate = cutoffDates[0];
        LocalDate endDate = cutoffDates[1];

        // Get employee
        Employee employee = inputManager.findEmployee();
        if (employee == null) return;

        // Display attendance summary
        Map<String, Object> attendanceSummary = outputManager.displayPayrollSummary(
                employee, startDate, endDate, payrollType);
        if (attendanceSummary == null) return;

        // Confirm records
        if (!inputManager.getConfirmation("\nAre these records accurate? (Y/N): ")) {
            System.out.println("Please double check the attendance file and try again.");
            return;
        }

        // Process payroll details
        processPayrollDetails(
                inputManager,
                outputManager,
                payrollProcessor,
                employee,
                attendanceSummary,
                startDate,
                endDate,
                year,
                month,
                payrollType
        );
    }

    /**
     * Process detailed payroll for an employee
     */
    private static void processPayrollDetails(
            PayrollInputManager inputManager,
            PayrollOutputManager outputManager,
            PayrollProcessor payrollProcessor,
            Employee employee,
            Map<String, Object> attendanceSummary,
            LocalDate startDate,
            LocalDate endDate,
            int year,
            int month,
            int payrollType) {
        // Extract attendance data
        double totalHours = (double) attendanceSummary.get("hours");
        double overtimeHours = (double) attendanceSummary.get("overtimeHours");
        double lateMinutes = (double) attendanceSummary.get("lateMinutes");
        double undertimeMinutes = (double) attendanceSummary.get("undertimeMinutes");
        boolean isLateAnyDay = (boolean) attendanceSummary.get("isLateAnyDay");
        boolean hasUnpaidAbsences = (boolean) attendanceSummary.get("hasUnpaidAbsences");

        // Process payroll
        payrollProcessor.processPayroll(
                employee, totalHours, overtimeHours, lateMinutes, undertimeMinutes,
                isLateAnyDay, payrollType, startDate, endDate, year, month, hasUnpaidAbsences);

        // Display salary details
        outputManager.displaySalaryDetails(employee);

        // Final confirmation
        if (inputManager.getConfirmation("\nConfirm payroll for processing (Y/N): ")) {
            System.out.println("\nPayroll confirmed and processed.");
            System.out.println("Payment will be issued according to the company schedule.");
        } else {
            System.out.println("\nPayroll processing canceled.");
        }
    }

    /**
     * Find employee workflow
     */
    private static void findEmployee(
            PayrollInputManager inputManager,
            PayrollOutputManager outputManager,
            PayrollProcessor payrollProcessor) {
        Employee employee = inputManager.findEmployee();
        if (employee == null) return;

        outputManager.displayEmployeeDetails(employee);
        String choice = inputManager.getMenuChoice();

        switch (choice) {
            case "1":
                checkEmployeeAttendance(inputManager, outputManager, employee);
                break;
            case "2":
                processPayroll(inputManager, outputManager, payrollProcessor);
                break;
            case "3":
                // Return to main menu
                break;
            default:
                System.out.println("Invalid option. Returning to main menu.");
        }
    }

    /**
     * Check employee attendance
     */
    private static void checkEmployeeAttendance(
            PayrollInputManager inputManager,
            PayrollOutputManager outputManager,
            Employee employee) {
        outputManager.displayAttendanceOptions(employee);
        String viewType = inputManager.getMenuChoice();

        LocalDate[] dateRange = inputManager.getDateRange();
        if (dateRange == null) return;

        if (viewType.equals("1")) {
            outputManager.displayDailyAttendance(employee, dateRange[0], dateRange[1]);
        } else {
            outputManager.displayWeeklyAttendance(employee, dateRange[0], dateRange[1]);
        }
    }

    /**
     * View payroll calendar
     */
    private static void viewPayrollCalendar(
            PayrollInputManager inputManager,
            PayrollOutputManager outputManager) {
        int year = inputManager.getYear();
        int month = inputManager.getMonth();

        outputManager.displayPayrollCalendar(year, month);
        inputManager.waitForEnter();
    }
}