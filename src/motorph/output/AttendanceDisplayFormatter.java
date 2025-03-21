// File: motorph/output/AttendanceDisplayFormatter.java
package motorph.output;

import motorph.holidays.HolidayManager;
import motorph.hours.AttendanceRecord;
import motorph.hours.WorkHoursCalculator;
import motorph.util.TimeConverter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Formatter for attendance report displays with optimized column widths
 */
public class AttendanceDisplayFormatter {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final LocalTime STANDARD_START_TIME = LocalTime.of(8, 0); // 8:00 AM
    private static final LocalTime GRACE_PERIOD_END = LocalTime.of(8, 10);   // 8:10 AM
    private static final LocalTime STANDARD_END_TIME = LocalTime.of(17, 0);  // 5:00 PM
    private static final int LUNCH_BREAK_MINUTES = 60; // 1 hour lunch break
    private static final double STANDARD_HOURS = 8.0;  // 8 hours standard workday
    private static final HolidayManager holidayManager = new HolidayManager();

    /**
     * Display an attendance report with optimized column widths
     */
    public static void displayAttendanceReport(Map<LocalDate, Map<String, Object>> dailyAttendance) {
        if (dailyAttendance == null || dailyAttendance.isEmpty()) {
            System.out.println("No attendance data available.");
            return;
        }

        // Print legend for abbreviated column headers
        System.out.println("LEGEND:");
        System.out.println("Std - Standard Hours | In - Time In | Out - Time Out | Hol - Holiday | Prem - Premium Rate");
        System.out.println("L - Late | U/T - Undertime | UA - Unapproved Absence | AL - Approved Leave");
        System.out.println("Act - Actual Hours | O/T - Overtime Hours");
        System.out.println();

        // Print header with abbreviated column names
        System.out.printf("%-12s %-6s %-7s %-7s %-6s %-6s %-7s %-7s %-7s %-7s %-7s %-6s\n",
                "Date", "Std", "In", "Out", "Hol", "Prem", "L", "U/T", "UA", "AL", "Act", "O/T");
        System.out.println("----------------------------------------------------------------------------------------------");

        // Initialize totals
        double totalStandardHours = 0;
        double totalActualHours = 0;
        double totalOvertimeHours = 0;
        double totalLateMinutes = 0;
        double totalUndertimeMinutes = 0;
        double totalHolidayPay = 0;
        int totalUnapprovedAbsences = 0;
        int totalApprovedLeaves = 0;
        int holidayWorked = 0;

        // Process each day
        for (Map.Entry<LocalDate, Map<String, Object>> entry : dailyAttendance.entrySet()) {
            LocalDate date = entry.getKey();
            Map<String, Object> dayData = entry.getValue();

            // Extract data with proper null checks
            String timeInStr = (String) dayData.getOrDefault("timeIn", "");
            String timeOutStr = (String) dayData.getOrDefault("timeOut", "");
            String absenceType = (String) dayData.getOrDefault("absenceType", "");

            // Always use standard 8 hours for regular workdays
            double standardHours = STANDARD_HOURS;

            // Parse time strings to LocalTime for calculation
            LocalTime timeIn = null;
            LocalTime timeOut = null;
            boolean hasTimeRecords = false;

            try {
                if (!timeInStr.isEmpty() && !timeOutStr.isEmpty()) {
                    timeIn = LocalTime.parse(timeInStr, DateTimeFormatter.ofPattern("HH:mm"));
                    timeOut = LocalTime.parse(timeOutStr, DateTimeFormatter.ofPattern("HH:mm"));
                    hasTimeRecords = true;
                }
            } catch (Exception e) {
                // If time cannot be parsed, use default values for working days
                if (absenceType.isEmpty()) {
                    timeIn = STANDARD_START_TIME;
                    timeOut = STANDARD_END_TIME;
                    hasTimeRecords = true;
                }
            }

            // Check if day is a holiday
            boolean isRegularHoliday = holidayManager.isRegularHoliday(date);
            boolean isSpecialNonWorkingHoliday = holidayManager.isSpecialNonWorkingHoliday(date);
            boolean isHoliday = isRegularHoliday || isSpecialNonWorkingHoliday;

            String holidayType = "-";
            String holidayPremium = "-";

            if (isRegularHoliday) {
                holidayType = "Reg";
                holidayPremium = "200%";
                if (hasTimeRecords) {
                    holidayWorked++;
                }
            } else if (isSpecialNonWorkingHoliday) {
                holidayType = "Spec";
                holidayPremium = "130%";
                if (hasTimeRecords) {
                    holidayWorked++;
                }
            }

            // Default values for leave days or non-working days
            double lateMinutes = 0;
            double undertimeMinutes = 0;
            double actualHours = 0;
            double overtimeHours = 0;

            // For days with time records, calculate hours and deductions
            if (hasTimeRecords) {
                // Determine if employee is late (after grace period)
                boolean isLate = timeIn.isAfter(GRACE_PERIOD_END);

                // Calculate late minutes (if late)
                if (isLate) {
                    Duration lateDuration = Duration.between(STANDARD_START_TIME, timeIn);
                    lateMinutes = lateDuration.toMinutes();
                }

                // Calculate undertime minutes (if left before standard end time)
                if (timeOut.isBefore(STANDARD_END_TIME)) {
                    Duration undertimeDuration = Duration.between(timeOut, STANDARD_END_TIME);
                    undertimeMinutes = undertimeDuration.toMinutes();
                }

                // Calculate actual hours worked based on company policy
                if (isLate) {
                    // For late employees: actual hours = time from log-in to 5pm minus lunch
                    // Any time after 5pm is disregarded for late employees
                    LocalTime endTimeToUse = timeOut.isBefore(STANDARD_END_TIME) ? timeOut : STANDARD_END_TIME;
                    Duration workDuration = Duration.between(timeIn, endTimeToUse);
                    actualHours = workDuration.toMinutes() / 60.0;
                    // Subtract lunch break
                    actualHours -= (LUNCH_BREAK_MINUTES / 60.0);
                } else {
                    // For on-time employees: can earn overtime after 5pm
                    Duration regularDuration = Duration.between(timeIn,
                            timeOut.isBefore(STANDARD_END_TIME) ? timeOut : STANDARD_END_TIME);
                    actualHours = regularDuration.toMinutes() / 60.0;
                    // Subtract lunch break
                    actualHours -= (LUNCH_BREAK_MINUTES / 60.0);

                    // Calculate overtime (only for non-late employees)
                    if (timeOut.isAfter(STANDARD_END_TIME)) {
                        Duration overtimeDuration = Duration.between(STANDARD_END_TIME, timeOut);
                        overtimeHours = overtimeDuration.toMinutes() / 60.0;
                    }
                }

                // If holiday and worked, add to holiday pay calculation
                if (isHoliday) {
                    double rate = isRegularHoliday ? 2.0 : 1.3;
                    totalHolidayPay += actualHours * rate;
                }

                // Ensure actual hours isn't negative (in case of very early departure)
                actualHours = Math.max(0, actualHours);
            } else if (!absenceType.isEmpty()) {
                // For approved leave days, credit full standard hours
                if (absenceType.toLowerCase().contains("sick") ||
                        absenceType.toLowerCase().contains("vacation") ||
                        absenceType.toLowerCase().contains("leave")) {
                    actualHours = standardHours;
                }
            } else if (isRegularHoliday) {
                // For regular holidays not worked, credit full standard hours (100% pay)
                actualHours = standardHours;
            }

            // Format late and undertime for display
            String lateDisplay = lateMinutes > 0 ? formatMinutes(lateMinutes) : "-";
            String undertimeDisplay = undertimeMinutes > 0 ? formatMinutes(undertimeMinutes) : "-";

            // Categorize absence types
            String unapprovedAbsence = "-";
            String approvedLeave = "-";

            if (absenceType != null && !absenceType.isEmpty()) {
                String type = absenceType.toLowerCase();
                if (type.contains("unpaid") || type.contains("unauthoriz") || type.contains("unapproved")) {
                    unapprovedAbsence = "Yes";
                    totalUnapprovedAbsences++;
                } else if (type.contains("sick") || type.contains("vacation") || type.contains("leave") ||
                        type.contains("authorized")) {
                    approvedLeave = type.substring(0, Math.min(5, type.length()));
                    totalApprovedLeaves++;
                }
            }

            // Special display format for time in/out when on leave or holiday
            String displayTimeIn = hasTimeRecords ? timeInStr : "-";
            String displayTimeOut = hasTimeRecords ? timeOutStr : "-";

            // Print the row with optimized column widths
            System.out.printf("%-12s %-6.2f %-7s %-7s %-6s %-6s %-7s %-7s %-7s %-7s %-7.2f %-6.2f\n",
                    formatDate(date), standardHours, displayTimeIn, displayTimeOut,
                    holidayType, holidayPremium, lateDisplay, undertimeDisplay,
                    unapprovedAbsence, approvedLeave,
                    actualHours, overtimeHours);

            // Update totals
            totalStandardHours += standardHours;
            totalActualHours += actualHours;
            totalOvertimeHours += overtimeHours;
            totalLateMinutes += lateMinutes;
            totalUndertimeMinutes += undertimeMinutes;
        }

        // Print totals with corrected alignment
        System.out.println("----------------------------------------------------------------------------------------------");
        System.out.printf("%-12s %-6.2f %-7s %-7s %-6s %-6s %-7s %-7s %-7s %-7s %-7.2f %-6.2f\n",
                "TOTALS:", totalStandardHours, "", "", "", "", "", "", "", "", totalActualHours, totalOvertimeHours);

        // Additional summary
        System.out.println("----------------------------------------------------------------------------------------------");
        System.out.println("SUMMARY:");
        System.out.printf("Standard Hours: %.2f\n", totalStandardHours);

        if (totalLateMinutes > 0 || totalUndertimeMinutes > 0) {
            System.out.println("DEDUCTIONS:");
            if (totalLateMinutes > 0) {
                System.out.printf("  Late: %s (%.2f hours)\n",
                        formatMinutes(totalLateMinutes), totalLateMinutes / 60.0);
            }
            if (totalUndertimeMinutes > 0) {
                System.out.printf("  Undertime: %s (%.2f hours)\n",
                        formatMinutes(totalUndertimeMinutes), totalUndertimeMinutes / 60.0);
            }
            double totalDeductionHours = (totalLateMinutes + totalUndertimeMinutes) / 60.0;
            System.out.printf("  Total Deductions: %.2f hours\n", totalDeductionHours);
        }

        if (totalUnapprovedAbsences > 0 || totalApprovedLeaves > 0) {
            System.out.println("ABSENCES:");
            if (totalUnapprovedAbsences > 0) {
                System.out.printf("  Unapproved: %d day(s)\n", totalUnapprovedAbsences);
            }
            if (totalApprovedLeaves > 0) {
                System.out.printf("  Approved: %d day(s)\n", totalApprovedLeaves);
            }
        }

        // Holiday information summary
        if (holidayWorked > 0 || totalHolidayPay > 0) {
            System.out.println("HOLIDAY PAY:");
            System.out.printf("  Holidays Worked: %d day(s)\n", holidayWorked);
            if (totalHolidayPay > 0) {
                System.out.printf("  Estimated Holiday Premium: %.2f hours equivalent\n", totalHolidayPay);
            }

            // Holiday pay policy information
            System.out.println("\nHOLIDAY PAY POLICY:");
            System.out.println("  Regular Holidays (200%):");
            System.out.println("    • Not Working: 100% of daily rate");
            System.out.println("    • Working: 200% of hourly rate for the first 8 hours");
            System.out.println("    • Overtime: Additional 30% on top of holiday premium");
            System.out.println("    • On Rest Day: Additional 30% on top of holiday premium");

            System.out.println("  Special Non-Working Holidays (130%):");
            System.out.println("    • Not Working: No additional pay");
            System.out.println("    • Working: 130% of hourly rate for the first 8 hours");
            System.out.println("    • Overtime: Additional 30% on top of holiday premium");
            System.out.println("    • On Rest Day: Additional 30% on top of holiday premium");
        }

        // Total payable hours
        System.out.println("----------------------------------------------------------------------------------------------");
        System.out.printf("TOTAL PAYABLE HOURS: %.2f (Regular: %.2f, Overtime: %.2f)\n",
                totalActualHours + totalOvertimeHours, totalActualHours, totalOvertimeHours);
    }

    /**
     * Format a date for display
     */
    private static String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DATE_FORMAT);
    }

    /**
     * Format minutes as "HH:MM" for display
     */
    private static String formatMinutes(double minutes) {
        int hours = (int) (minutes / 60);
        int mins = (int) (minutes % 60);

        if (hours > 0) {
            return String.format("%d:%02d", hours, mins);
        } else {
            return String.format("%d min", mins);
        }
    }
}