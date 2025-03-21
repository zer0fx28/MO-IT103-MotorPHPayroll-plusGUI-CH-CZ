// File: motorph/output/PayrollOutputManager.java
package motorph.output;

import motorph.employee.Employee;
import motorph.hours.AttendanceReader;
import motorph.process.PayrollDateManager;
import motorph.process.PayrollProcessor;
import motorph.util.DateTimeUtil;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Handles display of payroll information
 * Manages all output formatting and display for the payroll system
 */
public class PayrollOutputManager {
    // For user input
    private final Scanner scanner;

    // Data sources
    private final AttendanceReader attendanceReader;
    private final PayrollProcessor payrollProcessor;

    // Lunch break in minutes
    private static final int LUNCH_BREAK_MINUTES = 60; // 1-hour lunch break

    /**
     * Create a new output manager
     *
     * @param scanner Scanner for user input
     * @param attendanceReader Reader for attendance data
     * @param payrollProcessor Processor for payroll calculations
     */
    public PayrollOutputManager(Scanner scanner, AttendanceReader attendanceReader, PayrollProcessor payrollProcessor) {
        this.scanner = scanner;
        this.attendanceReader = attendanceReader;
        this.payrollProcessor = payrollProcessor;
    }

    /**
     * Display main menu
     */
    public void displayMainMenu() {
        System.out.println("\nMAIN MENU:");
        System.out.println("1. Process Payroll");
        System.out.println("2. Find Employee");
        System.out.println("3. View Payroll Calendar");
        System.out.println("4. Exit");
        System.out.print("Enter choice (1-4): ");
    }

    /**
     * Display employee details
     *
     * @param employee Employee to display details for
     */
    public void displayEmployeeDetails(Employee employee) {
        if (employee == null) {
            return;
        }

        System.out.println("\n===== EMPLOYEE DETAILS =====");
        System.out.println("ID: " + employee.getEmployeeId());
        System.out.println("Name: " + employee.getFullName());
        System.out.println("Position: " + employee.getPosition());
        System.out.println("Basic Salary: ₱" + String.format("%,.2f", employee.getBasicSalary()));
        System.out.println("Rice Subsidy: ₱" + String.format("%,.2f", employee.getRiceSubsidy()));
        System.out.println("Phone Allowance: ₱" + String.format("%,.2f", employee.getPhoneAllowance()));
        System.out.println("Clothing Allowance: ₱" + String.format("%,.2f", employee.getClothingAllowance()));
        System.out.println("Hourly Rate: ₱" + String.format("%.2f", employee.getHourlyRate()));

        System.out.println("\nOptions:");
        System.out.println("1. View Attendance");
        System.out.println("2. Process Payroll");
        System.out.println("3. Return to Main Menu");
        System.out.print("Enter choice: ");
    }

    /**
     * Display attendance summary for payroll processing
     *
     * @param employee Employee to display summary for
     * @param attendanceSummary Summary data from attendance reader
     * @param startDate Start date of payroll period
     * @param endDate End date of payroll period
     */
    public void displayAttendanceSummary(Employee employee, Map<String, Object> attendanceSummary,
                                         LocalDate startDate, LocalDate endDate) {
        if (employee == null || attendanceSummary == null) {
            return;
        }

        System.out.println("\n===== ATTENDANCE SUMMARY =====");
        System.out.println("Employee: " + employee.getFullName() + " (ID: " + employee.getEmployeeId() + ")");
        System.out.println("Period: " + DateTimeUtil.formatDateStandard(startDate) +
                " to " + DateTimeUtil.formatDateStandard(endDate));

        // Extract attendance data
        double totalHours = (double) attendanceSummary.get("hours");
        double overtimeHours = (double) attendanceSummary.get("overtimeHours");
        double lateMinutes = (double) attendanceSummary.get("lateMinutes");
        double undertimeMinutes = (double) attendanceSummary.get("undertimeMinutes");
        boolean isLateAnyDay = (boolean) attendanceSummary.get("isLateAnyDay");
        int recordCount = (int) attendanceSummary.getOrDefault("recordCount", 0);

        // Display summary
        System.out.println("\nSUMMARY:");
        System.out.println("Total Days Worked: " + recordCount);
        System.out.println("Total Hours: " + String.format("%.2f", totalHours));
        System.out.println("Overtime Hours: " + String.format("%.2f", overtimeHours));

        if (lateMinutes > 0) {
            System.out.println("Late Minutes: " + String.format("%.0f", lateMinutes));
        }

        if (undertimeMinutes > 0) {
            System.out.println("Undertime Minutes: " + String.format("%.0f", undertimeMinutes));
        }

        // Show notices
        if (isLateAnyDay) {
            System.out.println("\nNOTE: Employee was late during this period.");
            System.out.println("      Late employees are not eligible for overtime pay.");
        }

        if (attendanceSummary.containsKey("hasUnpaidAbsences") &&
                (boolean) attendanceSummary.get("hasUnpaidAbsences")) {
            System.out.println("\nNOTE: Employee has unpaid absences during this period.");
        }
    }

