// File: motorph/employee/EmployeeDataReader.java
package motorph.employee;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads and manages employee data from CSV
 *
 * This class is responsible for loading employee data from a CSV file,
 * parsing it into Employee objects, and providing methods to search and
 * retrieve employee information.
 */
public class EmployeeDataReader {
    private final String employeeFilePath;
    private final Map<String, Employee> employeeMap;

    /**
     * Create reader and load employee data
     *
     * @param employeeFilePath Path to the employee CSV file
     */
    public EmployeeDataReader(String employeeFilePath) {
        this.employeeFilePath = employeeFilePath;
        this.employeeMap = new HashMap<>();
        loadEmployees();
    }

    /**
     * Read employees from CSV file
     */
    private void loadEmployees() {
        System.out.println("Loading employee data...");

        if (employeeFilePath == null || employeeFilePath.trim().isEmpty()) {
            System.out.println("Error: Employee file path is null or empty");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(employeeFilePath))) {
            String line;
            boolean headerSkipped = false;
            int lineNumber = 0;

            while ((line = br.readLine()) != null) {
                lineNumber++;

                // Skip header row
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue;
                }

                // Handle commas inside quoted values
                List<String> values = parseCSVLine(line);

                // Convert to array
                String[] dataArray = values.toArray(new String[0]);

                if (dataArray.length < 19) {
                    System.out.println("Warning: Incomplete employee record on line " + lineNumber +
                            ". Expected 19 fields, got " + dataArray.length);
                    continue;
                }

                try {
                    Employee employee = new Employee(dataArray);
                    String employeeId = employee.getEmployeeId();

                    if (employeeId == null || employeeId.trim().isEmpty()) {
                        System.out.println("Warning: Employee record on line " + lineNumber +
                                " has empty employee ID. Skipping record.");
                        continue;
                    }

                    employeeMap.put(employeeId, employee);
                } catch (Exception e) {
                    System.out.println("Error processing employee record on line " + lineNumber +
                            ": " + e.getMessage());
                }
            }

            System.out.println("Employee data loaded successfully. Total records: " + employeeMap.size());

        } catch (IOException e) {
            System.out.println("Error reading employee file: " + e.getMessage());
        }
    }

    /**
     * Parse CSV line handling quotes and commas
     *
     * @param line CSV line to parse
     * @return List of parsed values
     */
    private List<String> parseCSVLine(String line) {
        if (line == null) {
            return new ArrayList<>();
        }

        List<String> result = new ArrayList<>();
        StringBuilder currentValue = new StringBuilder();
        boolean inQuotes = false;

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(currentValue.toString());
                currentValue = new StringBuilder();
            } else {
                currentValue.append(c);
            }
        }

        // Add the last value
        result.add(currentValue.toString());

        return result;
    }

    /**
     * Get employee by ID
     *
     * @param employeeId Employee ID to search for
     * @return Employee object if found, null otherwise
     */
    public Employee getEmployee(String employeeId) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            return null;
        }
        return employeeMap.get(employeeId.trim());
    }

    /**
     * Find employee by name (full or partial match)
     *
     * @param fullName Full name or partial name to search for
     * @return First matching Employee object, or null if not found
     */
    public Employee findEmployeeByName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return null;
        }

        String searchName = fullName.toLowerCase().trim();

        for (Employee employee : employeeMap.values()) {
            String empFullName = (employee.getFirstName() + " " + employee.getLastName()).toLowerCase();

            // Check for match or partial match
            if (empFullName.equals(searchName) ||
                    empFullName.contains(searchName) ||
                    searchName.contains(empFullName)) {
                return employee;
            }
        }

        return null;
    }

    /**
     * Find employee by ID or name
     *
     * @param searchTerm ID or name to search for
     * @return Matching Employee object, or null if not found
     */
    public Employee findEmployee(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return null;
        }

        // Try by ID first
        Employee employee = getEmployee(searchTerm.trim());

        // If not found, try by name
        if (employee == null) {
            employee = findEmployeeByName(searchTerm);
        }

        return employee;
    }

    /**
     * Get all employees
     *
     * @return List of all employees
     */
    public List<Employee> getAllEmployees() {
        return new ArrayList<>(employeeMap.values());
    }
}