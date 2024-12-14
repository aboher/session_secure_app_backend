package com.aboher.sessionsecureapp.exception;

import com.aboher.sessionsecureapp.dto.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class UserExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
            InvalidFormFieldException.class,
            InvalidSessionException.class,
            InvalidAttributeException.class})
    public ErrorMessage handleInvalidValueException(RuntimeException ex) {
        return new ErrorMessage(ex.getMessage());
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(InvalidTokenException.class)
    public ErrorMessage handleInvalidTokenException(RuntimeException ex) {
        return new ErrorMessage(ex.getMessage());
    }
}