    /**
     * Calculate actual hours worked considering lunch break
     *
     * @param timeIn Time employee clocked in
     * @param timeOut Time employee clocked out
     * @param isLate Whether employee was late
     * @return Actual hours worked with lunch break deducted
     */
    private double calculateActualHours(LocalTime timeIn, LocalTime timeOut, boolean isLate) {
        if (timeIn == null || timeOut == null) {
            return 0.0;
        }

        // Standard end time is 5:00 PM
        LocalTime standardEndTime = LocalTime.of(17, 0);

        // For late employees, cap time out at 5:00 PM
        LocalTime effectiveTimeOut = timeOut;
        if (isLate && timeOut.isAfter(standardEndTime)) {
            effectiveTimeOut = standardEndTime;
        }

        Duration workDuration = Duration.between(timeIn, effectiveTimeOut);
        double totalMinutes = workDuration.toMinutes();

        // Deduct 1 hour (60 minutes) for lunch break if working more than 5 hours
        if (totalMinutes >= 300) { // Only deduct lunch if worked at least 5 hours
            totalMinutes -= LUNCH_BREAK_MINUTES;
        }

        double hours = totalMinutes / 60.0;

        // Round to 2 decimal places
        return Math.round(hours * 100) / 100.0;
    }

    /**
     * Helper method to check if an employee is late
     *
     * @param timeIn The time the employee clocked in
     * @return true if the employee is late (after 8:10 AM)
     */
    private boolean isLate(LocalTime timeIn) {
        return timeIn != null && timeIn.isAfter(LocalTime.of(8, 10));
    }

