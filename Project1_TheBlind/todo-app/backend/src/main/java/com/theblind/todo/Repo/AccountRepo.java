package com.theblind.todo.Repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.theblind.todo.Entity.User;

@Repository
public interface AccountRepo extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
}
