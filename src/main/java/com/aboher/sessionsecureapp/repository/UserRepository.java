package com.aboher.sessionsecureapp.repository;

import com.aboher.sessionsecureapp.model.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
    boolean existsByEmail(String email);

    User findByEmail(String email);
}
