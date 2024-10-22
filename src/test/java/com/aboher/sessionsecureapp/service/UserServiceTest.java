package com.aboher.sessionsecureapp.service;

import com.aboher.sessionsecureapp.config.FrontendProperties;
import com.aboher.sessionsecureapp.enums.Role;
import com.aboher.sessionsecureapp.exception.InvalidEntityException;
import com.aboher.sessionsecureapp.exception.InvalidTokenException;
import com.aboher.sessionsecureapp.model.User;
import com.aboher.sessionsecureapp.model.UserInfo;
import com.aboher.sessionsecureapp.repository.UserRepository;
import com.aboher.sessionsecureapp.service.verification.PasswordChangeThroughEmailService;
import com.aboher.sessionsecureapp.service.verification.TokenBasedVerificationService;
import com.aboher.sessionsecureapp.util.EntityValidator;
import com.aboher.sessionsecureapp.util.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User validUser;

    @BeforeEach
    public void setUp() {
        validUser = User.builder()
                .userInfo(new UserInfo("ValidFirstName", "ValidLastName"))
                .email("validUser@mail.com")
                .password("ValidPassword1!")
                .roles(Set.of(Role.ROLE_USER)).build();
    }

    @Test
    void givenInvalidUser_whenCreateUser_thenThrowInvalidEntityException() {
        // Given
        User invalidUser = new User();
        EntityValidator<User> userValidator = mock(UserValidator.class);
        userService.setUserValidator(userValidator);

        // When
        doThrow(InvalidEntityException.class).when(userValidator).validate(any(User.class));

        //Then
        assertThatThrownBy(() -> userService.createUser(invalidUser))
                .isInstanceOf(InvalidEntityException.class);
        verify(userValidator, times(1)).validate(any(User.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void givenValidUser_whenCreateUser_thenReturnUserWithDefaultFields() {
        // Given valid user
        String expectedEncodedPassword = "encoded_password";
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        userService.setPasswordEncoder(passwordEncoder);
        EntityValidator<User> userValidator = mock(UserValidator.class);
        userService.setUserValidator(userValidator);
        TokenBasedVerificationService emailAccountConfirmationService = mock(TokenBasedVerificationService.class);
        userService.setEmailAccountConfirmationService(emailAccountConfirmationService);

        // When
        doNothing().when(userValidator).validate(any(User.class));
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn(expectedEncodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> (User) invocation.getArgument(0));
        doNothing().when(emailAccountConfirmationService).sendMessageWithConfirmationToken(any(User.class));

        // Then
        User createdUser = userService.createUser(validUser);
        verify(userValidator, times(1)).validate(any(User.class));
        verify(userRepository, times(1)).existsByEmail(anyString());
        verify(passwordEncoder, times(1)).encode(anyString());
        verify(userRepository, times(1)).save(any(User.class));
        verify(emailAccountConfirmationService, times(1)).sendMessageWithConfirmationToken(any(User.class));
        assertThat(createdUser.getUserInfo()).isEqualTo(validUser.getUserInfo());
        assertThat(createdUser.getEmail()).isEqualTo(validUser.getEmail());
        assertThat(createdUser.getPassword()).isEqualTo(expectedEncodedPassword);
        assertThat(createdUser.isEnabled()).isFalse();
        assertThat(createdUser.getRoles()).isEqualTo(validUser.getRoles());
    }

    @Test
    public void givenThereIsAnUnconfirmedAccountWithTheSameEmail_whenCreateUser_thenReturnUserWithDefaultFields() {
        // Given valid user
        String expectedEncodedPassword = "encoded_password";
        User userStoredInDB = User.builder()
                .id(234L)
                .enabled(false)
                .build();
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        userService.setPasswordEncoder(passwordEncoder);
        EntityValidator<User> userValidator = mock(UserValidator.class);
        userService.setUserValidator(userValidator);
        TokenBasedVerificationService emailAccountConfirmationService = mock(TokenBasedVerificationService.class);
        userService.setEmailAccountConfirmationService(emailAccountConfirmationService);

        // When
        doNothing().when(userValidator).validate(any(User.class));
        when(userRepository.existsByEmail(anyString())).thenReturn(true);
        when(userRepository.findByEmail(anyString())).thenReturn(userStoredInDB);
        doNothing().when(emailAccountConfirmationService).deleteConfirmationTokenIfExists(any(User.class));
        when(passwordEncoder.encode(anyString())).thenReturn(expectedEncodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> (User) invocation.getArgument(0));
        doNothing().when(emailAccountConfirmationService).sendMessageWithConfirmationToken(any(User.class));


        User createdUser = userService.createUser(validUser);

        // Then
        verify(userValidator, times(1)).validate(any(User.class));
        verify(userRepository, times(1)).existsByEmail(anyString());
        verify(userRepository, times(1)).findByEmail(anyString());
        verify(emailAccountConfirmationService, times(1)).deleteConfirmationTokenIfExists(any(User.class));
        verify(passwordEncoder, times(1)).encode(anyString());
        verify(userRepository, times(1)).save(any(User.class));
        verify(emailAccountConfirmationService, times(1)).sendMessageWithConfirmationToken(any(User.class));
        assertThat(createdUser.getId()).isEqualTo(userStoredInDB.getId());
        assertThat(createdUser.getUserInfo()).isEqualTo(validUser.getUserInfo());
        assertThat(createdUser.getEmail()).isEqualTo(validUser.getEmail());
        assertThat(createdUser.getPassword()).isEqualTo(expectedEncodedPassword);
        assertThat(createdUser.isEnabled()).isFalse();
        assertThat(createdUser.getRoles()).isEqualTo(validUser.getRoles());
    }

    @Test
    public void givenThereIsAConfirmedAccountWithTheSameEmail_whenCreateUser_thenReturnUserWithDefaultFieldsAndNotifyExistentUser() {
        // Given valid user
        User userStoredInDB = User.builder()
                .email(validUser.getEmail())
                .enabled(true)
                .build();
        String frontendUrl = "frontendUrl";
        String requestPasswordChangePath = "RequestPasswordChangePath";
        SimpleMailMessage expectedMail = getExpectedMail(userStoredInDB, frontendUrl, requestPasswordChangePath);
        EntityValidator<User> userValidator = mock(UserValidator.class);
        userService.setUserValidator(userValidator);
        FrontendProperties frontendProperties = mock(FrontendProperties.class);
        userService.setFrontendProperties(frontendProperties);
        EmailMessageSender emailMessageSender = mock(EmailMessageSender.class);
        userService.setEmailMessageSender(emailMessageSender);

        // When
        doNothing().when(userValidator).validate(any(User.class));
        when(userRepository.existsByEmail(anyString())).thenReturn(true);
        when(userRepository.findByEmail(anyString())).thenReturn(userStoredInDB);
        when(frontendProperties.getUrl()).thenReturn(frontendUrl);
        when(frontendProperties.getRequestPasswordChangePath()).thenReturn(requestPasswordChangePath);
        doNothing().when(emailMessageSender).sendMessage(any(SimpleMailMessage.class));


        User createdUser = userService.createUser(validUser);

        // Then
        verify(userValidator, times(1)).validate(any(User.class));
        verify(userRepository, times(1)).existsByEmail(anyString());
        verify(userRepository, times(1)).findByEmail(anyString());
        verify(frontendProperties, times(1)).getUrl();
        verify(frontendProperties, times(1)).getRequestPasswordChangePath();
        verify(emailMessageSender, times(1)).sendMessage(any(SimpleMailMessage.class));
        ArgumentCaptor<SimpleMailMessage> mailCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(emailMessageSender).sendMessage(mailCaptor.capture());
        SimpleMailMessage capturedMail = mailCaptor.getValue();

        assertThat(createdUser.getUserInfo()).isEqualTo(validUser.getUserInfo());
        assertThat(createdUser.getEmail()).isEqualTo(validUser.getEmail());
        assertThat(createdUser.getRoles()).isEqualTo(validUser.getRoles());
        assertThat(capturedMail).isEqualTo(expectedMail);
    }

    private SimpleMailMessage getExpectedMail(User userStoredInDB, String frontendUrl, String requestPasswordChangePath) {
        SimpleMailMessage expectedMail = new SimpleMailMessage();
        expectedMail.setTo(userStoredInDB.getEmail());
        expectedMail.setSubject("Account already validated");
        expectedMail.setText(String.format("""
                        Your account is already validated.
                        If you don't remember your password, please click the "I forgot my password" link in the Sign In page.
                        Or click here: %s%s
                                        
                        If it wasn't you trying to create an account, just ignore this email.""",
                frontendUrl,
                requestPasswordChangePath));
        return expectedMail;
    }

    @Test
    public void givenValidToken_whenValidateTokenAndEnableUser_thenEnableUser() {
        // Given
        validUser.setEnabled(false);
        String confirmationToken = "someConfirmationToken";
        TokenBasedVerificationService emailAccountConfirmationService = mock(TokenBasedVerificationService.class);
        userService.setEmailAccountConfirmationService(emailAccountConfirmationService);

        // When
        when(emailAccountConfirmationService.validateTokenAndReturnCorrespondingUser(anyString())).thenReturn(validUser);
        when(userRepository.save(any(User.class))).thenReturn(validUser);
        userService.validateTokenAndEnableUser(confirmationToken);

        // Then
        verify(emailAccountConfirmationService, times(1)).validateTokenAndReturnCorrespondingUser(anyString());
        verify(userRepository, times(1)).save(any(User.class));
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        assertThat(capturedUser.isEnabled()).isEqualTo(true);
        assertThat(capturedUser.getUserInfo()).isEqualTo(validUser.getUserInfo());
        assertThat(capturedUser.getEmail()).isEqualTo(validUser.getEmail());
        assertThat(capturedUser.getPassword()).isEqualTo(validUser.getPassword());
        assertThat(capturedUser.getRoles()).isEqualTo(validUser.getRoles());
    }

    @Test
    public void givenInvalidToken_whenValidateTokenAndEnableUser_thenThrowInvalidTokenException() {
        // Given
        String confirmationToken = "someConfirmationToken";
        TokenBasedVerificationService emailAccountConfirmationService = mock(TokenBasedVerificationService.class);
        userService.setEmailAccountConfirmationService(emailAccountConfirmationService);

        // When
        doThrow(InvalidTokenException.class).when(emailAccountConfirmationService).validateTokenAndReturnCorrespondingUser(anyString());

        // Then
        assertThatThrownBy(() -> emailAccountConfirmationService.validateTokenAndReturnCorrespondingUser(confirmationToken))
                .isInstanceOf(InvalidTokenException.class);
        verify(emailAccountConfirmationService, times(1)).validateTokenAndReturnCorrespondingUser(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void givenUserExistAndItsEnabled_whenRequestPasswordChange_thenRequestPasswordChangeSucceeds() {
        // Given
        String email = "user@mail.com";
        User user = User.builder()
                .email(email)
                .enabled(true)
                .build();
        TokenBasedVerificationService passwordChangeThroughEmailService = mock(PasswordChangeThroughEmailService.class);
        userService.setPasswordChangeThroughEmailService(passwordChangeThroughEmailService);

        // When
        when(userRepository.findByEmail(anyString())).thenReturn(user);
        doNothing().when(passwordChangeThroughEmailService).deleteConfirmationTokenIfExists(any(User.class));
        doNothing().when(passwordChangeThroughEmailService).sendMessageWithConfirmationToken(any(User.class));

        // Then
        userService.requestPasswordChange(email);
        verify(userRepository, times(1)).findByEmail(anyString());
        verify(passwordChangeThroughEmailService, times(1)).deleteConfirmationTokenIfExists(any(User.class));
        verify(passwordChangeThroughEmailService, times(1)).sendMessageWithConfirmationToken(any(User.class));
    }

    @Test
    public void givenUserExistAndItsNotEnabled_whenRequestPasswordChange_thenUserIsNotified() {
        // Given
        String email = "user@mail.com";
        User user = User.builder()
                .email(email)
                .enabled(false)
                .build();
        SimpleMailMessage expectedMail = new SimpleMailMessage();
        expectedMail.setTo(user.getEmail());
        expectedMail.setSubject("Account required");
        expectedMail.setText("""
                You don't have an account created with this email.
                You have to create an account and confirm your email before being able to change your password.
                                
                If it wasn't you trying to change your password, just ignore this email.""");

        TokenBasedVerificationService passwordChangeThroughEmailService = mock(PasswordChangeThroughEmailService.class);
        userService.setPasswordChangeThroughEmailService(passwordChangeThroughEmailService);
        EmailMessageSender emailMessageSender = mock(EmailMessageSender.class);
        userService.setEmailMessageSender(emailMessageSender);

        // When
        when(userRepository.findByEmail(anyString())).thenReturn(user);
        doNothing().when(emailMessageSender).sendMessage(any(SimpleMailMessage.class));

        // Then
        userService.requestPasswordChange(email);
        verify(userRepository, times(1)).findByEmail(anyString());
        verify(emailMessageSender, times(1)).sendMessage(any(SimpleMailMessage.class));
        ArgumentCaptor<SimpleMailMessage> emailCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(emailMessageSender).sendMessage(emailCaptor.capture());
        SimpleMailMessage capturedEmail = emailCaptor.getValue();
        assertThat(capturedEmail).isEqualTo(expectedMail);
    }

    @Test
    public void givenUserDoesNotExist_whenRequestPasswordChange_thenDoesNothing() {
        // Given
        String email = "user@mail.com";
        TokenBasedVerificationService passwordChangeThroughEmailService = mock(PasswordChangeThroughEmailService.class);
        userService.setPasswordChangeThroughEmailService(passwordChangeThroughEmailService);
        EmailMessageSender emailMessageSender = mock(EmailMessageSender.class);
        userService.setEmailMessageSender(emailMessageSender);

        // When
        when(userRepository.findByEmail(anyString())).thenReturn(null);

        // Then
        userService.requestPasswordChange(email);
        verify(userRepository, times(1)).findByEmail(anyString());
        verify(emailMessageSender, never()).sendMessage(any(SimpleMailMessage.class));
        verify(passwordChangeThroughEmailService, never()).deleteConfirmationTokenIfExists(any(User.class));
        verify(passwordChangeThroughEmailService, never()).sendMessageWithConfirmationToken(any(User.class));
    }

    @Test
    public void givenValidPassword_whenValidateTokenAndChangePassword_thenPasswordIsChanged() {
        // Given
        String newPassword = "newValidPassword1!";
        String encodedPassword = "encodedPassword";
        String token = "someToken";
        TokenBasedVerificationService passwordChangeThroughEmailService = mock(PasswordChangeThroughEmailService.class);
        userService.setPasswordChangeThroughEmailService(passwordChangeThroughEmailService);
        EntityValidator<User> userValidator = mock(UserValidator.class);
        userService.setUserValidator(userValidator);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        userService.setPasswordEncoder(passwordEncoder);

        // When
        when(passwordChangeThroughEmailService.validateTokenAndReturnCorrespondingUser(anyString())).thenReturn(validUser);
        doNothing().when(userValidator).validate(any(User.class));
        when(passwordEncoder.encode(anyString())).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> (User) invocation.getArgument(0));

        // Then
        userService.validateTokenAndChangePassword(token, newPassword);

        verify(passwordChangeThroughEmailService, times(1)).validateTokenAndReturnCorrespondingUser(anyString());
        verify(userValidator, times(1)).validate(any(User.class));
        verify(userRepository, times(1)).save(any(User.class));
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getPassword()).isEqualTo(encodedPassword);
    }

    @Test
    public void givenInvalidPassword_whenValidateTokenAndChangePassword_thenThrowsInvalidEntityException() {
        // Given
        String newPassword = "newInvalidPassword";
        String token = "someToken";
        TokenBasedVerificationService passwordChangeThroughEmailService = mock(PasswordChangeThroughEmailService.class);
        userService.setPasswordChangeThroughEmailService(passwordChangeThroughEmailService);
        EntityValidator<User> userValidator = mock(UserValidator.class);
        userService.setUserValidator(userValidator);

        // When
        when(passwordChangeThroughEmailService.validateTokenAndReturnCorrespondingUser(anyString())).thenReturn(validUser);
        doThrow(InvalidEntityException.class).when(userValidator).validate(any(User.class));

        // Then
        assertThatThrownBy(() -> userService.validateTokenAndChangePassword(token, newPassword))
                .isInstanceOf(InvalidEntityException.class);

        verify(passwordChangeThroughEmailService, times(1)).validateTokenAndReturnCorrespondingUser(anyString());
        verify(userValidator, times(1)).validate(any(User.class));
    }

    @Test
    public void givenInvalidToken_whenValidateTokenAndChangePassword_thenThrowsInvalidTokenException() {
        // Given
        String newPassword = "newValidPassword1!";
        String token = "someToken";
        TokenBasedVerificationService passwordChangeThroughEmailService = mock(PasswordChangeThroughEmailService.class);
        userService.setPasswordChangeThroughEmailService(passwordChangeThroughEmailService);

        // When
        doThrow(InvalidTokenException.class).when(passwordChangeThroughEmailService).validateTokenAndReturnCorrespondingUser(anyString());

        // Then
        assertThatThrownBy(() -> userService.validateTokenAndChangePassword(token, newPassword))
                .isInstanceOf(InvalidTokenException.class);
        verify(passwordChangeThroughEmailService, times(1)).validateTokenAndReturnCorrespondingUser(anyString());
    }
}