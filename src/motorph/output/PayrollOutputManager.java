// File: motorph/output/PayrollOutputManager.java
package motorph.output;

import motorph.employee.Employee;
import motorph.hours.AttendanceReader;
import motorph.process.PayrollProcessor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Scanner;

/**
 * Handles all display and formatting of payroll information
 */
public class PayrollOutputManager {
    private final DateTimeFormatter dateFormatter;
    private final Scanner scanner;
    private final AttendanceReader attendanceReader;
    private final PayrollProcessor payrollProcessor;

    /**
     * Constructor to initialize the output manager
     */
    public PayrollOutputManager(Scanner scanner, AttendanceReader attendanceReader,
                                PayrollProcessor payrollProcessor) {
        this.scanner = scanner;
        this.attendanceReader = attendanceReader;
        this.payrollProcessor = payrollProcessor;
        this.dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    }

    /**
     * Display the main menu options
     */
    public void displayMainMenu() {
        System.out.println("\nMAIN MENU:");
        System.out.println("1. Find Employee");
        System.out.println("2. Show All Employee Details");
        System.out.println("3. Process Payroll");
        System.out.println("4. Exit");
        System.out.print("Enter choice (1-4): ");
    }

    /**
     * Display employee details
     */
    public void displayEmployeeDetails(Employee employee) {
        System.out.println("\n===== EMPLOYEE DETAILS =====");
        System.out.println(employee);

        // Options after viewing employee
        System.out.println("\nOptions:");
        System.out.println("1. Check Employee Attendance");
        System.out.println("2. Back to Main Menu");
        System.out.print("Enter choice (1-2): ");
    }

    /**
     * Display the attendance view options
     */
    public void displayAttendanceOptions(Employee employee) {
        System.out.println("\n===== CHECK ATTENDANCE =====");
        System.out.println("Employee: " + employee.getFullName());

        // Choose view type
        System.out.println("\nView attendance by:");
        System.out.println("1. Daily");
        System.out.println("2. Weekly");
        System.out.print("Enter choice (1-2): ");
    }

    /**
     * Display daily attendance for an employee
     */
    public void displayDailyAttendance(Employee employee, LocalDate startDate, LocalDate endDate) {
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

        // Show headers
        System.out.println("\nDate\t\tDay\t\tTime In\t\tTime Out\tHours\tLate(min)\tUT(min)\tOT(hrs)\tStatus");
        System.out.println("------------------------------------------------------------------------------------------------");

        // Show each day's attendance
        double totalHours = 0;
        double totalLateMinutes = 0;
        double totalUndertimeMinutes = 0;
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
            double undertimeMinutes = (double) dailyData.getOrDefault("undertimeMinutes", 0.0);
            double overtimeHours = (double) dailyData.get("overtimeHours");
            boolean isLate = (boolean) dailyData.get("isLate");
            boolean isUndertime = (boolean) dailyData.getOrDefault("isUndertime", false);

            String status = "ON TIME";
            if (isLate && isUndertime) {
                status = "LATE+UT";
            } else if (isLate) {
                status = "LATE";
            } else if (isUndertime) {
                status = "UNDERTIME";
            }

            totalHours += hours;
            totalLateMinutes += lateMinutes;
            totalUndertimeMinutes += undertimeMinutes;
            totalOvertimeHours += overtimeHours;

            System.out.printf("%s\t%s\t\t%s\t%s\t%.2f\t%.0f\t\t%.0f\t%.2f\t%s\n",
                    date.format(dateFormatter), dayOfWeek, timeIn, timeOut,
                    hours, lateMinutes, undertimeMinutes, overtimeHours, status);
        }

        // Show totals
        System.out.println("------------------------------------------------------------------------------------------------");
        System.out.printf("TOTALS:\t\t\t\t\t\t%.2f\t%.0f\t\t%.0f\t%.2f\n",
                totalHours, totalLateMinutes, totalUndertimeMinutes, totalOvertimeHours);

