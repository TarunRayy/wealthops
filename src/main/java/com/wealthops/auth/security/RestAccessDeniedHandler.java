package com.wealthops.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wealthops.common.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Fires when the caller IS authenticated but @PreAuthorize (or a URL
 * rule) rejects them for lacking the required role/scope — different
 * from RestAuthenticationEntryPoint, which fires when there's no valid
 * authentication at all. Same reasoning: without this, Spring
 * Security's default 403 has no body, inconsistent with every other
 * error in the app.
 */
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public RestAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                        AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse body = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                "You do not have permission to perform this action",
                request.getRequestURI(),
                null
        );
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
