//
// ABOUTME: RequestLogger utility for comprehensive application logging
// ABOUTME: Provides centralized logging for requests, EJBs, database operations, and templates
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
package com.ibm.websphere.samples.pbw.utils;

import java.util.Enumeration;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Comprehensive logging utility for Plants by WebSphere application modernization.
 * Logs HTTP requests, EJB invocations, database operations, and template rendering.
 */
public class RequestLogger {

    private static final String LOG_PREFIX = "[PBW-LOG] ";

    /**
     * Log HTTP request details including path, parameters, and body
     */
    public static void logRequest(HttpServletRequest request) {
        if (!Util.debugOn()) return;

        StringBuilder logEntry = new StringBuilder();
        logEntry.append(LOG_PREFIX).append("REQUEST: ");
        logEntry.append("Path=").append(request.getRequestURI());
        logEntry.append(", Method=").append(request.getMethod());
        logEntry.append(", QueryString=").append(request.getQueryString());

        // Log parameters
        logEntry.append(", Params={");
        Enumeration<String> paramNames = request.getParameterNames();
        boolean firstParam = true;
        while (paramNames.hasMoreElements()) {
            if (!firstParam) logEntry.append(", ");
            String paramName = paramNames.nextElement();
            String[] paramValues = request.getParameterValues(paramName);
            logEntry.append(paramName).append("=");
            if (paramValues != null && paramValues.length > 0) {
                if (paramValues.length == 1) {
                    logEntry.append(sanitizeValue(paramValues[0]));
                } else {
                    logEntry.append("[").append(String.join(",", paramValues)).append("]");
                }
            }
            firstParam = false;
        }
        logEntry.append("}");

        Util.debug(logEntry.toString());
    }

    /**
     * Log HTTP response details including status code
     */
    public static void logResponse(HttpServletRequest request, int statusCode) {
        if (!Util.debugOn()) return;

        StringBuilder logEntry = new StringBuilder();
        logEntry.append(LOG_PREFIX).append("RESPONSE: ");
        logEntry.append("Path=").append(request.getRequestURI());
        logEntry.append(", Status=").append(statusCode);

        Util.debug(logEntry.toString());
    }

    /**
     * Log controller (servlet/bean) invocation
     */
    public static void logController(String controllerName, String methodName) {
        if (!Util.debugOn()) return;

        StringBuilder logEntry = new StringBuilder();
        logEntry.append(LOG_PREFIX).append("CONTROLLER: ");
        logEntry.append(controllerName).append(".").append(methodName).append("()");

        Util.debug(logEntry.toString());
    }

    /**
     * Log EJB method invocation with parameters
     */
    public static void logEJBInvocation(String ejbName, String methodName, Object... params) {
        if (!Util.debugOn()) return;

        StringBuilder logEntry = new StringBuilder();
        logEntry.append(LOG_PREFIX).append("EJB: ");
        logEntry.append(ejbName).append(".").append(methodName).append("(");

        if (params != null && params.length > 0) {
            for (int i = 0; i < params.length; i++) {
                if (i > 0) logEntry.append(", ");
                logEntry.append(sanitizeValue(params[i]));
            }
        }
        logEntry.append(")");

        Util.debug(logEntry.toString());
    }

    /**
     * Log EJB method completion with return value
     */
    public static void logEJBResult(String ejbName, String methodName, Object result) {
        if (!Util.debugOn()) return;

        StringBuilder logEntry = new StringBuilder();
        logEntry.append(LOG_PREFIX).append("EJB_RESULT: ");
        logEntry.append(ejbName).append(".").append(methodName);
        logEntry.append(" returned ").append(sanitizeValue(result));

        Util.debug(logEntry.toString());
    }

    /**
     * Log database operation (JPA query)
     */
    public static void logDatabaseOperation(String operation, String query, Object... params) {
        if (!Util.debugOn()) return;

        StringBuilder logEntry = new StringBuilder();
        logEntry.append(LOG_PREFIX).append("DB_OP: ");
        logEntry.append(operation).append(" - ").append(query);

        if (params != null && params.length > 0) {
            logEntry.append(" with params: [");
            for (int i = 0; i < params.length; i++) {
                if (i > 0) logEntry.append(", ");
                logEntry.append(sanitizeValue(params[i]));
            }
            logEntry.append("]");
        }

        // Note: Actual SQL will be logged by EclipseLink at FINE level
        logEntry.append(" (SQL details in EclipseLink logs)");

        Util.debug(logEntry.toString());
    }

