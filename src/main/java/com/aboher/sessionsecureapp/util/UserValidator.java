package com.aboher.sessionsecureapp.util;

import com.aboher.sessionsecureapp.exception.InvalidEntityException;
import com.aboher.sessionsecureapp.exception.InvalidFormFieldException;
import com.aboher.sessionsecureapp.model.User;
import com.aboher.sessionsecureapp.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@AllArgsConstructor
@Component
public class UserValidator implements EntityValidator<User> {

    private static final String PWD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%]).{8,24}$";
    private static final String EMAIL_REGEX = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";
    private static final String NAMES_REGEX = "^\\D{1,24}$";

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(PWD_REGEX);
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
    private static final Pattern NAMES_PATTERN = Pattern.compile(NAMES_REGEX);

    private final UserRepository userRepository;

    @Override
    public void validate(User user) throws InvalidEntityException {
        checkFirstAndLastnameValidity(
                user.getUserInfo().getFirstName(),
                user.getUserInfo().getLastName());
        checkEmailValidity(user.getEmail());
        checkPasswordValidity(user.getPassword());
    }

    private void checkPasswordValidity(String password) {
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new InvalidFormFieldException("Invalid password. Valid password " +
                                                "must have between 8 to 24 characters. Must include uppercase " +
                                                "and lowercase letters, a number and a special character. Allowed " +
                                                "special Characters: !@#$%");
        }
    }

    private void checkEmailValidity(String email) {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidFormFieldException(String.format(
                    "Email '%s it not valid", email));
        }
    }

    private void checkFirstAndLastnameValidity(String firstName, String lastName) {
        if (!NAMES_PATTERN.matcher(firstName).matches()) {
            throw new InvalidFormFieldException(String.format(
                    "Invalid first name '%s'. First name can not have digits, " +
                    "and must be between 1 and 24 characters long", firstName));
        }
        if (!NAMES_PATTERN.matcher(lastName).matches()) {
            throw new InvalidFormFieldException(String.format(
                    "Invalid last name '%s'. Last name can not have digits, " +
                    "and must be between 1 and 24 characters long", lastName));
        }
    }
}