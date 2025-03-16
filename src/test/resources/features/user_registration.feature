Feature: User Registration and Login

  Scenario: Successful user registration
    Given I have a valid user registration request
    When I submit the registration form
    Then The user should be registered successfully