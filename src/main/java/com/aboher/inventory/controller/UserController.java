package com.aboher.inventory.controller;

import com.aboher.inventory.dto.UserDto;
import com.aboher.inventory.model.User;
import com.aboher.inventory.service.UserService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final ModelMapper modelMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@RequestBody UserDto newUserDto) {
        User newUser = modelMapper.map(newUserDto, User.class);
        User createdUser = userService.createUser(newUser);
        UserDto createdUserDto = modelMapper.map(createdUser, UserDto.class);
        createdUserDto.setPassword(null);
        return createdUserDto;
    }
}
