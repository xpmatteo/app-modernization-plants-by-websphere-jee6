// ABOUTME: REST endpoint servlet for controlling trace scenarios.
// ABOUTME: Provides /trace/start, /trace/stop, and /trace/status endpoints.

package com.ibm.websphere.samples.pbw.trace;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * REST endpoint for controlling trace scenarios.
 *
 * Endpoints:
 * - POST /trace/start?scenario=name&description=desc - Start a new scenario
 * - POST /trace/stop - Stop the current scenario
 * - GET /trace/status - Get current scenario status
 */
public class ScenarioController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo();

        if ("/status".equals(path)) {
            handleStatus(resp);
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"error\": \"Unknown endpoint\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo();

        if ("/start".equals(path)) {
            handleStart(req, resp);
        } else if ("/stop".equals(path)) {
            handleStop(resp);
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"error\": \"Unknown endpoint\"}");
        }
    }

    private void handleStart(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        String scenario = req.getParameter("scenario");
        String description = req.getParameter("description");

        if (scenario == null || scenario.trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\": \"Missing required parameter: scenario\"}");
            return;
        }

        // Sanitize scenario name for use as filename
        scenario = scenario.trim().replaceAll("[^a-zA-Z0-9_-]", "-");

        if (ScenarioState.isActive()) {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            out.write("{\"error\": \"Scenario already active: " +
                    ScenarioState.getCurrentName() + "\"}");
            return;
        }

        if (ScenarioState.start(scenario, description)) {
            TraceWriter.startScenario(scenario, description);
            resp.setStatus(HttpServletResponse.SC_OK);
            out.write("{\"status\": \"started\", \"scenario\": \"" + scenario + "\"}");
        } else {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            out.write("{\"error\": \"Failed to start scenario\"}");
        }
    }

    private void handleStop(HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        ScenarioState.ScenarioInfo info = ScenarioState.stop();
        TraceWriter.stopScenario();

        if (info != null) {
            long duration = System.currentTimeMillis() - info.getStartTime();
            resp.setStatus(HttpServletResponse.SC_OK);
            out.write("{\"status\": \"stopped\", \"scenario\": \"" + info.getName() +
                    "\", \"duration_ms\": " + duration + "}");
        } else {
            resp.setStatus(HttpServletResponse.SC_OK);
            out.write("{\"status\": \"stopped\", \"message\": \"No scenario was active\"}");
        }
    }

    private void handleStatus(HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        ScenarioState.ScenarioInfo info = ScenarioState.getCurrent();

        if (info != null) {
            long elapsed = System.currentTimeMillis() - info.getStartTime();
            out.write("{\"active\": true, \"scenario\": \"" + info.getName() +
                    "\", \"description\": \"" + (info.getDescription() != null ?
                    info.getDescription() : "") + "\", \"elapsed_ms\": " + elapsed + "}");
        } else {
            out.write("{\"active\": false}");
        }
    }
}
