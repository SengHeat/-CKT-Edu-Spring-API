# AGENTS.md - Development Guide for CKT-Edu-Spring-API

## Project Overview

Spring Boot 3.2.0 REST API with Java 17, Gradle, JPA/Hibernate, Spring Security with JWT authentication, and PostgreSQL.

## Build Commands

```bash
./gradlew build          # Build the project
./gradlew clean         # Clean build artifacts
./gradlew bootRun       # Run with Spring Boot
./gradlew test          # Run all tests
./gradlew test --tests "ClassName"       # Run tests in a class
./gradlew test --tests "ClassName.methodName"  # Run single test
./gradlew check         # Run all checks
```

**Note**: Tests are disabled in `build.gradle.kts` (`enabled = false`). Enable by setting `enabled = true`.

## Project Structure

```
src/main/java/com/ckt/api/
├── base/                # ApiResponse, Meta, PaginatedResponse
├── config/              # Configuration classes
├── exception/           # Custom exceptions + GlobalExceptionHandler
├── enums/               # Enumerations
└── <module>/
    ├── controller/      # @RestController
    ├── service/         # @Service
    ├── repository/      # JpaRepository
    ├── model/entity/    # JPA entities (extend BaseEntity)
    ├── model/dto/       # DTOs
    ├── mapper/          # Entity <-> DTO mappers
    └── security/        # Security classes
```

## Code Style Guidelines

### Package Organization
- Layered: `controller`, `service`, `repository`, `model/entity`, `model/dto`, `mapper`
- Example: `com.ckt.api.user.controller.AuthController`

### Naming Conventions
- **Classes**: PascalCase (`AuthController`, `UserService`)
- **Methods/variables**: camelCase (`findByEmail`, `userRepo`)
- **Constants**: UPPER_SNAKE_CASE
- **Database tables/columns**: snake_case (`users`, `password_hash`)

### Entity Design
- Extend `BaseEntity` for all JPA entities
- Use `@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})`
- Use `@PrePersist`/`@PreUpdate` for audit fields

```java
@Entity
@Table(name = "users")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User extends BaseEntity {
    @Column(unique = true)
    private String email;
}
```

### DTOs and Request Objects
- Use Lombok `@Getter`, `@Setter`, `@Builder`
- Use Jakarta Validation: `@NotBlank`, `@Email`, `@NotNull`, `@Size`
- Use `@Valid` on controller parameters

### Controllers
- Use `@RestController`, `@RequestMapping`
- Constructor injection for dependencies
- Return `ResponseEntity<?>` or `ResponseEntity<ApiResponse<?>>`

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;
    public AuthController(AuthService auth) { this.auth = auth; }
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) { }
}
```

### Services
- Use `@Service` with constructor injection
- Keep business logic in services, not controllers
- Throw custom exceptions for errors

### Repositories
- Extend `JpaRepository<Entity, ID>`
- Use query methods or `@Query`

```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
```

### Response Format
Use `ApiResponse<T>` record:

```java
return ResponseEntity.ok(ApiResponse.success(data));
return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(data, "Created"));
return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound("Not found"));
```

### Exception Handling
- Use `GlobalExceptionHandler` with `@RestControllerAdvice`
- Custom exceptions: `DuplicateException` (409), `NotFoundException` (404), `UnauthorizedException` (401)

### Mappers
Static mapper classes for Entity <-> DTO:

```java
public class UserMapper {
    public static UserProfileDTO toDTO(User user) {
        if (user == null) return null;
        return UserProfileDTO.builder().id(user.getId()).email(user.getEmail()).build();
    }
}
```

### Security
- JWT via `JwtUtil`, PAT tokens for API auth
- Token format: `id|plain_token` (PAT) or JWT

### Imports Ordering
1. Java/Jakarta EE, 2. Spring, 3. Third-party (Lombok, Jackson), 4. Project (com.ckt.api.*)

### Additional Guidelines
- Validate input with `@Valid`, use `Optional` for nullable repository returns
- Use builder pattern for DTOs, avoid logic in controllers
- Add null checks in mappers, use `LocalDateTime` for timestamps
- API Testing using Karate https://docs.karatelabs.io and with JUnit https://docs.karatelabs.io/running-tests/junit