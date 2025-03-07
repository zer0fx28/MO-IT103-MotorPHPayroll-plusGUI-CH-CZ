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
 */
public class EmployeeDataReader {
    private final String employeeFilePath;
    private final Map<String, Employee> employeeMap;

    /**
     * Create reader and load employee data
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
                    // Skip incomplete records
                    continue;
                }

                try {
                    Employee employee = new Employee(dataArray);
                    employeeMap.put(employee.getEmployeeId(), employee);
                } catch (Exception e) {
                    // Skip bad records silently
                }
            }

            System.out.println("Employee data loaded successfully");

        } catch (IOException e) {
            System.out.println("Error reading employee file: " + e.getMessage());
        }
    }

    /**
     * Parse CSV line handling quotes and commas
     */
    private List<String> parseCSVLine(String line) {
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
     */
    public Employee getEmployee(String employeeId) {
        return employeeMap.get(employeeId);
    }

    /**
     * Find employee by name
     */
    public Employee findEmployeeByName(String fullName) {
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
     */
    public List<Employee> getAllEmployees() {
        return new ArrayList<>(employeeMap.values());
    }
}