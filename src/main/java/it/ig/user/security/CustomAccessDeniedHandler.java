package it.ig.user.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.ig.user.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        var auth = SecurityContextHolder.getContext().getAuthentication();

        var errorResponse = new ErrorResponse(String.valueOf(HttpStatus.FORBIDDEN.value()), "Unauthorized");
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            var username = jwt.getClaimAsString("preferred_username");
            errorResponse = new ErrorResponse(String.valueOf(HttpStatus.FORBIDDEN.value()),
                    String.format("User %s is not authorized for this operation", username));

            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));

        }
    }
}