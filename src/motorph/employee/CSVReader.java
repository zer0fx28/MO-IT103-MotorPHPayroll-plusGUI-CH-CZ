// File: motorph/employee/CSVReader.java
package motorph.employee;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for reading CSV files
 */
public class CSVReader {

    /**
     * Read CSV file and return its contents as a list of string arrays
     * @param filePath Path to the CSV file
     * @return List of string arrays, each representing a row in the CSV
     * @throws IOException If there's an error reading the file
     */
    public static List<String[]> read(String filePath) throws IOException {
        List<String[]> data = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean headerSkipped = false;

            while ((line = br.readLine()) != null) {
                // Skip header row
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue;
                }

                // Parse CSV line with proper handling of quoted values
                List<String> values = parseCSVLine(line);
                String[] rowData = values.toArray(new String[0]);
                data.add(rowData);
            }
        }

        return data;
    }

    /**
     * Parse a CSV line, handling quoted values and commas within fields
     * @param line The CSV line to parse
     * @return List of parsed values
     */
    private static List<String> parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder currentValue = new StringBuilder();
        boolean inQuotes = false;

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(currentValue.toString().trim());
                currentValue = new StringBuilder();
            } else {
                currentValue.append(c);
            }
        }

        // Add the last value
        result.add(currentValue.toString().trim());

        return result;
    }
}