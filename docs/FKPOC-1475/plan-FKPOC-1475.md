# FKPOC-1475: Refactor bekraftabeslut-bff to use rimfrost-framework-bff and implement BEKB-FR-07

## Goal

1. Replace hand-rolled exception handling, MDC management, and Authorization header
   forwarding in `rimfrost-regel-bekraftabeslut-bff` with the shared utilities from
   `rimfrost-framework-bff`, matching the pattern established in
   `rimfrost-regel-rtf-manuell-bff`.
2. Document service requirements in `docs/krav.md`.
3. Implement informationsaccess publishing (BEKB-FR-07).

## Background

`rimfrost-framework-bff` provides:

| Component | Replaces |
|-----------|----------|
| `GlobalExceptionMapper` | 5 manual try-catch blocks in `BekraftabeslutController` |
| `LogContext` | 3 manual `MDC.put()/finally MDC.remove()` blocks |
| `OutgoingAuthorizationHeaderFilter` | Manual `@HeaderParam("Authorization")` extraction and passing in `done()` |
| `UpstreamHealthCheck` | Missing health check |

The backend (`rimfrost-regel-bekraftabeslut`) already wraps its GET response in
`RegelManuellReadResponse<GetDataResponse>`, which serializes to
`{ "data": {...}, "informationsaccess": {...} }`. The BFF currently ignores
`informationsaccess` by deserializing directly to `GetDataResponse`. BEKB-FR-07 requires
the BFF to extract the informationsaccess data, publish it to ILogg via Kafka, and return
only `data` to the frontend.

## Steps

- [x] **1. Add rimfrost-framework-bff dependency**

  Add to `pom.xml` under the existing rimfrost dependency:

  ```xml
  <dependency>
      <groupId>se.fk.rimfrost.framework.bff</groupId>
      <artifactId>rimfrost-framework-bff</artifactId>
      <version>0.0.4</version>
  </dependency>
  ```

  Run `mvn test-compile -q` to verify the dependency resolves.

- [x] **2. Remove manual exception handling from BekraftabeslutController**

  `GlobalExceptionMapper` (auto-registered by the framework) handles all three exception
  types already caught manually. Remove all try-catch blocks from the five controller
  methods and let exceptions propagate.

  Also remove the now-unused imports: `WebApplicationException`, `ProcessingException`, `Map`.

- [x] **3. Replace manual MDC with LogContext**

  Three methods use `MDC.put("handlaggningId", ...)/finally MDC.remove(...)`.
  Replace each with `LogContext.put(...)` in a try-with-resources.

  Affected methods: `getBekraftabeslut()`, `patchBekraftabeslut()`, `done()`.

- [x] **4. Remove manual Authorization header forwarding**

  `OutgoingAuthorizationHeaderFilter` forwards the `Authorization` header automatically.
  Remove `@HeaderParam("Authorization") String authorization` from `done()` and
  `BekraftabeslutClient.postDone()`.

- [x] **5. Add UpstreamHealthCheck**

  Create `HealthCheckProducer` in `health/` package using
  `UpstreamHealthCheck.fromConfig("bekraftabeslut-backend", "quarkus.rest-client.bekraftabeslut.url", 2000)`.

- [x] **6. Create docs/krav.md**

  Reverse-engineer and document service requirements using prefix `BEKB`.
  Covers BEKB-FR-01 through BEKB-FR-07 and BEKB-NFR-01/02.
  Fix `API-gränssnitt` table to reference `rimfrost-regel-bekraftabeslut-bff-openapi`
  (frontend contract) and `rimfrost-regel-bekraftabeslut-openapi` (backend contract)
  as separate specs.

- [x] **7. Add backend response model**

  Create two records in `model/`:

  - `BackendInformationsaccess(UUID uppgiftId, UUID regelId, String regelVersion, BackendAnvandare anvandare)`
  - `BackendAnvandare(String typId, String varde)`
  - `BackendGetResponse(GetDataResponse data, BackendInformationsaccess informationsaccess)`

  These mirror the `RegelManuellReadResponse` structure already returned by the backend.

- [x] **8. Update BekraftabeslutClient return type**

  Change `regelBekraftabeslutHandlaggningIdGet()` return type from `GetDataResponse` to
  `BackendGetResponse` so Jackson deserializes both `data` and `informationsaccess`.

  Run `mvn test-compile -q` to verify.

- [x] **9. Add Kafka channel config for ILogg**

  Add to `src/main/resources/application.properties`:

  ```properties
  mp.messaging.outgoing.ilogg-event.connector=smallrye-kafka
  mp.messaging.outgoing.ilogg-event.topic=informationsaccess
  mp.messaging.outgoing.ilogg-event.value.serializer=io.quarkus.kafka.client.serialization.ObjectMapperSerializer
  ```

  Run `mvn test-compile -q` to verify.

- [x] **10. Add InformationsaccessPublisher**

  Create `integration/InformationsaccessPublisher` and
  `integration/InformationsaccessPublishException` mirroring the rtf-manuell pattern.

  - Inject `ILogg` from `rimfrost-framework-bff`
  - Build `InformationsaccessEvent` from `BackendInformationsaccess` and `handlaggningId`
  - Set `kalla` to `"rimfrost-regel-bekraftabeslut-bff"`
  - Set `visadInformation` to the `data` field (BEKB-FR-07.1)
  - Throw `InformationsaccessPublishException` if `ILogg.publish()` fails (BEKB-FR-07.5)

  Run `mvn test-compile -q` to verify.

- [x] **11. Update controller to publish informationsaccess and return only data**

  In `getBekraftabeslut()`:
  - Change client call to receive `BackendGetResponse`
  - Map before publishing so no access is logged for data never shown (BEKB-FR-07.3)
  - Call `informationsaccessPublisher.publish(handlaggningId, response)`
  - Return `Response.ok(response.data())` — not the full wrapper

  Run `mvn test` and verify all tests pass.

- [x] **12. Add tests for BEKB-FR-07**

  Add to `BekraftabeslutControllerTest`:

  - `getBekraftabeslut_publishesInformationsaccessEvent` — verify ILogg Kafka topic receives
    an event when GET succeeds (BEKB-FR-07.1)
  - `getBekraftabeslut_returnsErrorAndDoesNotReturnData_whenPublishFails` — verify that if
    the Kafka publish fails, no data is returned to the frontend (BEKB-FR-07.5)
  - `getBekraftabeslut_doesNotForwardInformationsaccessToFrontend` — verify the response
    body does not contain an `informationsaccess` field (BEKB-FR-07.3)

## Architectural layers

All changes stay within the BFF layer. `rimfrost-framework-bff` is a BFF-layer shared
library. The Kafka channel for ILogg is a new outgoing integration but follows the same
pattern already established in `rimfrost-regel-rtf-manuell-bff`.

## Definition of Done

- [x] All five controller methods free of manual try-catch
- [x] All three MDC blocks replaced with `LogContext`
- [x] `done()` no longer extracts or forwards `Authorization` manually
- [x] `HealthCheckProducer` added
- [x] `docs/krav.md` created with BEKB-FR-01 through BEKB-FR-07
- [x] `BackendGetResponse` and `BackendInformationsaccess` records added
- [x] `BekraftabeslutClient` returns `BackendGetResponse` for GET handlaggning
- [x] `InformationsaccessPublisher` publishes event on successful GET
- [x] No informationsaccess data forwarded to frontend (BEKB-FR-07.3)
- [x] Publish failure returns error without data (BEKB-FR-07.5)
- [x] `mvn test` green
- [x] `mvn spotless:apply` applied before commit
