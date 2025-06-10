package motorph.employee;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Reads and manages employee data from CSV using OpenCSV
 * Same interface as before - your GUI code doesn't need to change!
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
     * Read employees from CSV file using OpenCSV
     */
    private void loadEmployees() {
        System.out.println("Loading employee data with OpenCSV...");

        try {
            // Use OpenCSV for reliable CSV parsing
            CSVReader csvReader = new CSVReader(new FileReader(employeeFilePath));
            List<String[]> allRows = csvReader.readAll();
            csvReader.close();

            int lineNumber = 0;

            // Skip header row (first row) and process data
            for (int i = 1; i < allRows.size(); i++) {
                lineNumber++;
                String[] dataArray = allRows.get(i);

                if (dataArray.length < 19) {
                    // Skip incomplete records
                    continue;
                }

                try {
                    // Use your existing Employee constructor - no changes needed!
                    Employee employee = new Employee(dataArray);
                    employeeMap.put(employee.getEmployeeId(), employee);
                } catch (Exception e) {
                    // Skip bad records silently (same as your original logic)
                }
            }

            System.out.println("Employee data loaded successfully with OpenCSV");
            System.out.println("Total employees loaded: " + employeeMap.size());

        } catch (IOException e) {
            System.out.println("Error reading employee file: " + e.getMessage());
        } catch (CsvException e) {
            System.out.println("Error parsing CSV file with OpenCSV: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected error loading employees: " + e.getMessage());
        }
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