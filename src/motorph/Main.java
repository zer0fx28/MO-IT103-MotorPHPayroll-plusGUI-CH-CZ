package motorph;

import motorph.employee.Employee;
import motorph.employee.EmployeeDataReader;
import motorph.holidays.HolidayManager;
import motorph.hours.AttendanceReader;
import motorph.input.PayrollInputManager;
import motorph.output.PayrollOutputManager;
import motorph.process.PayrollProcessor;

import java.time.LocalDate;
import java.util.Map;
import java.util.Scanner;

/**
 * Main class for the MotorPH Payroll System
 * Entry point for the application
 */
public class Main {
    /**
     * Main method - application entry point
     *
     * @param args Command-line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("===== MOTORPH PAYROLL SYSTEM =====");

        // Define file paths
        String employeeFilePath = "resources/MotorPH Employee Data - Employee Details.csv";
        String attendanceFilePath = "resources/MotorPH Employee Data - Attendance Record.csv";

        try (Scanner scanner = new Scanner(System.in)) {
            // Load data and initialize components
            System.out.println("Loading data...");

            // Initialize readers
            EmployeeDataReader employeeDataReader = new EmployeeDataReader(employeeFilePath);
            AttendanceReader attendanceReader = new AttendanceReader(attendanceFilePath);
            HolidayManager holidayManager = new HolidayManager();

            // Initialize processor
            PayrollProcessor payrollProcessor = new PayrollProcessor(employeeFilePath, attendanceFilePath);

            // Initialize UI managers
            PayrollOutputManager outputManager = new PayrollOutputManager(scanner, attendanceReader, payrollProcessor);
            PayrollInputManager inputManager = new PayrollInputManager(scanner, employeeDataReader);

            System.out.println("Data loaded successfully!");

            // Main menu loop
            boolean exit = false;
            while (!exit) {
                // Display menu
                System.out.println("\nMAIN MENU:");
                System.out.println("1. Process Payroll");
                System.out.println("2. Find Employee");
                System.out.println("3. View Payroll Calendar");
                System.out.println("4. Exit");
                System.out.print("Enter choice (1-4): ");

                String choice = scanner.nextLine().trim();

                // Process choice
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
        }
    }

    /**
     * Process payroll for an employee
     */
    private static void processPayroll(
            PayrollInputManager inputManager,
            PayrollOutputManager outputManager,
            PayrollProcessor payrollProcessor) {
        System.out.println("\n===== PROCESS PAYROLL =====");

        // Get year and month
        int year = inputManager.getYear();
        int month = inputManager.getMonth();

        // Get payroll type
        int payrollType = inputManager.getPayPeriodType();

        // Get cutoff dates
        LocalDate[] cutoffDates = inputManager.getCutoffDateRange(year, month, payrollType);
        LocalDate startDate = cutoffDates[0];
        LocalDate endDate = cutoffDates[1];

        // Get employee to process
        System.out.print("\n");
        Employee employee = inputManager.findEmployee();
        if (employee == null) {
            return;
        }

        // Display attendance summary and get attendance data
        Map<String, Object> attendanceSummary = outputManager.displayPayrollSummary(
                employee, startDate, endDate, payrollType);

        if (attendanceSummary == null) {
            return;
        }

        // Get confirmation
        if (!inputManager.getConfirmation("\nAre these records accurate? (Y/N): ")) {
            System.out.println("Please double check the attendance file and try again.");
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
     * Find and show employee details
     */
    private static void findEmployee(
            PayrollInputManager inputManager,
            PayrollOutputManager outputManager,
            PayrollProcessor payrollProcessor) {
        System.out.println("\n===== FIND EMPLOYEE =====");

        Employee employee = inputManager.findEmployee();
        if (employee == null) {
            return;
        }

        outputManager.displayEmployeeDetails(employee);
        String choice = inputManager.getMenuChoice();

        switch (choice) {
            case "1":
                checkEmployeeAttendance(inputManager, outputManager, employee);
                break;
            case "2":
                processEmployeePayroll(inputManager, outputManager, payrollProcessor, employee);
                break;
            case "3":
                // Return to main menu
                break;
            default:
                System.out.println("Invalid option. Returning to main menu.");
                break;
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
        if (dateRange == null) {
            return;
        }

        if (viewType.equals("1")) {
            // Daily view
            outputManager.displayDailyAttendance(employee, dateRange[0], dateRange[1]);
        } else {
            // Weekly view
            outputManager.displayWeeklyAttendance(employee, dateRange[0], dateRange[1]);
        }
    }

    /**
     * Process payroll for a specific employee
     */
    private static void processEmployeePayroll(
            PayrollInputManager inputManager,
            PayrollOutputManager outputManager,
            PayrollProcessor payrollProcessor,
            Employee employee) {
        // Get year and month
        int year = inputManager.getYear();
        int month = inputManager.getMonth();

        // Get payroll type
        int payPeriodType = inputManager.getPayPeriodType();

        // Get cutoff dates
        LocalDate[] cutoffDates = inputManager.getCutoffDateRange(year, month, payPeriodType);
        LocalDate startDate = cutoffDates[0];
        LocalDate endDate = cutoffDates[1];

        // Process this employee
        Map<String, Object> attendanceSummary = outputManager.displayPayrollSummary(
                employee, startDate, endDate, payPeriodType);

        if (attendanceSummary == null) {
            return;
        }

        // Get confirmation
        if (!inputManager.getConfirmation("\nAre these records accurate? (Y/N): ")) {
            System.out.println("Please double check the attendance file and try again.");
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
        payrollProcessor.processPayroll(
                employee, totalHours, overtimeHours, lateMinutes, undertimeMinutes,
                isLateAnyDay, payPeriodType, startDate, endDate, year, month, hasUnpaidAbsences);

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
