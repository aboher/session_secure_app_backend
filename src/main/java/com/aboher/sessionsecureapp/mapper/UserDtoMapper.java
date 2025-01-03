package com.aboher.sessionsecureapp.mapper;

import com.aboher.sessionsecureapp.dto.UserDto;
import com.aboher.sessionsecureapp.model.User;
import com.aboher.sessionsecureapp.model.UserInfo;
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
        userDto.setFirstName(user.getUserInfo().getFirstName());
        userDto.setLastName(user.getUserInfo().getLastName());
        userDto.setPassword(null);
        return userDto;
    }

    @Override
    public User toEntity(UserDto dto) {
        User user = modelMapper.map(dto, User.class);
        user.setUserInfo(new UserInfo(dto.getFirstName(), dto.getLastName()));
        return user;
    }
}
