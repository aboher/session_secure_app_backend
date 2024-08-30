package com.aboher.inventory.util;

import com.aboher.inventory.exception.InvalidFormFieldException;
import com.aboher.inventory.exception.UserValueAlreadyInUseException;
import com.aboher.inventory.model.User;
import com.aboher.inventory.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

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
                .username("validUsername")
                .firstName("ValidFirstName")
                .lastName("ValidLastName")
                .email("valid_email@mail.com")
                .password("ValidPassword1!").build();
    }

    @Test
    void givenValidUser_whenValidate_thenValidationCompletesCorrectly() {
        // Given the valid user

        // When
        when(userRepository.existsById(validUser.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(validUser.getEmail())).thenReturn(false);

        // Then
        userValidator.validate(validUser);
        verify(userRepository, times(1)).existsById(validUser.getUsername());
        verify(userRepository, times(1)).existsByEmail(validUser.getEmail());
    }


    @Test
    void givenUsernameIsTaken_whenValidate_thenThrowUserValueAlreadyInUseException() {
        // Given the valid user, but with username taken

        // When
        when(userRepository.existsById(validUser.getUsername())).thenReturn(true);

        // Then
        assertThatThrownBy(() -> userValidator.validate(validUser))
                .isInstanceOf(UserValueAlreadyInUseException.class)
                .hasMessageContaining(String.format("Username '%s' already in use. " +
                        "Please choose another username", validUser.getUsername()));
        verify(userRepository, times(1)).existsById(validUser.getUsername());
        verify(userRepository, never()).existsByEmail(validUser.getEmail());
    }

    @Test
    void givenEmailIsTaken_whenValidate_thenThrowUserValueAlreadyInUseException() {
        // Given the valid user, but with email taken

        // When
        when(userRepository.existsById(validUser.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(validUser.getEmail())).thenReturn(true);


        // Then
        assertThatThrownBy(() -> userValidator.validate(validUser))
                .isInstanceOf(UserValueAlreadyInUseException.class)
                .hasMessageContaining(String.format("Email '%s' already in use. " +
                        "Please choose another email", validUser.getEmail()));
        verify(userRepository, times(1)).existsById(validUser.getUsername());
        verify(userRepository, times(1)).existsByEmail(validUser.getEmail());
    }

    @Test
    void givenInvalidUsername_whenValidate_thenThrowInvalidFormFieldException() {
        // Given
        User invalidUser = User.builder()
                .username("1invalidUsername")
                .firstName(validUser.getFirstName())
                .lastName(validUser.getLastName())
                .email(validUser.getEmail())
                .password(validUser.getPassword()).build();

        // When
        when(userRepository.existsById(invalidUser.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(invalidUser.getEmail())).thenReturn(false);

        // Then
        assertThatThrownBy(() -> userValidator.validate(invalidUser))
                .isInstanceOf(InvalidFormFieldException.class)
                .hasMessageContaining(String.format("Invalid username '%s'. Username " +
                        "must have 4 to 24 characters, must begin with a letter, " +
                        "and characters allowed are letters, numbers, underscores " +
                        "and hyphens", invalidUser.getUsername()));
        verify(userRepository, times(1)).existsById(invalidUser.getUsername());
        verify(userRepository, times(1)).existsByEmail(invalidUser.getEmail());
    }

    @Test
    void givenInvalidFirstName_whenValidate_thenThrowInvalidFormFieldException() {
        // Given
        User invalidUser = User.builder()
                .username(validUser.getUsername())
                .firstName("1invalidFirstName")
                .lastName(validUser.getLastName())
                .email(validUser.getEmail())
                .password(validUser.getPassword()).build();

        // When
        when(userRepository.existsById(invalidUser.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(invalidUser.getEmail())).thenReturn(false);

        // Then
        assertThatThrownBy(() -> userValidator.validate(invalidUser))
                .isInstanceOf(InvalidFormFieldException.class)
                .hasMessageContaining(String.format("Invalid first name '%s'. First " +
                        "name can not have digits, and must be between 1 and 24 " +
                        "characters long", invalidUser.getFirstName()));
        verify(userRepository, times(1)).existsById(invalidUser.getUsername());
        verify(userRepository, times(1)).existsByEmail(invalidUser.getEmail());
    }

    @Test
    void givenInvalidLastName_whenValidate_thenThrowInvalidFormFieldException() {
        // Given
        User invalidUser = User.builder()
                .username(validUser.getUsername())
                .firstName(validUser.getFirstName())
                .lastName("1invalidLastName")
                .email(validUser.getEmail())
                .password(validUser.getPassword()).build();

        // When
        when(userRepository.existsById(invalidUser.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(invalidUser.getEmail())).thenReturn(false);

        // Then
        assertThatThrownBy(() -> userValidator.validate(invalidUser))
                .isInstanceOf(InvalidFormFieldException.class)
                .hasMessageContaining(String.format("Invalid last name '%s'. Last " +
                        "name can not have digits, and must be between 1 and 24 " +
                        "characters long", invalidUser.getLastName()));
        verify(userRepository, times(1)).existsById(invalidUser.getUsername());
        verify(userRepository, times(1)).existsByEmail(invalidUser.getEmail());
    }

    @Test
    void givenInvalidEmail_whenValidate_thenThrowInvalidFormFieldException() {
        // Given
        User invalidUser = User.builder()
                .username(validUser.getUsername())
                .firstName(validUser.getFirstName())
                .lastName(validUser.getLastName())
                .email("email_with_domain_missing@sdf")
                .password(validUser.getPassword()).build();

        // When
        when(userRepository.existsById(invalidUser.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(invalidUser.getEmail())).thenReturn(false);

        // Then
        assertThatThrownBy(() -> userValidator.validate(invalidUser))
                .isInstanceOf(InvalidFormFieldException.class)
                .hasMessageContaining(String.format("Email '%s it not valid",
                        invalidUser.getEmail()));
        verify(userRepository, times(1)).existsById(invalidUser.getUsername());
        verify(userRepository, times(1)).existsByEmail(invalidUser.getEmail());
    }

    @Test
    void givenInvalidPassword_whenValidate_thenThrowInvalidFormFieldException() {
        // Given
        User invalidUser = User.builder()
                .username(validUser.getUsername())
                .firstName(validUser.getFirstName())
                .lastName(validUser.getLastName())
                .email(validUser.getEmail())
                .password("some_easy_to_remember_password").build();

        // When
        when(userRepository.existsById(invalidUser.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(invalidUser.getEmail())).thenReturn(false);

        // Then
        assertThatThrownBy(() -> userValidator.validate(invalidUser))
                .isInstanceOf(InvalidFormFieldException.class)
                .hasMessageContaining("Invalid password. Valid password must have " +
                        "between 8 to 24 characters. Must include uppercase and " +
                        "lowercase letters, a number and a special character. Allowed " +
                        "special Characters: !@#$%");
        verify(userRepository, times(1)).existsById(invalidUser.getUsername());
        verify(userRepository, times(1)).existsByEmail(invalidUser.getEmail());
    }
}
