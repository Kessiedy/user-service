package com.userservice.service;

import com.userservice.entity.User;

import java.util.List;

public interface UserService {

    User createUser(String name, String email, Integer age);

    User getUserById(Long id);

    List<User> getAllUsers();

    User updateUser(Long id, String name, String email, Integer age);

    void deleteUser(Long id);

    User getUserByEmail(String email);

    long getUserCount();

    void deleteAllUsers();
}