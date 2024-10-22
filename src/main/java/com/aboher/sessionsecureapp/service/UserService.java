package com.aboher.sessionsecureapp.service;

import com.aboher.sessionsecureapp.config.FrontendProperties;
import com.aboher.sessionsecureapp.exception.InvalidTokenException;
import com.aboher.sessionsecureapp.model.User;
import com.aboher.sessionsecureapp.repository.UserRepository;
import com.aboher.sessionsecureapp.service.verification.TokenBasedVerificationService;
import com.aboher.sessionsecureapp.util.EntityValidator;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@Data
@AllArgsConstructor
public class UserService {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private EntityValidator<User> userValidator;
    private MessageSender<SimpleMailMessage> emailMessageSender;
    private TokenBasedVerificationService emailAccountConfirmationService;
    private TokenBasedVerificationService passwordChangeThroughEmailService;
    private TokenBasedVerificationService accountDeletionThroughEmailService;
    private FrontendProperties frontendProperties;
    private SessionService sessionService;

    public User createUser(User user) {
        userValidator.validate(user);

        if (isEmailAvailable(user.getEmail())) {
            return saveNewUserInDatabase(user);
        }

        User savedUser = userRepository.findByEmail(user.getEmail());
        if (savedUser.isEnabled()) {
            notifyUserHeAlreadyHasAConfirmedAccount(user);
            /*It's important to note here that we return the user data that we get
             * from the request and not the savedUser. We'll be giving away confidential
             * information if we did so. And also supplying a mean for knowing whether
             * a given email has an account or not. And we want only the person
             * who really owns the email to know that, that's why he's notified
             * only though email*/
            return user;
        }

        emailAccountConfirmationService.deleteConfirmationTokenIfExists(savedUser);
        user.setId(savedUser.getId());
             /*Here, at this point, I'm replacing the unverified user in the database
             with the new one. I do this because someone may have used this email by
             mistake, for example with a typo, and not being able to use it again,
             only because of that, will, in effect, make the email unavailable for the
             person who really has this email.*/
        return saveNewUserInDatabase(user);
    }

    public void validateTokenAndEnableUser(String confirmationToken) throws InvalidTokenException {
        User user = emailAccountConfirmationService.validateTokenAndReturnCorrespondingUser(confirmationToken);
        enableUser(user);
    }

    private void notifyUserHeAlreadyHasAConfirmedAccount(User user) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(user.getEmail());
        mail.setSubject("Account already validated");
        mail.setText(String.format("""
                        Your account is already validated.
                        If you don't remember your password, please click the "I forgot my password" link in the Sign In page.
                        Or click here: %s%s
                                        
                        If it wasn't you trying to create an account, just ignore this email.""",
                frontendProperties.getUrl(),
                frontendProperties.getRequestPasswordChangePath()));
        emailMessageSender.sendMessage(mail);
    }

    private User saveNewUserInDatabase(User user) {
        encodePassword(user);
        user.setEnabled(false);
        User savedUser = userRepository.save(user);
        emailAccountConfirmationService.sendMessageWithConfirmationToken(savedUser);
        return savedUser;
    }

    private boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    private void encodePassword(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
    }

    public void requestPasswordChange(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return;
        }
        if (!user.isEnabled()) {
            notifyUserHeDoesNotHaveAnAccount(user);
            return;
        }
        passwordChangeThroughEmailService.deleteConfirmationTokenIfExists(user);
        passwordChangeThroughEmailService.sendMessageWithConfirmationToken(user);
    }

    private void notifyUserHeDoesNotHaveAnAccount(User user) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(user.getEmail());
        mail.setSubject("Account required");
        mail.setText("""
                You don't have an account created with this email.
                You have to create an account and confirm your email before being able to change your password.
                                
                If it wasn't you trying to change your password, just ignore this email.""");
        emailMessageSender.sendMessage(mail);
    }

    private void enableUser(User user) {
        user.setEnabled(true);
        userRepository.save(user);
    }

    public void validateTokenAndChangePassword(String token, String newPassword) throws InvalidTokenException {
        User user = passwordChangeThroughEmailService.validateTokenAndReturnCorrespondingUser(token);
        changeUserPassword(user, newPassword);
    }

    private void changeUserPassword(User user, String newPassword) {
        user.setPassword(newPassword);
        userValidator.validate(user);
        encodePassword(user);
        userRepository.save(user);
    }

    public void requestAccountDeletion() {
        User user = userRepository.findByEmail(sessionService.getEmail());
        accountDeletionThroughEmailService.deleteConfirmationTokenIfExists(user);
        accountDeletionThroughEmailService.sendMessageWithConfirmationToken(user);
    }

    public void validateTokenAndDeleteAccount(String token) throws InvalidTokenException {
        User user = accountDeletionThroughEmailService.validateTokenAndReturnCorrespondingUser(token);
        deleteAccount(user);
    }

    private void deleteAccount(User user) {
        closeAllActiveSessions(user);
        userRepository.delete(user);
    }

    private void closeAllActiveSessions(User user) {
        Set<String> activeSessions = sessionService.getAllActiveSessionsIds(user.getEmail());
        activeSessions.forEach(sessionService::invalidateSession);
    }
}
