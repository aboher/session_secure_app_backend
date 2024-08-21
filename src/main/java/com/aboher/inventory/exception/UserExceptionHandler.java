package com.aboher.inventory.exception;

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
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(UserValueAlreadyInUseException.class)
    public Map<String, String> handleUserValueAlreadyInUseException(UserValueAlreadyInUseException ex) {
        return getErrorMapFromErrorMessage(ex.getMessage());
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidFormFieldException.class)
    public Map<String, String> handleInvalidValueException(RuntimeException ex) {
        return getErrorMapFromErrorMessage(ex.getMessage());
    }

    private Map<String, String> getErrorMapFromErrorMessage(String errorMessage) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("errorMessage", errorMessage);
        return errorMap;
    }

}
