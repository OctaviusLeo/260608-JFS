Feature: User Registration

  Scenario: Successful registration with valid credentials
    Given I am on the register page
    When I fill in username "newUser1" password "Valid**12" and confirm password "Valid**12"
    And I click the create account button
    Then I should see a registration success message

  Scenario: Registration fails when username is too short
    Given I am on the register page
    When I fill in username "ab" password "Valid**12" and confirm password "Valid**12"
    And I click the create account button
    Then I should see a username validation error

  Scenario: Registration fails when username is too long
    Given I am on the register page
    When I fill in username "thisusernameistoolong" password "Valid**12" and confirm password "Valid**12"
    And I click the create account button
    Then I should see a username validation error

  Scenario: Registration fails when username has spaces
    Given I am on the register page
    When I fill in username "bad user" password "Valid**12" and confirm password "Valid**12"
    And I click the create account button
    Then I should see a username validation error

  Scenario: Registration fails when password is too short
    Given I am on the register page
    When I fill in username "newUser2" password "Ab*1" and confirm password "Ab*1"
    And I click the create account button
    Then I should see a password validation error

  Scenario: Registration fails when password has no uppercase letter
    Given I am on the register page
    When I fill in username "newUser3" password "test**12" and confirm password "test**12"
    And I click the create account button
    Then I should see a password validation error

  Scenario: Registration fails when password has no special characters
    Given I am on the register page
    When I fill in username "newUser4" password "TestPass1" and confirm password "TestPass1"
    And I click the create account button
    Then I should see a password validation error

  Scenario: Registration fails when passwords do not match
    Given I am on the register page
    When I fill in username "newUser5" password "Valid**12" and confirm password "Different1**"
    And I click the create account button
    Then I should see a confirm password mismatch error

  Scenario: Registration fails when username already exists
    Given a registered user exists with username "existUser" and password "Test**12"
    And I am on the register page
    When I fill in username "existUser" password "Test**12" and confirm password "Test**12"
    And I click the create account button
    Then I should see a registration error message
