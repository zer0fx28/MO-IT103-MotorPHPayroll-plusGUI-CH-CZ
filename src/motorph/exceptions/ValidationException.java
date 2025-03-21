// File: motorph/exceptions/ValidationException.java
package motorph.exceptions;

/**
 * Exception for input validation errors
 * This exception is thrown when input validation fails, such as invalid dates,
 * negative values, or other constraint violations.
 */
public class ValidationException extends PayrollException {

    /**
     * Serial version UID for serialization
     */
    private static final long serialVersionUID = 1L;

    /**
     * Error code for invalid input value
     */
    public static final int INVALID_VALUE = 1001;

    /**
     * Error code for invalid date
     */
    public static final int INVALID_DATE = 1002;

    /**
     * Error code for invalid time
     */
    public static final int INVALID_TIME = 1003;

    /**
     * Error code for invalid ID
     */
    public static final int INVALID_ID = 1004;

    /**
     * Error code for out of range value
     */
    public static final int OUT_OF_RANGE = 1005;

    /**
     * Error code for null value
     */
    public static final int NULL_VALUE = 1006;

    /**
     * Create a new validation exception with a message
     *
     * @param message Exception message
     */
    public ValidationException(String message) {
        super(message, INVALID_VALUE);
    }

    /**
     * Create a new validation exception with a message and specific error code
     *
     * @param message Exception message
     * @param errorCode Specific validation error code
     */
    public ValidationException(String message, int errorCode) {
        super(message, errorCode);
    }

    /**
     * Create a new validation exception with a message, cause, and error code
     *
     * @param message Exception message
     * @param cause Root cause exception
     * @param errorCode Specific validation error code
     */
    public ValidationException(String message, Throwable cause, int errorCode) {
        super(message, cause, errorCode);
    }
}