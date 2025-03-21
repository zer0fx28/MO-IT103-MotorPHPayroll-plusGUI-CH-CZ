// File: motorph/employee/Employee.java
package motorph.employee;

/**
 * Stores all employee information
 * This class represents an employee in the MotorPH system and contains
 * all relevant personal, employment, and salary information.
 */
public class Employee {
    // Employee basic details
    private String employeeId;       // Column A
    private String lastName;         // Column B
    private String firstName;        // Column C
    private String birthday;         // Column D
    private String address;          // Column E
    private String phoneNumber;      // Column F
    private String sssNo;            // Column G
    private String philhealthNo;     // Column H
    private String tinNo;            // Column I
    private String pagibigNo;        // Column J
    private String status;           // Column K
    private String position;         // Column L
    private String immediateSupervisor; // Column M

    // Salary information
    private double basicSalary;      // Column N - monthly salary
    private double riceSubsidy;      // Column O - monthly benefit
    private double phoneAllowance;   // Column P - monthly benefit
    private double clothingAllowance; // Column Q - monthly benefit
    private double grossSemiMonthlyRate; // Column R - semi-monthly rate
    private double hourlyRate;       // Column S - hourly rate

    /**
     * Create employee from CSV data
     *
     * @param data Array of CSV data values (must contain at least basic employee information)
     */
    public Employee(String[] data) {
        // Validate input
        if (data == null) {
            throw new IllegalArgumentException("Employee data array cannot be null");
        }

        // Check if we have enough data
        if (data.length >= 19) {
            this.employeeId = data[0].trim();
            this.lastName = data[1].trim();
            this.firstName = data[2].trim();
            this.birthday = data[3].trim();
            this.address = data[4].trim();
            this.phoneNumber = data[5].trim();
            this.sssNo = data[6].trim();
            this.philhealthNo = data[7].trim();
            this.tinNo = data[8].trim();
            this.pagibigNo = data[9].trim();
            this.status = data[10].trim();
            this.position = data[11].trim();
            this.immediateSupervisor = data[12].trim();

            // Parse number values
            this.basicSalary = parseDouble(data[13].trim());
            this.riceSubsidy = parseDouble(data[14].trim());
            this.phoneAllowance = parseDouble(data[15].trim());
            this.clothingAllowance = parseDouble(data[16].trim());
            this.grossSemiMonthlyRate = parseDouble(data[17].trim());
            this.hourlyRate = parseDouble(data[18].trim());
        } else {
            System.out.println("Warning: Not enough employee data, only " + data.length + " fields");
            // Set minimal data
            this.employeeId = data.length > 0 ? data[0].trim() : "";
            this.lastName = data.length > 1 ? data[1].trim() : "";
            this.firstName = data.length > 2 ? data[2].trim() : "";
        }
    }

    /**
     * Convert string to number, handling currency symbols and formatting
     *
     * @param value String value to parse
     * @return Parsed double value, or 0.0 if parsing fails
     */
    private double parseDouble(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0.0;
        }

        try {
            // Remove currency signs and commas
            String cleanValue = value
                    .replace("₱", "")
                    .replace("P", "")
                    .replace(",", "")
                    .replace(" ", "")
                    .trim();

            // Check if empty after cleaning
            if (cleanValue.isEmpty()) {
                return 0.0;
            }

            // Parse number
            return Double.parseDouble(cleanValue);
        } catch (NumberFormatException e) {
            System.out.println("Error parsing number: " + value);
            return 0.0;
        }
    }

    // Getters for employee information
    public String getEmployeeId() { return employeeId; }
    public String getLastName() { return lastName; }
    public String getFirstName() { return firstName; }
    public String getFullName() { return firstName + " " + lastName; }
    public String getBirthday() { return birthday; }
    public String getAddress() { return address; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getPosition() { return position; }
    public String getStatus() { return status; }
    public String getImmediateSupervisor() { return immediateSupervisor; }

    // Getters for government ID numbers
    public String getSssNo() { return sssNo; }
    public String getPhilhealthNo() { return philhealthNo; }
    public String getTinNo() { return tinNo; }
    public String getPagibigNo() { return pagibigNo; }

    // Getters for salary information
    public double getBasicSalary() { return basicSalary; }
    public double getHourlyRate() {
        // Calculate hourly rate if not provided
        if (hourlyRate == 0.0 && basicSalary > 0.0) {
            return basicSalary / (22 * 8); // 22 working days, 8 hours per day
        }
        return hourlyRate;
    }

    // Getters for benefits
    public double getRiceSubsidy() { return riceSubsidy; }
    public double getPhoneAllowance() { return phoneAllowance; }
    public double getClothingAllowance() { return clothingAllowance; }
    public double getGrossSemiMonthlyRate() { return grossSemiMonthlyRate; }

    /**
     * Calculate total monthly benefits
     *
     * @return Sum of all monthly benefits
     */
    public double getTotalBenefits() {
        return riceSubsidy + phoneAllowance + clothingAllowance;
    }

    /**
     * Convert employee to string for display
     *
     * @return Formatted string with employee details
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Employee ID: ").append(employeeId).append("\n");
        sb.append("Name: ").append(firstName).append(" ").append(lastName).append("\n");
        sb.append("Birthday: ").append(birthday).append("\n");
        sb.append("Address: ").append(address).append("\n");
        sb.append("Phone: ").append(phoneNumber).append("\n");
        sb.append("Position: ").append(position).append("\n");
        sb.append("Status: ").append(status).append("\n");
        sb.append("Supervisor: ").append(immediateSupervisor).append("\n");

        sb.append("\nSalary Information:\n");
        sb.append("  Basic Salary: ₱").append(String.format("%,.2f", basicSalary)).append("/month\n");
        sb.append("  Semi-Monthly Rate: ₱").append(String.format("%,.2f", grossSemiMonthlyRate)).append("\n");
        sb.append("  Hourly Rate: ₱").append(String.format("%.2f", hourlyRate)).append("/hour\n");

        sb.append("\nGovernment IDs:\n");
        sb.append("  SSS: ").append(sssNo).append("\n");
        sb.append("  PhilHealth: ").append(philhealthNo).append("\n");
        sb.append("  Pag-IBIG: ").append(pagibigNo).append("\n");
        sb.append("  TIN: ").append(tinNo).append("\n");

        sb.append("\nMonthly Benefits:\n");
        sb.append("  Rice Subsidy: ₱").append(String.format("%,.2f", riceSubsidy)).append("\n");
        sb.append("  Phone Allowance: ₱").append(String.format("%,.2f", phoneAllowance)).append("\n");
        sb.append("  Clothing Allowance: ₱").append(String.format("%,.2f", clothingAllowance)).append("\n");
        sb.append("  Total Benefits: ₱").append(String.format("%,.2f", getTotalBenefits()));

        return sb.toString();
    }
}