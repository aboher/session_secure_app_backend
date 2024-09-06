package com.aboher.inventory.mapper.impl;

import com.aboher.inventory.dto.UserDto;
import com.aboher.inventory.mapper.Mapper;
import com.aboher.inventory.model.User;
import lombok.Builder;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Builder
@Component
public class UserDtoMapper implements Mapper<User, UserDto> {

    private final ModelMapper modelMapper;

    @Override
    public UserDto toDto(User user) {
        UserDto userDto = modelMapper.map(user, UserDto.class);
        userDto.setPassword(null);
        return userDto;
    }

    @Override
    public User toEntity(UserDto dto) {
        return modelMapper.map(dto, User.class);
    }
}
