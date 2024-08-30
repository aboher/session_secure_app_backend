package com.aboher.inventory.controller;

import com.aboher.inventory.config.TestSecurityConfig;
import com.aboher.inventory.model.User;
import com.aboher.inventory.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@Import(TestSecurityConfig.class)
class UserControllerIT {

    private static final Logger logger = LoggerFactory.getLogger(UserControllerIT.class);

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    private MockMvc mockMvc;

    private User validUser;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        validUser = User.builder()
                .username("validUsername")
                .firstName("ValidFirstName")
                .lastName("ValidLastName")
                .email("valid_email@mail.com")
                .password("ValidPassword1!").build();
    }

    @Test
    public void givenValidUserDto_whenCreateUser_thenReturnCreatedUserWithoutPassword() throws Exception {
        // Given
        String newUserDtoJson = objectMapper
                .writeValueAsString(validUser);

        // When
        when(userRepository.existsById(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> (User) invocation.getArgument(0));

        // Then
        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newUserDtoJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username", is(validUser.getUsername())))
                .andExpect(jsonPath("$.firstName", is(validUser.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(validUser.getLastName())))
                .andExpect(jsonPath("$.email", is(validUser.getEmail())))
                .andExpect(jsonPath("$.password", is(nullValue())));

        verify(userRepository, times(1)).existsById(anyString());
        verify(userRepository, times(1)).existsByEmail(anyString());
        verify(userRepository, times(1)).save(any(User.class));
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getUsername()).isEqualTo(validUser.getUsername());
        assertThat(savedUser.getFirstName()).isEqualTo(validUser.getFirstName());
        assertThat(savedUser.getLastName()).isEqualTo(validUser.getLastName());
        assertThat(savedUser.getEmail()).isEqualTo(validUser.getEmail());
        assertThat(savedUser.isEnabled()).isTrue();
        assertThat(savedUser.getAuthority()).isEqualTo("ROLE_USER");
        assertThat(savedUser.isEmailValidated()).isFalse();

        if (!BCrypt.checkpw(validUser.getPassword(), savedUser.getPassword())) {
            throw new AssertionError("Password was not hashed correctly");
        }
    }

    @Test
    public void givenUsernameAlreadyTaken_whenCreateUser_thenRespondWithError() throws Exception {
        // Given valid user, but with username taken
        String newUserDtoJson = objectMapper
                .writeValueAsString(validUser);

        // When
        when(userRepository.existsById(anyString())).thenReturn(true);

        // Then
        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newUserDtoJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorMessage", is(String.format(
                        "Username '%s' already in use. Please choose another username",
                        validUser.getUsername()))));
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void givenEmailAlreadyTaken_whenCreateUser_thenRespondWithError() throws Exception {
        // Given valid user, but with email taken
        String newUserDtoJson = objectMapper
                .writeValueAsString(validUser);

        // When
        when(userRepository.existsById(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Then
        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newUserDtoJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorMessage", is(String.format(
                        "Email '%s' already in use. Please choose another email",
                        validUser.getEmail()))));
        verify(userRepository, times(1)).existsById(anyString());
        verify(userRepository, times(1)).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void givenInvalidUsername_whenCreateUser_thenRespondWithError() throws Exception {
        // Given
        User invalidUser = User.builder()
                .username("1invalidUsername")
                .firstName(validUser.getFirstName())
                .lastName(validUser.getLastName())
                .email(validUser.getEmail())
                .password(validUser.getPassword()).build();
        String newUserDtoJson = objectMapper
                .writeValueAsString(invalidUser);

        // When
        when(userRepository.existsById(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        // Then
        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newUserDtoJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage", is(String.format(
                        "Invalid username '%s'. Username must have 4 to 24 characters, " +
                                "must begin with a letter, and characters allowed are letters, " +
                                "numbers, underscores and hyphens",
                        invalidUser.getUsername()))));
        verify(userRepository, times(1)).existsById(anyString());
        verify(userRepository, times(1)).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void givenInvalidFirstName_whenCreateUser_thenRespondWithError() throws Exception {
        // Given
        User invalidUser = User.builder()
                .username(validUser.getUsername())
                .firstName("1invalidFirstName")
                .lastName(validUser.getLastName())
                .email(validUser.getEmail())
                .password(validUser.getPassword()).build();
        String newUserDtoJson = objectMapper
                .writeValueAsString(invalidUser);

        // When
        when(userRepository.existsById(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        // Then
        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newUserDtoJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage", is(String.format(
                        "Invalid first name '%s'. First name can not have digits, " +
                                "and must be between 1 and 24 characters long",
                        invalidUser.getFirstName()))));
        verify(userRepository, times(1)).existsById(anyString());
        verify(userRepository, times(1)).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void givenInvalidLastName_whenCreateUser_thenRespondWithError() throws Exception {
        // Given
        User invalidUser = User.builder()
                .username(validUser.getUsername())
                .firstName(validUser.getFirstName())
                .lastName("1invalidLastName")
                .email(validUser.getEmail())
                .password(validUser.getPassword()).build();
        String newUserDtoJson = objectMapper
                .writeValueAsString(invalidUser);

        // When
        when(userRepository.existsById(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        // Then
        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newUserDtoJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage", is(String.format(
                        "Invalid last name '%s'. Last name can not have digits, " +
                                "and must be between 1 and 24 characters long",
                        invalidUser.getLastName()))));
        verify(userRepository, times(1)).existsById(anyString());
        verify(userRepository, times(1)).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void givenInvalidEmail_whenCreateUser_thenRespondWithError() throws Exception {
        // Given
        User invalidUser = User.builder()
                .username(validUser.getUsername())
                .firstName(validUser.getFirstName())
                .lastName(validUser.getLastName())
                .email("email_with_domain_missing@sdf")
                .password(validUser.getPassword()).build();
        String newUserDtoJson = objectMapper
                .writeValueAsString(invalidUser);

        // When
        when(userRepository.existsById(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        // Then
        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newUserDtoJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage", is(String.format(
                        "Email '%s it not valid",
                        invalidUser.getEmail()))));
        verify(userRepository, times(1)).existsById(anyString());
        verify(userRepository, times(1)).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void givenInvalidPassword_whenCreateUser_thenRespondWithError() throws Exception {
        // Given
        User invalidUser = User.builder()
                .username(validUser.getUsername())
                .firstName(validUser.getFirstName())
                .lastName(validUser.getLastName())
                .email(validUser.getEmail())
                .password("some_easy_to_remember_password").build();
        String newUserDtoJson = objectMapper
                .writeValueAsString(invalidUser);

        // When
        when(userRepository.existsById(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        // Then
        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newUserDtoJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage", is("Invalid password. Valid " +
                        "password must have between 8 to 24 characters. Must include " +
                        "uppercase and lowercase letters, a number and a special " +
                        "character. Allowed special Characters: !@#$%")));
        verify(userRepository, times(1)).existsById(anyString());
        verify(userRepository, times(1)).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }
}