    /**
     * Display payroll summary before processing with enhanced format
     * Accounts for 1-hour lunch break
     *
     * @param employee Employee to display summary for
     * @param startDate Start date of payroll period
     * @param endDate End date of payroll period
     * @param payPeriodType Pay period type (MID_MONTH or END_MONTH)
     * @return Attendance summary map or null if no records found
     */
    public Map<String, Object> displayPayrollSummary(Employee employee, LocalDate startDate,
                                                     LocalDate endDate, int payPeriodType) {
        if (employee == null) {
            return null;
        }

        System.out.println("\n===== PAYROLL SUMMARY =====");
        System.out.println("Employee: " + employee.getFullName() + " (ID: " + employee.getEmployeeId() + ")");
        System.out.println("Period: " + DateTimeUtil.formatDateStandard(startDate) +
                " to " + DateTimeUtil.formatDateStandard(endDate));
        System.out.println("Payroll Type: " +
                (payPeriodType == PayrollDateManager.MID_MONTH ? "Mid-month" : "End-month"));

        // Get daily attendance records
        Map<LocalDate, Map<String, Object>> dailyAttendance =
                attendanceReader.getDailyAttendanceForEmployee(employee.getEmployeeId(), startDate, endDate);

        if (dailyAttendance.isEmpty()) {
            System.out.println("\nNo attendance records found for this period.");
            return null;
        }

        // Track totals
        double totalBaseHours = 0;
        double totalActualHours = 0;
        double totalOvertimeHours = 0;
        double totalLateMinutes = 0;
        double totalUndertimeMinutes = 0;
        boolean isLateAnyDay = false;
        boolean hasUnpaidAbsences = false;
        int recordCount = dailyAttendance.size();

        // Display header for the enhanced format
        System.out.println("\n--- ATTENDANCE DETAILS ---");
        System.out.printf("%-10s %-5s %-8s %-8s %-5s %-5s %-5s %-5s %-5s %-6s\n",
                "Date", "Base", "Time In", "Time Out", "Late", "UT", "OT", "UA", "LV", "Actual");
        System.out.println("----------------------------------------------------------------------");

        // Process each day
        List<LocalDate> sortedDates = new ArrayList<>(dailyAttendance.keySet());
        java.util.Collections.sort(sortedDates);

        for (LocalDate date : sortedDates) {
            Map<String, Object> dayData = dailyAttendance.get(date);

            // Base hours is always 8.0
            double baseHours = 8.0;
            totalBaseHours += baseHours;

            String timeInStr = (String) dayData.get("timeIn");
            String timeOutStr = (String) dayData.get("timeOut");

            LocalTime timeIn = DateTimeUtil.parseTime(timeInStr);
            LocalTime timeOut = DateTimeUtil.parseTime(timeOutStr);

            // Standard times
            LocalTime graceEndTime = LocalTime.of(8, 10);
            LocalTime standardEndTime = LocalTime.of(17, 0);

            // Calculate late minutes (after 8:10 AM)
            double lateMinutes = 0;
            if (timeIn != null && timeIn.isAfter(graceEndTime)) {
                Duration lateBy = Duration.between(graceEndTime, timeIn);
                lateMinutes = lateBy.toMinutes();
                isLateAnyDay = true;
            }

            // Calculate undertime minutes (before 5:00 PM)
            double undertimeMinutes = 0;
            if (timeOut != null && timeOut.isBefore(standardEndTime)) {
                Duration undertimeBy = Duration.between(timeOut, standardEndTime);
                undertimeMinutes = undertimeBy.toMinutes();
            }

            // Calculate overtime hours (only for non-late employees after 5:00 PM)
            double overtimeHours = 0;
            if (timeIn != null && timeOut != null && !isLate(timeIn) && timeOut.isAfter(standardEndTime)) {
                Duration overtimeDuration = Duration.between(standardEndTime, timeOut);
                overtimeHours = overtimeDuration.toMinutes() / 60.0;
                // Round to 2 decimal places
                overtimeHours = Math.round(overtimeHours * 100) / 100.0;
            }

            // Calculate actual hours worked with proper capping for late employees
            double actualHours = calculateActualHours(timeIn, timeOut, lateMinutes > 0);

            // Check for unpaid absence
            boolean isUnpaidAbsence = false;
            if (dayData.containsKey("isUnpaidAbsence")) {
                isUnpaidAbsence = (boolean) dayData.get("isUnpaidAbsence");
            } else {
                String absenceType = (String) dayData.getOrDefault("absenceType", "");
                if (absenceType != null && !absenceType.isEmpty()) {
                    String type = absenceType.toLowerCase();
                    isUnpaidAbsence = type.contains("unpaid") ||
                            type.contains("unauthoriz") ||
                            type.contains("unapproved");
                }
            }

            if (isUnpaidAbsence) {
                hasUnpaidAbsences = true;
            }

            // Format display time out (capped at 5:00 PM for late employees)
            String displayTimeOut = timeOutStr;
            if (lateMinutes > 0 && timeOut != null && timeOut.isAfter(standardEndTime)) {
                displayTimeOut = "5:00 PM";
            }

            // Format values for display
            String dateStr = DateTimeUtil.formatDateStandard(date);
            String baseHoursStr = String.format("%.1f", baseHours);
            String lateStr = lateMinutes > 0 ? String.format("%.0fm", lateMinutes) : "-";
            String undertimeStr = undertimeMinutes > 0 ? String.format("%.0fm", undertimeMinutes) : "-";
            String overtimeStr = overtimeHours > 0 ? String.format("%.2f", overtimeHours) : "-";
            String unpaidAbsenceStr = isUnpaidAbsence ? "X" : "-";
            String leaveStr = "-"; // Placeholder
            String actualHoursStr = String.format("%.2f", actualHours);

            // Display row
            System.out.printf("%-10s %-5s %-8s %-8s %-5s %-5s %-5s %-5s %-5s %-6s\n",
                    dateStr, baseHoursStr, timeInStr, displayTimeOut,
                    lateStr, undertimeStr, overtimeStr, unpaidAbsenceStr, leaveStr, actualHoursStr);

            // Update totals
            totalActualHours += actualHours;
            totalOvertimeHours += overtimeHours;
            totalLateMinutes += lateMinutes;
            totalUndertimeMinutes += undertimeMinutes;
        }

        // Display totals
        System.out.println("----------------------------------------------------------------------");
        System.out.printf("%-10s %-5.1f %-17s %-5.0fm %-5.0fm %-5.2f %-5s %-5s %-6.2f\n",
                "TOTALS:", totalBaseHours, "", totalLateMinutes, totalUndertimeMinutes,
                totalOvertimeHours, "-", "-", totalActualHours);

        // Add legends
        System.out.println("\nLEGENDS:");
        System.out.println("Base = Base hours expected (8 hours per day)");
        System.out.println("Late = Late minutes (after 8:10 AM grace period)");
        System.out.println("UT = Undertime minutes (left before 5:00 PM)");
        System.out.println("OT = Overtime hours (after 5:00 PM, only for on-time employees)");
        System.out.println("UA = Unapproved Absence");
        System.out.println("LV = Leave");
        System.out.println("Actual = Actual hours worked (1-hour lunch break deducted)");

        // Create summary
        Map<String, Object> summary = new HashMap<>();
        summary.put("hours", totalActualHours);
        summary.put("overtimeHours", totalOvertimeHours);
        summary.put("lateMinutes", totalLateMinutes);
        summary.put("undertimeMinutes", totalUndertimeMinutes);
        summary.put("isLateAnyDay", isLateAnyDay);
        summary.put("hasUnpaidAbsences", hasUnpaidAbsences);
        summary.put("recordCount", recordCount);
        summary.put("baseHours", totalBaseHours);

        return summary;
    }

