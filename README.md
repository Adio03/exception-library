# SemicolonLab Exception Handling Library

A Spring Boot autoconfiguration library that:

* **Automatically** catches and handles **all** exceptions (web layer and startup failures).
* Returns concise, consistent JSON error responses.
* Provides dynamic analysis and remediation hints for both runtime and startup exceptions.
* Requires **zero** host configuration to get started (but offers optional customization).

---

## 🚀 Features

1. **Global Web Exception Handler** (`UniversalExceptionHandler`)

   * Catches **any** exception thrown by controllers.
   * Dynamically derives HTTP status, error code, title, and detail from the exception.
   * Builds a uniform JSON payload:

   ```json
   {
     "timestamp": "2025-06-16T12:34:56.789Z",
     "status": 404,
     "code": "RESOURCE_NOT_FOUND",
     "title": "ResourceNotFoundException",
     "detail": "User with id 123 not found",
     "instance": "/api/users/123",
     "type": "https://errors.semicolonlab.africa/errors/RESOURCE_NOT_FOUND",
     "debugInformation": { ... }    // optional
   }
   ```

2. **Startup Failure Analyzer** (`UniversalStartupFailureAnalyzer`)

   * Intercepts **any** exception during Spring Boot startup (`ApplicationFailedEvent`).
   * Uses the same dynamic analyzer logic to provide **concise** failure messages and remediation hints instead of huge stack traces.

3. **Dynamic Exception Analysis** (`DynamicExceptionAnalyzer`)

   * Extracts the **root cause** of any `Throwable`.
   * Determines HTTP status via `@ResponseStatus` or naming conventions.
   * Generates structured error codes (e.g. `FOO_BAR` from `FooBarException`).
   * Provides dynamic **remediation** hints (SQL errors, NPE locations, validation messages).
   * Optionally includes context details (environment‑sensitive).

4. **Zero-Config Auto‑Configuration**

   * Spring Boot 3.x+ style using **`AutoConfiguration.imports`**.
   * No `spring.factories` needed for auto‑config (but supported for legacy analyzers).
   * Optional configuration properties under `exception.library.*`.

---

## 🛠️ Installation

1. **Add the library** to your `pom.xml`:

   ```xml
   <dependency>
     <groupId>org.semicolonlab</groupId>
     <artifactId>exception-library</artifactId>
     <version>1.0.0</version>
   </dependency>
   ```

2. **Ensure** your `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` contains:

   ```text
   org.semicolonlab.infrastructure.output.ExceptionLibraryAutoConfiguration
   ```

3. (Optional) **Add** the Spring Boot Configuration Processor for metadata:

   ```xml
   <dependency>
     <groupId>org.springframework.boot</groupId>
     <artifactId>spring-boot-configuration-processor</artifactId>
     <optional>true</optional>
   </dependency>
   ```

---

## ⚙️ Configuration Properties

The library ships with `ExceptionProperties` under prefix `exception.library` (all defaults are safe):

```properties
# Toggle analyzers
exception.library.enabled=true
exception.library.startup-analyzer.enabled=true
exception.library.database-analyzer.enabled=true
# etc.
```

None of these are required; the library will work out-of-the-box.

---

## 💡 Throwing Custom Exceptions

Your application can throw `SemicolonLabException` anywhere to leverage the library's full power:

```java
import org.semicolonlab.domain.exceptions.SemicolonLabException;
import org.springframework.http.HttpStatus;

// Example in a service method:
public User getUserById(String id) {
    return userRepository.findById(id)
       .orElseThrow(() -> new SemicolonLabException(
           "User with id " + id + " not found",
           HttpStatus.NOT_FOUND,
           "RESOURCE_NOT_FOUND",
           "ResourceNotFoundException",
           Map.of("userId", id)
       ));
}
```

* **Constructors**:

  * `SemicolonLabException(String message)` — wraps a message, defaults to 500.
  * `SemicolonLabException(String msg, Throwable cause)` — wraps cause.
  * `SemicolonLabException(String msg, HttpStatus status, String code, String title, Map<String,Object> ctx)` — full.

Once thrown, your `UniversalExceptionHandler` will catch it and return a structured JSON with your status, code, title, detail, and optional context.

---

## 🧪 Testing

The library includes comprehensive JUnit 5 tests:

* **`UniversalExceptionHandlerTest`**: tests web layer responses for various exception types.
* **`DynamicExceptionAnalyzerTest`**: verifies root-cause analysis, status resolution, error codes, and remediation hints.

Run tests with:

```bash
mvn test
```

---

## 📦 Packaging

Build and install locally:

```bash
mvn clean install
```

Your host application then simply includes the JAR in its dependencies.

---

## 🙋 FAQs

**Q: I’m still seeing Spring’s default error JSON.**
A: Ensure your handler never throws (`NullPointerException` or null status). Use safe defaults as shown above.

**Q: How do I disable debug info in production?**
A: Set:

```properties
exception.library.debug-analyzer.enabled=false
```

or toggle `exception.library.*.enabled=false` per profile.

**Q: Can I extend analyzers?**
A: Yes: implement `ExceptionAnalysisStrategy` and expose a bean to customize handling order/priorities.

---

Enjoy concise, consistent error handling—out-of-the-box!
SemicolonLab Team
