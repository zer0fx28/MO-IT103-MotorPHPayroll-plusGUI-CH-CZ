// File: motorph/holidays/Holiday.java
package motorph.holidays;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Simple class to store holiday information
 * This class represents a Philippine holiday with a name and date.
 */
public class Holiday {
    private String name;
    private LocalDate date;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy");

    /**
     * Create a new holiday
     *
     * @param name Holiday name
     * @param date Holiday date
     */
    public Holiday(String name, LocalDate date) {
        this.name = name;
        this.date = date;
    }

    /**
     * Get the holiday name
     *
     * @return Holiday name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the holiday date
     *
     * @return Holiday date
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Format the holiday as a string
     *
     * @return Formatted holiday string
     */
    @Override
    public String toString() {
        return name + " (" + date.format(DATE_FORMATTER) + ")";
    }

    /**
     * Compare this holiday with another object
     *
     * @param obj Object to compare with
     * @return true if the objects are equal
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Holiday other = (Holiday) obj;
        return date.equals(other.date) && name.equals(other.name);
    }

    /**
     * Generate a hash code for this holiday
     *
     * @return Hash code
     */
    @Override
    public int hashCode() {
        return 31 * name.hashCode() + date.hashCode();
    }
}