    /**
     * Display salary details for an employee
     *
     * @param employee The employee to display details for
     */
    public void displaySalaryDetails(Employee employee) {
        payrollProcessor.displaySalaryDetails(employee);
    }

    /**
     * Display attendance options
     *
     * @param employee Employee to display options for
     */
    public void displayAttendanceOptions(Employee employee) {
        System.out.println("\n===== ATTENDANCE OPTIONS =====");
        System.out.println("Employee: " + employee.getFullName());
        System.out.println("\nSelect view type:");
        System.out.println("1. Daily Attendance");
        System.out.println("2. Weekly Summary");
        System.out.print("Enter choice (1-2): ");
    }

    /**
     * Display daily attendance with the enhanced format
     * Accounts for 1-hour lunch break
     *
     * @param employee Employee to display attendance for
     * @param startDate Start date of range
     * @param endDate End date of range
     */
    public void displayDailyAttendance(Employee employee, LocalDate startDate, LocalDate endDate) {
        System.out.println("\n===== DAILY ATTENDANCE =====");
        System.out.println("Employee: " + employee.getFullName());
        System.out.println("Period: " + DateTimeUtil.formatDateStandard(startDate) +
                " to " + DateTimeUtil.formatDateStandard(endDate));

        // Get daily attendance records
        Map<LocalDate, Map<String, Object>> dailyAttendance =
                attendanceReader.getDailyAttendanceForEmployee(employee.getEmployeeId(), startDate, endDate);

        if (dailyAttendance.isEmpty()) {
            System.out.println("\nNo attendance records found for this period.");
            return;
        }

        // Display header for the enhanced format
        System.out.println("\n--- ATTENDANCE DETAILS ---");
        System.out.printf("%-10s %-5s %-8s %-8s %-5s %-5s %-5s %-5s %-5s %-6s\n",
                "Date", "Base", "Time In", "Time Out", "Late", "UT", "OT", "UA", "LV", "Actual");
        System.out.println("----------------------------------------------------------------------");

        // Track totals
        double totalBaseHours = 0;
        double totalActualHours = 0;
        double totalLateMinutes = 0;
        double totalUndertimeMinutes = 0;
        double totalOvertimeHours = 0;

        // Process each day
        List<LocalDate> sortedDates = new ArrayList<>(dailyAttendance.keySet());
        java.util.Collections.sort(sortedDates);

        for (LocalDate date : sortedDates) {
            Map<String, Object> dayData = dailyAttendance.get(date);

            // Base hours is always 8.0
            double baseHours = 8.0;
            totalBaseHours += baseHours;

            String timeInStr = (String) dayData.get("timeIn");
            String timeOutStr = (String) dayData.get("timeOut");

            LocalTime timeIn = DateTimeUtil.parseTime(timeInStr);
            LocalTime timeOut = DateTimeUtil.parseTime(timeOutStr);

            // Standard times
            LocalTime graceEndTime = LocalTime.of(8, 10);
            LocalTime standardEndTime = LocalTime.of(17, 0);

            // Calculate late minutes (after 8:10 AM)
            double lateMinutes = 0;
            if (timeIn != null && timeIn.isAfter(graceEndTime)) {
                Duration lateBy = Duration.between(graceEndTime, timeIn);
                lateMinutes = lateBy.toMinutes();
            }

            // Calculate undertime minutes (before 5:00 PM)
            double undertimeMinutes = 0;
            if (timeOut != null && timeOut.isBefore(standardEndTime)) {
                Duration undertimeBy = Duration.between(timeOut, standardEndTime);
                undertimeMinutes = undertimeBy.toMinutes();
            }

            // Calculate overtime hours (only for non-late employees after 5:00 PM)
            double overtimeHours = 0;
            if (timeIn != null && timeOut != null && !isLate(timeIn) && timeOut.isAfter(standardEndTime)) {
                Duration overtimeDuration = Duration.between(standardEndTime, timeOut);
                overtimeHours = overtimeDuration.toMinutes() / 60.0;
                // Round to 2 decimal places
                overtimeHours = Math.round(overtimeHours * 100) / 100.0;
            }

            // Calculate actual hours worked using the helper method
            double actualHours = calculateActualHours(timeIn, timeOut, lateMinutes > 0);

            // Format display time out (capped at 5:00 PM for late employees)
            String displayTimeOut = timeOutStr;
            if (lateMinutes > 0 && timeOut != null && timeOut.isAfter(standardEndTime)) {
                displayTimeOut = "5:00 PM";
            }

            // Format values for display
            String dateStr = DateTimeUtil.formatDateStandard(date);
            String baseHoursStr = String.format("%.1f", baseHours);
            String lateStr = lateMinutes > 0 ? String.format("%.0fm", lateMinutes) : "-";
            String undertimeStr = undertimeMinutes > 0 ? String.format("%.0fm", undertimeMinutes) : "-";
            String overtimeStr = overtimeHours > 0 ? String.format("%.2f", overtimeHours) : "-";
            String unpaidAbsenceStr = "-"; // Placeholder
            String leaveStr = "-"; // Placeholder
            String actualHoursStr = String.format("%.2f", actualHours);

            // Display row
            // Display row
                System.out.printf("%-10s %-5s %-8s %-8s %-5s %-5s %-5s %-5s %-5s %-6s\n",
                        dateStr, baseHoursStr, timeInStr, displayTimeOut,
                        lateStr, undertimeStr, overtimeStr, unpaidAbsenceStr, leaveStr, actualHoursStr);

            // Update totals
            totalActualHours += actualHours;
            totalLateMinutes += lateMinutes;
            totalUndertimeMinutes += undertimeMinutes;
            totalOvertimeHours += overtimeHours;
        }

        // Display totals
        System.out.println("----------------------------------------------------------------------");
        System.out.printf("%-10s %-5.1f %-17s %-5.0fm %-5.0fm %-5.2f %-5s %-5s %-6.2f\n",
                "TOTALS:", totalBaseHours, "", totalLateMinutes, totalUndertimeMinutes,
                totalOvertimeHours, "-", "-", totalActualHours);

        // Add legends
        System.out.println("\nLEGENDS:");
        System.out.println("Base = Base hours expected (8 hours per day)");
        System.out.println("Late = Late minutes (after 8:10 AM grace period)");
        System.out.println("UT = Undertime minutes (left before 5:00 PM)");
        System.out.println("OT = Overtime hours (after 5:00 PM, only for on-time employees)");
        System.out.println("UA = Unapproved Absence");
        System.out.println("LV = Leave");
        System.out.println("Actual = Actual hours worked (1-hour lunch break deducted)");

        // Wait for user
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }

