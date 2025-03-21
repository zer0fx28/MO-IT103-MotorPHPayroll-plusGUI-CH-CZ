package motorph.ui;

import java.time.LocalDate;
import java.util.Scanner;
import motorph.Main;
import motorph.test.StatutoryDeductionsTest;
import motorph.test.PayrollProcessingTest;
import motorph.test.WorkHoursCalculatorTest;
import motorph.reports.WeeklyHoursReport;
import motorph.holidays.HolidayPayReport;
import motorph.util.DateTimeUtil;

/**
 * Main menu system for the MotorPH Payroll System
 * Handles navigation between different modules
 */
public class MainMenu {
    private Scanner scanner;

    // File paths for data files
    private static final String EMPLOYEE_FILE_PATH = "resources/MotorPH Employee Data - Employee Details.csv";
    private static final String ATTENDANCE_FILE_PATH = "resources/MotorPH Employee Data - Attendance Record.csv";

    /**
     * Constructor
     */
    public MainMenu() {
        this.scanner = new Scanner(System.in);
    }

    /**
     * Display and handle the main menu
     */
    public void displayMainMenu() {
        boolean exitSystem = false;

        while (!exitSystem) {
            displayHeader();

            System.out.println("MAIN MENU:");
            System.out.println("1. Run Payroll System");
            System.out.println("2. Run System Tests");
            System.out.println("3. Run Reports");
            System.out.println("4. Exit");
            System.out.print("Enter your choice (1-4): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    runPayrollSystem();
                    break;
                case "2":
                    runSystemTests();
                    break;
                case "3":
                    runReports();
                    break;
                case "4":
                    exitSystem = true;
                    System.out.println("Thank you for using MotorPH Payroll System. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid selection. Please try again.");
                    break;
            }
        }
    }

    /**
     * Display header for the application
     */
    private void displayHeader() {
        System.out.println("\n==============================");
        System.out.println("  MotorPH Payroll System");
        System.out.println("==============================");
    }

    /**
     * Run the main payroll system
     */
    private void runPayrollSystem() {
        System.out.println("\nStarting Payroll System...");

        // Call the Main class's runPayrollSystem method
        Main.runPayrollSystem();
    }

    /**
     * Run system tests
     */
    private void runSystemTests() {
        boolean returnToMain = false;

        while (!returnToMain) {
            System.out.println("\nSYSTEM TESTS MENU:");
            System.out.println("1. Statutory Deductions Tests");
            System.out.println("2. Payroll Processing Tests");
            System.out.println("3. Work Hours Calculator Tests");
            System.out.println("4. Return to Main Menu");
            System.out.print("Enter your choice (1-4): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    runStatutoryDeductionsTests();
                    break;
                case "2":
                    runPayrollProcessingTests();
                    break;
                case "3":
                    runWorkHoursCalculatorTests();
                    break;
                case "4":
                    returnToMain = true;
                    break;
                default:
                    System.out.println("Invalid selection. Please try again.");
                    break;
            }

            if (!returnToMain) {
                System.out.print("\nPress Enter to continue...");
                scanner.nextLine();
            }
        }
    }

    /**
     * Run statutory deductions tests
     */
    private void runStatutoryDeductionsTests() {
        System.out.println("\n--- Statutory Deductions Tests ---");
        StatutoryDeductionsTest test = new StatutoryDeductionsTest();
        test.runTests();
    }

    /**
     * Run payroll processing tests
     */
    private void runPayrollProcessingTests() {
        System.out.println("\n--- Payroll Processing Tests ---");
        PayrollProcessingTest test = new PayrollProcessingTest(EMPLOYEE_FILE_PATH, ATTENDANCE_FILE_PATH);
        test.runTests();
    }

    /**
     * Run work hours calculator tests
     */
    private void runWorkHoursCalculatorTests() {
        System.out.println("\n--- Work Hours Calculator Tests ---");
        WorkHoursCalculatorTest test = new WorkHoursCalculatorTest();
        test.runTests();
    }

    /**
     * Run reports menu
     */
    private void runReports() {
        boolean returnToMain = false;

        while (!returnToMain) {
            System.out.println("\nREPORTS MENU:");
            System.out.println("1. Weekly Hours Report");
            System.out.println("2. Holiday Pay Report");
            System.out.println("3. Return to Main Menu");
            System.out.print("Enter your choice (1-3): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    runWeeklyHoursReport();
                    break;
                case "2":
                    runHolidayPayReport();
                    break;
                case "3":
                    returnToMain = true;
                    break;
                default:
                    System.out.println("Invalid selection. Please try again.");
                    break;
            }

            if (!returnToMain) {
                System.out.print("\nPress Enter to continue...");
                scanner.nextLine();
            }
        }
    }

    /**
     * Run weekly hours report
     */
    private void runWeeklyHoursReport() {
        System.out.println("\n--- Weekly Hours Report ---");

        System.out.print("Enter Employee ID: ");
        String employeeId = scanner.nextLine().trim();

        System.out.print("Enter Start Date (MM/DD/YYYY): ");
        String startDateStr = scanner.nextLine().trim();

        System.out.print("Enter End Date (MM/DD/YYYY): ");
        String endDateStr = scanner.nextLine().trim();

        try {
            LocalDate startDate = DateTimeUtil.parseDate(startDateStr);
            LocalDate endDate = DateTimeUtil.parseDate(endDateStr);

            if (startDate != null && endDate != null) {
                WeeklyHoursReport report = new WeeklyHoursReport(EMPLOYEE_FILE_PATH, ATTENDANCE_FILE_PATH);
                report.generateReportForEmployee(employeeId, startDate, endDate);
            } else {
                System.out.println("Invalid date format. Please use MM/DD/YYYY.");
            }
        } catch (Exception e) {
            System.out.println("Error generating report: " + e.getMessage());
        }
    }

    /**
     * Run holiday pay report
     */
    private void runHolidayPayReport() {
        System.out.println("\n--- Holiday Pay Report ---");

        System.out.print("Enter Year: ");
        String yearStr = scanner.nextLine().trim();

        System.out.print("Enter Month (1-12): ");
        String monthStr = scanner.nextLine().trim();

        try {
            int year = Integer.parseInt(yearStr);
            int month = Integer.parseInt(monthStr);

            HolidayPayReport report = new HolidayPayReport(EMPLOYEE_FILE_PATH, ATTENDANCE_FILE_PATH);
            report.generateReport(year, month);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format. Please enter valid numbers.");
        } catch (Exception e) {
            System.out.println("Error generating report: " + e.getMessage());
        }
    }
}