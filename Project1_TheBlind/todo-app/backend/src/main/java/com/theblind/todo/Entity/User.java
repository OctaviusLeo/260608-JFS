package com.theblind.todo.Entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.userdetails.UserDetails;

/*
* UserDetails is an interface provided by Spring Security which
* contains methods to contain and etrieve user information for authentication.
*/
@Data
@NoArgsConstructor
@Entity
@Table(name = "Users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 50, nullable = false, unique = true)
    private String username;

    @Column(length = 50, nullable = false)
    private String password;

    @Column(name = "user_creation", updatable = false)
    private LocalDateTime userCreation = LocalDateTime.now();

    /** User constructor
    * 
    * @param username - string of name
    * @param password - string of password (will be hashed)
    * @return new User object
    */
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /* Overridden functions from UserDetails
    *
    *  Allows for JWT Authentication
    */
    // Gives list of permissions user has (will be unused)
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    // retrieves username
    @Override
    public String getUsername() {
        return username;
    }

    // retrieves user id
    public UUID getUserId() {
        return id;
    }

    // retrieves password
    public String getPassword() {
        return password;
    }

    // is JSON Web Token still active (true) or expired (false)?
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
