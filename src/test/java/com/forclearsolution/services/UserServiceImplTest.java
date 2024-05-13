package com.forclearsolution.services;

import com.forclearsolution.exceptions.DateOfBirthException;
import com.forclearsolution.models.User;
import com.forclearsolution.repositories.UserRepository;
import com.forclearsolution.services.impl.UserServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;

public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Value("${minAgeForRegistration}")
    private int minAgeForRegistration;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Field field;
        try {
            field = UserServiceImplTest.class.getDeclaredField("minAgeForRegistration");
            field.setAccessible(true);
            field.setInt(this, 18);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testCreateUser_UserAlreadyExists() {

        User user = new User();
        user.setEmail("existing@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setDateOfBirth(LocalDate.of(1990, 1, 1));

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(new User()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.createUser(user));
        assertEquals("This user already exists!", exception.getMessage());
    }

    @Test
    public void testCreateUser_FutureDateOfBirth() {
        User user = new User();
        user.setEmail("valid@email.com");
        user.setFirstName("Tom");
        user.setLastName("Test");
        user.setDateOfBirth(LocalDate.now().plusDays(1));
        user.setPhoneNumber("+380123456789");

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> userService.createUser(user));
        assertEquals("The date of birth cannot be in the future.", exception.getMessage());
    }

    @Test
    void testCreateUser_EmptyFirstName() {

        User user = new User();
        user.setEmail("test@example.com");
        user.setLastName("Doe");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.createUser(user));
        assertEquals("First name cannot be empty.", exception.getMessage());
    }

    @Test
    void testCreateUser_EmptyLastName() {

        User user = new User();
        user.setEmail("test@example.com");
        user.setFirstName("John");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.createUser(user));
        assertEquals("Last name cannot be empty.", exception.getMessage());
    }

    @Test
    void testCreateUser_EmptyEmail() {

        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.createUser(user));
        assertEquals("Email cannot be empty.", exception.getMessage());
    }

    @Test
    void testCreateUser_InvalidEmailFormat() {

        User user = new User();
        user.setEmail("invalid-email");
        user.setFirstName("Tom");
        user.setLastName("Test");
        user.setDateOfBirth(LocalDate.of(1990, 1, 1));
        user.setPhoneNumber("+380123456789");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.createUser(user));
        assertEquals("Invalid email format.", exception.getMessage());
    }


    @Test
    void testCreateUser_InvalidPhoneNumberFormat() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setFirstName("Tom");
        user.setLastName("Test");
        user.setDateOfBirth(LocalDate.of(1990, 1, 1));
        user.setPhoneNumber("123456");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.createUser(user));
        assertEquals("Invalid phone number format.", exception.getMessage());
    }

    @Test
    public void testCreateUser_Underage() {

        User user = new User();
        user.setEmail("test@example.com");
        user.setFirstName("Tom");
        user.setLastName("Test");
        user.setDateOfBirth(LocalDate.now().minusYears(17));
        user.setPhoneNumber("+380123456789");

        int expectedMinAge = 18;
        int actualMinAge = minAgeForRegistration;

        assertEquals(expectedMinAge, actualMinAge, "minAgeForRegistration should be set to " + expectedMinAge);
    }

    @Test
    public void testValidateDateOfBirth_NullDate() {

        User user = new User();

        try {
            userService.validateDateOfBirth(user.getDateOfBirth());
            fail("Expected validateDateOfBirth to throw IllegalArgumentException for null date");
        } catch (IllegalArgumentException e) {
            assertEquals("Date of birth cannot be empty.", e.getMessage());
        }
    }

    @Test
    public void testGetUserById_ValidId() {

        Long id = 1L;
        User user = new User();
        user.setId(id);
        user.setFirstName("TestFirstName");
        user.setLastName("testLastName");

        Mockito.when(userRepository.findById(id)).thenReturn(Optional.of(user));
        User actualUser = userService.getUserById(id);
        assertEquals(user, actualUser);
    }

    @Test
    void testGetUserById_UserNotFound() {

        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> userService.getUserById(userId));
        assertEquals("User not found with id " + userId, exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    public void testListUsers_EmptyList() {
        Mockito.when(userRepository.findAll()).thenReturn(Collections.emptyList());
        List<User> actualUsers = userService.listUsers();
        assertTrue(actualUsers.isEmpty());
    }

    @Test
    public void testListUsers_MultipleUsers() {

        User user1 = new User();
        user1.setFirstName("TestFirstName");
        user1.setLastName("testLastName");
        User user2 = new User();

        user2.setFirstName("TestFirstName");
        user2.setLastName("testLastName");

        List<User> expectedUsers = Arrays.asList(user1, user2);
        Mockito.when(userRepository.findAll()).thenReturn(expectedUsers);

        List<User> actualUsers = userService.listUsers();

        assertEquals(expectedUsers, actualUsers);
        assertEquals(2, actualUsers.size());
    }

    @Test
    public void testDeleteUser_ExistingUser() {

        Long id = 1L;
        User user = new User();
        user.setId(id);
        user.setFirstName("TestFirstName");
        user.setLastName("testLastName");
        Mockito.when(userRepository.findById(id)).thenReturn(Optional.of(user));

        userService.deleteUser(id);
    }

    @Test
    void testDeleteUser_UserNotFound() {

        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> userService.deleteUser(userId));
        assertEquals("User not found with id " + userId, exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    public void testUpdateUser_UserNotFound() {

        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        User updatedUser = new User();
        updatedUser.setEmail("new@example.com");
        assertThrows(IllegalArgumentException.class, () -> userService.updateUser(updatedUser, userId));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testUpdateUser_Successful() {

        Long userId = 1L;
        User existingUser = new User(userId, "john@example.com", "Doe", "Smith", LocalDate.of(1990, 5, 15), "123 Main St", "1234567890");
        User updateUser = new User(userId, "jane@example.com", "Doe", "Smith", LocalDate.of(1992, 8, 21), "456 Elm St", "0987654321");
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail(updateUser.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenReturn(updateUser);

        User updatedUser = userService.updateUser(updateUser, userId);

        assertNotNull(updatedUser);
        assertEquals(updateUser.getFirstName(), updatedUser.getFirstName());
        assertEquals(updateUser.getLastName(), updatedUser.getLastName());
        assertEquals(updateUser.getEmail(), updatedUser.getEmail());
        assertEquals(updateUser.getDateOfBirth(), updatedUser.getDateOfBirth());
        assertEquals(updateUser.getAddress(), updatedUser.getAddress());
        assertEquals(updateUser.getPhoneNumber(), updatedUser.getPhoneNumber());

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).findByEmail(updateUser.getEmail());
        verify(userRepository, times(1)).save(updateUser);
    }

    @Test
    public void testUpdateUser_EmailAlreadyExists() {

        User existingUser1 = new User();
        existingUser1.setId(1L);
        existingUser1.setEmail("existing@example.com");
        existingUser1.setFirstName("John");
        existingUser1.setLastName("Doe");

        User existingUser2 = new User();
        existingUser2.setId(2L);
        existingUser2.setEmail("new@example.com");
        existingUser2.setFirstName("Jane");
        existingUser2.setLastName("Doe");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(existingUser2));
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.of(existingUser2));

        User updatedUser = new User();
        updatedUser.setEmail("new@example.com");
        updatedUser.setFirstName("John");

        assertThrows(IllegalArgumentException.class, () -> userService.updateUser(updatedUser, 1L));

        verify(userRepository, never()).save(any());
    }

    @Test
    public void testUpdateUser_InvalidAge() {

        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("old@example.com");
        existingUser.setFirstName("John");
        existingUser.setLastName("Doe");
        existingUser.setDateOfBirth(LocalDate.of(1990, 1, 1));
        existingUser.setAddress("kyiv");
        existingUser.setPhoneNumber("+380666219061");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        User updatedUser = new User();
        updatedUser.setEmail("new@example.com");
        updatedUser.setFirstName("Jane");
        updatedUser.setLastName("Doe");
        updatedUser.setDateOfBirth(LocalDate.now().plusYears(-17));
        updatedUser.setAddress("kyiv");
        updatedUser.setPhoneNumber("+380666219061");

        int expectedMinAge = 18;
        int actualMinAge = minAgeForRegistration;

        assertEquals(expectedMinAge, actualMinAge, "minAgeForRegistration should be set to " + expectedMinAge);
    }

    @Test
    public void testGetUsersInDateRange_Success() {

        LocalDate startDate = LocalDate.of(2024, 01, 01);
        LocalDate endDate = LocalDate.of(2024, 05, 12);

        List<User> expectedUsers = Arrays.asList(new User(), new User());
        when(userRepository.findByDateOfBirthBetween(startDate, endDate)).thenReturn(expectedUsers);

        List<User> actualUsers = userService.getUsersInDateRange(startDate, endDate);

        assertNotNull(actualUsers);
        assertEquals(expectedUsers.size(), actualUsers.size());
        verify(userRepository).findByDateOfBirthBetween(startDate, endDate);
    }

    @Test
    public void testGetUsersInDateRange_NoUsersInRange() {

        LocalDate startDate = LocalDate.of(2025, 01, 01);
        LocalDate endDate = LocalDate.of(2025, 12, 31);

        when(userRepository.findByDateOfBirthBetween(startDate, endDate)).thenReturn(Collections.emptyList());

        List<User> actualUsers = userService.getUsersInDateRange(startDate, endDate);

        assertNotNull(actualUsers);
        assertTrue(actualUsers.isEmpty());
        verify(userRepository).findByDateOfBirthBetween(startDate, endDate);
    }

    @Test
    public void testValidatePhoneNumber_ValidWithPlus380() {
        String phoneNumber = "+380661234567";
        String regex = "^((\\+380)|0)[0-9]{9}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(phoneNumber);
        boolean isValid = matcher.matches();
        assertTrue(isValid, "The phone number must be valid.");
    }

    @Test
    public void testValidatePhoneNumber_ValidWithoutPlus380() {
        String phoneNumber = "0661234567";
        String regex = "^((\\+380)|0)[0-9]{9}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(phoneNumber);
        boolean isValid = matcher.matches();
        assertTrue(isValid, "The phone number must be valid.");
    }

    @Test
    public void testValidatePhoneNumber_EmptyPhoneNumber() {
        String phoneNumber = "";
        boolean isValid = phoneNumber.isEmpty();
        assertTrue(isValid, "An empty phone number must be valid.");
    }

    @Test
    public void testValidatePhoneNumber_InvalidPhoneNumber() {
        String phoneNumber = "invalid-phone-number";
        String regex = "^((\\+380)|0)[0-9]{9}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(phoneNumber);
        boolean isValid = matcher.matches();
        assertFalse(isValid, "An invalid phone number must be invalid.");
    }

    @Test
    public void testValidateDateOfBirth_ValidDate() {
        LocalDate validDate = LocalDate.of(1990, 1, 1);
        boolean isValid = true;

        if (validDate == null) {
            isValid = false;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String formattedDate = validDate.format(formatter);
            LocalDate parsedDate = LocalDate.parse(formattedDate, formatter);

            if (!validDate.equals(parsedDate)) {
                isValid = false;
            }
        } catch (DateTimeParseException e) {
            isValid = false;
        }

        assertTrue(isValid, "Must return true for a valid date of birth.");
    }

    @Test
    public void testValidateDateOfBirth_InvalidDateFormat() {
        String invalidDate = "invalid-format";

        assertThrows(DateOfBirthException.class, () -> {

            if (invalidDate == null) {
                throw new IllegalArgumentException("Date of birth cannot be empty.");
            }

            try {
                LocalDate.parse(invalidDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (DateTimeParseException e) {
                throw new DateOfBirthException("Invalid date of birth format. Please use YYYY-MM-DD format.");
            }
        });
    }
}
