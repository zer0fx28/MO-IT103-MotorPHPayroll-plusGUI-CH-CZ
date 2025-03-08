// File: motorph/output/Main.java
package motorph.output;

import motorph.employee.Employee;
import motorph.employee.EmployeeDataReader;
import motorph.holidays.HolidayManager;
import motorph.hours.AttendanceReader;
import motorph.input.PayrollInputManager;
import motorph.process.PayrollDateManager;
import motorph.process.PayrollProcessor;

import java.time.LocalDate;
import java.util.Map;
import java.util.Scanner;

/**
 * Main class for the MotorPH Payroll System
 */
public class Main {
    // For user input
    private static Scanner scanner;

    // System components
    private static EmployeeDataReader employeeDataReader;
    private static AttendanceReader attendanceReader;
    private static PayrollProcessor payrollProcessor;
    private static PayrollOutputManager outputManager;
    private static PayrollInputManager inputManager;
    private static HolidayManager holidayManager;

    public static void main(String[] args) {
        System.out.println("===== MOTORPH PAYROLL SYSTEM =====");

        // File paths
        String employeeFilePath = "resources/MotorPH Employee Data - Employee Details.csv";
        String attendanceFilePath = "resources/MotorPH Employee Data - Attendance Record.csv";

        try {
            // Initialize scanner
            scanner = new Scanner(System.in);

            // Load data and initialize components
            System.out.println("Loading data...");
            employeeDataReader = new EmployeeDataReader(employeeFilePath);
            attendanceReader = new AttendanceReader(attendanceFilePath);
            holidayManager = new HolidayManager();
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
                        processPayroll();
                        break;
                    case "2":
                        findEmployee();
                        break;
                    case "3":
                        viewPayrollCalendar();
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
     * Process payroll for an employee
     */
    private static void processPayroll() {
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

        // Process payroll
        payrollProcessor.processPayrollForPeriod(
                employee, totalHours, overtimeHours, lateMinutes, undertimeMinutes,
                isLateAnyDay, payrollType, startDate, endDate, year, month);

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
    private static void findEmployee() {
        System.out.println("\n===== FIND EMPLOYEE =====");

        Employee employee = inputManager.findEmployee();
        if (employee == null) {
            return;
        }

        outputManager.displayEmployeeDetails(employee);
        String choice = inputManager.getMenuChoice();

        switch (choice) {
            case "1":
                checkEmployeeAttendance(employee);
                break;
            case "2":
                // Go directly to process payroll for this employee
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

                // Process this employee
                Map<String, Object> attendanceSummary = outputManager.displayPayrollSummary(
                        employee, startDate, endDate, payrollType);

                if (attendanceSummary != null) {
                    processEmployeePayroll(employee, attendanceSummary, payrollType, startDate, endDate, year, month);
                }
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
    private static void checkEmployeeAttendance(Employee employee) {
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
    private static void processEmployeePayroll(Employee employee, Map<String, Object> attendanceSummary,
                                               int payrollType, LocalDate startDate, LocalDate endDate,
                                               int year, int month) {
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

        // Process payroll
        payrollProcessor.processPayrollForPeriod(
                employee, totalHours, overtimeHours, lateMinutes, undertimeMinutes,
                isLateAnyDay, payrollType, startDate, endDate, year, month);

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
    private static void viewPayrollCalendar() {
        int year = inputManager.getYear();
        int month = inputManager.getMonth();

        outputManager.displayPayrollCalendar(year, month);
        inputManager.waitForEnter();
    }
}