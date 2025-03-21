// File: motorph/exceptions/DataException.java
package motorph.exceptions;

/**
 * Exception for data-related errors
 * This exception is thrown when there are issues with data access, parsing,
 * or file operations related to employee or attendance data.
 */
public class DataException extends PayrollException {

    /**
     * Serial version UID for serialization
     */
    private static final long serialVersionUID = 1L;

    /**
     * Error code for file not found
     */
    public static final int FILE_NOT_FOUND = 2001;

    /**
     * Error code for parsing errors
     */
    public static final int PARSING_ERROR = 2002;

    /**
     * Error code for invalid data format
     */
    public static final int INVALID_FORMAT = 2003;

    /**
     * Error code for missing data
     */
    public static final int MISSING_DATA = 2004;

    /**
     * Error code for invalid CSV structure
     */
    public static final int INVALID_CSV = 2005;

    /**
     * Error code for data access failure
     */
    public static final int DATA_ACCESS_ERROR = 2006;

    /**
     * Create a new data exception with a message
     *
     * @param message Exception message
     */
    public DataException(String message) {
        super(message, DATA_ACCESS_ERROR);
    }

    /**
     * Create a new data exception with a message and specific error code
     *
     * @param message Exception message
     * @param errorCode Specific data error code
     */
    public DataException(String message, int errorCode) {
        super(message, errorCode);
    }

    /**
     * Create a new data exception with a message, cause, and error code
     *
     * @param message Exception message
     * @param cause Root cause exception
     * @param errorCode Specific data error code
     */
    public DataException(String message, Throwable cause, int errorCode) {
        super(message, cause, errorCode);
    }

    /**
     * Create a data exception from an IO exception
     *
     * @param message Exception message
     * @param cause The original IOException
     * @return A new DataException with appropriate error code
     */
    public static DataException fromIOException(String message, Exception cause) {
        return new DataException(message, cause, FILE_NOT_FOUND);
    }
}