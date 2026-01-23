# Gemini Code Generation Rules for lan2rf-gateway-stats

This document outlines the conventions and best practices to follow when generating or modifying code for the `lan2rf-gateway-stats` project. Adhering to these rules will help maintain code quality, consistency, and alignment with the project's established architecture.

## 1. Logging

All classes that perform logging should use SLF4J. A `LOGGER` instance must be defined as a `val` within a `companion object` in the class that uses it.

**Example:**
```kotlin
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MyClass {
    // ... class implementation ...

    companion object 
        val LOGGER: Logger = LoggerFactory.getLogger(MyClass::class.java)
    }
}
```

## 2. Testing Conventions

### Test Naming
The project has a strict naming convention for test classes, which is used by the Gradle build to differentiate between test types.

-   **Unit Tests:** Class names must end with `Test`. (e.g., `MyServiceTest.kt`)
-   **Integration Tests:** Class names must end with `IntegrationTest`. (e.g., `MyServiceIntegrationTest.kt`)
-   **End-to-End Tests:** Class names must end with `EndToEndTest`. (e.g., `DataFlowEndToEndTest.kt`)

### Test Frameworks
-   **[Kotest](https://kotest.io/)** is the preferred assertion library.
-   **[Testcontainers](https://www.testcontainers.org/)** should be used for integration tests that require external services like the InfluxDB database.

## 3. Dependency Injection

This project uses Micronaut's dependency injection framework.

-   **Constructor Injection:** Always prefer constructor injection over field injection. This makes dependencies explicit and classes easier to test. Micronaut handles constructor injection automatically for beans.

-   **Avoid `@Inject` on Constructors:** You do not need to annotate the constructor with `@Inject` if it's the only one.

**Do this:**
```kotlin
@Singleton
class MyService(private val otherService: OtherService) {
    // ...
}
```

**Avoid this:**
```kotlin
@Singleton
class MyService {
    @Inject
    lateinit var otherService: OtherService
}
```

## 4. Reactive Programming

The project uses **Project Reactor** for all asynchronous and reactive programming.

-   Use `Mono` for single-item asynchronous sequences.
-   Use `Flux` for multi-item asynchronous sequences.
-   Do not introduce other reactive libraries like RxJava to maintain consistency.

## 5. Build and Configuration

-   **Java Version:** The project is standardized on **Java 21**. Ensure that the `build.gradle.kts` file consistently specifies Java 21 in the `java.toolchain` configuration.
-   **Gradle Kotlin DSL:** The build is configured using Gradle's Kotlin DSL. All build logic should be written in `.gradle.kts` files.
-   **Dependencies:** When adding new dependencies, check `build.gradle.kts` to see if a similar dependency already exists to avoid duplication.
