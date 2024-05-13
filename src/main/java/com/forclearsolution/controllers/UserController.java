package com.forclearsolution.controllers;

import com.forclearsolution.models.User;
import com.forclearsolution.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping()
    public ResponseEntity<String> register(@RequestBody User user) {

        try {
            userService.createUser(user);
            return ResponseEntity.ok("User added successfully.");
        } catch (IllegalArgumentException e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Please check your details.";
            return ResponseEntity.badRequest().body(errorMessage);
        }
    }

    @GetMapping()
    public List<User> allUsers() {
        return userService.listUsers();
    }

    @GetMapping("{id}")
    public ResponseEntity<Object> getUserById(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with id: " + id);
        }
    }

    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id);
            if (user != null) {
                userService.deleteUser(id);
                return ResponseEntity.ok("User deleted successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with id: " + id);
            }
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The user was not deleted because the user was not found by id: " + id);
        }
    }

    @PatchMapping("{id}")
    public ResponseEntity<Object> updateUser(@PathVariable Long id, @RequestBody User user) {

        try {
            User updatedUser = userService.updateUser(user, id);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Please check your details.";
            return ResponseEntity.badRequest().body(errorMessage);
        }
    }

    @GetMapping("/birthdate-range")
    public ResponseEntity<List<User>> getUsersInDateRange(@RequestParam("startDate") String startDate,
                                                          @RequestParam("endDate") String endDate) {

        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            List<User> users = userService.getUsersInDateRange(start, end);

            return ResponseEntity.ok(users);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
