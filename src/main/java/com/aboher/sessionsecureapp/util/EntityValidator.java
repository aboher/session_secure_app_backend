package com.aboher.sessionsecureapp.util;

import com.aboher.sessionsecureapp.exception.InvalidEntityException;

public interface EntityValidator<T> {

    void validate(T entity) throws InvalidEntityException;
}
