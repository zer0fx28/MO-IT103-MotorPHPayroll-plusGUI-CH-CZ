// File: motorph/employee/EmployeeDataReader.java
package motorph.employee;

import motorph.util.DebugLogger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads and manages employee data from CSV
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
        DebugLogger.log("Loading employee data from: " + employeeFilePath);

        if (employeeFilePath == null || employeeFilePath.trim().isEmpty()) {
            DebugLogger.error("Employee file path is null or empty");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(employeeFilePath))) {
            String line;
            boolean headerSkipped = false;
            int lineNumber = 0;
            int successCount = 0;
            int errorCount = 0;

            DebugLogger.log("File opened successfully. Reading data...");

            while ((line = br.readLine()) != null) {
                lineNumber++;

                // Skip header row
                if (!headerSkipped) {
                    DebugLogger.log("Header row: " + line);
                    headerSkipped = true;
                    continue;
                }

                try {
                    // Handle commas inside quoted values
                    List<String> values = parseCSVLine(line);
                    DebugLogger.log("Line " + lineNumber + ": Parsed " + values.size() + " fields");

                    // Convert to array
                    String[] dataArray = values.toArray(new String[0]);

                    if (dataArray.length < 19) {
                        DebugLogger.warn("Incomplete employee record on line " + lineNumber +
                                ". Expected 19 fields, got " + dataArray.length);
                        errorCount++;
                        continue;
                    }

                    try {
                        Employee employee = new Employee(dataArray);
                        String employeeId = employee.getEmployeeId();
                        DebugLogger.log("Created employee with ID: " + employeeId + ", Name: " +
                                employee.getFirstName() + " " + employee.getLastName());

                        if (employeeId == null || employeeId.trim().isEmpty()) {
                            DebugLogger.warn("Employee record on line " + lineNumber +
                                    " has empty employee ID. Skipping record.");
                            errorCount++;
                            continue;
                        }

                        employeeMap.put(employeeId, employee);
                        successCount++;
                    } catch (Exception e) {
                        DebugLogger.error("Processing employee record on line " + lineNumber +
                                ": " + e.getMessage());
                        errorCount++;
                    }
                } catch (Exception e) {
                    DebugLogger.error("Parsing line " + lineNumber + ": " + e.getMessage());
                    errorCount++;
                }
            }

            DebugLogger.log("Employee data loading complete:");
            DebugLogger.log("- Total records processed: " + (successCount + errorCount));
            DebugLogger.log("- Successfully loaded: " + successCount);
            DebugLogger.log("- Errors: " + errorCount);
            DebugLogger.log("- Total employees in map: " + employeeMap.size());

            // Print some of the loaded employee IDs for verification
            if (!employeeMap.isEmpty()) {
                DebugLogger.log("Sample employee IDs: ");
                int count = 0;
                for (String id : employeeMap.keySet()) {
                    DebugLogger.log("  - " + id + ": " + employeeMap.get(id).getFullName());
                    count++;
                    if (count >= 5) break;
                }
            }

        } catch (IOException e) {
            DebugLogger.error("Reading employee file: " + e.getMessage());
            e.printStackTrace();
        }

        // Add a test employee for troubleshooting if debug is enabled
        if (DebugLogger.DEBUG_ENABLED) {
            Employee testEmployee = new Employee(new String[]{
                    "10001", "Doe", "John", "01/01/1990",
                    "Manila", "123-456-7890", "SSS123", "PH123", "TIN123", "PAGIBIG123",
                    "Regular", "Software Engineer", "Jane Manager", "25000", "1500", "1000",
                    "1000", "12500", "142.05"
            });
            employeeMap.put("10001", testEmployee);
            DebugLogger.log("Added test employee with ID: 10001, Name: John Doe");
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
            DebugLogger.log("getEmployee: Empty employee ID provided");
            return null;
        }

        String trimmedId = employeeId.trim();
        Employee result = employeeMap.get(trimmedId);

        if (result == null) {
            DebugLogger.log("getEmployee: No employee found with ID: " + trimmedId);
            DebugLogger.log("Available IDs: " + String.join(", ", employeeMap.keySet()));
        } else {
            DebugLogger.log("getEmployee: Found employee with ID: " + trimmedId +
                    ", Name: " + result.getFullName());
        }

        return result;
    }

    /**
     * Find employee by name (full or partial match)
     *
     * @param fullName Full name or partial name to search for
     * @return First matching Employee object, or null if not found
     */
    public Employee findEmployeeByName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            DebugLogger.log("findEmployeeByName: Empty name provided");
            return null;
        }

        String searchName = fullName.toLowerCase().trim();
        DebugLogger.log("findEmployeeByName: Searching for name: '" + searchName + "'");

        for (Employee employee : employeeMap.values()) {
            String empFullName = (employee.getFirstName() + " " + employee.getLastName()).toLowerCase();

            DebugLogger.log("Comparing with: '" + empFullName + "'");

            // Check for match or partial match
            if (empFullName.equals(searchName) ||
                    empFullName.contains(searchName) ||
                    searchName.contains(empFullName)) {
                DebugLogger.log("findEmployeeByName: Found match: " + employee.getFullName());
                return employee;
            }
        }

        DebugLogger.log("findEmployeeByName: No employee found with name: " + searchName);
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
            DebugLogger.log("findEmployee: Empty search term");
            return null;
        }

        String trimmedTerm = searchTerm.trim();
        DebugLogger.log("findEmployee: Searching for: '" + trimmedTerm + "'");

        // Try by ID first
        Employee employee = getEmployee(trimmedTerm);

        // If not found, try by name
        if (employee == null) {
            DebugLogger.log("findEmployee: Not found by ID, trying by name");
            employee = findEmployeeByName(trimmedTerm);
        }

        if (employee == null) {
            System.out.println("No employee found with ID or name: " + trimmedTerm);
        } else {
            System.out.println("Found employee: " + employee.getEmployeeId() + " - " + employee.getFullName());
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