package com.userservice.controller;

import com.userservice.dto.UserCreateDto;
import com.userservice.dto.UserDto;
import com.userservice.dto.UserUpdateDto;
import com.userservice.service.UserService;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger log = LogManager.getLogger(UserController.class);
    private final UserService userService;

    @Autowired
    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserCreateDto createDto){
        log.info("REST request to create user: {}", createDto.getEmail());
        UserDto createdUser = userService.createUser(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getUserCount(){
        log.info("REST request to get user count");
        long count = userService.getUserCount();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/by-email")
    public ResponseEntity<UserDto> getUserByEmail(@RequestParam String email){
        log.info("REST request to get user by email: {}", email);
        UserDto user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id){
        log.info("REST request to get user by ID: {}", id);
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers(){
        log.info("REST request to get all users");
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDto updateDto){
        log.info("REST request to update user with ID: {}", id);
        UserDto updatedUser = userService.updateUser(id, updateDto);
        return ResponseEntity.ok(updatedUser);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserDto> patchUser(@PathVariable Long id, @RequestBody UserUpdateDto updateDto){
        log.info("REST request to patch user with ID: {}", id);
        UserDto updatedUser = userService.updateUser(id, updateDto);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id){
        log.info("REST request to delete user with ID: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

}