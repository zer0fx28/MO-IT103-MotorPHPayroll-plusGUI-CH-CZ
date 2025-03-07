// File: motorph/hours/AttendanceRecord.java
package motorph.hours;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;

/**
 * Represents an employee attendance record for a specific date
 */
public class AttendanceRecord {
    private String employeeId;
    private String lastName;
    private String firstName;
    private LocalDate date;
    private LocalTime timeIn;
    private LocalTime timeOut;

    // Standard work schedule (moved to constants for better reuse)
    public static final LocalTime STANDARD_START_TIME = LocalTime.of(8, 0); // 8:00 AM
    public static final LocalTime GRACE_PERIOD_END = LocalTime.of(8, 10);   // 8:10 AM (grace period)
    public static final LocalTime STANDARD_END_TIME = LocalTime.of(17, 0);  // 5:00 PM

    // Date and time formatters
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Constructor with array data (typically from CSV)
     * @param data Array containing employee data
     */
    public AttendanceRecord(String[] data) {
        if (data.length >= 6) {
            this.employeeId = data[0];
            this.lastName = data[1];
            this.firstName = data[2];
            this.date = parseDate(data[3]);
            this.timeIn = parseTime(data[4]);
            this.timeOut = parseTime(data[5]);
        }
    }

    /**
     * No-args constructor for creating a blank record
     */
    public AttendanceRecord() {
        this.employeeId = "";
        this.lastName = "";
        this.firstName = "";
        this.date = LocalDate.now();
        this.timeIn = LocalTime.of(0, 0);
        this.timeOut = LocalTime.of(0, 0);
    }

    /**
     * Parse date string to LocalDate
     * @param dateStr Date string in MM/dd/yyyy format
     * @return LocalDate object, or today's date if parsing fails
     */
    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DATE_FORMAT);
        } catch (Exception e) {
            System.out.println("Error parsing date: " + dateStr);
            return LocalDate.now(); // Default to today on error
        }
    }

    /**
     * Parse time string to LocalTime
     * @param timeStr Time string in various formats
     * @return LocalTime object, or midnight if parsing fails
     */
    private LocalTime parseTime(String timeStr) {
        try {
            // Handle cases where time might be without leading zeros
            if (timeStr.length() == 4 && !timeStr.contains(":")) {  // Format like "0800"
                return LocalTime.parse(timeStr.substring(0, 2) + ":" + timeStr.substring(2, 4), TIME_FORMAT);
            } else if (timeStr.length() == 3 && !timeStr.contains(":")) {  // Format like "800"
                return LocalTime.parse("0" + timeStr.substring(0, 1) + ":" + timeStr.substring(1, 3), TIME_FORMAT);
            }

            // Handle standard time format
            return LocalTime.parse(timeStr, TIME_FORMAT);
        } catch (Exception e) {
            System.out.println("Error parsing time: " + timeStr + " - " + e.getMessage());
            return LocalTime.of(0, 0); // Default to 00:00 on error
        }
    }

    // Getters and Setters
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getFullName() { return firstName + " " + lastName; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getTimeIn() { return timeIn; }
    public void setTimeIn(LocalTime timeIn) { this.timeIn = timeIn; }

    public LocalTime getTimeOut() { return timeOut; }
    public void setTimeOut(LocalTime timeOut) { this.timeOut = timeOut; }

    /**
     * Check if employee is late (after grace period)
     * @return true if employee arrived after grace period
     */
    public boolean isLate() {
        return timeIn != null && timeIn.isAfter(GRACE_PERIOD_END);
    }

    /**
     * Get the number of minutes late
     * @return Minutes past grace period, or 0 if not late
     */
    public double getLateMinutes() {
        if (!isLate() || timeIn == null) {
            return 0.0;
        }

        Duration lateBy = Duration.between(GRACE_PERIOD_END, timeIn);
        return lateBy.toMinutes();
    }

    @Override
    public String toString() {
        return "Date: " + date +
                ", Employee: " + getFullName() +
                ", Time In: " + timeIn +
                ", Time Out: " + timeOut;
    }
}