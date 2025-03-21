// File: motorph/util/PerformanceMonitor.java
package motorph.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Performance monitoring utility for the payroll system
 *
 * This class provides timing and measurement functionality to monitor
 * system performance, track method execution times, and identify
 * potential bottlenecks.
 */
public class PerformanceMonitor {
    // Singleton instance
    private static final PerformanceMonitor INSTANCE = new PerformanceMonitor();

    // Timer storage with thread-safe map
    private final Map<String, TimerInfo> timers = new ConcurrentHashMap<>();

    // Statistics for operations
    private final Map<String, OperationStats> operationStats = new ConcurrentHashMap<>();

    /**
     * Private constructor for singleton pattern
     */
    private PerformanceMonitor() {
        // Initialize the performance monitor
        DebugLogger.log("Performance monitor initialized");
    }

    /**
     * Get singleton instance
     *
     * @return PerformanceMonitor instance
     */
    public static PerformanceMonitor getInstance() {
        return INSTANCE;
    }

    /**
     * Start a timer with a specific label
     *
     * @param label Label to identify the timer
     */
    public void startTimer(String label) {
        timers.put(label, new TimerInfo(System.nanoTime()));
        DebugLogger.debug("Timer started: " + label);
    }

    /**
     * Stop a timer and return elapsed time in milliseconds
     *
     * @param label Label of the timer to stop
     * @return Elapsed time in milliseconds, or -1 if timer not found
     */
    public long stopTimer(String label) {
        TimerInfo timer = timers.remove(label);
        if (timer == null) {
            DebugLogger.warn("Timer not found: " + label);
            return -1;
        }

        long endTime = System.nanoTime();
        long elapsedNanos = endTime - timer.startTimeNanos;
        long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(elapsedNanos);

        DebugLogger.debug(String.format(
                "Timer stopped: %s - Elapsed time: %d ms",
                label, elapsedMillis));

        return elapsedMillis;
    }

    /**
     * Record execution of an operation for statistics
     *
     * @param operationName Name of the operation
     * @param executionTimeMs Execution time in milliseconds
     */
    public void recordOperation(String operationName, long executionTimeMs) {
        OperationStats stats = operationStats.computeIfAbsent(
                operationName, k -> new OperationStats(operationName));
        stats.recordExecution(executionTimeMs);
    }

    /**
     * Record execution of an operation with automatic timing
     *
     * @param operationName Name of the operation
     * @param runnable Operation to execute and time
     */
    public void timeOperation(String operationName, Runnable runnable) {
        startTimer(operationName);
        try {
            runnable.run();
        } finally {
            long executionTime = stopTimer(operationName);
            if (executionTime >= 0) {
                recordOperation(operationName, executionTime);
            }
        }
    }

    /**
     * Print performance summary for all recorded operations
     */
    public void printPerformanceSummary() {
        if (operationStats.isEmpty()) {
            System.out.println("No performance data available");
            return;
        }

        System.out.println("\n===== PERFORMANCE SUMMARY =====");
        System.out.printf("%-30s %-10s %-10s %-10s %-10s %-10s\n",
                "Operation", "Count", "Min (ms)", "Avg (ms)", "Max (ms)", "Total (ms)");
        System.out.println("---------------------------------------------------------------------------------");

        for (OperationStats stats : operationStats.values()) {
            System.out.printf("%-30s %-10d %-10d %-10.2f %-10d %-10d\n",
                    stats.operationName,
                    stats.executionCount,
                    stats.minExecutionTime,
                    stats.getAverageExecutionTime(),
                    stats.maxExecutionTime,
                    stats.totalExecutionTime);
        }

        System.out.println("---------------------------------------------------------------------------------");
    }

    /**
     * Reset all performance statistics
     */
    public void resetStatistics() {
        operationStats.clear();
        timers.clear();
        DebugLogger.log("Performance monitor statistics reset");
    }

    /**
     * Get statistics for a specific operation
     *
     * @param operationName Name of the operation
     * @return OperationStats or null if not found
     */
    public OperationStats getOperationStats(String operationName) {
        return operationStats.get(operationName);
    }

    /**
     * Inner class to store timer information
     */
    private static class TimerInfo {
        final long startTimeNanos;

        TimerInfo(long startTimeNanos) {
            this.startTimeNanos = startTimeNanos;
        }
    }

    /**
     * Inner class to store operation statistics
     */
    public static class OperationStats {
        private final String operationName;
        private long executionCount = 0;
        private long totalExecutionTime = 0;
        private long minExecutionTime = Long.MAX_VALUE;
        private long maxExecutionTime = 0;

        public OperationStats(String operationName) {
            this.operationName = operationName;
        }

        /**
         * Record a single execution
         *
         * @param executionTimeMs Execution time in milliseconds
         */
        public synchronized void recordExecution(long executionTimeMs) {
            executionCount++;
            totalExecutionTime += executionTimeMs;

            if (executionTimeMs < minExecutionTime) {
                minExecutionTime = executionTimeMs;
            }

            if (executionTimeMs > maxExecutionTime) {
                maxExecutionTime = executionTimeMs;
            }
        }

        /**
         * Get average execution time
         *
         * @return Average execution time in milliseconds
         */
        public double getAverageExecutionTime() {
            if (executionCount == 0) {
                return 0.0;
            }
            return (double) totalExecutionTime / executionCount;
        }

        /**
         * Get operation name
         *
         * @return Operation name
         */
        public String getOperationName() {
            return operationName;
        }

        /**
         * Get execution count
         *
         * @return Number of executions
         */
        public long getExecutionCount() {
            return executionCount;
        }

        /**
         * Get total execution time
         *
         * @return Total execution time in milliseconds
         */
        public long getTotalExecutionTime() {
            return totalExecutionTime;
        }

        /**
         * Get minimum execution time
         *
         * @return Minimum execution time in milliseconds
         */
        public long getMinExecutionTime() {
            return minExecutionTime == Long.MAX_VALUE ? 0 : minExecutionTime;
        }

        /**
         * Get maximum execution time
         *
         * @return Maximum execution time in milliseconds
         */
        public long getMaxExecutionTime() {
            return maxExecutionTime;
        }
    }

    /**
     * AutoCloseable timer implementation for try-with-resources
     */
    public static class AutoTimer implements AutoCloseable {
        private final String operationName;
        private final long startTime;

        /**
         * Create and start an auto-timer
         *
         * @param operationName Name of the operation
         */
        public AutoTimer(String operationName) {
            this.operationName = operationName;
            this.startTime = System.nanoTime();
        }

        @Override
        public void close() {
            long endTime = System.nanoTime();
            long elapsedNanos = endTime - startTime;
            long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(elapsedNanos);

            PerformanceMonitor.getInstance().recordOperation(operationName, elapsedMillis);
        }
    }
}