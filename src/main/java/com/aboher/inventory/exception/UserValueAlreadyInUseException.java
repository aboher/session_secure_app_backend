package com.aboher.inventory.exception;

public class UserValueAlreadyInUseException extends RuntimeException {

    public UserValueAlreadyInUseException(String message) {
        super(message);
    }
}
