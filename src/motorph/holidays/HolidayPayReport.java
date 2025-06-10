package motorph.holidays;

import motorph.employee.EmployeeDataReader;
import motorph.employee.Employee;
import motorph.hours.AttendanceReader;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Report for holiday pay calculations
 */
public class HolidayPayReport {
    private EmployeeDataReader EmployeeDataReader;
    private AttendanceReader attendanceReader;
    private HolidayManager holidayManager;

    /**
     * Constructor
     */
    public HolidayPayReport(String employeeFilePath, String attendanceFilePath) {
        this.EmployeeDataReader = new EmployeeDataReader(employeeFilePath);
        this.attendanceReader = new AttendanceReader(attendanceFilePath);
        this.holidayManager = new HolidayManager();
    }

    /**
     * Generate holiday pay report for a specific month
     */
    public void generateReport(int year, int month) {
        // Validate inputs
        if (month < 1 || month > 12) {
            System.out.println("Error: Month must be between 1 and 12");
            return;
        }

        String monthName = Month.of(month).getDisplayName(TextStyle.FULL, Locale.US);
        System.out.println("\n===== HOLIDAY PAY REPORT =====");
        System.out.println("Month: " + monthName + " " + year);

        // Get holidays for the month
        Map<LocalDate, String> holidays = getHolidaysForMonth(year, month);

        if (holidays.isEmpty()) {
            System.out.println("\nNo holidays found for " + monthName + " " + year);
            return;
        }

        // Display holidays
        System.out.println("\nHolidays in " + monthName + ":");
        for (Map.Entry<LocalDate, String> entry : holidays.entrySet()) {
            System.out.println("- " + formatDate(entry.getKey()) + ": " + entry.getValue());
        }

        // Get all employees
        List<Employee> employees = EmployeeDataReader.getAllEmployees();

        // Display holiday pay summary
        System.out.println("\nHOLIDAY PAY SUMMARY:");
        System.out.printf("%-5s %-20s %-15s %-15s %-15s\n",
                "ID", "Employee Name", "Regular Pay", "Holiday Pay", "Total Pay");
        System.out.println("---------------------------------------------------------------");

        double totalRegularPay = 0;
        double totalHolidayPay = 0;
        double grandTotal = 0;

        for (Employee employee : employees) {
            // Calculate holiday pay for this employee
            double regularPay = calculateRegularPay(employee, year, month);
            double holidayPay = calculateHolidayPay(employee, holidays);
            double totalPay = regularPay + holidayPay;

            // Display employee row
            System.out.printf("%-5s %-20s ₱%-14.2f ₱%-14.2f ₱%-14.2f\n",
                    employee.getEmployeeId(),
                    employee.getLastName() + ", " + employee.getFirstName(),
                    regularPay,
                    holidayPay,
                    totalPay);

            // Update totals
            totalRegularPay += regularPay;
            totalHolidayPay += holidayPay;
            grandTotal += totalPay;
        }

        // Display totals
        System.out.println("---------------------------------------------------------------");
        System.out.printf("%-26s ₱%-14.2f ₱%-14.2f ₱%-14.2f\n",
                "TOTALS:",
                totalRegularPay,
                totalHolidayPay,
                grandTotal);
    }

    /**
     * Get holidays for a specific month
     */
    private Map<LocalDate, String> getHolidaysForMonth(int year, int month) {
        Map<LocalDate, String> monthHolidays = new HashMap<>();
        Map<LocalDate, String> yearHolidays = holidayManager.getHolidaysForYear(year);

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        for (Map.Entry<LocalDate, String> entry : yearHolidays.entrySet()) {
            LocalDate date = entry.getKey();
            if (!date.isBefore(startDate) && !date.isAfter(endDate)) {
                monthHolidays.put(date, entry.getValue());
            }
        }

        return monthHolidays;
    }

    /**
     * Calculate regular pay for an employee
     */
    private double calculateRegularPay(Employee employee, int year, int month) {
        // Simple calculation based on daily rate
        YearMonth yearMonth = YearMonth.of(year, month);
        int workingDays = 22; // Approximate working days in a month

        double dailyRate = employee.getDailyRate();
        return dailyRate * workingDays;
    }

    /**
     * Calculate holiday pay for an employee
     */
    private double calculateHolidayPay(Employee employee, Map<LocalDate, String> holidays) {
        double totalHolidayPay = 0;
        double dailyRate = employee.getDailyRate();

        for (Map.Entry<LocalDate, String> entry : holidays.entrySet()) {
            LocalDate holidayDate = entry.getKey();
            String holidayType = entry.getValue();

            // Determine holiday pay multiplier based on type
            double multiplier = 1.0; // Default for regular holiday
            if (holidayType.contains("Special")) {
                multiplier = 0.3; // 30% for special non-working holiday
            }

            totalHolidayPay += dailyRate * multiplier;
        }

        return totalHolidayPay;
    }

    /**
     * Format date as MM/dd/yyyy
     */
    private String formatDate(LocalDate date) {
        return date.getMonthValue() + "/" + date.getDayOfMonth() + "/" + date.getYear();
    }
}