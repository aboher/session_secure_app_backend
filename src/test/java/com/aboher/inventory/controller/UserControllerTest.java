package com.aboher.inventory.controller;

import com.aboher.inventory.dto.UserDto;
import com.aboher.inventory.model.User;
import com.aboher.inventory.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UserController userController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    void givenValidUserDto_whenCreateUser_thenReturnCreatedUserWithoutPassword() throws Exception {
        // Given
        String newUserDtoJson = objectMapper.writeValueAsString(UserDto.builder()
                .username("username")
                .firstName("firstName")
                .lastName("lastName")
                .email("username@mail.com")
                .password("dfdFE%$543DFG").build());
        User mockUser = new User();
        UserDto mockReturnedUserDto = UserDto.builder()
                .username("username")
                .firstName("firstName")
                .lastName("lastName")
                .email("username@mail.com")
                .password("$2a$10$5pr1l69exFk8YmqtyR4k0ud9BLaBJO.7QKgy8SRLgfAEerxeh604K").build();

        // When
        when(modelMapper.map(any(UserDto.class), eq(User.class))).thenReturn(mockUser);
        when(userService.createUser(any(User.class))).thenReturn(mockUser);
        when(modelMapper.map(any(User.class), eq(UserDto.class))).thenReturn(mockReturnedUserDto);

        // Then
        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newUserDtoJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username", is(mockReturnedUserDto.getUsername())))
                .andExpect(jsonPath("$.firstName", is(mockReturnedUserDto.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(mockReturnedUserDto.getLastName())))
                .andExpect(jsonPath("$.email", is(mockReturnedUserDto.getEmail())))
                .andExpect(jsonPath("$.password", is(nullValue())));
    }
}