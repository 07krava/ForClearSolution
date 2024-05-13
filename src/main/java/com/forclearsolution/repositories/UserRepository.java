package com.forclearsolution.repositories;

import com.forclearsolution.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);

    List<User> findByDateOfBirthBetween(LocalDate startDate, LocalDate endDate);

}
