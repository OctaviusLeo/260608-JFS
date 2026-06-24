package com.theblind.todo.Controller;

import com.theblind.todo.Config.JWTConfig;
import com.theblind.todo.Entity.User;
import com.theblind.todo.Service.AccountService;
import com.theblind.todo.Exception.RegistrationFailureException;
import com.theblind.todo.Exception.LoginFailureException;
import com.theblind.todo.Response.LoginResponse;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api")
public class AccountController {
    private final AccountService accountService;
    private final JWTConfig jwtConfig;
    private final String requestOriginURL = "http://localhost:4200";

    /**
    *  Constructor for AccountController
    *
    * @param accountService - service to manage business logic 
    *                         of account registration and login.
    */
    public AccountController(AccountService accountService, JWTConfig jwtConfig) {
        this.accountService = accountService;
        this.jwtConfig = jwtConfig;
    }

    /**
    * Endpoint to register a new account
    *
    * @param user - a User object, with a username and password, from the request body
    * 
    * @return ResponseEntity object with new user info in body of response (201 CREATED)
    * @throws RegistrationFailure exception if username or password do not fit requirements
    */
    @CrossOrigin(origins = requestOriginURL)
    @PostMapping("/register")
    public ResponseEntity<User> createAccount(@RequestBody User user) throws RegistrationFailureException {
        User newUser = accountService.createAccount(user.getUsername(), user.getPassword());
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

    /**
    * Endpoint to have a user login and become authenticated
    *
    * @param user - a User object, with a username and password, from the request body
    * 
    * @return ResponseEntity object with existing user info in body of response (200 OK)
    * @throws LoginFailure exception if credentials don't match any user in database
    */
    @CrossOrigin(origins = requestOriginURL)
    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponse> loginAccount(@RequestBody User user) throws LoginFailureException {
        User existingUser = accountService.loginAccount(user.getUsername(), user.getPassword());
        String jwtToken = jwtConfig.generateToken(existingUser);

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(jwtToken);
        loginResponse.setExpiresIn(jwtConfig.getExpirationTime());
        loginResponse.setUsername(existingUser.getUsername());
        loginResponse.setUserId(existingUser.getUserId());
        
        return ResponseEntity.status(HttpStatus.OK).body(loginResponse);
    }
}
