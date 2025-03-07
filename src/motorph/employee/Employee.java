// File: motorph/employee/Employee.java
package motorph.employee;

/**
 * Represents an employee with personal, employment, and salary information
 */
public class Employee {
    // Employee details based on exact column order shown in screenshot
    private String employeeId;      // Index 0 - Column A in file
    private String lastName;        // Index 1 - Column B in file (Last Name)
    private String firstName;       // Index 2 - Column C in file (First Name)
    private String birthday;        // Index 3 - Column D in file (Birthday)
    private String address;         // Index 4 - Column E in file (Address)
    private String phoneNumber;     // Index 5 - Column F in file (Phone Number)
    private String sssNo;           // Index 6 - Column G in file (SSS #)
    private String philhealthNo;    // Index 7 - Column H in file (Philhealth #)
    private String tinNo;           // Index 8 - Column I in file (TIN #)
    private String pagibigNo;       // Index 9 - Column J in file (Pag-ibig #)
    private String status;          // Index 10 - Column K in file (Status)
    private String position;        // Index 11 - Column L in file (Position)
    private String immediateSupervisor; // Index 12 - Column M in file (Immediate Supervisor)
    private double basicSalary;     // Index 13 - Column N in file (Basic Salary) - e.g., 90,000
    private double riceSubsidy;     // Index 14 - Column O in file (Rice Subsidy) - e.g., 1,500
    private double phoneAllowance;  // Index 15 - Column P in file (Phone Allowance) - e.g., 2,000
    private double clothingAllowance; // Index 16 - Column Q in file (Clothing Allowance) - e.g., 1,000
    private double grossSemiMonthlyRate; // Index 17 - Column R in file (Gross Semi-Monthly Rate) - e.g., 45,000
    private double hourlyRate;      // Index 18 - Column S in file (Hourly Rate) - e.g., 535.71

    /**
     * Constructor with array data (typically from CSV)
     * @param data Array containing employee data
     */
    public Employee(String[] data) {
        // Ensure we have enough fields
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

            // Parse numeric values - careful handling for currency formats
            this.basicSalary = parseDouble(data[13].trim());      // e.g., 90,000
            this.riceSubsidy = parseDouble(data[14].trim());      // e.g., 1,500
            this.phoneAllowance = parseDouble(data[15].trim());   // e.g., 2,000
            this.clothingAllowance = parseDouble(data[16].trim()); // e.g., 1,000
            this.grossSemiMonthlyRate = parseDouble(data[17].trim()); // e.g., 45,000
            this.hourlyRate = parseDouble(data[18].trim());      // e.g., 535.71

            // Removed the println statements that were displaying employee data on load
        } else {
            System.out.println("WARNING: Incomplete employee data, only " + data.length + " fields available");
            // Handle minimal data
            this.employeeId = data.length > 0 ? data[0].trim() : "";
            this.lastName = data.length > 1 ? data[1].trim() : "";
            this.firstName = data.length > 2 ? data[2].trim() : "";
        }
    }

    /**
     * Helper method for parsing double values from various formats
     * @param value String representation of a number
     * @return Parsed double value, or 0.0 if parsing fails
     */
    private double parseDouble(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0.0;
        }

        try {
            // Remove currency symbols, commas, and spaces
            String cleanValue = value
                    .replace("₱", "")
                    .replace("P", "")
                    .replace(",", "")
                    .replace(" ", "")
                    .trim();

            // Check if value is empty after cleaning
            if (cleanValue.isEmpty()) {
                return 0.0;
            }

            // Try to parse the value
            return Double.parseDouble(cleanValue);
        } catch (NumberFormatException e) {
            System.out.println("Error parsing value '" + value + "': " + e.getMessage());
            return 0.0;
        }
    }

    // Getters
    public String getEmployeeId() { return employeeId; }
    public String getLastName() { return lastName; }
    public String getFirstName() { return firstName; }
    public String getFullName() { return firstName + " " + lastName; }

    public String getPosition() { return position; }
    public String getStatus() { return status; }
    public String getSssNo() { return sssNo; }
    public String getPhilhealthNo() { return philhealthNo; }
    public String getTinNo() { return tinNo; }
    public String getPagibigNo() { return pagibigNo; }

    // Salary getters
    public double getBasicSalary() { return basicSalary; }
    public double getHourlyRate() {
        // If hourly rate is zero but we have basic salary, calculate it
        if (hourlyRate == 0.0 && basicSalary > 0.0) {
            return basicSalary / (22 * 8); // 22 working days, 8 hours per day
        }
        return hourlyRate;
    }

    // Benefits getters
    public double getRiceSubsidy() { return riceSubsidy; }
    public double getPhoneAllowance() { return phoneAllowance; }
    public double getClothingAllowance() { return clothingAllowance; }
    public double getTotalBenefits() {
        return riceSubsidy + phoneAllowance + clothingAllowance;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Employee ID: ").append(employeeId).append("\n");
        sb.append("Name: ").append(firstName).append(" ").append(lastName).append("\n");
        sb.append("Position: ").append(position).append("\n");
        sb.append("Status: ").append(status).append("\n");
        sb.append("\nSalary Information:\n");
        sb.append("  Basic Salary: ₱").append(String.format("%,.2f", basicSalary)).append("\n");
        sb.append("  Hourly Rate: ₱").append(String.format("%.2f", hourlyRate)).append("\n");
        sb.append("\nStatutory IDs:\n");
        sb.append("  SSS: ").append(sssNo).append("\n");
        sb.append("  PhilHealth: ").append(philhealthNo).append("\n");
        sb.append("  Pag-IBIG: ").append(pagibigNo).append("\n");
        sb.append("  TIN: ").append(tinNo).append("\n");
        sb.append("\nNon-Taxable Benefits (Monthly):\n");
        sb.append("  Rice Subsidy: ₱").append(String.format("%,.2f", riceSubsidy)).append("\n");
        sb.append("  Phone Allowance: ₱").append(String.format("%,.2f", phoneAllowance)).append("\n");
        sb.append("  Clothing Allowance: ₱").append(String.format("%,.2f", clothingAllowance)).append("\n");
        sb.append("  Total Benefits: ₱").append(String.format("%,.2f", getTotalBenefits()));

        return sb.toString();
    }
}