package com.theblind.todo.Response;

import java.util.UUID;

public class LoginResponse {
    private String token;
    private long expiresIn;
    private String username;
    private UUID userId;

    public String getToken() {
        return token;
    }
    
    public long getExpiresIn() {
        return expiresIn;
    }

    public String getUsername() {
        return username;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setToken(String token) {
        this.token = token;
    }
    
    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}