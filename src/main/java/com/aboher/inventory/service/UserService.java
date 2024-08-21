package com.aboher.inventory.service;

import com.aboher.inventory.model.User;
import com.aboher.inventory.repository.UserRepository;
import com.aboher.inventory.util.EntityValidator;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityValidator<User> userValidator;

    public User createUser(User newUser) {
        userValidator.validate(newUser);
        encodePassword(newUser);
        setRemainingFieldsWithDefaultValues(newUser);
        return userRepository.save(newUser);
    }

    private void encodePassword(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
    }

    private void setRemainingFieldsWithDefaultValues(User user) {
        user.setEnabled(true);
        user.setAuthority("ROLE_USER");
        user.setEmailValidated(false);
    }

}
