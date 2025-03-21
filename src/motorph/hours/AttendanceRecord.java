// File: motorph/hours/AttendanceRecord.java
package motorph.hours;

import motorph.util.DateTimeUtil;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;

/**
 * Represents a single attendance record for an employee
 * Tracks time in, time out, and calculates lateness and undertime
 */
public class AttendanceRecord {
    // Employee information
    private String employeeId;
    private String lastName;
    private String firstName;

    // Attendance information
    private LocalDate date;
    private LocalTime timeIn;
    private LocalTime timeOut;

    // Work schedule times (standard company schedule)
    public static final LocalTime STANDARD_START_TIME = LocalTime.of(8, 0); // 8:00 AM
    public static final LocalTime GRACE_PERIOD_END = LocalTime.of(8, 10);   // 8:10 AM (10-minute grace period)
    public static final LocalTime STANDARD_END_TIME = LocalTime.of(17, 0);  // 5:00 PM

    /**
     * Create record from CSV data array
     *
     * @param data Array of strings from CSV row
     */
    public AttendanceRecord(String[] data) {
        if (data.length >= 6) {
            this.employeeId = data[0].trim();
            this.lastName = data[1].trim();
            this.firstName = data[2].trim();
            this.date = DateTimeUtil.parseDate(data[3].trim());
            this.timeIn = DateTimeUtil.parseTime(data[4].trim());
            this.timeOut = DateTimeUtil.parseTime(data[5].trim());
        } else {
            System.out.println("Warning: Not enough attendance data fields");
        }
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
     * Check if employee arrived late (after grace period)
     *
     * @return true if employee arrived after 8:10 AM
     */
    public boolean isLate() {
        return timeIn != null && timeIn.isAfter(GRACE_PERIOD_END);
    }

    /**
     * Check if employee left early (undertime)
     *
     * @return true if employee left before 5:00 PM
     */
    public boolean isUndertime() {
        return timeOut != null && timeOut.isBefore(STANDARD_END_TIME);
    }

    /**
     * Get minutes late
     *
     * @return Number of minutes late (0 if not late)
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
     * @return Number of minutes undertime (0 if not undertime)
     */
    public double getUndertimeMinutes() {
        if (!isUndertime() || timeOut == null) {
            return 0.0;
        }

        Duration undertimeBy = Duration.between(timeOut, STANDARD_END_TIME);
        return undertimeBy.toMinutes();
    }

    /**
     * Calculate the total hours worked for this record
     *
     * @return Total hours worked (including overtime)
     */
    public double getTotalHoursWorked() {
        if (timeIn == null || timeOut == null || timeOut.isBefore(timeIn)) {
            return 0.0;
        }

        Duration workDuration = Duration.between(timeIn, timeOut);
        return workDuration.toMinutes() / 60.0;
    }

    /**
     * Calculate regular hours worked (capped at 8 hours)
     *
     * @return Regular hours worked (maximum 8 hours)
     */
    public double getRegularHoursWorked() {
        return Math.min(getTotalHoursWorked(), 8.0);
    }

    /**
     * Calculate overtime hours
     *
     * @return Overtime hours (0 if late)
     */
    public double getOvertimeHours() {
        // Late employees don't get overtime
        if (isLate()) {
            return 0.0;
        }

        double totalHours = getTotalHoursWorked();
        return totalHours > 8.0 ? (totalHours - 8.0) : 0.0;
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

    public String getFormattedDate() {
        return DateTimeUtil.formatDateStandard(date);
    }

    public String getFormattedTimeIn() {
        return DateTimeUtil.formatTimeStandard(timeIn);
    }

    public String getFormattedTimeOut() {
        return DateTimeUtil.formatTimeStandard(timeOut);
    }

    @Override
    public String toString() {
        return "Date: " + getFormattedDate() +
                ", Employee: " + getFullName() +
                ", Time In: " + getFormattedTimeIn() +
                ", Time Out: " + getFormattedTimeOut();
    }
}