        // Wait for user
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }

    /**
     * Display weekly attendance for an employee
     */
    public void displayWeeklyAttendance(Employee employee, LocalDate startDate, LocalDate endDate) {
        System.out.println("\n===== WEEKLY ATTENDANCE =====");
        System.out.println("Employee: " + employee.getFullName() + " (ID: " + employee.getEmployeeId() + ")");
        System.out.println("Period: " + startDate.format(dateFormatter) + " to " + endDate.format(dateFormatter));

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

        // Show weekly data with daily logs
        double totalHours = 0;
        double totalLateMinutes = 0;
        double totalUndertimeMinutes = 0;
        double totalOvertimeHours = 0;

        for (Map.Entry<String, Map<String, Double>> entry : weeklyRecords.entrySet()) {
            String weekLabel = entry.getKey();
            Map<String, Double> weekData = entry.getValue();

            double hours = weekData.getOrDefault("hours", 0.0);
            double lateMinutes = weekData.getOrDefault("lateMinutes", 0.0);
            double undertimeMinutes = weekData.getOrDefault("undertimeMinutes", 0.0);
            double overtimeHours = weekData.getOrDefault("overtimeHours", 0.0);

            totalHours += hours;
            totalLateMinutes += lateMinutes;
            totalUndertimeMinutes += undertimeMinutes;
            totalOvertimeHours += overtimeHours;

            // Show week summary
            System.out.println("\n" + weekLabel);
            System.out.println("Weekly Summary: " + String.format("%.2f", hours) + " hours, " +
                    String.format("%.0f", lateMinutes) + " min late, " +
                    String.format("%.0f", undertimeMinutes) + " min undertime, " +
                    String.format("%.2f", overtimeHours) + " OT hours");

            // Show daily logs header for this week
            System.out.println("\nDate\t\tDay\t\tTime In\t\tTime Out\tHours\tLate\tUT\tOT\tStatus");
            System.out.println("---------------------------------------------------------------------------------");

            // Get daily logs for this week
            List<Map<String, Object>> dailyLogs = dailyLogsByWeek.getOrDefault(weekLabel, new ArrayList<>());

            // Sort logs by date
            Collections.sort(dailyLogs, (a, b) -> {
                LocalDate dateA = (LocalDate) a.get("date");
                LocalDate dateB = (LocalDate) b.get("date");
                return dateA.compareTo(dateB);
            });

            // Show each day's log
            for (Map<String, Object> dayLog : dailyLogs) {
                LocalDate date = (LocalDate) dayLog.get("date");
                String dayOfWeek = date.getDayOfWeek().toString();
                dayOfWeek = dayOfWeek.charAt(0) + dayOfWeek.substring(1, 3).toLowerCase();

                String timeIn = (String) dayLog.get("timeIn");
                String timeOut = (String) dayLog.get("timeOut");
                double dayHours = (double) dayLog.get("hours");
                double dayLate = (double) dayLog.get("lateMinutes");
                double dayUT = (double) dayLog.getOrDefault("undertimeMinutes", 0.0);
                double dayOT = (double) dayLog.get("overtimeHours");
                boolean isLate = (boolean) dayLog.get("isLate");
                boolean isUndertime = (boolean) dayLog.getOrDefault("isUndertime", false);

                String status = "ON TIME";
                if (isLate && isUndertime) {
                    status = "LATE+UT";
                } else if (isLate) {
                    status = "LATE";
                } else if (isUndertime) {
                    status = "UNDERTIME";
                }

                System.out.printf("%s\t%s\t\t%s\t%s\t%.2f\t%.0f\t%.0f\t%.2f\t%s\n",
                        date.format(dateFormatter), dayOfWeek, timeIn, timeOut,
                        dayHours, dayLate, dayUT, dayOT, status);
            }
        }

        // Show totals
        System.out.println("\n=================================================================================");
        System.out.printf("TOTALS:\t\t\t\t\t\t\t%.2f\t%.0f\t%.0f\t%.2f\n",
                totalHours, totalLateMinutes, totalUndertimeMinutes, totalOvertimeHours);

        // Wait for user
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }

    /**
     * Display all employees in a tabular format
     */
    public void displayAllEmployeesSummary(List<Employee> employees) {
        System.out.println("\n===== ALL EMPLOYEES =====");

        if (employees.isEmpty()) {
            System.out.println("No employees found.");
            return;
        }

        // Show employee summary table
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

        // Options after viewing all employees
        System.out.println("\nOptions:");
        System.out.println("1. View Detailed Info for an Employee");
        System.out.println("2. Back to Main Menu");
        System.out.print("Enter choice (1-2): ");
    }

    /**
     * Display payroll summary report
     * Returns the attendance totals for further processing
     */
    public Map<String, Double> displayPayrollSummary(Employee employee, LocalDate startDate, LocalDate endDate) {
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
            return null;
        }

        // Calculate totals and display weekly data
        double totalHours = 0;
        double totalLateMinutes = 0;
        double totalUndertimeMinutes = 0;
        double totalOvertimeHours = 0;
        boolean isLateAnyDay = false;
        boolean isUndertimeAnyDay = false;

        for (Map.Entry<String, Map<String, Double>> entry : weeklyRecords.entrySet()) {
            String weekLabel = entry.getKey();
            Map<String, Double> weekData = entry.getValue();

            double hours = weekData.getOrDefault("hours", 0.0);
            double lateMinutes = weekData.getOrDefault("lateMinutes", 0.0);
            double undertimeMinutes = weekData.getOrDefault("undertimeMinutes", 0.0);
            double overtimeHours = weekData.getOrDefault("overtimeHours", 0.0);

            totalHours += hours;
            totalLateMinutes += lateMinutes;
            totalUndertimeMinutes += undertimeMinutes;
            totalOvertimeHours += overtimeHours;

            // Show week summary
            System.out.println("\n" + weekLabel);
            System.out.println("Weekly Summary: " + String.format("%.2f", hours) + " hours, " +
                    String.format("%.0f", lateMinutes) + " min late, " +
                    String.format("%.0f", undertimeMinutes) + " min undertime, " +
                    String.format("%.2f", overtimeHours) + " OT hours");

            // Show daily logs header
            System.out.println("\nDate\t\tDay\t\tTime In\t\tTime Out\tHours\tLate\tUT\tOT\tStatus");
            System.out.println("---------------------------------------------------------------------------------");

            // Get daily logs for this week
            List<Map<String, Object>> dailyLogs = dailyLogsByWeek.getOrDefault(weekLabel, new ArrayList<>());

            // Sort logs by date
            Collections.sort(dailyLogs, (a, b) -> {
                LocalDate dateA = (LocalDate) a.get("date");
                LocalDate dateB = (LocalDate) b.get("date");
                return dateA.compareTo(dateB);
            });

            // Show each day's log
            for (Map<String, Object> dayLog : dailyLogs) {
                LocalDate date = (LocalDate) dayLog.get("date");
                String dayOfWeek = date.getDayOfWeek().toString();
                dayOfWeek = dayOfWeek.charAt(0) + dayOfWeek.substring(1, 3).toLowerCase();

                String timeIn = (String) dayLog.get("timeIn");
                String timeOut = (String) dayLog.get("timeOut");
                double dayHours = (double) dayLog.get("hours");
                double dayLate = (double) dayLog.get("lateMinutes");
                double dayUT = (double) dayLog.getOrDefault("undertimeMinutes", 0.0);
                double dayOT = (double) dayLog.get("overtimeHours");
                boolean isDayLate = (boolean) dayLog.get("isLate");
                boolean isDayUndertime = (boolean) dayLog.getOrDefault("isUndertime", false);

                if (isDayLate) {
                    isLateAnyDay = true;
                }
                if (isDayUndertime) {
                    isUndertimeAnyDay = true;
                }

                String status = "ON TIME";
                if (isDayLate && isDayUndertime) {
                    status = "LATE+UT";
                } else if (isDayLate) {
                    status = "LATE";
                } else if (isDayUndertime) {
                    status = "UNDERTIME";
                }

                System.out.printf("%s\t%s\t\t%s\t%s\t%.2f\t%.0f\t%.0f\t%.2f\t%s\n",
                        date.format(dateFormatter), dayOfWeek, timeIn, timeOut,
                        dayHours, dayLate, dayUT, dayOT, status);
            }
        }

        // Show totals
        System.out.println("\n=================================================================================");
        System.out.printf("TOTALS:\t\t\t\t\t\t\t%.2f\t%.0f\t%.0f\t%.2f\n",
                totalHours, totalLateMinutes, totalUndertimeMinutes, totalOvertimeHours);
        System.out.println("* Hours are capped at 8.0 for regular pay, but employees can complete full 8 hours even if late.");
        System.out.println("* Employees who log out before 5:00 PM get undertime deductions.");
        System.out.println("* Late employees (after 8:10 AM) cannot earn overtime pay.");

        // Return the totals for further processing
        Map<String, Double> totals = new HashMap<>();
        totals.put("hours", totalHours);
        totals.put("lateMinutes", totalLateMinutes);
        totals.put("undertimeMinutes", totalUndertimeMinutes);
        totals.put("overtimeHours", totalOvertimeHours);
        totals.put("isLateAnyDay", isLateAnyDay ? 1.0 : 0.0);
        totals.put("isUndertimeAnyDay", isUndertimeAnyDay ? 1.0 : 0.0);

        return totals;
    }

    /**
     * Display salary calculation details
     */
    public void displaySalaryDetails(Employee employee) {
        System.out.println("\n===== SALARY DETAILS =====");
        payrollProcessor.displaySalaryDetails(employee);
    }
}