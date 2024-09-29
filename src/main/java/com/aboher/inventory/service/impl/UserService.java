package com.aboher.inventory.service.impl;

import com.aboher.inventory.enums.Role;
import com.aboher.inventory.enums.TokenType;
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
    private final ConfirmationTokenService confirmationTokenService;
    private final PasswordEncoder passwordEncoder;
    private final EntityValidator<User> userValidator;
    private final EmailConfirmationService emailConfirmationService;
    private final EmailService emailService;

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
        confirmationTokenService.deleteUserConfirmationTokenIfExist(savedUser, TokenType.EMAIL_CONFIRMATION_TOKEN);
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
}
