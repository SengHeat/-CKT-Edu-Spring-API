# Karate Testing Setup Guide

This guide covers setting up, configuring, and running Karate API tests for the CKT-Edu-Spring-API project.

## Prerequisites

- Java 17+
- Gradle
- PostgreSQL running on localhost:5432
- Spring Boot application running on localhost:8080

## Project Structure

```
karate/
├── karate-config.js          # Karate configuration
└── features/
    ├── auth-register.feature  # User registration tests
    └── auth-login.feature     # User login tests

src/test/java/com/ckt/api/karate/
└── KarateTest.java           # JUnit runner class
```

## Setup & Configuration

### 1. Dependencies

Karate dependencies are already added to `build.gradle.kts`:
- `karate-core` - Core Karate library
- `karate-junit5` - JUnit 5 integration

### 2. Configuration (karate-config.js)

Located at `karate/karate-config.js`. Key settings:
- `baseUrl`: API base URL (default: `http://localhost:8080`)
- Environment support: `dev`, `test`, `prod`
- Timeouts: 10 seconds connect/read timeout

### 3. Run the Application

Before running tests, ensure the Spring Boot application is running:

```bash
# Start the application
./gradlew bootRun
```

## Running Tests

### Run All Karate Tests

```bash
# Using Gradle
./gradlew test

# Run with verbose output
./gradlew test --info
```

### Run Specific Test Classes

```bash
# Run only register tests
./gradlew test --tests "com.ckt.api.karate.KarateTest.testRegister"

# Run only login tests  
./gradlew test --tests "com.ckt.api.karate.KarateTest.testLogin"
```

### Run with Different Environment

```bash
# Test environment
./gradlew test -Dkarate.env=test

# Dev environment (default)
./gradlew test -Dkarate.env=dev
```

## Test Scenarios

### Register Tests (`auth-register.feature`)

| Scenario | Description |
|----------|-------------|
| `@register` | Register a new user with valid data |
| `@register-invalid-email` | Register with invalid email (should fail 400) |

### Login Tests (`auth-login.feature`)

| Scenario | Description |
|----------|-------------|
| `@login-success` | Login with valid credentials |
| `@login-invalid-credentials` | Login with wrong credentials (should fail 400) |
| `@login-missing-fields` | Login with missing required fields (should fail 400) |
| `@login-wrong-password` | Login with incorrect password (should fail 401) |

## Test Execution with Gradle

```bash
# Clean and run tests
./gradlew clean test

# Run tests and generate report
./gradlew test
# Reports are in: build/reports/tests/test/index.html
# Karate reports: build/karate-reports/karate-summary.html
```

## Writing New Tests

### Create a New Feature File

Create `karate/features/<feature-name>.feature`:

```gherkin
@KarateDsl
Feature: API Feature Name

  Background:
    * def baseUrl = 'http://localhost:8080'

  @scenario-tag
  Scenario: Test description
    Given url baseUrl
    And path '/api/endpoint'
    And request { "field": "value" }
    When method POST
    Then status 200
    And match response.status == true
```

### Add Test to Runner

Update `KarateTest.java`:

```java
@Test
public void testNewFeature() {
    Results results = Runner.builder()
        .path("classpath:features/<feature-name>.feature")
        .outputCucumberJson(true)
        .parallel(1);
    assertEquals(0, results.getFailCount(), results.getErrorMessages());
}
```

## Build Configuration

The `build.gradle.kts` includes:
- Karate dependencies (karate-core, karate-junit5)
- Source sets configuration to include karate features in test classpath
- JUnit 5 platform launcher (aligned with Spring Boot version)

## Additional Resources

- [Karate DSL Documentation](https://karatelabs.github.io/karate/)
- [Karate JUnit 5 Integration](https://karatelabs.github.io/karate/running-tests/junit/)
- [Karate GitHub](https://github.com/karatelabs/karate)
