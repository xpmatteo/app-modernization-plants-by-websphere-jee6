//
// ABOUTME: HTTP request/response logging filter for application modernization analysis
// ABOUTME: Intercepts all HTTP requests to log comprehensive request/response details
//
// COPYRIGHT LICENSE: This information contains sample code provided in source code form. You may copy,
// modify, and distribute these sample programs in any form without payment to IBM for the purposes of
// developing, using, marketing or distributing application programs conforming to the application
// programming interface for the operating platform for which the sample code is written.
// Notwithstanding anything to the contrary, IBM PROVIDES THE SAMPLE SOURCE CODE ON AN "AS IS" BASIS
// AND IBM DISCLAIMS ALL WARRANTIES, EXPRESS OR IMPLIED, INCLUDING, BUT NOT LIMITED TO, ANY IMPLIED
// WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY, FITNESS FOR A PARTICULAR PURPOSE,
// TITLE, AND ANY WARRANTY OR CONDITION OF NON-INFRINGEMENT. IBM SHALL NOT BE LIABLE FOR ANY DIRECT,
// INDIRECT, INCIDENTAL, SPECIAL OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OR OPERATION OF THE
// SAMPLE SOURCE CODE. IBM HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS
// OR MODIFICATIONS TO THE SAMPLE SOURCE CODE.
//
// (C) COPYRIGHT International Business Machines Corp., 2025
// All Rights Reserved * Licensed Materials - Property of IBM
//
package com.ibm.websphere.samples.pbw.war;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import com.ibm.websphere.samples.pbw.utils.RequestLogger;

/**
 * Servlet Filter for logging HTTP requests and responses.
 * Captures all HTTP traffic for application modernization analysis.
 */
@WebFilter(filterName = "RequestLoggingFilter", urlPatterns = {"/*"})
public class RequestLoggingFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Filter initialization
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            // Skip logging for static resources to reduce noise
            String requestURI = httpRequest.getRequestURI();
            if (shouldLogRequest(requestURI)) {
                // Log incoming request
                RequestLogger.logRequest(httpRequest);

                // Wrap response to capture status code
                StatusCapturingResponseWrapper responseWrapper =
                    new StatusCapturingResponseWrapper(httpResponse);

                try {
                    // Continue with the request
                    chain.doFilter(request, responseWrapper);
                } finally {
                    // Log response details
                    RequestLogger.logResponse(httpRequest, responseWrapper.getStatus());
                }
            } else {
                // Skip logging but continue processing
                chain.doFilter(request, response);
            }
        } else {
            // Not HTTP request/response, continue without logging
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
        // Filter cleanup
    }

    /**
     * Determine if we should log this request based on the URI
     */
    private boolean shouldLogRequest(String requestURI) {
        if (requestURI == null) return false;

        // Skip common static resources to reduce log noise
        String[] skipExtensions = {".css", ".js", ".png", ".jpg", ".jpeg", ".gif", ".ico", ".woff", ".woff2"};
        String lowerURI = requestURI.toLowerCase();

        for (String ext : skipExtensions) {
            if (lowerURI.endsWith(ext)) {
                return false;
            }
        }

        // Skip certain paths
        if (lowerURI.contains("/images/") ||
            lowerURI.contains("/css/") ||
            lowerURI.contains("/js/") ||
            lowerURI.contains("/favicon.ico")) {
            return false;
        }

        return true;
    }

    /**
     * Response wrapper to capture HTTP status code
     */
    private static class StatusCapturingResponseWrapper extends HttpServletResponseWrapper {
        private int httpStatus = HttpServletResponse.SC_OK;

        public StatusCapturingResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public void setStatus(int sc) {
            this.httpStatus = sc;
            super.setStatus(sc);
        }

        @Override
        public void setStatus(int sc, String sm) {
            this.httpStatus = sc;
            super.setStatus(sc, sm);
        }

        @Override
        public void sendError(int sc) throws IOException {
            this.httpStatus = sc;
            super.sendError(sc);
        }

        @Override
        public void sendError(int sc, String msg) throws IOException {
            this.httpStatus = sc;
            super.sendError(sc, msg);
        }

        @Override
        public void sendRedirect(String location) throws IOException {
            this.httpStatus = HttpServletResponse.SC_MOVED_TEMPORARILY;
            super.sendRedirect(location);
        }

        public int getStatus() {
            return httpStatus;
        }
    }
}