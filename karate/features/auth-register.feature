@KarateDsl
Feature: User Authentication API Tests

  Background:
    * def baseUrl = karate.properties['baseUrl'] || 'http://localhost:8080'
    * def timestamp = 
      """
      function() {
        var d = new Date();
        return d.getTime();
      }
      """

  @register
  Scenario: Register a new user
    Given url baseUrl
    And path '/api/auth/register'
    And def testEmail = 'test_' + timestamp() + '@example.com'
    And request
      """
      {
        "firstName": "Test",
        "lastName": "User",
        "username": "#(testEmail)",
        "email": "#(testEmail)",
        "password": "password123"
      }
      """
    When method POST
    Then status 201
    And match response.status == true
    And match response.code == 200
    And match response.message == 'Success'
    And match response.data.accessToken == '#notnull'
    And match response.data.user.email == testEmail
    * print 'Registered user with email:', testEmail

  @register-invalid-email
  Scenario: Register with invalid email should fail
    Given url baseUrl
    And path '/api/auth/register'
    And request
      """
      {
        "firstName": "Test",
        "lastName": "User",
        "username": "invalid",
        "email": "not-an-email",
        "password": "password123"
      }
      """
    When method POST
    Then status 400
    And match response.status == false
