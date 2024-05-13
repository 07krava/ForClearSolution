package com.forclearsolution.services;

import com.forclearsolution.models.User;

import java.time.LocalDate;
import java.util.List;

public interface UserService{

    User createUser(User user);

    User getUserById(Long id);

    List<User> listUsers();

    void deleteUser(Long id);

    User updateUser(User user, Long id);

    List<User> getUsersInDateRange(LocalDate startDate, LocalDate endDate);

}
