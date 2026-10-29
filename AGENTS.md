# Public Transport Web

Monorepo for public-transport timetable planning. An agent working inside a module also receives that module's `AGENTS.md`. Instructions from the directory closer to the file take precedence.

## Modules

- `public-transport-web-client` — Angular 19, carrier console and journey planner. Context: `public-transport-web-client/AGENTS.md`.
- `public-transport-web-server` — Spring Boot 3.2, Java 17, PostgreSQL. Context: `public-transport-web-server/AGENTS.md`.
- `public-transport-documentation` — AsciiDoc user and technical manuals. Context: `public-transport-documentation/AGENTS.md`.
- `public-transport-web-e2e` — WebdriverIO. Keep e2e tests separate from an API change until the contract is in place.
- `keycloak` — realm `public-transport`. The server is a JWT resource server.
- `docs/trip-profile-model.md` — trip model: route, variant, time profile.

## API contract

The source of truth is the OpenAPI spec in `public-transport-web-server/src/main/resources/api/`. Do not edit generated code by hand.

Changing an endpoint:

1. Update the server YAML (`public-transport-api.yaml` and the files under `paths/`).
2. Run `mvn compile` in `public-transport-web-server`. The generator builds the `*Api` interfaces.
3. Implement the controller that implements the generated interface.
4. Run `npm run generate` in `public-transport-web-client`.
5. Call the generated Angular service. Do not add a hand-written `HttpClient` next to the generated client.

## Domain

The carrier (`agency`) is the tenant. Resources hang off the agency code.

`Route` → `Trip` (trip structure: variant, geometry, designation) → `TripProfile` (`NORMAL` | `TRAFFIC`, travel times) → `StopTime`.

Vocabulary: route, trip, variant, time profile, brigade, calendar, timetable. The Java package `bussiness` is misspelled. Keep that spelling. New code goes in that package, not in a corrected name.

## Leave untouched

- Generated clients in `public-transport-web-client/src/app/generated/`.
- Generated server sources (`src/gen`, `target/generated-sources`).
- The `data/` directory — local Postgres files.
- Secrets and keys from `application.properties`. Do not copy them into new files.