    /**
     * Log JPA entity manager operation with timing
     */
    public static void logJPAOperation(String operation, String entityType, Object id, long durationMs) {
        if (!Util.debugOn()) return;

        StringBuilder logEntry = new StringBuilder();
        logEntry.append(LOG_PREFIX).append("JPA_TIMING: ");
        logEntry.append(operation).append(" ").append(entityType);
        if (id != null) {
            logEntry.append("[").append(sanitizeValue(id)).append("]");
        }
        logEntry.append(" took ").append(durationMs).append("ms");

        Util.debug(logEntry.toString());
    }

    /**
     * Log template rendering (JSP forward/include)
     */
    public static void logTemplate(String templatePath, String action) {
        if (!Util.debugOn()) return;

        StringBuilder logEntry = new StringBuilder();
        logEntry.append(LOG_PREFIX).append("TEMPLATE: ");
        logEntry.append(action).append(" ").append(templatePath);

        Util.debug(logEntry.toString());
    }

    /**
     * Log data passed to template (request/session attributes)
     */
    public static void logTemplateData(HttpServletRequest request) {
        if (!Util.debugOn()) return;

        StringBuilder logEntry = new StringBuilder();
        logEntry.append(LOG_PREFIX).append("TEMPLATE_DATA: ");

        // Log request attributes
        logEntry.append("RequestAttrs={");
        Enumeration<String> reqAttrNames = request.getAttributeNames();
        boolean firstReqAttr = true;
        while (reqAttrNames.hasMoreElements()) {
            if (!firstReqAttr) logEntry.append(", ");
            String attrName = reqAttrNames.nextElement();
            Object attrValue = request.getAttribute(attrName);
            logEntry.append(attrName).append("=").append(getObjectSummary(attrValue));
            firstReqAttr = false;
        }
        logEntry.append("}");

        // Log session attributes
        HttpSession session = request.getSession(false);
        if (session != null) {
            logEntry.append(", SessionAttrs={");
            Enumeration<String> sessAttrNames = session.getAttributeNames();
            boolean firstSessAttr = true;
            while (sessAttrNames.hasMoreElements()) {
                if (!firstSessAttr) logEntry.append(", ");
                String attrName = sessAttrNames.nextElement();
                Object attrValue = session.getAttribute(attrName);
                logEntry.append(attrName).append("=").append(getObjectSummary(attrValue));
                firstSessAttr = false;
            }
            logEntry.append("}");
        }

        Util.debug(logEntry.toString());
    }

    /**
     * Get a summary representation of an object for logging
     */
    private static String getObjectSummary(Object obj) {
        if (obj == null) {
            return "null";
        } else if (obj instanceof String) {
            return "\"" + sanitizeValue(obj) + "\"";
        } else if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        } else if (obj instanceof java.util.Collection) {
            java.util.Collection<?> collection = (java.util.Collection<?>) obj;
            return collection.getClass().getSimpleName() + "[size=" + collection.size() + "]";
        } else if (obj instanceof java.util.Map) {
            java.util.Map<?, ?> map = (java.util.Map<?, ?>) obj;
            return map.getClass().getSimpleName() + "[size=" + map.size() + "]";
        } else {
            return obj.getClass().getSimpleName() + "@" + Integer.toHexString(obj.hashCode());
        }
    }

    /**
     * Sanitize sensitive values for logging (avoid logging passwords, etc.)
     */
    private static String sanitizeValue(Object value) {
        if (value == null) return "null";

        String strValue = value.toString();
        String lowerValue = strValue.toLowerCase();

        // Mask potentially sensitive fields
        if (lowerValue.contains("password") || lowerValue.contains("passwd")) {
            return "***MASKED***";
        }

        // Truncate very long values
        if (strValue.length() > 200) {
            return strValue.substring(0, 200) + "...[truncated]";
        }

        return strValue;
    }
}