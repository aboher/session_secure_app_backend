package com.aboher.inventory.util.impl;

import com.aboher.inventory.exception.InvalidFormFieldException;
import com.aboher.inventory.exception.UserValueAlreadyInUseException;
import com.aboher.inventory.model.User;
import com.aboher.inventory.repository.UserRepository;
import com.aboher.inventory.util.EntityValidator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@AllArgsConstructor
@Component
public class UserValidator implements EntityValidator<User> {

    private static final String PWD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%]).{8,24}$";
    private static final String EMAIL_REGEX = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";
    private static final String NAMES_REGEX = "^\\D{1,24}$";
    private static final String USER_REGEX = "^[A-z][A-z0-9-_]{3,23}$";

    private static final Pattern passwordPattern = Pattern.compile(PWD_REGEX);
    private static final Pattern emailPattern = Pattern.compile(EMAIL_REGEX);
    private static final Pattern namesPattern = Pattern.compile(NAMES_REGEX);
    private static final Pattern usernamePattern = Pattern.compile(USER_REGEX);

    private final UserRepository userRepository;

    @Override
    public void validate(User user) {
        checkUsernameIsAvailable(user.getUsername());
        checkEmailIsAvailable(user.getEmail());
        checkUsernameValidity(user.getUsername());
        checkFirstAndLastnameValidity(user.getFirstName(), user.getLastName());
        checkEmailValidity(user.getEmail());
        checkPasswordValidity(user.getPassword());
    }

    private void checkUsernameIsAvailable(String username) {
        if (userRepository.existsById(username)) {
            throw new UserValueAlreadyInUseException(String.format(
                    "Username '%s' already in use. Please choose another username",
                    username));
        }
    }

    private void checkEmailIsAvailable(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new UserValueAlreadyInUseException(String.format(
                    "Email '%s' already in use. Please choose another email",
                    email));
        }
    }

    private void checkPasswordValidity(String password) {
        if (!passwordPattern.matcher(password).matches()) {
            throw new InvalidFormFieldException("Invalid password. Valid password " +
                    "must have between 8 to 24 characters. Must include uppercase " +
                    "and lowercase letters, a number and a special character. Allowed " +
                    "special Characters: !@#$%");
        }
    }

    private void checkEmailValidity(String email) {
        if (!emailPattern.matcher(email).matches()) {
            throw new InvalidFormFieldException(String.format(
                    "Email '%s it not valid", email));
        }
    }

    private void checkFirstAndLastnameValidity(String firstName, String lastName) {
        if (!namesPattern.matcher(firstName).matches()) {
            throw new InvalidFormFieldException(String.format(
                    "Invalid first name '%s'. First name can not have digits, " +
                            "and must be between 1 and 24 characters long", firstName));
        }
        if (!namesPattern.matcher(lastName).matches()) {
            throw new InvalidFormFieldException(String.format(
                    "Invalid last name '%s'. Last name can not have digits, " +
                            "and must be between 1 and 24 characters long", lastName));
        }
    }

    private void checkUsernameValidity(String username) {
        if (!usernamePattern.matcher(username).matches()) {
            throw new InvalidFormFieldException(String.format(
                    "Invalid username '%s'. Username must have 4 to 24 characters, " +
                            "must begin with a letter, and characters allowed are letters, " +
                            "numbers, underscores and hyphens", username));
        }
    }
}