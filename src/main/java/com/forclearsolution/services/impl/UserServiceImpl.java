package com.forclearsolution.services.impl;

import com.forclearsolution.models.User;
import com.forclearsolution.repositories.UserRepository;
import com.forclearsolution.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements UserService {

    @Value("${minAgeForRegistration}")
    private int minAgeForRegistration;

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User createUser(User user) {

        Optional<User> existingUserOptional = userRepository.findByEmail(user.getEmail());
        if (existingUserOptional.isPresent()) {
            throw new IllegalArgumentException("This user already exists!");
        } else if (!validateUserAge(user)) {
            throw new IllegalArgumentException("Unknown error");
        }
        return userRepository.save(user);
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found with id " + id));
    }

    @Override
    public List<User> listUsers() {
        return userRepository.findAll();
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found with id " + id));
        userRepository.delete(user);
    }

    @Override
    public User updateUser(User user, Long id) {

        Optional<User> existingUser = userRepository.findById(id);

        if (existingUser.isPresent()) {
            User updateUser = existingUser.get();
            Optional<User> existingUserOptional = userRepository.findByEmail(user.getEmail());
            if (existingUserOptional.isPresent() && !Objects.equals(existingUserOptional.get().getId(), existingUser.get().getId())) {
                throw new IllegalArgumentException("This user already exists!");
            }
            updateUser.setEmail(user.getEmail());
            updateUser.setFirstName(user.getFirstName());
            updateUser.setLastName(user.getLastName());
            updateUser.setDateOfBirth(user.getDateOfBirth());
            updateUser.setAddress(user.getAddress());
            updateUser.setPhoneNumber(user.getPhoneNumber());

            if (validateUserAge(updateUser)) {
                return userRepository.save(updateUser);
            } else {
                throw new IllegalArgumentException("Check the entered data.");
            }

        } else {
            throw new IllegalArgumentException("User not found with id: " + id);
        }
    }

    @Override
    public List<User> getUsersInDateRange(LocalDate startDate, LocalDate endDate) {
        if (validateDateOfBirth(startDate) || validateDateOfBirth(endDate)) {
            throw new IllegalArgumentException("Invalid date of birth format. Please use YYYY-MM-DD format.");
        }
        return userRepository.findByDateOfBirthBetween(startDate, endDate);
    }

    public boolean validateUserAge(User user) {

        if (user.getFirstName() == null || user.getFirstName().isEmpty()) {
            throw new IllegalArgumentException("First name cannot be empty.");
        } else if (user.getLastName() == null || user.getLastName().isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be empty.");
        } else if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty.");
        } else if (!validateEmail(user.getEmail())) {
            throw new IllegalArgumentException("Invalid email format.");
        } else if (!validatePhoneNumber(user.getPhoneNumber())) {
            throw new IllegalArgumentException("Invalid phone number format.");
        }

        LocalDate dateOfBirth = user.getDateOfBirth();
        if (validateDateOfBirth(dateOfBirth)) {
            throw new IllegalArgumentException("Invalid date of birth format. Please use YYYY-MM-DD format.");
        }

        LocalDate today = LocalDate.now();
        if (dateOfBirth.isAfter(today)) {
            throw new IllegalArgumentException("The date of birth cannot be in the future.");
        }

        Period period = Period.between(dateOfBirth, today);
        if (period.getYears() < minAgeForRegistration) {
            throw new IllegalArgumentException("To register, the user must be over " + minAgeForRegistration + " years old.");
        }

        return true;
    }

    private boolean validateEmail(String email) {

        String regex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);

        return matcher.matches();
    }

    private boolean validatePhoneNumber(String phoneNumber) {

        String regex = "^((\\+380)|0)[0-9]{9}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(phoneNumber);

        if (matcher.matches()) {
            return true;
        }

        if (phoneNumber.isEmpty()) {
            return true;
        }

        return false;
    }

    public boolean validateDateOfBirth(LocalDate dateOfBirth) {

        if (dateOfBirth == null) {
            throw new IllegalArgumentException("Date of birth cannot be empty.");
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String formattedDate = dateOfBirth.format(formatter);
            LocalDate parsedDate = LocalDate.parse(formattedDate, formatter);
            return !parsedDate.equals(dateOfBirth);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date of birth format. Please use YYYY-MM-DD format.");
        }
    }
}
