// File: motorph/employee/CSVReader.java
package motorph.employee;

import motorph.exceptions.DataException;
import motorph.util.ValidationUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for reading CSV files
 * This class provides functionality to read and parse CSV files,
 * handling quoted values and special characters correctly.
 * It implements robust error handling and validation for CSV operations.
 */
public class CSVReader {

    /**
     * Read CSV file and return its contents as a list of string arrays
     *
     * @param filePath Path to the CSV file
     * @return List of string arrays, each representing a row in the CSV
     * @throws DataException If there's an error reading or parsing the file
     */
    public static List<String[]> read(String filePath) throws DataException {
        // Validate file path
        if (!ValidationUtils.validateString(filePath, "File path")) {
            throw new DataException("File path cannot be null or empty", DataException.INVALID_FORMAT);
        }

        List<String[]> data = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
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

                try {
                    // Parse CSV line with proper handling of quoted values
                    List<String> values = parseCSVLine(line);
                    String[] rowData = values.toArray(new String[0]);
                    data.add(rowData);
                } catch (Exception e) {
                    System.out.println("Warning: Error parsing line " + lineNumber +
                            " in CSV file. Skipping this line. Error: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw DataException.fromIOException("Error reading CSV file: " + filePath, e);
        }

        return data;
    }

    /**
     * Read CSV file including the header row
     *
     * @param filePath Path to the CSV file
     * @return List of string arrays, including the header row
     * @throws DataException If there's an error reading or parsing the file
     */
    public static List<String[]> readWithHeader(String filePath) throws DataException {
        // Validate file path
        if (!ValidationUtils.validateString(filePath, "File path")) {
            throw new DataException("File path cannot be null or empty", DataException.INVALID_FORMAT);
        }

        List<String[]> data = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNumber = 0;

            while ((line = br.readLine()) != null) {
                lineNumber++;

                try {
                    // Parse CSV line with proper handling of quoted values
                    List<String> values = parseCSVLine(line);
                    String[] rowData = values.toArray(new String[0]);
                    data.add(rowData);
                } catch (Exception e) {
                    System.out.println("Warning: Error parsing line " + lineNumber +
                            " in CSV file. Skipping this line. Error: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw DataException.fromIOException("Error reading CSV file: " + filePath, e);
        }

        return data;
    }

    /**
     * Parse a CSV line, handling quoted values and commas within fields
     * This method implements robust CSV parsing that correctly handles:
     * - Quoted values containing commas
     * - Escaped quotes within quoted values
     * - Proper field delimiting
     *
     * @param line The CSV line to parse
     * @return List of parsed values from the line
     */
    public static List<String> parseCSVLine(String line) {
        if (line == null) {
            return new ArrayList<>();
        }

        List<String> result = new ArrayList<>();
        StringBuilder currentValue = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                // Check if this is an escaped quote (double quote inside quoted field)
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // Add a single quote to the value and skip the next quote
                    currentValue.append('"');
                    i++;
                } else {
                    // Toggle quote state
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                // End of field
                result.add(currentValue.toString().trim());
                currentValue = new StringBuilder();
            } else {
                // Add character to current value
                currentValue.append(c);
            }
        }

        // Add the last value
        result.add(currentValue.toString().trim());

        return result;
    }

    /**
     * Get CSV header fields
     *
     * @param filePath Path to the CSV file
     * @return Array of header field names, or null if file cannot be read
     * @throws DataException If there's an error reading the file
     */
    public static String[] getCSVHeader(String filePath) throws DataException {
        // Validate file path
        if (!ValidationUtils.validateString(filePath, "File path")) {
            throw new DataException("File path cannot be null or empty", DataException.INVALID_FORMAT);
        }

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String headerLine = br.readLine();

            if (headerLine == null) {
                throw new DataException("CSV file is empty or has no header",
                        DataException.INVALID_FORMAT);
            }

            List<String> headerFields = parseCSVLine(headerLine);
            return headerFields.toArray(new String[0]);

        } catch (IOException e) {
            throw DataException.fromIOException("Error reading CSV header: " + filePath, e);
        }
    }

    /**
     * Check if a CSV file exists and can be read
     *
     * @param filePath Path to the CSV file
     * @return true if file exists and is readable
     */
    public static boolean isFileReadable(String filePath) {
        if (!ValidationUtils.validateString(filePath, "File path")) {
            return false;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Count the number of rows in a CSV file (excluding header)
     *
     * @param filePath Path to the CSV file
     * @return Number of data rows (excluding header)
     * @throws DataException If there's an error reading the file
     */
    public static int countCSVRows(String filePath) throws DataException {
        // Validate file path
        if (!ValidationUtils.validateString(filePath, "File path")) {
            throw new DataException("File path cannot be null or empty", DataException.INVALID_FORMAT);
        }

        int rowCount = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            // Skip header
            br.readLine();

            // Count data rows
            while (br.readLine() != null) {
                rowCount++;
            }

            return rowCount;
        } catch (IOException e) {
            throw DataException.fromIOException("Error counting CSV rows: " + filePath, e);
        }
    }
}