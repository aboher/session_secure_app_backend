package com.aboher.inventory.service.impl;

import com.aboher.inventory.enums.Role;
import com.aboher.inventory.model.User;
import com.aboher.inventory.repository.UserRepository;
import com.aboher.inventory.util.EntityValidator;
import lombok.AllArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityValidator<User> userValidator;
    private final EmailService emailService;
    private final EmailConfirmationService emailConfirmationService;
    private final PasswordChangeService passwordChangeService;

    public User createUser(User user) {
        userValidator.validate(user);
        if (isEmailAvailable(user.getEmail())) {
            return saveNewUserInDatabase(user);
        }
        User savedUser = userRepository.findByEmail(user.getEmail());
        if (savedUser.isEnabled()) {
            notifyUserHeAlreadyHasAnAccount(user);
            return user;
        }
        emailConfirmationService.deleteUserConfirmationTokenIfExists(savedUser);
        user.setId(savedUser.getId());
             /*Here, at this point, I'm replacing the unverified user in the database
             with the new one. I do this because someone may have used this email by
             mistake, and not being able to use it again, only because of that,
             will, in effect, make the email unavailable for the person who really
             has this email.*/
        return saveNewUserInDatabase(user);
    }

    private void notifyUserHeAlreadyHasAnAccount(User user) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(user.getEmail());
        mail.setSubject("Account already validated");
        mail.setText("""
                Your account is already validated.
                If you don't remember your password, please click the "I forgot my password" link in the Sign In page.
                                
                If it wasn't you trying to create an account, just ignore this email.""");
        emailService.sendEmail(mail);
    }

    private User saveNewUserInDatabase(User user) {
        encodePassword(user);
        user.getRoles().add(Role.ROLE_USER);
        user.setEnabled(false);
        User savedUser = userRepository.save(user);
        emailConfirmationService.sendEmailWithConfirmationToken(savedUser);
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
            notifyUserHeDoNotHaveAnAccount(user);
            return;
        }
        passwordChangeService.deleteUserConfirmationTokenIfExists(user);
        passwordChangeService.sendEmailWithConfirmationToken(user);
    }

    private void notifyUserHeDoNotHaveAnAccount(User user) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(user.getEmail());
        mail.setSubject("Account required");
        mail.setText("""
                Your don't have an account created with this email.
                You have to create an account and confirm your email before being able to change your password.
                                
                If it wasn't you trying to change your password, just ignore this email.""");
        emailService.sendEmail(mail);
    }

    public void validateTokenAndEnableUser(String confirmationToken) {
        User user = emailConfirmationService.validateTokenAndReturnCorrespondingUser(confirmationToken);
        enableUser(user);
    }

    public void enableUser(User user) {
        user.setEnabled(true);
        userRepository.save(user);
    }

    public void changeUserPassword(User user, String newPassword) {
        user.setPassword(newPassword);
        userValidator.validate(user);
        encodePassword(user);
        userRepository.save(user);
    }

    public void validateTokenAndChangePassword(String token, String newPassword) {
        User user = passwordChangeService.validateTokenAndReturnCorrespondingUser(token);
        changeUserPassword(user, newPassword);
    }
}
