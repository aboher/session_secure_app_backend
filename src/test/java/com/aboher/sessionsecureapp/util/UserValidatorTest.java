package com.aboher.sessionsecureapp.util;

import com.aboher.sessionsecureapp.exception.InvalidFormFieldException;
import com.aboher.sessionsecureapp.model.User;
import com.aboher.sessionsecureapp.model.UserInfo;
import com.aboher.sessionsecureapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class UserValidatorTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserValidator userValidator;

    private User validUser;

    @BeforeEach
    public void setUp() {
        validUser = User.builder()
                .userInfo(new UserInfo("ValidFirstName", "ValidLastName"))
                .email("valid_email@mail.com")
                .password("ValidPassword1!").build();
    }

    @Test
    void givenValidUser_whenValidate_thenValidationCompletesCorrectly() {
        // Given the valid user

        // Then
        userValidator.validate(validUser);
    }

    @Test
    void givenInvalidFirstName_whenValidate_thenThrowInvalidFormFieldException() {
        // Given
        User invalidUser = User.builder()
                .userInfo(new UserInfo("1invalidFirstName", validUser.getUserInfo().getLastName()))
                .email(validUser.getEmail())
                .password(validUser.getPassword()).build();

        // Then
        assertThatThrownBy(() -> userValidator.validate(invalidUser))
                .isInstanceOf(InvalidFormFieldException.class)
                .hasMessageContaining(String.format("Invalid first name '%s'. First " +
                                                    "name can not have digits, and must be between 1 and 24 " +
                                                    "characters long", invalidUser.getUserInfo().getFirstName()));
    }

    @Test
    void givenInvalidLastName_whenValidate_thenThrowInvalidFormFieldException() {
        // Given
        User invalidUser = User.builder()
                .userInfo(new UserInfo(validUser.getUserInfo().getFirstName(), "1invalidLastName"))
                .email(validUser.getEmail())
                .password(validUser.getPassword()).build();

        // Then
        assertThatThrownBy(() -> userValidator.validate(invalidUser))
                .isInstanceOf(InvalidFormFieldException.class)
                .hasMessageContaining(String.format("Invalid last name '%s'. Last " +
                                                    "name can not have digits, and must be between 1 and 24 " +
                                                    "characters long", invalidUser.getUserInfo().getLastName()));
    }

    @Test
    void givenInvalidEmail_whenValidate_thenThrowInvalidFormFieldException() {
        // Given
        User invalidUser = User.builder()
                .userInfo(new UserInfo(validUser.getUserInfo()))
                .email("email_with_domain_missing@sdf")
                .password(validUser.getPassword()).build();

        // Then
        assertThatThrownBy(() -> userValidator.validate(invalidUser))
                .isInstanceOf(InvalidFormFieldException.class)
                .hasMessageContaining(String.format("Email '%s it not valid",
                        invalidUser.getEmail()));
    }

    @Test
    void givenInvalidPassword_whenValidate_thenThrowInvalidFormFieldException() {
        // Given
        User invalidUser = User.builder()
                .userInfo(new UserInfo(validUser.getUserInfo()))
                .email(validUser.getEmail())
                .password("some_easy_to_remember_password").build();

        // Then
        assertThatThrownBy(() -> userValidator.validate(invalidUser))
                .isInstanceOf(InvalidFormFieldException.class)
                .hasMessageContaining("Invalid password. Valid password must have " +
                                      "between 8 to 24 characters. Must include uppercase and " +
                                      "lowercase letters, a number and a special character. Allowed " +
                                      "special Characters: !@#$%");
    }
}
