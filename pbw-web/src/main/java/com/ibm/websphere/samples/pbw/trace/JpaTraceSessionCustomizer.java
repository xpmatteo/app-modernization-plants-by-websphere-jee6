// ABOUTME: EclipseLink SessionCustomizer that enables SQL tracing when scenario is active.
// ABOUTME: Logs SQL queries and parameters through TraceWriter for captured scenarios.

package com.ibm.websphere.samples.pbw.trace;

import org.eclipse.persistence.config.SessionCustomizer;
import org.eclipse.persistence.logging.SessionLog;
import org.eclipse.persistence.sessions.Session;

import java.util.logging.Logger;

/**
 * EclipseLink SessionCustomizer that enables SQL tracing.
 * Configured in persistence.xml to intercept all SQL operations.
 */
public class JpaTraceSessionCustomizer implements SessionCustomizer {

    private static final Logger LOGGER = Logger.getLogger(JpaTraceSessionCustomizer.class.getName());

    @Override
    public void customize(Session session) {
        LOGGER.info("Phoenix JPA trace customizer initialized");

        // Set custom session log that captures SQL
        session.setSessionLog(new TracingSessionLog(session.getSessionLog()));
    }
}
