package com.example.clothesshop.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SellerAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger logger = LoggerFactory.getLogger(SellerAccessDeniedHandler.class);

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String username = (auth != null) ? auth.getName() : "Anonymous";
        Object authorities = (auth != null) ? auth.getAuthorities() : "None";
        String requestedUri = request.getRequestURI();
        String query = request.getQueryString();
        if (query != null && !query.isEmpty()) {
            requestedUri += "?" + query;
        }

        // Log the access denied attempt with structured message
        logger.warn("SELLER ACCESS DENIED - user='{}', authorities='{}', uri='{}', reason='{}'",
                username, authorities, requestedUri,
                accessDeniedException != null ? accessDeniedException.getMessage() : "unspecified");

        // If response already committed we cannot redirect
        if (response.isCommitted()) {
            logger.debug("Response already committed. Skipping redirect to /access-denied.");
            return;
        }

        // Set 403 status and redirect to seller-specific access denied page (respecting context path)
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        String redirectPath = request.getContextPath() + "/access-denied";
        response.sendRedirect(redirectPath);
    }
}