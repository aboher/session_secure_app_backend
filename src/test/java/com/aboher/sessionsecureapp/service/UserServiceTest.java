package com.aboher.sessionsecureapp.service;

import com.aboher.sessionsecureapp.enums.Role;
import com.aboher.sessionsecureapp.model.User;
import com.aboher.sessionsecureapp.model.UserInfo;
import com.aboher.sessionsecureapp.repository.UserRepository;
import com.aboher.sessionsecureapp.service.impl.UserService;
import com.aboher.sessionsecureapp.util.EntityValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EntityValidator<User> userValidator;

    @InjectMocks
    private UserService userService;

    @Test
    public void givenValidUser_whenCreateUser_thenReturnUserWithDefaultFields() {
        // Given
        User validUser = User.builder()
                .userInfo(new UserInfo("firstName", "lastName"))
                .email("user@mail.com")
                .password("sfRG$%34sd3lf")
                .roles(new HashSet<>()).build();

        String expectedEncodedPassword = "encoded_password";

        // When
        doNothing().when(userValidator).validate(any(User.class));
        when(passwordEncoder.encode(anyString())).thenReturn(expectedEncodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> (User) invocation.getArgument(0));

        // Then
        User createdUser = userService.createUser(validUser);
        verify(userValidator, times(1)).validate(any(User.class));
        verify(passwordEncoder, times(1)).encode(anyString());
        verify(userRepository, times(1)).save(any(User.class));
        assertThat(createdUser.getPassword()).isEqualTo(expectedEncodedPassword);
        assertThat(createdUser.isEnabled()).isTrue();
        assertThat(createdUser.getRoles().contains(Role.ROLE_USER)).isTrue();
    }
}
