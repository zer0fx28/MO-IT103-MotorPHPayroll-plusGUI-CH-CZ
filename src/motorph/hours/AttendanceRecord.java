// File: motorph/hours/AttendanceRecord.java
package motorph.hours;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;

/**
 * Keeps track of employee attendance for a day
 *
 * This class represents a single day's attendance record for an employee,
 * including clock-in and clock-out times, and provides methods to check
 * if the employee was late or left early.
 */
public class AttendanceRecord {
    // Employee identification
    private String employeeId;
    private String lastName;
    private String firstName;

    // Attendance data
    private LocalDate date;
    private LocalTime timeIn;
    private LocalTime timeOut;

    // Work schedule time constants
    public static final LocalTime STANDARD_START_TIME = LocalTime.of(8, 0); // 8:00 AM
    public static final LocalTime GRACE_PERIOD_END = LocalTime.of(8, 10);   // 8:10 AM
    public static final LocalTime STANDARD_END_TIME = LocalTime.of(17, 0);  // 5:00 PM

    // Date and time formats
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Create attendance record from CSV data
     *
     * @param data Array of CSV data values
     */
    public AttendanceRecord(String[] data) {
        if (data == null) {
            throw new IllegalArgumentException("Data array cannot be null");
        }

        if (data.length < 6) {
            throw new IllegalArgumentException("Insufficient data for attendance record. Expected 6 elements, got " + data.length);
        }

        this.employeeId = data[0];
        this.lastName = data[1];
        this.firstName = data[2];
        this.date = parseDate(data[3]);
        this.timeIn = parseTime(data[4]);
        this.timeOut = parseTime(data[5]);
    }

    /**
     * Create empty attendance record
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
     * Convert date string to LocalDate
     *
     * @param dateStr Date string in MM/dd/yyyy format
     * @return Parsed LocalDate or current date if parsing fails
     */
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            System.out.println("Warning: Empty or null date string");
            return LocalDate.now();
        }

        try {
            return LocalDate.parse(dateStr.trim(), DATE_FORMAT);
        } catch (Exception e) {
            System.out.println("Error parsing date: " + dateStr + " - " + e.getMessage());
            return LocalDate.now(); // Use today if format is wrong
        }
    }

    /**
     * Convert time string to LocalTime
     *
     * @param timeStr Time string in various formats
     * @return Parsed LocalTime or midnight if parsing fails
     */
    private LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            System.out.println("Warning: Empty or null time string");
            return LocalTime.of(0, 0);
        }

        String cleanTimeStr = timeStr.trim();

        try {
            // Handle simple time formats
            if (cleanTimeStr.length() == 4 && !cleanTimeStr.contains(":")) {  // Like "0800"
                return LocalTime.parse(cleanTimeStr.substring(0, 2) + ":" + cleanTimeStr.substring(2, 4), TIME_FORMAT);
            } else if (cleanTimeStr.length() == 3 && !cleanTimeStr.contains(":")) {  // Like "800"
                return LocalTime.parse("0" + cleanTimeStr.substring(0, 1) + ":" + cleanTimeStr.substring(1, 3), TIME_FORMAT);
            }

            // Handle normal time format
            return LocalTime.parse(cleanTimeStr, TIME_FORMAT);
        } catch (Exception e) {
            System.out.println("Error parsing time: " + timeStr + " - " + e.getMessage());
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
     *
     * @return true if employee arrived after grace period
     */
    public boolean isLate() {
        return timeIn != null && timeIn.isAfter(GRACE_PERIOD_END);
    }

    /**
     * Check if employee left early (undertime)
     *
     * @return true if employee left before standard end time
     */
    public boolean isUndertime() {
        return timeOut != null && timeOut.isBefore(STANDARD_END_TIME);
    }

    /**
     * Get minutes late
     *
     * @return Minutes late (0 if not late)
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
     *
     * @return Minutes of undertime (0 if not undertime)
     */
    public double getUndertimeMinutes() {
        if (!isUndertime() || timeOut == null) {
            return 0.0;
        }

        Duration undertimeBy = Duration.between(timeOut, STANDARD_END_TIME);
        return undertimeBy.toMinutes();
    }

    /**
     * Calculate total work duration
     *
     * @return Duration between time in and time out, or zero if invalid times
     */
    public Duration getWorkDuration() {
        if (timeIn == null || timeOut == null || timeOut.isBefore(timeIn)) {
            return Duration.ZERO;
        }

        return Duration.between(timeIn, timeOut);
    }

    /**
     * Convert attendance record to string
     *
     * @return String representation of the record
     */
    @Override
    public String toString() {
        return "Date: " + date +
                ", Employee: " + getFullName() +
                ", Time In: " + timeIn +
                ", Time Out: " + timeOut;
    }
}