package com.aboher.sessionsecureapp.controller;

import com.aboher.sessionsecureapp.config.TestSecurityConfig;
import com.aboher.sessionsecureapp.dto.UserDto;
import com.aboher.sessionsecureapp.enums.Role;
import com.aboher.sessionsecureapp.enums.TokenType;
import com.aboher.sessionsecureapp.model.ConfirmationToken;
import com.aboher.sessionsecureapp.model.User;
import com.aboher.sessionsecureapp.model.UserInfo;
import com.aboher.sessionsecureapp.repository.ConfirmationTokenRepository;
import com.aboher.sessionsecureapp.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Set;

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
    private final String ENDPOINT_PATH = "/users";

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ConfirmationTokenRepository confirmationTokenRepository;

    @MockBean
    private JavaMailSender javaMailSender;

    private MockMvc mockMvc;

    private UserDto validUserDto;

    private User userInTheDB;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        validUserDto = UserDto.builder()
                .firstName("ValidFirstName")
                .lastName("ValidLastName")
                .email("valid_email@mail.com")
                .password("ValidPassword1!")
                .roles(Set.of(Role.ROLE_USER)).build();

        userInTheDB = User.builder()
                .id(34L)
                .userInfo(new UserInfo("firsName", "lastName"))
                .email("user@mail.com")
                .password("$2a$10$iHbnqJssy.aTUGOQkzsCO.REOxnh7JkBR9zU4dq0dumYNFIPITMDe")
                .build();
    }

    @Test
    public void givenValidUserDto_whenCreateUser_thenReturnCreatedUserWithoutPassword() throws Exception {
        // Given
        String newUserDtoJson = objectMapper
                .writeValueAsString(validUserDto);

        // When
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> (User) invocation.getArgument(0));
        when(confirmationTokenRepository.save(any(ConfirmationToken.class)))
                .thenAnswer(invocation -> (ConfirmationToken) invocation.getArgument(0));
        doNothing().when(javaMailSender).send(any(SimpleMailMessage.class));

        // Then
        mockMvc.perform(post(ENDPOINT_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newUserDtoJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName", is(validUserDto.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(validUserDto.getLastName())))
                .andExpect(jsonPath("$.email", is(validUserDto.getEmail())))
                .andExpect(jsonPath("$.password", is(nullValue())));

        verify(userRepository, times(1)).existsByEmail(anyString());
        verify(userRepository, times(1)).save(any(User.class));
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getUserInfo().getFirstName()).isEqualTo(validUserDto.getFirstName());
        assertThat(savedUser.getUserInfo().getLastName()).isEqualTo(validUserDto.getLastName());
        assertThat(savedUser.getEmail()).isEqualTo(validUserDto.getEmail());
        assertThat(savedUser.isEnabled()).isFalse();
        assertThat(savedUser.getRoles()).isEqualTo(validUserDto.getRoles());

        if (!BCrypt.checkpw(validUserDto.getPassword(), savedUser.getPassword())) {
            throw new AssertionError("Password was not hashed correctly");
        }

        verify(confirmationTokenRepository, times(1)).save(any(ConfirmationToken.class));
        verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    public void givenInvalidFirstName_whenCreateUser_thenRespondWithError() throws Exception {
        // Given
        UserDto invalidUserDto = UserDto.builder()
                .firstName("1invalidFirstName")
                .lastName(validUserDto.getLastName())
                .email(validUserDto.getEmail())
                .password(validUserDto.getPassword()).build();
        String newUserDtoJson = objectMapper
                .writeValueAsString(invalidUserDto);

        // When

        // Then
        mockMvc.perform(post(ENDPOINT_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newUserDtoJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage", is(String.format(
                        "Invalid first name '%s'. First name can not have digits, " +
                        "and must be between 1 and 24 characters long",
                        invalidUserDto.getFirstName()))));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void givenInvalidLastName_whenCreateUser_thenRespondWithError() throws Exception {
        // Given
        UserDto invalidUserDto = UserDto.builder()
                .firstName(validUserDto.getFirstName())
                .lastName("1invalidLastName")
                .email(validUserDto.getEmail())
                .password(validUserDto.getPassword()).build();
        String newUserDtoJson = objectMapper
                .writeValueAsString(invalidUserDto);

        // When

        // Then
        mockMvc.perform(post(ENDPOINT_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newUserDtoJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage", is(String.format(
                        "Invalid last name '%s'. Last name can not have digits, " +
                        "and must be between 1 and 24 characters long",
                        invalidUserDto.getLastName()))));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void givenInvalidEmail_whenCreateUser_thenRespondWithError() throws Exception {
        // Given
        UserDto invalidUserDto = UserDto.builder()
                .firstName(validUserDto.getFirstName())
                .lastName(validUserDto.getLastName())
                .email("email_with_domain_missing@")
                .password(validUserDto.getPassword()).build();
        String newUserDtoJson = objectMapper
                .writeValueAsString(invalidUserDto);

        // When

        // Then
        mockMvc.perform(post(ENDPOINT_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newUserDtoJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage", is(String.format(
                        "Email '%s it not valid",
                        invalidUserDto.getEmail()))));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void givenInvalidPassword_whenCreateUser_thenRespondWithError() throws Exception {
        // Given
        UserDto invalidUserDto = UserDto.builder()
                .firstName(validUserDto.getFirstName())
                .lastName(validUserDto.getLastName())
                .email(validUserDto.getEmail())
                .password("some_easy_to_remember_password").build();
        String newUserDtoJson = objectMapper
                .writeValueAsString(invalidUserDto);

        // When

        // Then
        mockMvc.perform(post(ENDPOINT_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newUserDtoJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage", is(
                        "Invalid password. Valid password must have between 8 to " +
                        "24 characters. Must include uppercase and lowercase letters," +
                        " a number and a special character. Allowed special Characters: !@#$%")));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void givenVerifiedUserWithTheSameEmailExists_whenCreateUser_thenDoNotSaveUser() throws Exception {
        // Given valid user, but with email taken
        userInTheDB.setEnabled(true);
        String newUserDtoJson = objectMapper
                .writeValueAsString(validUserDto);

        // When
        when(userRepository.existsByEmail(anyString())).thenReturn(true);
        when(userRepository.findByEmail(anyString())).thenReturn(userInTheDB);
        doNothing().when(javaMailSender).send(any(SimpleMailMessage.class));

        // Then
        mockMvc.perform(post(ENDPOINT_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newUserDtoJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName", is(validUserDto.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(validUserDto.getLastName())))
                .andExpect(jsonPath("$.email", is(validUserDto.getEmail())))
                .andExpect(jsonPath("$.password", is(nullValue())));
        verify(userRepository, times(1)).existsByEmail(anyString());
        verify(userRepository, times(1)).findByEmail(anyString());
        verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void givenUnverifiedUserWithTheSameEmailExists_whenCreateUser_thenSaveUser() throws Exception {
        // Given valid user, but with email taken
        userInTheDB.setEnabled(false);
        String newUserDtoJson = objectMapper
                .writeValueAsString(validUserDto);

        // When
        when(userRepository.existsByEmail(anyString())).thenReturn(true);
        when(userRepository.findByEmail(anyString())).thenReturn(userInTheDB);
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> (User) invocation.getArgument(0));
        when(confirmationTokenRepository.findByUserAndTokenType(any(User.class), any(TokenType.class))).thenReturn(new ConfirmationToken());
        doNothing().when(confirmationTokenRepository).delete(any(ConfirmationToken.class));
        when(confirmationTokenRepository.save(any(ConfirmationToken.class)))
                .thenAnswer(invocation -> (ConfirmationToken) invocation.getArgument(0));
        doNothing().when(javaMailSender).send(any(SimpleMailMessage.class));

        // Then
        mockMvc.perform(post(ENDPOINT_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newUserDtoJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName", is(validUserDto.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(validUserDto.getLastName())))
                .andExpect(jsonPath("$.email", is(validUserDto.getEmail())))
                .andExpect(jsonPath("$.password", is(nullValue())));
        verify(userRepository, times(1)).existsByEmail(anyString());
        verify(userRepository, times(1)).findByEmail(anyString());
        verify(userRepository, times(1)).save(any(User.class));
        verify(confirmationTokenRepository, times(1)).findByUserAndTokenType(any(User.class), any(TokenType.class));
        verify(confirmationTokenRepository, times(1)).delete(any(ConfirmationToken.class));
        verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}
