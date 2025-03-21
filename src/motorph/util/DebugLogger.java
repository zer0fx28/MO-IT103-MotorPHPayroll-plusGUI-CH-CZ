// File: motorph/util/DebugLogger.java
package motorph.util;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Enhanced logging utility for the payroll system
 *
 * This class provides centralized logging functionality with configurable
 * debug levels, file output, and formatted messages. It supports tracking
 * errors, warnings, and informational messages across the application.
 */
public class DebugLogger {
    // Global debug flag
    public static boolean DEBUG_ENABLED = true;

    // Log level configuration
    private static Level FILE_LOG_LEVEL = Level.INFO;
    private static Level CONSOLE_LOG_LEVEL = Level.WARNING;

    // Log file configuration
    private static final String LOG_FOLDER = "logs";
    private static final String LOG_FILE_PREFIX = "motorph_payroll";

    // Main system logger
    private static final Logger SYSTEM_LOGGER = Logger.getLogger("motorph");

    // Static initializer to set up logging
    static {
        configureLogger();
    }

    /**
     * Configure the logger with file and console handlers
     */
    private static void configureLogger() {
        try {
            // Create logs directory if it doesn't exist
            File logDir = new File(LOG_FOLDER);
            if (!logDir.exists()) {
                logDir.mkdir();
            }

            // Create date-based log filename
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            String logFileName = LOG_FOLDER + File.separator +
                    LOG_FILE_PREFIX + "_" +
                    dateFormat.format(new Date()) + ".log";

            // Configure file handler with custom formatter
            FileHandler fileHandler = new FileHandler(logFileName, true);
            fileHandler.setFormatter(new CustomLogFormatter());
            fileHandler.setLevel(FILE_LOG_LEVEL);

            // Configure console handler with custom formatter
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new CustomLogFormatter());
            consoleHandler.setLevel(CONSOLE_LOG_LEVEL);

            // Remove default handlers and add our custom handlers
            Logger rootLogger = Logger.getLogger("");
            Handler[] handlers = rootLogger.getHandlers();
            for (Handler handler : handlers) {
                rootLogger.removeHandler(handler);
            }

            // Add handlers to system logger
            SYSTEM_LOGGER.addHandler(fileHandler);
            SYSTEM_LOGGER.addHandler(consoleHandler);
            SYSTEM_LOGGER.setLevel(Level.ALL);

            // Don't propagate to parent
            SYSTEM_LOGGER.setUseParentHandlers(false);

            // Log startup information
            SYSTEM_LOGGER.info("Logging system initialized. Log file: " + logFileName);

        } catch (IOException e) {
            System.err.println("Error setting up log file: " + e.getMessage());
            // Fall back to console-only logging
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new CustomLogFormatter());
            SYSTEM_LOGGER.addHandler(consoleHandler);
        }
    }

    /**
     * Log an informational message
     *
     * @param message Message to log
     */
    public static void log(String message) {
        if (DEBUG_ENABLED) {
            SYSTEM_LOGGER.info(message);
        }
    }

    /**
     * Log a warning message
     *
     * @param message Warning message to log
     */
    public static void warn(String message) {
        SYSTEM_LOGGER.warning(message);
    }

    /**
     * Log an error message
     *
     * @param message Error message to log
     */
    public static void error(String message) {
        SYSTEM_LOGGER.severe(message);
    }

    /**
     * Log debug information (only when debug is enabled)
     *
     * @param message Debug message to log
     */
    public static void debug(String message) {
        if (DEBUG_ENABLED) {
            SYSTEM_LOGGER.fine(message);
        }
    }

    /**
     * Log an exception with stack trace
     *
     * @param message Message describing the error
     * @param exception Exception to log
     */
    public static void exception(String message, Throwable exception) {
        SYSTEM_LOGGER.log(Level.SEVERE, message, exception);
    }

    /**
     * Log start of a method (useful for tracing)
     *
     * @param className Class name
     * @param methodName Method name
     */
    public static void methodStart(String className, String methodName) {
        if (DEBUG_ENABLED) {
            SYSTEM_LOGGER.entering(className, methodName);
        }
    }

    /**
     * Log end of a method (useful for tracing)
     *
     * @param className Class name
     * @param methodName Method name
     */
    public static void methodEnd(String className, String methodName) {
        if (DEBUG_ENABLED) {
            SYSTEM_LOGGER.exiting(className, methodName);
        }
    }

    /**
     * Set debug mode
     *
     * @param enabled Whether debug mode should be enabled
     */
    public static void setDebugEnabled(boolean enabled) {
        DEBUG_ENABLED = enabled;
        SYSTEM_LOGGER.info("Debug mode " + (enabled ? "enabled" : "disabled"));
    }

    /**
     * Set log levels
     *
     * @param fileLevel Level for file logging
     * @param consoleLevel Level for console logging
     */
    public static void setLogLevels(Level fileLevel, Level consoleLevel) {
        FILE_LOG_LEVEL = fileLevel;
        CONSOLE_LOG_LEVEL = consoleLevel;

        // Update handlers
        for (Handler handler : SYSTEM_LOGGER.getHandlers()) {
            if (handler instanceof FileHandler) {
                handler.setLevel(fileLevel);
            } else if (handler instanceof ConsoleHandler) {
                handler.setLevel(consoleLevel);
            }
        }

        SYSTEM_LOGGER.info("Log levels updated: File=" + fileLevel.getName() +
                ", Console=" + consoleLevel.getName());
    }

    /**
     * Custom log formatter for more readable output
     */
    private static class CustomLogFormatter extends Formatter {
        private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        @Override
        public String format(LogRecord record) {
            StringBuilder sb = new StringBuilder();

            // Add timestamp
            sb.append(DATE_FORMAT.format(new Date(record.getMillis())));
            sb.append(" | ");

            // Add log level
            sb.append(String.format("%-7s", record.getLevel().getName()));
            sb.append(" | ");

            // Add source class and method if available
            if (record.getSourceClassName() != null) {
                sb.append(record.getSourceClassName());

                if (record.getSourceMethodName() != null) {
                    sb.append(".");
                    sb.append(record.getSourceMethodName());
                }

                sb.append(" | ");
            }

            // Add message
            sb.append(formatMessage(record));
            sb.append(System.lineSeparator());

            // Add throwable if present
            if (record.getThrown() != null) {
                try {
                    Throwable thrown = record.getThrown();
                    sb.append("Exception: ")
                            .append(thrown.getClass().getName())
                            .append(": ")
                            .append(thrown.getMessage())
                            .append(System.lineSeparator());

                    // Add stack trace
                    for (StackTraceElement element : thrown.getStackTrace()) {
                        sb.append("\tat ")
                                .append(element.toString())
                                .append(System.lineSeparator());
                    }
                } catch (Exception ex) {
                    sb.append("Error formatting exception: ")
                            .append(ex.getMessage())
                            .append(System.lineSeparator());
                }
            }

            return sb.toString();
        }
    }
}