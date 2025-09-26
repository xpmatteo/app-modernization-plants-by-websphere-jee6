//
// ABOUTME: JSF PhaseListener to log template rendering and lifecycle events
// ABOUTME: Captures which XHTML templates are being rendered and JSF action invocations
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

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.util.logging.Logger;

/**
 * JSF PhaseListener to log template rendering and JSF lifecycle events
 * for application characterization and modernization analysis.
 */
public class TemplateLoggingPhaseListener implements PhaseListener {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(TemplateLoggingPhaseListener.class.getName());

    @Override
    public void afterPhase(PhaseEvent event) {
        FacesContext context = event.getFacesContext();
        PhaseId phaseId = event.getPhaseId();

        // Log template rendering with full details
        if (phaseId == PhaseId.RENDER_RESPONSE) {
            String viewId = context.getViewRoot().getViewId();
            HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
            String realPath = request.getSession().getServletContext().getRealPath(viewId);

            logger.info("[TEMPLATE] Rendering template: " + viewId);
            if (realPath != null) {
                logger.info("[TEMPLATE] Template file path: " + realPath);
            }
        }

        // Log navigation after action invocation
        if (phaseId == PhaseId.INVOKE_APPLICATION) {
            if (context.getRenderResponse()) {
                String viewId = context.getViewRoot().getViewId();
                logger.info("[NAVIGATION] Action completed -> navigating to: " + viewId);
            }
        }
    }

    @Override
    public void beforePhase(PhaseEvent event) {
        // We don't need beforePhase logging for template tracking
    }

    @Override
    public PhaseId getPhaseId() {
        // Listen to all phases
        return PhaseId.ANY_PHASE;
    }
}