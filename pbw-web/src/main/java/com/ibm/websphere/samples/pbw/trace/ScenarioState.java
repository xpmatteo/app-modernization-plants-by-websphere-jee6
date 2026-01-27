// ABOUTME: Global state for the current tracing scenario.
// ABOUTME: Manages scenario name, description, and active status.

package com.ibm.websphere.samples.pbw.trace;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Global state for the current tracing scenario. Only one scenario can be
 * active at a time. Thread-safe for concurrent access.
 */
public class ScenarioState {

    private static final AtomicReference<ScenarioInfo> CURRENT = new AtomicReference<>();

    /**
     * Information about an active scenario.
     */
    public static class ScenarioInfo {
        private final String name;
        private final String description;
        private final long startTime;

        public ScenarioInfo(String name, String description) {
            this.name = name;
            this.description = description;
            this.startTime = System.currentTimeMillis();
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public long getStartTime() {
            return startTime;
        }
    }

    /**
     * Start a new scenario. Only one scenario can be active at a time.
     *
     * @param name        The scenario name (used as filename)
     * @param description Human-readable description
     * @return true if started successfully, false if a scenario is already active
     */
    public static boolean start(String name, String description) {
        ScenarioInfo info = new ScenarioInfo(name, description);
        return CURRENT.compareAndSet(null, info);
    }

    /**
     * Stop the current scenario.
     *
     * @return The stopped scenario info, or null if none was active
     */
    public static ScenarioInfo stop() {
        return CURRENT.getAndSet(null);
    }

    /**
     * Check if a scenario is currently active.
     */
    public static boolean isActive() {
        return CURRENT.get() != null;
    }

    /**
     * Get the current scenario info.
     */
    public static ScenarioInfo getCurrent() {
        return CURRENT.get();
    }

    /**
     * Get the current scenario name.
     */
    public static String getCurrentName() {
        ScenarioInfo info = CURRENT.get();
        return info != null ? info.getName() : null;
    }
}
