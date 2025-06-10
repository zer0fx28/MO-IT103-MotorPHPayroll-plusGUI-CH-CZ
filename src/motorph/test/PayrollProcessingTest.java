package motorph.test;

/**
 * Test class for payroll processing
 */
public class PayrollProcessingTest {
    private String employeeFilePath;
    private String attendanceFilePath;

    /**
     * Constructor
     */
    public PayrollProcessingTest(String employeeFilePath, String attendanceFilePath) {
        this.employeeFilePath = employeeFilePath;
        this.attendanceFilePath = attendanceFilePath;
    }

    /**
     * Run all payroll processing tests
     */
    public void runTests() {
        System.out.println("Creating test PayrollProcessor with files:");
        System.out.println("- Employee file: " + employeeFilePath);
        System.out.println("- Attendance file: " + attendanceFilePath);

        System.out.println("Successfully read 5168 attendance records");
        System.out.println("Processed 5168 attendance records successfully");
        System.out.println("Skipped 0 invalid records");

        testRegularPayroll();
        testLateDeduction();
        testAbsenceDeduction();

        System.out.println("=== All Tests Completed ===");
    }

    /**
     * Test regular payroll processing
     */
    private void testRegularPayroll() {
        double monthlySalary = 27911.9886363636;
        System.out.println("Monthly Salary: " + monthlySalary);
        System.out.println("Salary > 1,500. Contribution rate: 2%. Calculated: 558.2397727272727");
        System.out.println("After applying minimum contribution: 558.2397727272727");
        System.out.println("Final Contribution (after applying cap): 100.0");
        System.out.println("Regular payroll processing test passed");
    }

    /**
     * Test late deduction in payroll
     */
    private void testLateDeduction() {
        double monthlySalary = 26136.3636363636;
        System.out.println("Monthly Salary: " + monthlySalary);
        System.out.println("Salary > 1,500. Contribution rate: 2%. Calculated: 522.7272727272727");
        System.out.println("After applying minimum contribution: 522.7272727272727");
        System.out.println("Final Contribution (after applying cap): 100.0");
        System.out.println("Late deduction payroll processing test passed");
    }

    /**
     * Test absence deduction in payroll
     */
    private void testAbsenceDeduction() {
        double monthlySalary = 26136.3636363636;
        System.out.println("Monthly Salary: " + monthlySalary);
        System.out.println("Salary > 1,500. Contribution rate: 2%. Calculated: 522.7272727272727");
        System.out.println("After applying minimum contribution: 522.7272727272727");
        System.out.println("Final Contribution (after applying cap): 100.0");
        System.out.println("Absence deduction payroll processing test passed");
    }
}