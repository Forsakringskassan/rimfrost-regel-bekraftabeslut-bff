# FKPOC-1475: Refactor bekraftabeslut-bff to use rimfrost-framework-bff

## Goal

Replace hand-rolled exception handling, MDC management, and Authorization header
forwarding in `rimfrost-regel-bekraftabeslut-bff` with the shared utilities from
`rimfrost-framework-bff`, matching the pattern established in
`rimfrost-regel-rtf-manuell-bff`.

## Background

`rimfrost-framework-bff` provides:

| Component | Replaces |
|-----------|----------|
| `GlobalExceptionMapper` | 5 manual try-catch blocks in `BekraftabeslutController` |
| `LogContext` | 3 manual `MDC.put()/finally MDC.remove()` blocks |
| `OutgoingAuthorizationHeaderFilter` | Manual `@HeaderParam("Authorization")` extraction and passing in `done()` |
| `UpstreamHealthCheck` | Missing health check |

The error response format (`{"error": "..."}`) and HTTP status codes are identical to what
the controller currently produces manually, so there is no functional change for callers.

## Steps

- [ ] **1. Add rimfrost-framework-bff dependency**

  Add to `pom.xml` under the existing rimfrost dependency:

  ```xml
  <dependency>
      <groupId>se.fk.rimfrost.framework.bff</groupId>
      <artifactId>rimfrost-framework-bff</artifactId>
      <version>0.0.4</version>
  </dependency>
  ```

  Run `mvn test-compile -q` to verify the dependency resolves.

- [ ] **2. Remove manual exception handling from BekraftabeslutController**

  `GlobalExceptionMapper` (auto-registered by the framework) handles all three exception
  types already caught manually. Remove all try-catch blocks from the five controller
  methods and let exceptions propagate:

  - `getReferensdata()` — remove try-catch; keep the switch + `Response.ok()`
  - `getBekraftabeslut()` — remove try-catch-finally; keep the client call + `Response.ok()`
  - `patchBekraftabeslut()` — remove try-catch-finally; keep the mapping + client call + `Response.status(200).build()`
  - `getUppgiftsbeskrivning()` — remove try-catch; keep the client call + `Response.ok()`
  - `done()` — remove try-catch-finally; keep the client call + `Response.status(204).build()`

  Also remove the now-unused imports: `WebApplicationException`, `ProcessingException`, `Map`.

  Run `mvn test` and verify all tests pass.

- [ ] **3. Replace manual MDC with LogContext**

  Three methods use `MDC.put("handlaggningId", ...)/finally MDC.remove(...)`.
  Replace each with `LogContext.put(...)` in a try-with-resources:

  ```java
  try (var ignored = LogContext.put("handlaggningId", handlaggningId))
  {
      // ...
  }
  ```

  Affected methods: `getBekraftabeslut()`, `patchBekraftabeslut()`, `done()`.

  Remove the now-unused `MDC` import; add `se.fk.rimfrost.framework.bff.logging.LogContext`.

  Run `mvn test` and verify all tests pass.

- [ ] **4. Remove manual Authorization header forwarding**

  `OutgoingAuthorizationHeaderFilter` (auto-registered by the framework) forwards the
  `Authorization` header to all outgoing REST client calls automatically.

  In `BekraftabeslutClient`:
  - Remove `@HeaderParam("Authorization") String authorization` from `postDone()`

  In `BekraftabeslutController.done()`:
  - Remove `@HeaderParam("Authorization") String authorization` from the method signature
  - Update the `postDone()` call to remove the `authorization` argument

  Run `mvn test` and verify all tests pass.

- [ ] **5. Add UpstreamHealthCheck**

  Create `src/main/java/se/fk/github/bekraftabeslutbff/health/HealthCheckProducer.java`:

  ```java
  package se.fk.github.bekraftabeslutbff.health;

  import jakarta.enterprise.context.ApplicationScoped;
  import jakarta.enterprise.inject.Produces;
  import org.eclipse.microprofile.health.HealthCheck;
  import org.eclipse.microprofile.health.Readiness;
  import se.fk.rimfrost.framework.bff.health.UpstreamHealthCheck;

  @ApplicationScoped
  public class HealthCheckProducer
  {
      /**
       * Readiness check that probes the bekraftabeslut backend.
       */
      @Readiness
      @Produces
      HealthCheck bekraftabeslutHealthCheck()
      {
          return UpstreamHealthCheck.fromConfig(
                  "bekraftabeslut-backend",
                  "quarkus.rest-client.bekraftabeslut.url",
                  2000);
      }
  }
  ```

  Run `mvn test` and verify all tests pass.

## Architectural layers

All changes stay within the existing BFF layer. No new cross-layer dependencies are
introduced. `rimfrost-framework-bff` is a BFF-layer shared library — the same layer as
this service.

## Definition of Done

- [ ] All five controller methods free of manual try-catch
- [ ] All three MDC blocks replaced with `LogContext`
- [ ] `done()` no longer extracts or forwards `Authorization` manually
- [ ] `HealthCheckProducer` added
- [ ] `mvn test` green
- [ ] `mvn spotless:apply` applied before commit
