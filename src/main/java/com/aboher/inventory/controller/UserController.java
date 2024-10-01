package com.aboher.inventory.controller;

import com.aboher.inventory.dto.UserDto;
import com.aboher.inventory.mapper.Mapper;
import com.aboher.inventory.model.User;
import com.aboher.inventory.service.impl.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final Mapper<User, UserDto> userDtoMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@RequestBody UserDto newUserDto) {
        User newUser = userDtoMapper.toEntity(newUserDto);
        User createdUser = userService.createUser(newUser);
        return userDtoMapper.toDto(createdUser);
    }

    @PostMapping("/confirm-account")
    public void confirmUserAccount(@RequestParam("token") String confirmationToken) {
        userService.validateTokenAndEnableUser(confirmationToken);
    }

    @PostMapping("/request-password-change")
    public void requestPasswordChange(@RequestParam("email") String email) {
        userService.requestPasswordChange(email);
    }

    @PostMapping("/password-change")
    public void changePassword(@RequestParam("token") String token, @RequestBody UserDto userDto) {
        User user = userDtoMapper.toEntity(userDto);
        userService.validateTokenAndChangePassword(token, user.getPassword());
    }
}
