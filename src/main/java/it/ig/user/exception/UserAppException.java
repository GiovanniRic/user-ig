package it.ig.user.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;


@AllArgsConstructor
@Getter
public class UserAppException extends RuntimeException {
    private String message;
    private HttpStatus statusCode;

}