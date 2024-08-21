package com.aboher.inventory.repository;

import com.aboher.inventory.model.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, String> {
    boolean existsByEmail(String email);
}
