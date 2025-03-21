package motorph.output;

import motorph.employee.Employee;
import motorph.hours.AttendanceReader;
import motorph.process.PayrollDateManager;
import motorph.process.PayrollProcessor;
import motorph.holidays.HolidayManager;
import motorph.util.DateTimeUtil;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
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
    private static final int LUNCH_BREAK_MINUTES = 60;
    private static final LocalTime GRACE_PERIOD_END = LocalTime.of(8, 10);
    private static final LocalTime STANDARD_END_TIME = LocalTime.of(17, 0);
    private static final double BASE_HOURS = 8.0;

    private final Scanner scanner;
    private final AttendanceReader attendanceReader;
    private final PayrollProcessor payrollProcessor;
    private final HolidayManager holidayManager;

    public PayrollOutputManager(Scanner scanner, AttendanceReader attendanceReader, PayrollProcessor payrollProcessor) {
        this.scanner = scanner;
        this.attendanceReader = attendanceReader;
        this.payrollProcessor = payrollProcessor;
        this.holidayManager = new HolidayManager();
    }

    private String formatTimeDuration(double minutes) {
        if (minutes <= 0) {
            return "-";
        }
        int hours = (int) (minutes / 60);
        int mins = (int) (minutes % 60);
        return hours > 0 ? String.format("%d:%02d", hours, mins) : String.format("0:%02d", mins);
    }

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

    public void displayAttendanceSummary(Employee employee, Map<String, Object> attendanceSummary, LocalDate startDate, LocalDate endDate) {
        if (employee == null || attendanceSummary == null) {
            return;
        }
        System.out.println("\n===== ATTENDANCE SUMMARY =====");
        System.out.println("Employee: " + employee.getFullName() + " (ID: " + employee.getEmployeeId() + ")");
        System.out.println("Period: " + DateTimeUtil.formatDateStandard(startDate) + " to " + DateTimeUtil.formatDateStandard(endDate));
        double totalHours = (double) attendanceSummary.get("hours");
        double overtimeHours = (double) attendanceSummary.get("overtimeHours");
        double lateMinutes = (double) attendanceSummary.get("lateMinutes");
        double undertimeMinutes = (double) attendanceSummary.get("undertimeMinutes");
        boolean isLateAnyDay = (boolean) attendanceSummary.get("isLateAnyDay");
        int recordCount = (int) attendanceSummary.getOrDefault("recordCount", 0);
        System.out.println("\nSUMMARY:");
        System.out.println("Total Days Worked: " + recordCount);
        System.out.println("Total Hours: " + String.format("%.2f", totalHours));
        System.out.println("Overtime Hours: " + String.format("%.2f", overtimeHours));
        if (lateMinutes > 0) {
            System.out.println("Late Time: " + formatTimeDuration(lateMinutes));
        }
        if (undertimeMinutes > 0) {
            System.out.println("Undertime: " + formatTimeDuration(undertimeMinutes));
        }
        displayHolidaysInPeriod(startDate, endDate);
        if (isLateAnyDay) {
            System.out.println("\nNOTE: Employee was late during this period.");
            System.out.println("      Late employees are not eligible for overtime pay.");
            System.out.println("      Late employees do not receive the additional 25% premium for holiday overtime.");
        }
        if (attendanceSummary.containsKey("hasUnpaidAbsences") && (boolean) attendanceSummary.get("hasUnpaidAbsences")) {
            System.out.println("\nNOTE: Employee has unpaid absences during this period.");
        }
    }

    private void displayHolidaysInPeriod(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> holidays = getHolidaysInPeriod(startDate, endDate);
        if (!holidays.isEmpty()) {
            System.out.println("\nHOLIDAYS IN THIS PERIOD:");
            for (LocalDate holiday : holidays) {
                String holidayType = holidayManager.isRegularHoliday(holiday) ? "Regular Holiday" : "Special Non-Working Holiday";
                String holidayName = holidayManager.getHolidaysForYear(holiday.getYear()).getOrDefault(holiday, "Holiday");
                System.out.println("- " + DateTimeUtil.formatDateStandard(holiday) + " (" + holidayType + "): " + holidayName);
            }
        }
    }

    private List<LocalDate> getHolidaysInPeriod(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> holidays = new ArrayList<>();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            if (holidayManager.isHoliday(currentDate)) {
                holidays.add(currentDate);
            }
            currentDate = currentDate.plusDays(1);
        }
        return holidays;
    }

    private double calculateActualHours(LocalTime timeIn, LocalTime timeOut, boolean isLate) {
        if (timeIn == null || timeOut == null) {
            return 0.0;
        }
        LocalTime effectiveTimeOut = isLate && timeOut.isAfter(STANDARD_END_TIME) ? STANDARD_END_TIME : timeOut;
        Duration workDuration = Duration.between(timeIn, effectiveTimeOut);
        double totalMinutes = workDuration.toMinutes();
        if (totalMinutes >= 300) {
            totalMinutes -= LUNCH_BREAK_MINUTES;
        }
        return Math.round(totalMinutes / 60.0 * 100) / 100.0;
    }

    private Map<String, Object> calculateAttendanceMetrics(LocalTime timeIn, LocalTime timeOut) {
        Map<String, Object> metrics = new HashMap<>();
        double lateMinutes = timeIn != null && timeIn.isAfter(GRACE_PERIOD_END) ? Duration.between(GRACE_PERIOD_END, timeIn).toMinutes() : 0;
        double undertimeMinutes = timeOut != null && timeOut.isBefore(STANDARD_END_TIME) ? Duration.between(timeOut, STANDARD_END_TIME).toMinutes() : 0;
        double overtimeHours = timeIn != null && timeOut != null && !isLate(timeIn) && timeOut.isAfter(STANDARD_END_TIME) ? Math.round(Duration.between(STANDARD_END_TIME, timeOut).toMinutes() / 60.0 * 100) / 100.0 : 0;
        double actualHours = calculateActualHours(timeIn, timeOut, lateMinutes > 0);
        metrics.put("lateMinutes", lateMinutes);
        metrics.put("undertimeMinutes", undertimeMinutes);
        metrics.put("overtimeHours", overtimeHours);
        metrics.put("actualHours", actualHours);
        metrics.put("isLate", lateMinutes > 0);
        return metrics;
    }

    private Map<String, Object> processAndDisplayDayAttendance(LocalDate date, Map<String, Object> dayData, String baseHoursStr) {
        LocalTime timeIn = DateTimeUtil.parseTime((String) dayData.get("timeIn"));
        LocalTime timeOut = DateTimeUtil.parseTime((String) dayData.get("timeOut"));
        Map<String, Object> metrics = calculateAttendanceMetrics(timeIn, timeOut);
        double lateMinutes = (double) metrics.get("lateMinutes");
        double undertimeMinutes = (double) metrics.get("undertimeMinutes");
        double overtimeHours = (double) metrics.get("overtimeHours");
        double actualHours = (double) metrics.get("actualHours");
        boolean isLate = (boolean) metrics.get("isLate");
        boolean isUnpaidAbsence = dayData.containsKey("isUnpaidAbsence") ? (boolean) dayData.get("isUnpaidAbsence") : false;
        if (!isUnpaidAbsence && dayData.containsKey("absenceType")) {
            String absenceType = (String) dayData.get("absenceType");
            isUnpaidAbsence = absenceType.toLowerCase().contains("unpaid") || absenceType.toLowerCase().contains("unauthorized") || absenceType.toLowerCase().contains("unapproved");
        }
        String displayTimeOut = isLate && timeOut != null && timeOut.isAfter(STANDARD_END_TIME) ? "5:00 PM" : (String) dayData.get("timeOut");
        String holidayColumn = holidayManager.isHoliday(date) ? (holidayManager.isRegularHoliday(date) ? "R" : "S") : "-";
        String dateStr = DateTimeUtil.formatDateStandard(date);
        String lateStr = formatTimeDuration(lateMinutes);
        String undertimeStr = formatTimeDuration(undertimeMinutes);
        String overtimeStr = overtimeHours > 0 ? String.format("%.2f", overtimeHours) : "-";
        String unpaidAbsenceStr = isUnpaidAbsence ? "X" : "-";
        String actualHoursStr = String.format("%.2f", actualHours);
        System.out.printf("%-10s %-5s %-8s %-8s %-5s %-5s %-5s %-5s %-5s %-5s %-6s\n", dateStr, baseHoursStr, dayData.get("timeIn"), displayTimeOut, lateStr, undertimeStr, overtimeStr, unpaidAbsenceStr, "-", holidayColumn, actualHoursStr);
        Map<String, Object> result = new HashMap<>();
        result.put("actualHours", actualHours);
        result.put("overtimeHours", overtimeHours);
        result.put("lateMinutes", lateMinutes);
        result.put("undertimeMinutes", undertimeMinutes);
        result.put("isUnpaidAbsence", isUnpaidAbsence);
        return result;
    }

    private boolean isLate(LocalTime timeIn) {
        return timeIn != null && timeIn.isAfter(GRACE_PERIOD_END);
    }

    private void displayAttendanceHeader() {
        System.out.printf("%-10s %-5s %-8s %-8s %-5s %-5s %-5s %-5s %-5s %-5s %-6s\n", "Date", "Base", "Time In", "Time Out", "Late", "UT", "OT", "UA", "LV", "HOL", "Actual");
        System.out.println("------------------------------------------------------------------------------");
    }

    private void displayAttendanceFooter(double totalBaseHours, double totalLateMinutes, double totalUndertimeMinutes, double totalOvertimeHours, double totalActualHours) {
        System.out.println("------------------------------------------------------------------------------");
        System.out.printf("%-10s %-5.1f %-17s %-5s %-5s %-5.2f %-5s %-5s %-5s %-6.2f\n", "TOTALS:", totalBaseHours, "", formatTimeDuration(totalLateMinutes), formatTimeDuration(totalUndertimeMinutes), totalOvertimeHours, "-", "-", "-", totalActualHours);
        System.out.println("\nLEGENDS:");
        System.out.println("Base = Base hours expected (8 hours per day)");
        System.out.println("Late = Late time (h:mm format, after 8:10 AM grace period)");
        System.out.println("UT = Undertime (h:mm format, left before 5:00 PM)");
        System.out.println("OT = Overtime hours (after 5:00 PM, only for on-time employees)");
        System.out.println("UA = Unapproved Absence");
        System.out.println("LV = Leave");
        System.out.println("HOL = Holiday (R=Regular, S=Special)");
        System.out.println("Actual = Actual hours worked (1-hour lunch break deducted)");
    }

    private Map<String, Object> processAttendanceRecords(Map<LocalDate, Map<String, Object>> dailyAttendance) {
        double totalBaseHours = 0;
        double totalActualHours = 0;
        double totalOvertimeHours = 0;
        double totalLateMinutes = 0;
        double totalUndertimeMinutes = 0;
        boolean isLateAnyDay = false;
        boolean hasUnpaidAbsences = false;
        int recordCount = dailyAttendance.size();
        displayAttendanceHeader();
        List<LocalDate> sortedDates = new ArrayList<>(dailyAttendance.keySet());
        sortedDates.sort(LocalDate::compareTo);
        for (LocalDate date : sortedDates) {
            Map<String, Object> dayData = dailyAttendance.get(date);
            totalBaseHours += BASE_HOURS;
            String baseHoursStr = String.format("%.1f", BASE_HOURS);
            Map<String, Object> dayResult = processAndDisplayDayAttendance(date, dayData, baseHoursStr);
            totalActualHours += (double) dayResult.get("actualHours");
            totalOvertimeHours += (double) dayResult.get("overtimeHours");
            totalLateMinutes += (double) dayResult.get("lateMinutes");
            totalUndertimeMinutes += (double) dayResult.get("undertimeMinutes");
            if ((double) dayResult.get("lateMinutes") > 0) {
                isLateAnyDay = true;
            }
            if ((boolean) dayResult.get("isUnpaidAbsence")) {
                hasUnpaidAbsences = true;
            }
        }
        displayAttendanceFooter(totalBaseHours, totalLateMinutes, totalUndertimeMinutes, totalOvertimeHours, totalActualHours);
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

    public Map<String, Object> displayPayrollSummary(Employee employee, LocalDate startDate, LocalDate endDate, int payPeriodType) {
        if (employee == null) {
            return null;
        }
        System.out.println("\n===== PAYROLL SUMMARY =====");
        System.out.println("Employee: " + employee.getFullName() + " (ID: " + employee.getEmployeeId() + ")");
        System.out.println("Period: " + DateTimeUtil.formatDateStandard(startDate) + " to " + DateTimeUtil.formatDateStandard(endDate));
        System.out.println("Payroll Type: " + (payPeriodType == PayrollDateManager.MID_MONTH ? "Mid-month" : "End-month"));
        Map<LocalDate, Map<String, Object>> dailyAttendance = attendanceReader.getDailyAttendanceForEmployee(employee.getEmployeeId(), startDate, endDate);
        if (dailyAttendance.isEmpty()) {
            System.out.println("\nNo attendance records found for this period.");
            return null;
        }
        System.out.println("\n--- ATTENDANCE DETAILS ---");
        return processAttendanceRecords(dailyAttendance);
    }

    public void displaySalaryDetails(Employee employee) {
        payrollProcessor.displaySalaryDetails(employee);
    }

    public void displayAttendanceOptions(Employee employee) {
        System.out.println("\n===== ATTENDANCE OPTIONS =====");
        System.out.println("Employee: " + employee.getFullName());
        System.out.println("\nSelect view type:");
        System.out.println("1. Daily Attendance");
        System.out.println("2. Weekly Summary");
        System.out.print("Enter choice (1-2): ");
    }

    public void displayDailyAttendance(Employee employee, LocalDate startDate, LocalDate endDate) {
        System.out.println("\n===== DAILY ATTENDANCE =====");
        System.out.println("Employee: " + employee.getFullName());
        System.out.println("Period: " + DateTimeUtil.formatDateStandard(startDate) + " to " + DateTimeUtil.formatDateStandard(endDate));
        Map<LocalDate, Map<String, Object>> dailyAttendance = attendanceReader.getDailyAttendanceForEmployee(employee.getEmployeeId(), startDate, endDate);
        if (dailyAttendance.isEmpty()) {
            System.out.println("\nNo attendance records found for this period.");
            return;
        }
        System.out.println("\n--- ATTENDANCE DETAILS ---");
        processAttendanceRecords(dailyAttendance);
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }

    public void displayWeeklyAttendance(Employee employee, LocalDate startDate, LocalDate endDate) {
        System.out.println("\n===== WEEKLY ATTENDANCE =====");
        System.out.println("Employee: " + employee.getFullName());
        System.out.println("Period: " + DateTimeUtil.formatDateStandard(startDate) + " to " + DateTimeUtil.formatDateStandard(endDate));
        Map<String, Map<String, Double>> weeklyAttendance = attendanceReader.getWeeklyAttendanceForEmployee(employee.getEmployeeId(), startDate, endDate);
        if (weeklyAttendance.isEmpty()) {
            System.out.println("\nNo attendance records found for this period.");
            return;
        }
        System.out.println("\n--- WEEKLY SUMMARY ---");
        System.out.printf("%-30s %-10s %-12s %-12s %-10s\n", "Week", "Hours", "Overtime", "Late", "Unpaid Abs");
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
            totalHours += hours;
            totalOvertimeHours += overtimeHours;
            totalLateMinutes += lateMinutes;
            totalUnpaidAbsences += unpaidAbsences;
            System.out.printf("%-30s %-10.2f %-12.2f %-12s %-10d\n", weekLabel, hours, overtimeHours, formatTimeDuration(lateMinutes), unpaidAbsences);
        }
        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("%-30s %-10.2f %-12.2f %-12s %-10d\n", "TOTALS:", totalHours, totalOvertimeHours, formatTimeDuration(totalLateMinutes), totalUnpaidAbsences);
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }

    public void displayPayrollCalendar(int year, int month) {
        System.out.println("\n===== PAYROLL CALENDAR =====");
        System.out.println("Year: " + year);
        System.out.println("Month: " + PayrollDateManager.getMonthName(month));
        LocalDate midMonth = PayrollDateManager.getPayrollDate(year, month, PayrollDateManager.MID_MONTH);
        LocalDate endMonth = PayrollDateManager.getPayrollDate(year, month, PayrollDateManager.END_MONTH);
        LocalDate[] midCutoff = PayrollDateManager.getCutoffDateRange(midMonth, PayrollDateManager.MID_MONTH);
        LocalDate[] endCutoff = PayrollDateManager.getCutoffDateRange(endMonth, PayrollDateManager.END_MONTH);
        System.out.println("\n--- MID-MONTH PAYROLL ---");
        System.out.println("Payroll Date: " + DateTimeUtil.formatDate(midMonth));
        System.out.println("Cutoff Period: " + DateTimeUtil.formatDate(midCutoff[0]) + " to " + DateTimeUtil.formatDate(midCutoff[1]));
        System.out.println("Deductions: SSS, PhilHealth, Pag-IBIG");
        System.out.println("\n--- END-MONTH PAYROLL ---");
        System.out.println("Payroll Date: " + DateTimeUtil.formatDate(endMonth));
        System.out.println("Cutoff Period: " + DateTimeUtil.formatDate(endCutoff[0]) + " to " + DateTimeUtil.formatDate(endCutoff[1]));
        System.out.println("Deductions: Withholding Tax");
        displayMonthHolidays(year, month);
    }

    private void displayMonthHolidays(int year, int month) {
        Map<LocalDate, String> holidays = holidayManager.getHolidaysForYear(year);
        List<LocalDate> monthHolidays = new ArrayList<>();
        for (LocalDate date : holidays.keySet()) {
            if (date.getYear() == year && date.getMonthValue() == month) {
                monthHolidays.add(date);
            }
        }
        if (!monthHolidays.isEmpty()) {
            System.out.println("\n--- HOLIDAYS THIS MONTH ---");
            monthHolidays.sort(LocalDate::compareTo);
            for (LocalDate date : monthHolidays) {
                String holidayName = holidays.get(date);
                System.out.println(DateTimeUtil.formatDate(date) + " - " + holidayName);
            }
        }
    }
}
