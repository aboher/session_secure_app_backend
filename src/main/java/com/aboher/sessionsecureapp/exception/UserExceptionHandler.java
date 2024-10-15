package com.aboher.sessionsecureapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class UserExceptionHandler {

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({InvalidFormFieldException.class, InvalidSessionException.class, InvalidAttributeException.class})
    public Map<String, String> handleInvalidValueException(RuntimeException ex) {
        return getErrorMapFromErrorMessage(ex.getMessage());
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(InvalidTokenException.class)
    public Map<String, String> handleInvalidTokenException(RuntimeException ex) {
        return getErrorMapFromErrorMessage(ex.getMessage());
    }

    private Map<String, String> getErrorMapFromErrorMessage(String errorMessage) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("errorMessage", errorMessage);
        return errorMap;
    }

}
