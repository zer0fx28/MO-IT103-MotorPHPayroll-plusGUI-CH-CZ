// File: motorph/output/Main.java
package motorph.output;

import motorph.employee.Employee;
import motorph.employee.EmployeeDataReader;
import motorph.hours.AttendanceReader;
import motorph.process.PayrollProcessor;
import motorph.util.TimeConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Main class for the MotorPH Payroll System
 */
public class Main {
    // Scanner for user input
    private static Scanner scanner = new Scanner(System.in);

    // Classes to read data and process payroll
    private static EmployeeDataReader employeeDataReader;
    private static AttendanceReader attendanceReader;
    private static PayrollProcessor payrollProcessor;

    // Date formatter for display and input
    private static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public static void main(String[] args) {
        System.out.println("===== MOTORPH PAYROLL SYSTEM =====");

        // File paths for data
        String employeeFilePath = "resources/MotorPH Employee Data - Employee Details.csv";
        String attendanceFilePath = "resources/MotorPH Employee Data - Attendance Record.csv";

        try {
            // Load employee and attendance data
            System.out.println("Loading data...");
            employeeDataReader = new EmployeeDataReader(employeeFilePath);
            attendanceReader = new AttendanceReader(attendanceFilePath);
            payrollProcessor = new PayrollProcessor(employeeFilePath, attendanceFilePath);
            System.out.println("Data loaded successfully!");

            // Main menu loop
            boolean exit = false;
            while (!exit) {
                displayMainMenu();
                String choice = scanner.nextLine();

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
            // Close the scanner when done
            if (scanner != null) {
                scanner.close();
            }
        }
    }

    /**
     * Display the main menu options
     */
    private static void displayMainMenu() {
        System.out.println("\nMAIN MENU:");
        System.out.println("1. Find Employee");
        System.out.println("2. Show All Employee Details");
        System.out.println("3. Process Payroll");
        System.out.println("4. Exit");
        System.out.print("Enter choice (1-4): ");
    }

    /**
     * Find and display employee details with attendance options
     */
    private static void findEmployee() {
        System.out.println("\n===== FIND EMPLOYEE =====");

        // Get employee ID or name from user
        System.out.print("Enter Employee Full Name or ID: ");
        String searchTerm = scanner.nextLine().trim();

        // Find the employee
        Employee employee = employeeDataReader.findEmployee(searchTerm);

        if (employee == null) {
            System.out.println("Employee not found. Please try again.");
            return;
        }

        // Display employee details
        System.out.println("\n===== EMPLOYEE DETAILS =====");
        System.out.println(employee);

        // Options after viewing employee
        System.out.println("\nOptions:");
        System.out.println("1. Check Employee Attendance");
        System.out.println("2. Back to Main Menu");
        System.out.print("Enter choice (1-2): ");

        String choice = scanner.nextLine().trim();

        if (choice.equals("1")) {
            checkEmployeeAttendance(employee);
        }
        // Any other choice returns to main menu
    }

    /**
     * Check and display employee attendance records
     */
    private static void checkEmployeeAttendance(Employee employee) {
        System.out.println("\n===== CHECK ATTENDANCE =====");
        System.out.println("Employee: " + employee.getFullName());

        // Choose attendance view type
        System.out.println("\nView attendance by:");
        System.out.println("1. Daily");
        System.out.println("2. Weekly");
        System.out.print("Enter choice (1-2): ");

        String viewType = scanner.nextLine().trim();

        // Get date range from user
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

        // Display attendance based on view type
        if (viewType.equals("1")) {
            // Daily view
            displayDailyAttendance(employee, startDate, endDate);
        } else {
            // Weekly view
            displayWeeklyAttendance(employee, startDate, endDate);
        }
    }

    /**
     * Display daily attendance for an employee
     */
    private static void displayDailyAttendance(Employee employee, LocalDate startDate, LocalDate endDate) {
        System.out.println("\n===== DAILY ATTENDANCE =====");
        System.out.println("Employee: " + employee.getFullName() + " (ID: " + employee.getEmployeeId() + ")");
        System.out.println("Period: " + startDate.format(dateFormatter) + " to " + endDate.format(dateFormatter));

        // Get attendance records
        Map<LocalDate, Map<String, Object>> dailyRecords = attendanceReader.getDailyAttendanceForEmployee(
                employee.getEmployeeId(), startDate, endDate);

        if (dailyRecords.isEmpty()) {
            System.out.println("\nNo attendance records found for this period.");
            return;
        }

        // Display headers
        System.out.println("\nDate\t\tDay\t\tTime In\t\tTime Out\tHours\tLate(min)\tOT(hrs)\tStatus");
        System.out.println("-------------------------------------------------------------------------------------");

        // Display each day's attendance
        double totalHours = 0;
        double totalLateMinutes = 0;
        double totalOvertimeHours = 0;

        for (Map.Entry<LocalDate, Map<String, Object>> entry : dailyRecords.entrySet()) {
            LocalDate date = entry.getKey();
            Map<String, Object> dailyData = entry.getValue();

            String dayOfWeek = date.getDayOfWeek().toString();
            dayOfWeek = dayOfWeek.charAt(0) + dayOfWeek.substring(1, 3).toLowerCase();

            String timeIn = (String) dailyData.get("timeIn");
            String timeOut = (String) dailyData.get("timeOut");
            double hours = (double) dailyData.get("hours");
            double lateMinutes = (double) dailyData.get("lateMinutes");
            double overtimeHours = (double) dailyData.get("overtimeHours");
            boolean isLate = (boolean) dailyData.get("isLate");

            String status = isLate ? "LATE" : "ON TIME";

            totalHours += hours;
            totalLateMinutes += lateMinutes;
            totalOvertimeHours += overtimeHours;

            System.out.printf("%s\t%s\t\t%s\t%s\t%.2f\t%.0f\t\t%.2f\t%s\n",
                    date.format(dateFormatter), dayOfWeek, timeIn, timeOut,
                    hours, lateMinutes, overtimeHours, status);
        }

        // Display totals
        System.out.println("-------------------------------------------------------------------------------------");
        System.out.printf("TOTALS:\t\t\t\t\t\t%.2f\t%.0f\t\t%.2f\n",
                totalHours, totalLateMinutes, totalOvertimeHours);

        // Wait for user to continue
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }

    /**
     * Display weekly attendance for an employee
     */
    private static void displayWeeklyAttendance(Employee employee, LocalDate startDate, LocalDate endDate) {
        System.out.println("\n===== WEEKLY ATTENDANCE =====");
        System.out.println("Employee: " + employee.getFullName() + " (ID: " + employee.getEmployeeId() + ")");
        System.out.println("Period: " + startDate.format(dateFormatter) + " to " + endDate.format(dateFormatter));

        // Get weekly attendance data with daily logs included
        Map<String, Object> attendanceData = attendanceReader.getWeeklyAttendanceWithDailyLogs(
                employee.getEmployeeId(), startDate, endDate);

        @SuppressWarnings("unchecked")
        Map<String, Map<String, Double>> weeklyRecords =
                (Map<String, Map<String, Double>>) attendanceData.get("weeklyRecords");

        @SuppressWarnings("unchecked")
        Map<String, List<Map<String, Object>>> dailyLogsByWeek =
                (Map<String, List<Map<String, Object>>>) attendanceData.get("dailyLogsByWeek");

        if (weeklyRecords.isEmpty()) {
            System.out.println("\nNo attendance records found for this period.");
            return;
        }

        // Display weekly data with daily logs
        double totalHours = 0;
        double totalLateMinutes = 0;
        double totalOvertimeHours = 0;

        for (Map.Entry<String, Map<String, Double>> entry : weeklyRecords.entrySet()) {
            String weekLabel = entry.getKey();
            Map<String, Double> weekData = entry.getValue();

            double hours = weekData.getOrDefault("hours", 0.0);
            double lateMinutes = weekData.getOrDefault("lateMinutes", 0.0);
            double overtimeHours = weekData.getOrDefault("overtimeHours", 0.0);

            totalHours += hours;
            totalLateMinutes += lateMinutes;
            totalOvertimeHours += overtimeHours;

            // Display week summary header
            System.out.println("\n" + weekLabel);
            System.out.println("Weekly Summary: " + String.format("%.2f", hours) + " hours, " +
                    String.format("%.0f", lateMinutes) + " min late, " +
                    String.format("%.2f", overtimeHours) + " OT hours");

            // Display daily logs header for this week
            System.out.println("\nDate\t\tDay\t\tTime In\t\tTime Out\tHours\tLate\tOT\tStatus");
            System.out.println("-----------------------------------------------------------------------------");

            // Get daily logs for this week
            List<Map<String, Object>> dailyLogs = dailyLogsByWeek.getOrDefault(weekLabel, new ArrayList<>());

            // Sort logs by date
            Collections.sort(dailyLogs, (a, b) -> {
                LocalDate dateA = (LocalDate) a.get("date");
                LocalDate dateB = (LocalDate) b.get("date");
                return dateA.compareTo(dateB);
            });

            // Display each day's log
            for (Map<String, Object> dayLog : dailyLogs) {
                LocalDate date = (LocalDate) dayLog.get("date");
                String dayOfWeek = date.getDayOfWeek().toString();
                dayOfWeek = dayOfWeek.charAt(0) + dayOfWeek.substring(1, 3).toLowerCase();

                String timeIn = (String) dayLog.get("timeIn");
                String timeOut = (String) dayLog.get("timeOut");
                double dayHours = (double) dayLog.get("hours");
                double dayLate = (double) dayLog.get("lateMinutes");
                double dayOT = (double) dayLog.get("overtimeHours");
                boolean isLate = (boolean) dayLog.get("isLate");

                String status = isLate ? "LATE" : "ON TIME";

                System.out.printf("%s\t%s\t\t%s\t%s\t%.2f\t%.0f\t%.2f\t%s\n",
                        date.format(dateFormatter), dayOfWeek, timeIn, timeOut,
                        dayHours, dayLate, dayOT, status);
            }
        }

        // Display totals
        System.out.println("\n=============================================================================");
        System.out.printf("TOTALS:\t\t\t\t\t\t\t%.2f\t%.0f\t%.2f\n",
                totalHours, totalLateMinutes, totalOvertimeHours);

        // Wait for user to continue
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }

    /**
     * Show all employee details
     */
    private static void showAllEmployeeDetails() {
        System.out.println("\n===== ALL EMPLOYEES =====");

        // Get all employees
        List<Employee> employees = employeeDataReader.getAllEmployees();

        if (employees.isEmpty()) {
            System.out.println("No employees found.");
            return;
        }

        // Display employee summary table
        System.out.println("\nID\tName\t\t\tPosition\t\tStatus");
        System.out.println("----------------------------------------------------------");

        for (Employee employee : employees) {
            String name = employee.getLastName() + ", " + employee.getFirstName();
            String displayName = name.length() > 20 ? name.substring(0, 17) + "..." : name;

            String position = employee.getPosition();
            String displayPosition = position.length() > 15 ? position.substring(0, 12) + "..." : position;

            System.out.printf("%-8s%-20s\t%-15s\t%s\n",
                    employee.getEmployeeId(), displayName, displayPosition, employee.getStatus());
        }

        // Option to view detailed info for a specific employee
        System.out.println("\nOptions:");
        System.out.println("1. View Detailed Info for an Employee");
        System.out.println("2. Back to Main Menu");
        System.out.print("Enter choice (1-2): ");

        String choice = scanner.nextLine().trim();

        if (choice.equals("1")) {
            System.out.print("\nEnter Employee ID to view details: ");
            String empId = scanner.nextLine().trim();

            Employee employee = employeeDataReader.getEmployee(empId);
            if (employee != null) {
                System.out.println("\n===== DETAILED EMPLOYEE INFO =====");
                System.out.println(employee);

                // Wait for user to continue
                System.out.print("\nPress Enter to continue...");
                scanner.nextLine();
            } else {
                System.out.println("Employee not found.");
            }
        }
        // Any other choice returns to main menu
    }

    /**
     * Process payroll for an employee
     */
    private static void processPayroll() {
        System.out.println("\n===== PROCESS PAYROLL =====");

        // Get pay period from user
        System.out.println("\nEnter Pay Period:");

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

        // Get employee to process
        System.out.print("\nEnter Employee Full Name or ID: ");
        String searchTerm = scanner.nextLine().trim();

        Employee employee = employeeDataReader.findEmployee(searchTerm);

        if (employee == null) {
            System.out.println("Employee not found. Please try again.");
            return;
        }

        // Display weekly attendance summary with daily logs
        System.out.println("\n===== ATTENDANCE SUMMARY =====");
        System.out.println("Employee: " + employee.getFullName() + " (ID: " + employee.getEmployeeId() + ")");
        System.out.println("Pay Period: " + startDate.format(dateFormatter) + " to " + endDate.format(dateFormatter));

        // Get weekly attendance data with daily logs
        Map<String, Object> attendanceData = attendanceReader.getWeeklyAttendanceWithDailyLogs(
                employee.getEmployeeId(), startDate, endDate);

        @SuppressWarnings("unchecked")
        Map<String, Map<String, Double>> weeklyRecords =
                (Map<String, Map<String, Double>>) attendanceData.get("weeklyRecords");

        @SuppressWarnings("unchecked")
        Map<String, List<Map<String, Object>>> dailyLogsByWeek =
                (Map<String, List<Map<String, Object>>>) attendanceData.get("dailyLogsByWeek");

        if (weeklyRecords.isEmpty()) {
            System.out.println("\nNo attendance records found for this period.");
            return;
        }

        // Display weekly data with daily logs - similar to displayWeeklyAttendance
        double totalHours = 0;
        double totalLateMinutes = 0;
        double totalOvertimeHours = 0;
        boolean isLateAnyDay = false;

        for (Map.Entry<String, Map<String, Double>> entry : weeklyRecords.entrySet()) {
            String weekLabel = entry.getKey();
            Map<String, Double> weekData = entry.getValue();

            double hours = weekData.getOrDefault("hours", 0.0);
            double lateMinutes = weekData.getOrDefault("lateMinutes", 0.0);
            double overtimeHours = weekData.getOrDefault("overtimeHours", 0.0);

            totalHours += hours;
            totalLateMinutes += lateMinutes;
            totalOvertimeHours += overtimeHours;

            // Display week summary header
            System.out.println("\n" + weekLabel);
            System.out.println("Weekly Summary: " + String.format("%.2f", hours) + " hours, " +
                    String.format("%.0f", lateMinutes) + " min late, " +
                    String.format("%.2f", overtimeHours) + " OT hours");

            // Display daily logs header for this week
            System.out.println("\nDate\t\tDay\t\tTime In\t\tTime Out\tHours\tLate\tOT\tStatus");
            System.out.println("-----------------------------------------------------------------------------");

            // Get daily logs for this week
            List<Map<String, Object>> dailyLogs = dailyLogsByWeek.getOrDefault(weekLabel, new ArrayList<>());

            // Sort logs by date
            Collections.sort(dailyLogs, (a, b) -> {
                LocalDate dateA = (LocalDate) a.get("date");
                LocalDate dateB = (LocalDate) b.get("date");
                return dateA.compareTo(dateB);
            });

            // Display each day's log
            for (Map<String, Object> dayLog : dailyLogs) {
                LocalDate date = (LocalDate) dayLog.get("date");
                String dayOfWeek = date.getDayOfWeek().toString();
                dayOfWeek = dayOfWeek.charAt(0) + dayOfWeek.substring(1, 3).toLowerCase();

                String timeIn = (String) dayLog.get("timeIn");
                String timeOut = (String) dayLog.get("timeOut");
                double dayHours = (double) dayLog.get("hours");
                double dayLate = (double) dayLog.get("lateMinutes");
                double dayOT = (double) dayLog.get("overtimeHours");
                boolean isDayLate = (boolean) dayLog.get("isLate");

                if (isDayLate) {
                    isLateAnyDay = true;
                }

                String status = isDayLate ? "LATE" : "ON TIME";

                System.out.printf("%s\t%s\t\t%s\t%s\t%.2f\t%.0f\t%.2f\t%s\n",
                        date.format(dateFormatter), dayOfWeek, timeIn, timeOut,
                        dayHours, dayLate, dayOT, status);
            }
        }

        // Display totals
        System.out.println("\n=============================================================================");
        System.out.printf("TOTALS:\t\t\t\t\t\t\t%.2f\t%.0f\t%.2f\n",
                totalHours, totalLateMinutes, totalOvertimeHours);
        System.out.println("* Hours are capped at 8.0 for regular pay, but employees can complete full 8 hours even if late.");

        // Confirm accuracy
        System.out.print("\nAre these records accurate? (Y/N): ");
        String confirmation = scanner.nextLine().trim().toUpperCase();

        if (!confirmation.equals("Y")) {
            System.out.println("Please double check the attendance file and try again.");
            return;
        }

        // Determine pay period (first or second half of month)
        int payPeriodType = 1; // Default to first half

        // If day is > 15, assume second half
        if (endDate.getDayOfMonth() > 15) {
            payPeriodType = 2;
        }

        System.out.println("\nCalculating salary...");

        // Process payroll with the calculated totals and apply late policy
        payrollProcessor.processPayrollForPeriod(employee, totalHours, totalOvertimeHours,
                totalLateMinutes, isLateAnyDay, payPeriodType, startDate, endDate);

        // Display salary details
        System.out.println("\n===== SALARY DETAILS =====");
        payrollProcessor.displaySalaryDetails(employee);

        // Final confirmation
        System.out.print("\nConfirm payroll for processing (Y/N): ");
        confirmation = scanner.nextLine().trim().toUpperCase();

        if (confirmation.equals("Y")) {
            System.out.println("\nPayroll confirmed and processed.");
            System.out.println("Payment will be issued according to the company schedule.");
        } else {
            System.out.println("\nPayroll processing canceled.");
        }
    }
}