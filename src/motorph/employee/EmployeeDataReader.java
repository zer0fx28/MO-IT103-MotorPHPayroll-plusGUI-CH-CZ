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
 * Class for reading and managing employee data from a CSV file
 */
public class EmployeeDataReader {
    private final String employeeFilePath;
    private final Map<String, Employee> employeeMap;

    /**
     * Constructor initializes the reader with the employee file path
     * @param employeeFilePath Path to the employee CSV file
     */
    public EmployeeDataReader(String employeeFilePath) {
        this.employeeFilePath = employeeFilePath;
        this.employeeMap = new HashMap<>();
        loadEmployees();
    }

    /**
     * Load employees from the CSV file
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

                // Handle potential quoted values with commas inside
                List<String> values = parseCSVLine(line);

                // Convert to array
                String[] dataArray = values.toArray(new String[0]);

                if (dataArray.length < 19) {
                    continue;
                }

                try {
                    Employee employee = new Employee(dataArray);
                    employeeMap.put(employee.getEmployeeId(), employee);
                } catch (Exception e) {
                    // Silent error handling to avoid cluttering the console
                }
            }

            System.out.println("Employee data loaded successfully");

        } catch (IOException e) {
            System.out.println("Error reading employee file: " + e.getMessage());
        }
    }

    /**
     * More robust CSV line parsing to handle quoted values and commas in fields
     * @param line CSV line to parse
     * @return List of parsed values
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
     * @param employeeId The employee ID
     * @return Employee object, or null if not found
     */
    public Employee getEmployee(String employeeId) {
        return employeeMap.get(employeeId);
    }

    /**
     * Find employee by name (exact or partial match)
     * @param fullName Full or partial name to search for
     * @return Employee object, or null if not found
     */
    public Employee findEmployeeByName(String fullName) {
        String searchName = fullName.toLowerCase().trim();

        for (Employee employee : employeeMap.values()) {
            String empFullName = (employee.getFirstName() + " " + employee.getLastName()).toLowerCase();

            // Check for exact match or partial match
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
     * @param searchTerm ID or name to search for
     * @return Employee object, or null if not found
     */
    public Employee findEmployee(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return null;
        }

        // Try by employee ID first
        Employee employee = getEmployee(searchTerm.trim());

        // If not found by ID, try by name
        if (employee == null) {
            employee = findEmployeeByName(searchTerm);
        }

        return employee;
    }

    /**
     * Get all employees
     * @return List of all employees
     */
    public List<Employee> getAllEmployees() {
        return new ArrayList<>(employeeMap.values());
    }
}