package com.userservice.service;

import com.userservice.dto.UserDto;
import com.userservice.dto.UserCreateDto;
import com.userservice.dto.UserUpdateDto;

import java.util.List;

public interface UserService {


    UserDto createUser(UserCreateDto createDto);

    UserDto getUserById(Long id);

    List<UserDto> getAllUsers();

    UserDto updateUser(Long id, UserUpdateDto updateDto);

    void deleteUser(Long id);

    UserDto getUserByEmail(String email);

    long getUserCount();
}