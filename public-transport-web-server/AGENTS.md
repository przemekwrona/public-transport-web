# Backend — public-transport-web-server

Spring Boot 3.2, Java 17, PostgreSQL 18, schema `public_transport`. HTTP context: `webserver.context.path=/api/v1` (port 8080).

## Run

Directory: `public-transport-web-server`, unless stated otherwise.

- Database: from the repo root, `docker compose up public-transport-db`. JDBC: `jdbc:postgresql://localhost:5432/public_transport_db`, user `postgres`.
- Application: `mvn spring-boot:run`.
- Tests: `mvn test`. The Docker image needs `mvn clean package -DskipTests` first — the Dockerfile copies the JAR from `target/`.
- OpenAPI: `mvn compile` runs `openapi-generator-maven-plugin` (`interfaceOnly`, Spring Boot 3).

## Layers

The controller stays thin. It implements the generated interface and delegates to a use-case service.

```java
@RestController
@AllArgsConstructor
@RequestMapping("${webserver.context.path}")
public class TripController implements TripApi {
    @Override
    public ResponseEntity<Status> createTrip(...) {
        return ResponseEntity.status(CREATED).body(tripCreatorService.createTrip(...));
    }
}
```

- `bussiness/<aggregate>/` — one use case per subpackage: `creator`, `updater`, `deletion`, `details`, `pagination`. Keep the `bussiness` spelling.
- Service: `@Service`, `@AllArgsConstructor`, method `@Transactional`. Authorization sits on the service method, not on the controller:

```java
@PreAgencyAuthorize(roles = {
        PreAgencyAuthorize.Role.SUPER_USER,
        PreAgencyAuthorize.Role.AGENCY_OWNER,
        PreAgencyAuthorize.Role.PLANNER
})
```

Roles: `SUPER_USER`, `AGENCY_OWNER`, `AGENCY_ADMIN`, `PLANNER`, `BRIGADIER`, `DISPATCHER`, `DRIVER`. The first method argument is usually the agency code.

- `core` — JPA entities (`@Entity` in `core.agency` and `core`), repositories, mappers (`core.mapper`). Lombok: `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`.
- `security` — Keycloak JWT, `PreAgencyAuthorize`, agency principal.
- Domain error: `BusinessException(exceptionCode, message)`. `GlobalExceptionHandler` returns `ErrorResponse` and HTTP 400.

## Contract and database

Specs live in `src/main/resources/api/`. The main one is `public-transport-api.yaml` (package `org.igeolab.iot.pt.server.api`). PDF and planner are separate. YAML first, then the interface, then the controller. Do not hand-write generated models under `pl.wrona`.

Liquibase: add the next file as `src/main/resources/db/changelog/0xx_description.yaml` and an `include` at the end of `db.changelog-master.yaml`. Do not edit changesets that have already been applied. Hibernate does not create the schema. Schema: `public_transport`. Identifiers: sequence, `allocationSize = 1`.

## Tests

JUnit 5, Mockito (`@ExtendWith(MockitoExtension.class)`, `@Mock`, `@InjectMocks`), AssertJ. The test sits beside the package under `src/test/java`. Test logic that does not need a database as a unit test. Leave Postgres integration to tests that already start a Spring context.

## Trip model

`TripEntity` is the trip structure (route, `variantMode`, geometry, trip code). `TripProfile` (`NORMAL` | `TRAFFIC`) carries the times. `StopTime` hangs off the profile. Details: `docs/trip-profile-model.md`. The trip code comes from the route sequence (`Hex.toHex3`), not from a name typed by the user.
