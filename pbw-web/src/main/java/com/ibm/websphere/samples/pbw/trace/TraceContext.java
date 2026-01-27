// ABOUTME: Holds per-request tracing state using ThreadLocal storage.
// ABOUTME: Manages traceId, sequence numbers, and active scenario tracking.

package com.ibm.websphere.samples.pbw.trace;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-local context for request tracing. Each HTTP request gets its own
 * trace context with a unique traceId and sequence counter.
 */
public class TraceContext {

    private static final ThreadLocal<TraceContext> CURRENT = new ThreadLocal<>();

    private final String traceId;
    private final AtomicInteger sequence;

    private TraceContext() {
        this.traceId = UUID.randomUUID().toString().substring(0, 8);
        this.sequence = new AtomicInteger(0);
    }

    /**
     * Check if tracing is currently active for any scenario.
     */
    public static boolean isActive() {
        return ScenarioState.isActive();
    }

    /**
     * Start a new request context. Called at the beginning of each HTTP request.
     */
    public static void startRequest() {
        CURRENT.set(new TraceContext());
    }

    /**
     * End the current request context. Called at the end of each HTTP request.
     */
    public static void endRequest() {
        CURRENT.remove();
    }

    /**
     * Get the current trace ID for this request.
     */
    public static String getTraceId() {
        TraceContext ctx = CURRENT.get();
        return ctx != null ? ctx.traceId : null;
    }

    /**
     * Get and increment the sequence number for the current request.
     */
    public static int nextSequence() {
        TraceContext ctx = CURRENT.get();
        return ctx != null ? ctx.sequence.incrementAndGet() : 0;
    }

    /**
     * Get the current sequence number without incrementing.
     */
    public static int currentSequence() {
        TraceContext ctx = CURRENT.get();
        return ctx != null ? ctx.sequence.get() : 0;
    }

    /**
     * Check if there is a current request context.
     */
    public static boolean hasContext() {
        return CURRENT.get() != null;
    }
}
