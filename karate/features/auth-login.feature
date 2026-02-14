@KarateDsl
Feature: User Login API Tests

  Background:
    * def baseUrl = karate.properties['baseUrl'] || 'http://localhost:8080'
    * def timestamp = 
      """
      function() {
        var d = new Date();
        return d.getTime();
      }
      """

  @login-success
  Scenario: Login with valid credentials
    # First, register a new user
    Given url baseUrl
    And path '/api/auth/register'
    And def testEmail = 'logintest_' + timestamp() + '@example.com'
    And request
      """
      {
        "firstName": "Login",
        "lastName": "Test",
        "username": "#(testEmail)",
        "email": "#(testEmail)",
        "password": "testpass123"
      }
      """
    When method POST
    Then status 201

    # Now login with the registered user
    Given url baseUrl
    And path '/api/auth/login'
    And request
      """
      {
        "email": "#(testEmail)",
        "password": "testpass123"
      }
      """
    When method POST
    Then status 200
    And match response.status == true
    And match response.code == 200
    And match response.message == 'Success'
    And match response.data.accessToken == '#notnull'
    * print 'Login successful with token:', response.data.accessToken

  @login-invalid-credentials
  Scenario: Login with invalid credentials should fail
    Given url baseUrl
    And path '/api/auth/login'
    And request
      """
      {
        "email": "nonexistent@example.com",
        "password": "wrongpassword"
      }
      """
    When method POST
    Then status 400
    And match response.status == false

  @login-missing-fields
  Scenario: Login with missing fields should fail
    Given url baseUrl
    And path '/api/auth/login'
    And request
      """
      {
        "email": "test@example.com"
      }
      """
    When method POST
    Then status 400
    And match response.status == false

  @login-wrong-password
  Scenario: Login with wrong password should fail
    # Register a user first
    Given url baseUrl
    And path '/api/auth/register'
    And def testEmail = 'wrongpass_' + timestamp() + '@example.com'
    And request
      """
      {
        "firstName": "Test",
        "lastName": "User",
        "username": "#(testEmail)",
        "email": "#(testEmail)",
        "password": "correctpassword"
      }
      """
    When method POST
    Then status 201

    # Try to login with wrong password
    Given url baseUrl
    And path '/api/auth/login'
    And request
      """
      {
        "email": "#(testEmail)",
        "password": "incorrectpassword"
      }
      """
    When method POST
    Then status 401
    And match response.error == 'Invalid credentials'
