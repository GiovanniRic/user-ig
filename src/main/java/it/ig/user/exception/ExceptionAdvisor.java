package it.ig.user.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import it.ig.user.domain.model.enums.Role;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;


@RestControllerAdvice
@Slf4j
@AllArgsConstructor
public class ExceptionAdvisor {

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ErrorResponse> unsupportedOperationException(
            UnsupportedOperationException e) {
        return handleError(HttpStatus.NOT_IMPLEMENTED, e);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> entityNotFoundException(EntityNotFoundException e) {
        return handleError(HttpStatus.NOT_FOUND, e);
    }

    @ExceptionHandler(UserAppException.class)
    public ResponseEntity<ErrorResponse> entityNotFoundException(UserAppException e) {
        return handleError(e.getStatusCode(), e);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> httpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.error("Error parsing request body: {}", ex.getMessage(), ex);
        Throwable cause = ex.getCause();

        ErrorResponse error = null;
        if (cause instanceof InvalidFormatException invalidFormatException
        && invalidFormatException.getTargetType().equals(Role.class)) {
            var fieldName = invalidFormatException.getPath().get(0).getFieldName();
            error = new ErrorResponse(String.valueOf(HttpStatus.BAD_REQUEST.value()),
                    String.format("%s not valid. Use %s", fieldName, Arrays.toString(Role.values())));
        } else {
            error = new ErrorResponse(String.valueOf(HttpStatus.BAD_REQUEST.value()),
                    "Error parsing request body. Check log application");

        }

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);

    }


    @ExceptionHandler
    public ResponseEntity<ErrorResponse> genericError(Exception e) {
        return handleError(HttpStatus.INTERNAL_SERVER_ERROR, e);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException e) {
        return handleError(HttpStatus.FORBIDDEN, e);
    }

    protected ResponseEntity<ErrorResponse> handleError(HttpStatus status, Throwable t) {
        String message = t != null ? t.getMessage() : "An unexpected error occurred";
        log.error("Handling error with status {}: {}", status, message, t);
        ErrorResponse errorResponse = null;
        try {
            errorResponse = new ErrorResponse(String.valueOf(status.value()), message);
        } catch (Exception e) {
            log.error("Error creating ErrorResponse: {}", e.getMessage(), e);
            errorResponse = new ErrorResponse("500", e.getMessage());
        }
        int code = 500;
        try {
            code = Integer.parseInt(errorResponse.getCode());
        } catch (NumberFormatException e) {
            log.error("Error parsing error code: {}", e.getMessage(), e);
        }
        return ResponseEntity.status(HttpStatus.valueOf(code)).body(errorResponse);
    }
}
