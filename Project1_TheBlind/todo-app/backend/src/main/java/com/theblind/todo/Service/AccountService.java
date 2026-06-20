package com.theblind.todo.Service;

import com.theblind.todo.Entity.User;
import com.theblind.todo.Repo.AccountRepo;
import com.theblind.todo.Exception.RegistrationFailureException;
import com.theblind.todo.Exception.LoginFailureException;

import org.springframework.stereotype.Service;
import com.theblind.todo.Service.AccountService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Map;
import java.util.List;
import java.util.Optional;

@Service
public class AccountService {
    private final AccountRepo accountRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;    
    
    public AccountService(AccountRepo accountRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    /**
    * createAccount - Creates a new account with the given username and password
    *
    * @param username - the username for the new account
    * @param password - the password for the new account
    * 
    * @return the newly created User object, is null if invalid (however, 
    *         a RegistrationFailure exception will be thrown instead)
    * @throws RegistrationFailure exception if username or password do not fit requirements
    */
    public User createAccount(String username, String password) throws RegistrationFailureException {
        // if either credential is null, registration exception occurs
        if (username == null || password == null) {
           throw new RegistrationFailureException("Either username or password is null");
        }

        User newAccount = null;

        if (usernameValidator(username) && passwordValidator(password)) {    
            String encodedPassword = passwordEncoder.encode(password);        
            newAccount = new User(username, encodedPassword);
            accountRepository.save(newAccount);
        }

        return newAccount;
    }


    /**
    * loginAccount - Checks for existing user and authenticates user's session
    *
    * @param username - the username for an existing account
    * @param password - the password for an existing account
    * 
    * @return a User object of an existing account with matching credentials
    * @throws LoginFailure exception if credentials don't match any user in database
    */
    public User loginAccount(String username, String password) throws LoginFailureException {
        Optional<User> optionalAccount = accountRepository.findByUsername(username);
        User loggedInAccount = null;

        if (optionalAccount.isPresent()) {
            if (passwordEncoder.matches(password, optionalAccount.get().getPassword())) {
                loggedInAccount = optionalAccount.get();
                // authentication happens after user is confirmed to exist in databaseS
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                username,
                                password
                        )
                );
            }
        }

        if (loggedInAccount == null) throw new LoginFailureException("Credentials do not match");

        return loggedInAccount;
    }

    /**
     * usernameValidator - this method checks whether a given username is valid
     * 
     * @param username
     * 
     * @return true if username is not null, does not contain whitespace, 
     *         is not a duplicate, and is between 5 and 15 characters long (inclusive)
     * @throws RegistrationFailure exception if username or password do not fit requirements
    */
    public boolean usernameValidator(String username) throws RegistrationFailureException {
        // flags for requirements
        boolean lengthFlag = isCorrectLength(username), noWhiteSpaceFlag = !hasWhiteSpace(username), 
            uniqueFlag = isUnique(username);

        // if all flags are set, valid username
        if (uniqueFlag && noWhiteSpaceFlag && lengthFlag) return true;

        // if not, return error (give specific feedback for violated requirements)
        List<Map.Entry<Boolean, String>> invalidRequirementsList = List.of(
            Map.entry(lengthFlag, "must be between 5 and 15 characters long (inclusive)"),
            Map.entry(noWhiteSpaceFlag, "must not contain whitespace"),
            Map.entry(uniqueFlag, "must be unique (username already exists)")
        );

        String message = "The username is invalid. Here are the following requirements:";
        for (Map.Entry<Boolean, String> entry : invalidRequirementsList) {
            if (entry.getKey() == false) message = message + "\n-" + entry.getValue();
        }

        throw new RegistrationFailureException(message);
    }

    /**
     * passwordValidator - this method checks whether a given password
     * 
     * @param password - a string
     * 
     * @return true if password is not null, does not contain whitespace, 
     *         contains at least two special characters, at least one uppercase
     *         letter, at least one lowercase letter, at least one digit, 
     *         and is between 5 and 15 characters long (inclusive)
     * @throws RegistrationFailure exception if username or password do not fit requirements
    */
    public boolean passwordValidator(String password) throws RegistrationFailureException {
        // flags for requirements
        boolean charsFlag = hasRequiredCharacters(password), lengthFlag = isCorrectLength(password), 
            noWhiteSpaceFlag = !hasWhiteSpace(password);

        // if all flags set, valid password
        if (noWhiteSpaceFlag && charsFlag && lengthFlag) return true;

        // if not, return error (give specific feedback for violated requirements)
        List<Map.Entry<Boolean, String>> invalidRequirementsList = List.of(
            Map.entry(charsFlag, "Must contain at least one uppercase, one lowercase, one numeric, and two special characters"),
            Map.entry(lengthFlag, "Must be between 5 and 15 characters long (inclusive)"),
            Map.entry(noWhiteSpaceFlag, "Must not contain whitespace")
        );

        String message = "The password is invalid. Here are the following requirements:";
        for (Map.Entry<Boolean, String> entry : invalidRequirementsList) {
            if (entry.getKey() == false) message = message + "\n-" + entry.getValue();
        }

        throw new RegistrationFailureException(message);
    }

    // check if credential is already in database
    public boolean isUnique(String credential) { return !accountRepository.findByUsername(credential).isPresent(); }

    // check if length is between 5 and 15 (inclusive)
    public boolean isCorrectLength(String credential){
        return 5 <= credential.length() && credential.length() <= 15;
    }

    // check if no whitespace
    public boolean hasWhiteSpace(String credential) {
        for (char ch : credential.toCharArray()) {
            if(Character.isWhitespace(ch)) return true;
        }

        return false;
    }

    // check if there is at least one uppercase and one lowercase letters, at least one digit, and if there are at least two special characters
    public boolean hasRequiredCharacters(String credential) {
        boolean hasLowerCase = false;
        boolean hasUpperCase = false;
        boolean hasDigit = false;
        boolean hasSpecialChars = false;

        // list of special chars, casted into string for search purposes
        final char[] SPECIAL_CHARS = {'*', '+', '-', '/', '=', '_', '{', '}', '[', ']', '|', '\\', ':', ';', '"', '\'', '<', '>', ',', '.', '?', '~', '`'};
        String specialCharsString = new String(SPECIAL_CHARS);
        // at least two special characters must be found
        int specialCharCount = 2;

        for (char ch : credential.toCharArray()) {
            if (!hasLowerCase && Character.isLowerCase(ch)) hasLowerCase = true;
            if (!hasUpperCase && Character.isUpperCase(ch)) hasUpperCase = true;
            if (!hasDigit && Character.isDigit(ch)) hasDigit = true;
            if (!hasSpecialChars && (specialCharsString.indexOf(ch) != -1)) {
                specialCharCount--;
                if (specialCharCount <= 0) hasSpecialChars = true;
            }
            // if all requirements are met, return true
            if (hasLowerCase && hasUpperCase && hasDigit && hasSpecialChars) return true;
        }

        // if at least one requirement is missing, return false
        return false;
    }
}