    /**
     * Display weekly attendance
     *
     * @param employee Employee to display attendance for
     * @param startDate Start date of range
     * @param endDate End date of range
     */
    public void displayWeeklyAttendance(Employee employee, LocalDate startDate, LocalDate endDate) {
        System.out.println("\n===== WEEKLY ATTENDANCE =====");
        System.out.println("Employee: " + employee.getFullName());
        System.out.println("Period: " + DateTimeUtil.formatDateStandard(startDate) +
                " to " + DateTimeUtil.formatDateStandard(endDate));

        // Get weekly attendance records
        Map<String, Map<String, Double>> weeklyAttendance =
                attendanceReader.getWeeklyAttendanceForEmployee(employee.getEmployeeId(), startDate, endDate);

        if (weeklyAttendance.isEmpty()) {
            System.out.println("\nNo attendance records found for this period.");
            return;
        }

        // Display weekly summary
        System.out.println("\n--- WEEKLY SUMMARY ---");
        System.out.printf("%-30s %-10s %-12s %-12s %-10s\n",
                "Week", "Hours", "Overtime", "Late (min)", "Unpaid Abs");
        System.out.println("--------------------------------------------------------------------------------");

        double totalHours = 0;
        double totalOvertimeHours = 0;
        double totalLateMinutes = 0;
        int totalUnpaidAbsences = 0;

        for (String weekLabel : weeklyAttendance.keySet()) {
            Map<String, Double> weekData = weeklyAttendance.get(weekLabel);

            double hours = weekData.getOrDefault("hours", 0.0);
            double overtimeHours = weekData.getOrDefault("overtimeHours", 0.0);
            double lateMinutes = weekData.getOrDefault("lateMinutes", 0.0);
            int unpaidAbsences = weekData.getOrDefault("unpaidAbsenceCount", 0.0).intValue();

            // Add to totals
            totalHours += hours;
            totalOvertimeHours += overtimeHours;
            totalLateMinutes += lateMinutes;
            totalUnpaidAbsences += unpaidAbsences;

            // Format the line
            System.out.printf("%-30s %-10.2f %-12.2f %-12.2f %-10d\n",
                    weekLabel, hours, overtimeHours, lateMinutes, unpaidAbsences);
        }

        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("%-30s %-10.2f %-12.2f %-12.2f %-10d\n",
                "TOTALS:", totalHours, totalOvertimeHours, totalLateMinutes, totalUnpaidAbsences);

        // Wait for user
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }

