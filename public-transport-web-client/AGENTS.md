# Frontend — public-transport-web-client

Angular 19 carrier console. The app is built on NgModules. Newer code is sometimes standalone and imported into the feature module. Match the style of the file you are editing.

## Run

Directory: `public-transport-web-client`.

- `npm start` — `ng serve`, `http://localhost:4200`. Proxy `/api` → `http://localhost:8080` (`src/proxy.conf.json`).
- `npm test` — Karma and Jasmine. Specs sit next to the component (`*.spec.ts`).
- `npm run generate` — i18n and the OpenAPI clients. After a server spec change, run this before touching the UI.
- `npm run build:prod` — the production build lands in `public-transport-web-server/src/main/resources/static`.

Routing uses `useHash: true`. Browser URLs look like `/#/agency/routes`. The generated API `BASE_PATH` is `/api/v1`.

## Layout

- `src/app/app.routes.ts` — routes. Screen data is loaded by resolvers, not `ngOnInit`, when the neighboring screen already does that.
- `src/app/agency/` — routes, trips, brigades, calendars, timetables, stops, users, carrier profile.
- `src/app/auth/` — sign-in, header interceptor, `AgencyStorageService`.
- `src/app/transport/` and `landing/` — journey planner and the public page.
- `src/app/generated/` — `typescript-angular` clients. Read only. Regenerate with `generate:api`, `generate:pdf:api`, `generate:planner:api`.

A feature screen is a directory with a component, template, scss, resolver, and module. Create/edit mode goes in the route `data.mode`, as in the trip editor (`TripEditorComponentMode`).

## UI

- User-facing copy goes through Transloco: `transloco="key"` or `{{ 'key' | transloco }}`. Dictionaries: `src/assets/i18n/pl.json` and `en.json`. After new keys: `npm run i18n:extract`. The default UI language is Polish.
- Forms: Reactive Forms. Validation and errors follow `src/app/shared/form.utils.ts`.
- Look: Angular Material plus Tailwind and Metronic classes (`btn`, `card`, `input`, `form-label`). Do not introduce another styling system.
- Maps: Leaflet. Dates: adapter with Monday as the first day of the week, locale `pl-PL`.

## API from the UI

Call services from `src/app/generated/public-transport-api` (`TripService`, `StopsService`, and the rest). They are registered in the feature module `providers`. Map HTTP errors to the generated `ErrorResponse` and show them through `NotificationService`.

Trip model in the UI: trip details carry a `tripProfiles` list (traffic mode and stop times). Do not keep travel time as the only field on the trip when the profile already holds it.
