// File: motorph/hours/AttendanceRecord.java
package motorph.hours;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;

/**
 * Keeps track of employee attendance for a day
 */
public class AttendanceRecord {
    private String employeeId;
    private String lastName;
    private String firstName;
    private LocalDate date;
    private LocalTime timeIn;
    private LocalTime timeOut;

    // Work schedule times
    public static final LocalTime STANDARD_START_TIME = LocalTime.of(8, 0); // 8:00 AM
    public static final LocalTime GRACE_PERIOD_END = LocalTime.of(8, 10);   // 8:10 AM
    public static final LocalTime STANDARD_END_TIME = LocalTime.of(17, 0);  // 5:00 PM

    // Date and time formats
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Create record from CSV data
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
     * Create empty record
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
     * Convert date string to actual date
     */
    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DATE_FORMAT);
        } catch (Exception e) {
            System.out.println("Bad date format: " + dateStr);
            return LocalDate.now(); // Use today if format is wrong
        }
    }

    /**
     * Convert time string to actual time
     */
    private LocalTime parseTime(String timeStr) {
        try {
            // Handle simple time formats
            if (timeStr.length() == 4 && !timeStr.contains(":")) {  // Like "0800"
                return LocalTime.parse(timeStr.substring(0, 2) + ":" + timeStr.substring(2, 4), TIME_FORMAT);
            } else if (timeStr.length() == 3 && !timeStr.contains(":")) {  // Like "800"
                return LocalTime.parse("0" + timeStr.substring(0, 1) + ":" + timeStr.substring(1, 3), TIME_FORMAT);
            }

            // Handle normal time format
            return LocalTime.parse(timeStr, TIME_FORMAT);
        } catch (Exception e) {
            System.out.println("Bad time format: " + timeStr);
            return LocalTime.of(0, 0); // Use midnight if format is wrong
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
     * Check if employee arrived late
     */
    public boolean isLate() {
        return timeIn != null && timeIn.isAfter(GRACE_PERIOD_END);
    }

    /**
     * Check if employee left early (undertime)
     */
    public boolean isUndertime() {
        return timeOut != null && timeOut.isBefore(STANDARD_END_TIME);
    }

    /**
     * Get minutes late
     */
    public double getLateMinutes() {
        if (!isLate() || timeIn == null) {
            return 0.0;
        }

        Duration lateBy = Duration.between(GRACE_PERIOD_END, timeIn);
        return lateBy.toMinutes();
    }

    /**
     * Get minutes undertime
     */
    public double getUndertimeMinutes() {
        if (!isUndertime() || timeOut == null) {
            return 0.0;
        }

        Duration undertimeBy = Duration.between(timeOut, STANDARD_END_TIME);
        return undertimeBy.toMinutes();
    }

    @Override
    public String toString() {
        return "Date: " + date +
                ", Employee: " + getFullName() +
                ", Time In: " + timeIn +
                ", Time Out: " + timeOut;
    }
}