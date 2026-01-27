// ABOUTME: Servlet filter that captures HTTP request/response details.
// ABOUTME: Logs method, URL, parameters, session state before and after request.

package com.ibm.websphere.samples.pbw.trace;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Servlet filter that captures HTTP request and response details for tracing.
 * Only active when a trace scenario is running.
 */
@WebFilter("/*")
public class HttpTraceFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(HttpTraceFilter.class.getName());

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOGGER.info("HttpTraceFilter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // Skip non-HTTP requests
        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Skip trace endpoint itself to avoid recursion
        String uri = httpRequest.getRequestURI();
        if (uri.startsWith("/trace/")) {
            chain.doFilter(request, response);
            return;
        }

        // Skip static resources
        if (isStaticResource(uri)) {
            chain.doFilter(request, response);
            return;
        }

        // If not tracing, pass through
        if (!ScenarioState.isActive()) {
            chain.doFilter(request, response);
            return;
        }

        // Start request context
        TraceContext.startRequest();

        try {
            // Log request
            logRequest(httpRequest);

            // Wrap response to capture status and redirect
            StatusCapturingResponseWrapper responseWrapper =
                    new StatusCapturingResponseWrapper(httpResponse);

            // Process request
            chain.doFilter(request, responseWrapper);

            // Log response
            logResponse(httpRequest, responseWrapper);

        } finally {
            TraceContext.endRequest();
        }
    }

    private void logRequest(HttpServletRequest request) {
        String method = request.getMethod();
        String url = request.getRequestURI();
        String queryString = request.getQueryString();
        if (queryString != null && !queryString.isEmpty()) {
            url = url + "?" + queryString;
        }

        @SuppressWarnings("unchecked")
        Map<String, String[]> params = request.getParameterMap();
        Map<String, Object> session = captureSession(request);

        TraceWriter.writeHttpRequest(method, url, params, session);
    }

    private void logResponse(HttpServletRequest request, StatusCapturingResponseWrapper response) {
        int status = response.getStatus();
        String redirect = response.getRedirectLocation();
        Map<String, Object> session = captureSession(request);

        TraceWriter.writeHttpResponse(status, redirect, session);
    }

    private Map<String, Object> captureSession(HttpServletRequest request) {
        Map<String, Object> sessionData = new HashMap<>();

        HttpSession session = request.getSession(false);
        if (session != null) {
            Enumeration<String> names = session.getAttributeNames();
            while (names.hasMoreElements()) {
                String name = names.nextElement();
                Object value = session.getAttribute(name);
                // Only capture simple types and known bean types
                if (isSerializableForTrace(value)) {
                    sessionData.put(name, serializeForTrace(name, value));
                }
            }
        }

        return sessionData;
    }

    private boolean isSerializableForTrace(Object value) {
        if (value == null) return true;
        if (value instanceof String) return true;
        if (value instanceof Number) return true;
        if (value instanceof Boolean) return true;
        // Skip complex objects to avoid bloating traces
        return false;
    }

    private Object serializeForTrace(String name, Object value) {
        if (value == null) return null;
        if (value instanceof String) return value;
        if (value instanceof Number) return value;
        if (value instanceof Boolean) return value;
        return value.toString();
    }

    private boolean isStaticResource(String uri) {
        return uri.endsWith(".css") ||
                uri.endsWith(".js") ||
                uri.endsWith(".png") ||
                uri.endsWith(".jpg") ||
                uri.endsWith(".jpeg") ||
                uri.endsWith(".gif") ||
                uri.endsWith(".ico") ||
                uri.endsWith(".woff") ||
                uri.endsWith(".woff2") ||
                uri.endsWith(".ttf") ||
                uri.endsWith(".svg") ||
                uri.contains("/resources/");
    }

    @Override
    public void destroy() {
        LOGGER.info("HttpTraceFilter destroyed");
    }

    /**
     * Response wrapper that captures status code and redirect location.
     */
    private static class StatusCapturingResponseWrapper extends HttpServletResponseWrapper {

        private int status = 200;
        private String redirectLocation;

        public StatusCapturingResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public void setStatus(int sc) {
            this.status = sc;
            super.setStatus(sc);
        }

        @Override
        public void sendRedirect(String location) throws IOException {
            this.status = 302;
            this.redirectLocation = location;
            super.sendRedirect(location);
        }

        @Override
        public void sendError(int sc) throws IOException {
            this.status = sc;
            super.sendError(sc);
        }

        @Override
        public void sendError(int sc, String msg) throws IOException {
            this.status = sc;
            super.sendError(sc, msg);
        }

        public int getStatus() {
            return status;
        }

        public String getRedirectLocation() {
            return redirectLocation;
        }
    }
}
