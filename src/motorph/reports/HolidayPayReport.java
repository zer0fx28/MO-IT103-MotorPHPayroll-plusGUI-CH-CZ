// File: motorph/reports/HolidayPayReport.java
package motorph.reports;

import motorph.employee.Employee;
import motorph.employee.EmployeeDataReader;
import motorph.holidays.HolidayManager;
import motorph.hours.AttendanceReader;
import motorph.hours.AttendanceRecord;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates detailed reports on holiday pay calculations
 */
public class HolidayPayReport {
    private final EmployeeDataReader employeeDataReader;
    private final AttendanceReader attendanceReader;
    private final HolidayManager holidayManager;
    private final DateTimeFormatter dateFormatter;

    public HolidayPayReport(String employeeFilePath, String attendanceFilePath) {
        this.employeeDataReader = new EmployeeDataReader(employeeFilePath);
        this.attendanceReader = new AttendanceReader(attendanceFilePath);
        this.holidayManager = new HolidayManager();
        this.dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    }

    public void generateReportForEmployee(String employeeId, LocalDate startDate, LocalDate endDate) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            System.out.println("Error: Employee ID cannot be empty");
            return;
        }

        if (startDate == null || endDate == null) {
            System.out.println("Error: Invalid date range");
            return;
        }

        if (endDate.isBefore(startDate)) {
            System.out.println("Error: End date cannot be before start date");
            return;
        }

        Employee employee = employeeDataReader.findEmployee(employeeId);
        if (employee == null) {
            System.out.println("Employee not found: " + employeeId);
            return;
        }

        List<AttendanceRecord> records = attendanceReader.getRecordsForEmployee(employeeId);
        if (records.isEmpty()) {
            System.out.println("No attendance records found for employee: " + employeeId);
            return;
        }

        Map<LocalDate, String> holidays = holidayManager.getHolidaysInRange(startDate, endDate);
        if (holidays.isEmpty()) {
            System.out.println("No holidays found within date range.");
            return;
        }

        Map<LocalDate, AttendanceRecord> recordsByDate = new HashMap<>();
        for (AttendanceRecord record : records) {
            LocalDate date = record.getDate();
            if ((date.isEqual(startDate) || date.isAfter(startDate)) &&
                    (date.isEqual(endDate) || date.isBefore(endDate))) {
                recordsByDate.put(date, record);
            }
        }

        printReportHeader(employee, startDate, endDate);

        boolean foundHolidayWork = false;
        for (Map.Entry<LocalDate, String> entry : holidays.entrySet()) {
            LocalDate holidayDate = entry.getKey();
            String holidayName = entry.getValue();

            if (recordsByDate.containsKey(holidayDate)) {
                foundHolidayWork = true;

                AttendanceRecord record = recordsByDate.get(holidayDate);
                double dailyRate = employee.getBasicSalary() / 22;
                boolean isRegularHoliday = holidayManager.isRegularHoliday(holidayDate);
                boolean isLate = record.isLate();

                double hoursWorked = calculateHoursWorked(record);
                double overtimeHours = calculateOvertimeHours(record, isLate);

                boolean isRestDay = isRestDay(holidayDate);
                double holidayPay = holidayManager.calculateHolidayPay(
                        dailyRate, hoursWorked, isRegularHoliday, isRestDay, overtimeHours, isLate);

                printHolidayPayDetails(
                        holidayDate, holidayName, isRegularHoliday, record,
                        dailyRate, hoursWorked, overtimeHours, isLate, isRestDay, holidayPay);
            }
        }

        if (!foundHolidayWork) {
            System.out.println("\nNo work recorded on holidays during this period.");
        }
    }

    private void printReportHeader(Employee employee, LocalDate startDate, LocalDate endDate) {
        System.out.println("\n===== HOLIDAY PAY REPORT =====");
        System.out.println("Employee: " + employee.getFullName() + " (ID: " + employee.getEmployeeId() + ")");
        System.out.println("Position: " + employee.getPosition());
        System.out.println("Basic Salary: ₱" + String.format("%,.2f", employee.getBasicSalary()) + "/month");
        System.out.println("Hourly Rate: ₱" + String.format("%.2f", employee.getHourlyRate()) + "/hour");
        System.out.println("Period: " + startDate.format(dateFormatter) + " to " + endDate.format(dateFormatter));
        System.out.println("\nHOLIDAY WORK BREAKDOWN:");
    }

    private void printHolidayPayDetails(
            LocalDate date, String holidayName, boolean isRegularHoliday,
            AttendanceRecord record, double dailyRate, double hoursWorked,
            double overtimeHours, boolean isLate, boolean isRestDay, double holidayPay) {

        System.out.println("\n-----------------------------------------");
        System.out.println("Date: " + date.format(dateFormatter));
        System.out.println("Holiday: " + holidayName);
        System.out.println("Holiday Type: " + (isRegularHoliday ? "Regular Holiday" : "Special Non-Working Holiday"));
        System.out.println("Time In: " + record.getTimeIn());
        System.out.println("Time Out: " + record.getTimeOut());
        System.out.println("Hours Worked: " + String.format("%.2f", hoursWorked) + " hours");
        System.out.println("Overtime Hours: " + String.format("%.2f", overtimeHours) + " hours");
        System.out.println("Late Status: " + (isLate ? "Late (ineligible for overtime)" : "On Time"));
        System.out.println("Rest Day: " + (isRestDay ? "Yes (additional 30% premium)" : "No"));
        System.out.println("Daily Rate: ₱" + String.format("%.2f", dailyRate));

        System.out.println("\nPAY CALCULATION BREAKDOWN:");

        if (isRegularHoliday) {
            if (hoursWorked == 0) {
                System.out.println("Base Holiday Pay: ₱" + String.format("%.2f", dailyRate) +
                        " (100% of daily rate)");
            } else {
                double baseHolidayPay = Math.min(hoursWorked, 8.0) * (dailyRate / 8) * 2;
                System.out.println("Base Holiday Pay: ₱" + String.format("%.2f", baseHolidayPay) +
                        " (" + Math.min(hoursWorked, 8.0) + " hours × 200% of hourly rate)");

                if (overtimeHours > 0 && !isLate) {
                    double hourlyRate = dailyRate / 8;
                    double overtimeRate = hourlyRate * 2 * 1.3;
                    if (!isLate) {
                        overtimeRate *= 1.25;
                    }
                    double overtimePay = overtimeHours * overtimeRate;
                    System.out.println("Overtime Pay: ₱" + String.format("%.2f", overtimePay));
                }

                if (isRestDay) {
                    System.out.println("Rest Day Premium: 30% additional on total holiday pay");
                }
            }
        } else {
            if (hoursWorked == 0) {
                System.out.println("No pay for non-working special non-working holiday");
            } else {
                double baseHolidayPay = Math.min(hoursWorked, 8.0) * (dailyRate / 8) * 1.3;
                System.out.println("Base Holiday Pay: ₱" + String.format("%.2f", baseHolidayPay) +
                        " (" + Math.min(hoursWorked, 8.0) + " hours × 130% of hourly rate)");

                if (overtimeHours > 0 && !isLate) {
                    double hourlyRate = dailyRate / 8;
                    double overtimeRate = hourlyRate * 1.3;
                    if (!isLate) {
                        overtimeRate *= 1.25;
                    }
                    double overtimePay = overtimeHours * overtimeRate;
                    System.out.println("Overtime Pay: ₱" + String.format("%.2f", overtimePay));
                }

                if (isRestDay) {
                    System.out.println("Rest Day Premium: 30% additional on total holiday pay");
                }
            }
        }

        System.out.println("TOTAL HOLIDAY PAY: ₱" + String.format("%.2f", holidayPay));
    }

    private double calculateHoursWorked(AttendanceRecord record) {
        if (record == null || record.getTimeIn() == null || record.getTimeOut() == null) {
            return 0.0;
        }

        double totalMinutes = record.getWorkDuration().toMinutes();
        return Math.min(totalMinutes / 60.0, 8.0);
    }

    private double calculateOvertimeHours(AttendanceRecord record, boolean isLate) {
        if (record == null || record.getTimeIn() == null || record.getTimeOut() == null || isLate) {
            return 0.0;
        }

        double totalMinutes = record.getWorkDuration().toMinutes();
        double totalHours = totalMinutes / 60.0;

        return Math.max(0, totalHours - 8.0);
    }

    private boolean isRestDay(LocalDate date) {
        return date.getDayOfWeek().getValue() == 7;
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: HolidayPayReport <employeeId> <startDate MM/dd/yyyy> <endDate MM/dd/yyyy>");
            return;
        }

        String employeeId = args[0];

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            LocalDate startDate = LocalDate.parse(args[1], formatter);
            LocalDate endDate = LocalDate.parse(args[2], formatter);

            if (endDate.isBefore(startDate)) {
                System.out.println("Error: End date cannot be before start date");
                return;
            }

            HolidayPayReport report = new HolidayPayReport(
                    "resources/MotorPH Employee Data - Employee Details.csv",
                    "resources/MotorPH Employee Data - Attendance Record.csv");

            report.generateReportForEmployee(employeeId, startDate, endDate);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("Usage: HolidayPayReport <employeeId> <startDate MM/dd/yyyy> <endDate MM/dd/yyyy>");
        }
    }
}