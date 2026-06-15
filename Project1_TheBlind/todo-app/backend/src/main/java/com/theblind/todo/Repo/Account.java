package com.theblind.todo.Repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.theblind.todo.Entity.users;

@Repository
public interface Account extends JpaRepository<users, Integer> {
    Optional<users> findByUsername(String username);
}
