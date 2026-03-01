![Java](https://img.shields.io/badge/Java-21+-blue) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x%20%7C%204.x-brightgreen) ![License](https://img.shields.io/badge/License-MIT-yellow) ![Type](https://img.shields.io/badge/type-spring--boot--starter-orange) [![](https://jitpack.io/v/dhanesh76/d76-spring-boot-starter-exception.svg)](https://jitpack.io/#dhanesh76/d76-spring-boot-starter-exception)

# d76-spring-boot-starter-exception

> Stop writing the same exception handler in every microservice.

A lightweight, zero-configuration Spring Boot starter that gives every service in your platform a consistent, structured, and extensible error response — out of the box.

No boilerplate. No copy-paste. One dependency.

---

## Why This Exists

Every Spring Boot service needs exception handling. Most teams end up with one of two outcomes:

- A copy-pasted `@RestControllerAdvice` that drifts across services over time
- An inconsistent mess where different endpoints return errors in different shapes

This starter solves both. Drop it in, define your error codes, and every service speaks the same error language — automatically.

---

## Features

- **Zero configuration** — auto-configured on startup, no `@EnableXxx` needed
- **Structured JSON responses** — consistent shape across every service
- **`BusinessException`** — throw typed, meaningful exceptions from anywhere in your code
- **Validation handler** — `MethodArgumentNotValidException` handled with field-level detail
- **Fallback handler** — uncaught exceptions never leak stack traces to clients
- **`ApiErrorResponseCustomizer`** — inject custom fields without touching the library
- **Immutable response model** — backed by Java records, safe by design
- **Back-off friendly** — steps aside if you define your own `@RestControllerAdvice`
- **Disableable** — opt out via a single property if needed

---

## Requirements

| Dependency  | Version    |
|-------------|------------|
| Java        | 21+        |
| Spring Boot | 3.x / 4.x  |

---

## Installation

This library is distributed via [JitPack](https://jitpack.io/#dhanesh76/d76-spring-boot-starter-exception).

### Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.dhanesh76</groupId>
        <artifactId>d76-spring-boot-starter-exception</artifactId>
        <version>v1.0.0</version>
    </dependency>
</dependencies>
```

### Gradle (Kotlin DSL)

```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.dhanesh76:d76-spring-boot-starter-exception:v1.0.0")
}
```

### Gradle (Groovy DSL)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.dhanesh76:d76-spring-boot-starter-exception:v1.0.0'
}
```

### Scala SBT

```scala
resolvers += "jitpack" at "https://jitpack.io"

libraryDependencies += "com.github.dhanesh76" % "d76-spring-boot-starter-exception" % "v1.0.0"
```

> **Note:** No authentication or token required. The repository is public and resolves immediately.

---

## How It Works

```
Your Code
   │
   └── throw new BusinessException(UserErrorCode.USER_NOT_FOUND)
                        │
                        ▼
          GlobalExceptionHandler          ← @RestControllerAdvice, auto-registered
                        │
                        ├── resolves HTTP status     ← from your ErrorCode
                        ├── builds ApiErrorResponse  ← via response builder
                        │
                        ▼
          ApiErrorResponseCustomizer[]    ← your beans, invoked in order
                        │
                        ├── builder.extension("traceId", ...)
                        └── builder.extension("tenantId", ...)
                                        │
                                        ▼
                        ApiErrorResponse (immutable)    ← Java record, sealed after all customizers
                                        │
                                        ▼
                                JSON response to client
```

**Internally, the auto-configuration does four things:**

1. Registers via `AutoConfiguration.imports` — no component scan or `@Enable` annotation needed
2. Backs off entirely if you already have a `@RestControllerAdvice` bean (`@ConditionalOnMissingBean`)
3. Backs off entirely if `d76.exception.enabled=false` (`@ConditionalOnProperty`)
4. Collects all `ApiErrorResponseCustomizer` beans from the context and invokes them in order before sealing the response

---

## What You Get Immediately

The moment you add the dependency, three exception handlers are active:

| Exception                         | HTTP Status                 | Behaviour                                  |
|-----------------------------------|-----------------------------|-------------------------------------------|
| `BusinessException`               | Defined by your `ErrorCode` | Maps your domain error to a typed response |
| `MethodArgumentNotValidException` | 400                         | Returns per-field validation failures      |
| `Exception` (catch-all)           | 500                         | Prevents stack trace leakage to clients    |

---

## Error Response Shape

### Standard response

```json
{
  "timestamp": "2026-03-01T10:00:00Z",
  "httpStatusCode": 404,
  "errorCode": "USER_NOT_FOUND",
  "message": "No account is associated with this email address.",
  "path": "/api/users/999"
}
```

### Validation response

```json
{
  "timestamp": "2026-03-01T10:00:00Z",
  "httpStatusCode": 400,
  "errorCode": "VALIDATION_FAILED",
  "message": "Validation failed",
  "path": "/api/users",
  "errors": [
    { "field": "email",    "message": "must not be blank" },
    { "field": "username", "message": "size must be between 3 and 50" }
  ]
}
```

Empty fields (`errors`, `extensions`) are suppressed from the output automatically.

---

## Defining Your Error Codes

Implement `ErrorCode` on an enum per domain:

```java
public enum UserErrorCode implements ErrorCode {

    USER_NOT_FOUND(404,     "No account is associated with this email address."),
    USERNAME_TAKEN(409,     "This username is already in use."),
    INCORRECT_PASSWORD(409, "Incorrect password.");

    private final int    status;
    private final String message;

    UserErrorCode(int status, String message) {
        this.status  = status;
        this.message = message;
    }

    @Override public int    getHttpStatus()  { return status; }
    @Override public String defaultMessage() { return message; }
}
```

`getErrorCode()` defaults to the enum constant name — `"USER_NOT_FOUND"`, `"USERNAME_TAKEN"`, etc. Override it only if you need a different format.

---

## Throwing Exceptions

```java
// Uses the default message from the error code
throw new BusinessException(UserErrorCode.USER_NOT_FOUND);

// Override the message for this specific occurrence
throw new BusinessException(UserErrorCode.USER_NOT_FOUND, "No user found with id: " + userId);
```

The handler reads the HTTP status directly from your `ErrorCode` and maps it to the response automatically.

---

## Built-in Error Codes

`CommonErrorCode` ships with standard HTTP errors ready to use:

| Constant                 | Status | Default Message                  |
|--------------------------|--------|----------------------------------|
| `BAD_REQUEST`            | 400    | Bad request                      |
| `UNAUTHORIZED`           | 401    | Unauthorized                     |
| `FORBIDDEN`              | 403    | Forbidden                        |
| `NOT_FOUND`              | 404    | Resource not found               |
| `CONFLICT`               | 409    | Conflict occurred                |
| `VALIDATION_FAILED`      | 400    | Validation failed                |
| `INTERNAL_SERVER_ERROR`  | 500    | Internal server error            |
| `SERVICE_UNAVAILABLE`    | 503    | Service temporarily unavailable  |

---

## Extending Responses — The Customizer Pattern

Need `traceId`, `tenantId`, or any platform-specific field on your error responses? Register an `ApiErrorResponseCustomizer` bean. The handler invokes all registered customizers before sealing the response — no handler duplication, no library forking required.

### Example — Attach a trace ID from a request header

```java
@Configuration
public class ErrorResponseConfig {

    @Bean
    public ApiErrorResponseCustomizer traceIdCustomizer() {
        return (builder, ex, request) -> {
            String traceId = request.getHeader("X-Trace-Id");
            if (traceId != null) {
                builder.extension("traceId", traceId);
            }
        };
    }
}
```

Custom fields are flattened to the JSON root — no nesting:

```json
{
  "timestamp": "2026-03-01T10:00:00Z",
  "httpStatusCode": 404,
  "errorCode": "USER_NOT_FOUND",
  "message": "No account is associated with this email address.",
  "path": "/api/users/999",
  "traceId": "abc-123"
}
```

### Example — Conditionally add fields based on exception type

```java
@Bean
public ApiErrorResponseCustomizer authProviderCustomizer() {
    return (builder, ex, request) -> {
        if (!(ex instanceof BusinessException be)) return;
        if (be.getErrorCode() != AuthErrorCode.INVALID_AUTH_PROVIDER) return;
        builder.extension("authProvider", resolveProvider(request));
    };
}
```

Multiple customizers can be registered. They are invoked sequentially and operate on the same builder instance. Customizers are additive — they cannot remove fields added by earlier customizers. The response is finalized and made immutable only after all customizers have run.

---

## Back-off Behavior

If your application defines its own `@RestControllerAdvice`, this starter automatically backs off and does not register its handler. Your handler takes full precedence — the starter is non-intrusive by design.

---

## Disabling Auto-Configuration

Opt out entirely via application properties:

```yaml
d76:
  exception:
    enabled: false
```

When disabled, Spring Boot's default error handling applies.

---

## Module Structure

The library is split into three Maven modules with deliberate dependency boundaries:

```
d76-spring-exception/
├── d76-spring-exception-core               # Pure Java — zero Spring Web dependency
│   ├── ErrorCode.java                      # Interface — implement on your enums
│   ├── BusinessException.java              # Runtime exception carrying an ErrorCode
│   └── CommonErrorCode.java                # Built-in HTTP error codes
│
├── d76-spring-exception-autoconfigure      # Spring Web + Jackson + Servlet API
│   ├── ExceptionAutoConfiguration.java     # Auto-config entry point
│   ├── GlobalExceptionHandler.java         # @RestControllerAdvice + customizer pipeline
│   └── web/
│       ├── ApiErrorResponse.java           # Immutable record — the JSON contract
│       └── ApiErrorResponseCustomizer.java # Extension point for consumers
│
└── d76-spring-boot-starter-exception       # The one dependency you actually add
    └── pom.xml                             # Pulls in autoconfigure transitively
```

### Using only the core module

If you have a non-web module — a Kafka consumer, a batch job, a shared domain library — depend on `d76-spring-exception-core` directly. It carries zero Spring Web dependency and lets you throw `BusinessException` anywhere Java runs.

**Maven:**
```xml
<dependency>
    <groupId>com.github.dhanesh76</groupId>
    <artifactId>d76-spring-exception-core</artifactId>
    <version>v1.0.0</version>
</dependency>
```

**Gradle:**
```kotlin
implementation("com.github.dhanesh76:d76-spring-exception-core:v1.0.0")
```

---

## Design Philosophy

This library is built on a small set of deliberate rules:

- **Infrastructure only** — no domain coupling, no auth concepts, no business logic
- **Immutable response model** — built once, read many times, never mutated after construction
- **Explicit extension over inheritance** — consumers add fields via customizers, not subclassing
- **Back-off friendly auto-configuration** — never fights with your existing setup
- **Zero Spring dependency in core** — `ErrorCode` and `BusinessException` work anywhere Java runs

The goal is consistency without control — your services speak the same error language, and you keep full ownership of the details.

---

## Roadmap

- [] Publish to Maven Central
- [ ] Integration tests via `ApplicationContextRunner`
- [ ] `ProblemDetail` response format (RFC 9457) as an opt-in alternative

---

## License

Licensed under the [MIT License](LICENSE).
