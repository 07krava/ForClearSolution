package com.forclearsolution.controllers;

import com.forclearsolution.models.User;
import com.forclearsolution.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    public void testRegisterUserSuccessfully() {
        User user = new User();
        when(userService.createUser(user)).thenReturn(user);

        ResponseEntity<String> response = userController.register(user);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User added successfully.", response.getBody());
        verify(userService, times(1)).createUser(user);
    }

    @Test
    public void testRegisterUserWithInvalidDetails() {
        User user = new User();
        when(userService.createUser(user)).thenThrow(new IllegalArgumentException("Invalid user details"));

        ResponseEntity<String> response = userController.register(user);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid user details", response.getBody());
        verify(userService, times(1)).createUser(user);
    }

    @Test
    void testAllUsers() {
        List<User> userList = new ArrayList<>();
        userList.add(new User(1L, "test1@example.com", "John", "Doe", LocalDate.now(), "Address 1", "123456789"));
        userList.add(new User(2L, "test2@example.com", "Jane", "Doe", LocalDate.now(), "Address 2", "987654321"));

        when(userService.listUsers()).thenReturn(userList);

        List<User> returnedUsers = userController.allUsers();

        assertEquals(userList.size(), returnedUsers.size());
        for (int i = 0; i < userList.size(); i++) {
            assertEquals(userList.get(i), returnedUsers.get(i));
        }
    }

    @Test
    void testGetUserById_UserFound() {
        User user = new User(1L, "test@example.com", "John", "Doe", LocalDate.now(), "Address", "123456789");

        when(userService.getUserById(1L)).thenReturn(user);

        ResponseEntity<Object> response = userController.getUserById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(user, response.getBody());
    }

    @Test
    void testGetUserById_UserNotFound() {

        when(userService.getUserById(1L)).thenThrow(EntityNotFoundException.class);

        ResponseEntity<Object> response = userController.getUserById(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found with id: 1", response.getBody());
    }

    @Test
    void testDeleteUser_UserFound() {
        UserService userService = mock(UserService.class);
        UserController userController = new UserController(userService);
        Long userId = 1L;

        when(userService.getUserById(userId)).thenReturn(new User());

        ResponseEntity<String> response = userController.deleteUser(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User deleted successfully.", response.getBody());
        verify(userService, times(1)).deleteUser(userId);
    }

    @Test
    void testDeleteUser_UserNotFound() {
        UserService userService = mock(UserService.class);
        UserController userController = new UserController(userService);
        Long userId = 1L;

        when(userService.getUserById(userId)).thenReturn(null);

        ResponseEntity<String> response = userController.deleteUser(userId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found with id: " + userId, response.getBody());
        verify(userService, never()).deleteUser(userId);
    }

    @Test
    void testDeleteUser_UserNotFoundException() {
        UserService userService = mock(UserService.class);
        UserController userController = new UserController(userService);
        Long userId = 1L;

        when(userService.getUserById(userId)).thenThrow(EntityNotFoundException.class);

        ResponseEntity<String> response = userController.deleteUser(userId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("The user was not deleted because the user was not found by id: " + userId, response.getBody());
        verify(userService, never()).deleteUser(userId);
    }

    @Test
    void testUpdateUser_UserUpdatedSuccessfully() {
        UserService userService = mock(UserService.class);
        UserController userController = new UserController(userService);
        Long userId = 1L;
        User updatedUser = new User(userId, "test@example.com", "John", "Doe", LocalDate.now(), "Address", "123456789");

        when(userService.updateUser(any(User.class), eq(userId))).thenReturn(updatedUser);

        ResponseEntity<Object> response = userController.updateUser(userId, updatedUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedUser, response.getBody());
    }

    @Test
    void testUpdateUser_InvalidDetails() {
        UserService userService = mock(UserService.class);
        UserController userController = new UserController(userService);
        Long userId = 1L;
        User updatedUser = new User(userId, "test@example.com", "John", "Doe", LocalDate.now(), "Address", "123456789");

        when(userService.updateUser(any(User.class), eq(userId))).thenThrow(IllegalArgumentException.class);

        ResponseEntity<Object> response = userController.updateUser(userId, updatedUser);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Please check your details.", response.getBody());
    }

    @Test
    void testGetUsersInDateRange_ValidDateRange() {

        UserService userService = mock(UserService.class);
        UserController userController = new UserController(userService);
        String startDate = "2024-01-01";
        String endDate = "2024-12-31";
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        List<User> expectedUsers = new ArrayList<>();

        when(userService.getUsersInDateRange(start, end)).thenReturn(expectedUsers);

        ResponseEntity<List<User>> response = userController.getUsersInDateRange(startDate, endDate);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedUsers, response.getBody());
    }

    @Test
    void testGetUsersInDateRange_InvalidDateFormat() {

        GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();
        String invalidStartDate = "invalid-date";
        String endDate = "2024-12-31";

        ResponseEntity<Object> responseEntity = globalExceptionHandler.handleHttpMessageNotReadable(
                                                             new HttpMessageNotReadableException(""));
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("Invalid date of birth format. Please use YYYY-MM-DD format.", responseEntity.getBody());
    }
}
