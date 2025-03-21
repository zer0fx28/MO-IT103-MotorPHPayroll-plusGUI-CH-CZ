// File: motorph/exceptions/PayrollException.java
package motorph.exceptions;

/**
 * Base exception class for payroll system errors
 * This class serves as the root exception for all payroll-specific exceptions,
 * providing a consistent error handling approach across the system.
 */
public class PayrollException extends Exception {

    /**
     * Serial version UID for serialization
     */
    private static final long serialVersionUID = 1L;

    /**
     * Error code for the exception
     */
    private final int errorCode;

    /**
     * Create a new payroll exception with a message
     *
     * @param message Exception message
     */
    public PayrollException(String message) {
        super(message);
        this.errorCode = 0;
    }

    /**
     * Create a new payroll exception with a message and cause
     *
     * @param message Exception message
     * @param cause Root cause exception
     */
    public PayrollException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = 0;
    }

    /**
     * Create a new payroll exception with a message and error code
     *
     * @param message Exception message
     * @param errorCode Numeric error code
     */
    public PayrollException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Create a new payroll exception with message, cause, and error code
     *
     * @param message Exception message
     * @param cause Root cause exception
     * @param errorCode Numeric error code
     */
    public PayrollException(String message, Throwable cause, int errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * Get the error code for this exception
     *
     * @return The numeric error code
     */
    public int getErrorCode() {
        return errorCode;
    }
}