    /**
     * Display payroll calendar
     *
     * @param year Year to display calendar for
     * @param month Month to display calendar for
     */
    public void displayPayrollCalendar(int year, int month) {
        System.out.println("\n===== PAYROLL CALENDAR =====");
        System.out.println("Year: " + year);
        System.out.println("Month: " + PayrollDateManager.getMonthName(month));

        // Calculate payroll dates
        LocalDate midMonth = PayrollDateManager.getPayrollDate(year, month, PayrollDateManager.MID_MONTH);
        LocalDate endMonth = PayrollDateManager.getPayrollDate(year, month, PayrollDateManager.END_MONTH);

        // Get cutoff periods
        LocalDate[] midCutoff = PayrollDateManager.getCutoffDateRange(midMonth, PayrollDateManager.MID_MONTH);
        LocalDate[] endCutoff = PayrollDateManager.getCutoffDateRange(endMonth, PayrollDateManager.END_MONTH);

        // Display payroll information
        System.out.println("\n--- MID-MONTH PAYROLL ---");
        System.out.println("Payroll Date: " + DateTimeUtil.formatDate(midMonth));
        System.out.println("Cutoff Period: " + DateTimeUtil.formatDate(midCutoff[0]) +
                " to " + DateTimeUtil.formatDate(midCutoff[1]));
        System.out.println("Deductions: SSS, PhilHealth, Pag-IBIG");

        System.out.println("\n--- END-MONTH PAYROLL ---");
        System.out.println("Payroll Date: " + DateTimeUtil.formatDate(endMonth));
        System.out.println("Cutoff Period: " + DateTimeUtil.formatDate(endCutoff[0]) +
                " to " + DateTimeUtil.formatDate(endCutoff[1]));
        System.out.println("Deductions: Withholding Tax");
    }
}