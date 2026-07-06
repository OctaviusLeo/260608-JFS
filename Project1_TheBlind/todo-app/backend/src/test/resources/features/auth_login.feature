Feature: User Login

  Background:
    Given a registered user exists with username "testUser1" and password "Test**12"

  Scenario: Successful login with valid credentials
    Given I am on the login page
    When I enter username "testUser1" and password "Test**12"
    And I click the login button
    Then I should be redirected to the dashboard

  Scenario: Login fails with wrong password
    Given I am on the login page
    When I enter username "testUser1" and password "WrongPass1!"
    And I click the login button
    Then I should see a login error message

  Scenario: Login fails with unknown username
    Given I am on the login page
    When I enter username "nobody123" and password "Test**12"
    And I click the login button
    Then I should see a login error message

  Scenario: Login fails when username is blank
    Given I am on the login page
    When I enter username "" and password "Test**12"
    And I click the login button
    Then I should stay on the login page

  Scenario: Login fails when password is blank
    Given I am on the login page
    When I enter username "testUser1" and password ""
    And I click the login button
    Then I should stay on the login page

  Scenario: Logged-in user is redirected away from login page
    Given I am logged in as "testUser1" with password "Test**12"
    When I navigate to the login page
    Then I should be redirected to the dashboard
