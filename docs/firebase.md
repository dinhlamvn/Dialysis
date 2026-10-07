# Firebase Crashlytics and Analytics

The app reports crashes, non-fatal errors and every failure to save or sync water-intake / urine data
to Firebase Crashlytics and Google Analytics for Firebase.

## Setup: `google-services.json`

1. In the Firebase console, open the project and download `google-services.json` for the Android app
   `com.dialysis.app` (one Firebase project serves both the `dev` and `prod` flavors).
2. Put it at `app/google-services.json`. The file is git-ignored (`app/.gitignore`); never commit it.
3. Build as usual. Collection is on for all builds (debug and release, both flavors).

Without the file the build still succeeds: the Google Services plugin is set to
`MissingGoogleServicesStrategy.WARN`. At runtime Firebase is then not initialized, and
`AppTelemetryProvider` falls back to `NoOpAppTelemetry`, so the app runs normally and sends nothing.

## Code layout

- `telemetry/AppTelemetry.kt`: the `AppTelemetry` interface, `NoOpAppTelemetry`, and `AppTelemetryProvider`.
  All Firebase calls go through this interface (Koin `single`). Only `FirebaseAppTelemetry` imports Firebase.
- `telemetry/SyncFailureReporter.kt`: builds every event, custom key and Crashlytics log line below.
- `telemetry/ErrorClassifier.kt`: maps an error to `error_type` and `http_code`.
- `data/network/ApiException.kt`: `NetworkManager.resolve` / `resolveNullable` fail with `ApiException`,
  which keeps the HTTP status (`httpCode`) and the original exception (`cause`).
- `telemetry/TelemetryIdentity.kt`: Crashlytics user id and app-wide custom keys.
- `sync/WaterIntakeSyncRunner.kt`, `sync/UrineSyncRunner.kt`: the sync loops (the WorkManager workers are thin adapters).
- Unit tests use `FakeAppTelemetry` (`app/src/test/.../telemetry/`) and never call Firebase.

## Events

Each failure event is also sent to Crashlytics as a non-fatal (`recordException`) with the same values as
custom keys and in one log line (`<event> key=value ...`), so a stuck record can be found and restored by hand.

### `sync_failed`

Sent for each failed create / delete in `WaterIntakeSyncWorker`, each failed upload in `UrineSyncWorker`,
and a failed water history pull in `HomeViewModel`.

| Param | Type | Description |
|---|---|---|
| `data_type` | string | `water` or `urine` |
| `operation` | string | `create`, `delete` or `pull` |
| `api_method` | string | HTTP method of the endpoint (`POST`, `DELETE`, `GET`) |
| `api_path` | string | Endpoint path template, e.g. `/mobile/water/intake/{syncedId}` |
| `error_type` | string | `network`, `timeout`, `http` (4xx), `server` (5xx or `success=false`), `parse`, `unknown` |
| `http_code` | number | HTTP status; left out when there is none |
| `error_message` | string | Error message, max 100 chars |
| `attempt` | number | WorkManager `runAttemptCount` (workers only) |
| `record_id` | string | Water create: local entry id. Water delete: local pending-delete id. Urine: `client_id` |
| `record_created_at` | string | When the record was created (water: ISO-8601 UTC; urine: as stored) |
| `pending_count` | number | Records waiting to sync at the start of the run (water: entries + deletes) |
| `amount_ml` | number | Water / urine amount (create only) |
| `drink_name` | string | Water only |
| `logged_at` | string | Time logged, as sent to the server |
| `note` | string | Urine only, max 100 chars |
| `client_id` | string | Urine only |

A history pull failure has no record, so it carries only `data_type`, `operation`, `api_method`, `api_path`,
`error_type`, `http_code` and `error_message`.

### `api_failed`

Sent by `data/network/interceptor/ApiFailureInterceptor` (on the shared OkHttp client, so every API call)
when a request fails with a transport error (no network, SSL, timeout), returns HTTP >= 400, or returns JSON
with `success = false`. Cancelled calls are not reported. A failed sync call sends both `api_failed` and
`sync_failed`.

| Param | Type | Description |
|---|---|---|
| `api_method` | string | HTTP method |
| `api_path` | string | Real request path, e.g. `/mobile/weight/chart` |
| `api_query` | string | Query string, e.g. `period=month&month=10&year=2026`; left out when empty |
| `error_type` | string | As in `sync_failed` |
| `http_code` | number | HTTP status; left out for transport errors and `success = false` on HTTP 200 |
| `error_message` | string | Server `message`, else the exception message (e.g. SSL error), max 100 chars |
| `request_body` | string | JSON body the app sent (POST/PUT), to re-enter the data by hand in the web admin. Left out for `/mobile/auth/*`; fields named like `password`, `otp`, `token`, `phone`, `email` are masked as `***` |
| `error_body` | string | Server response body for HTTP >= 400 |
| `user_id` | string | Server user id of the logged-in account; left out when logged out |

Headers (auth token) are never sent.

### `water_intake_save_failed`

Sent when the local Room insert in `HomeViewModel.addDrink` throws (the app does not crash; the entry is not saved).
Params: `data_type` (`water`), `error_type`, `http_code` (if any), `error_message`, `amount_ml`, `drink_name`,
`logged_at`.

### `sync_completed`

Sent once per worker run (also when not logged in, with zero counts). Analytics only, no non-fatal.
Params: `data_type`, `success_count`, `failed_count`, `attempt`.

## Reading failures in Crashlytics

Each failure is a non-fatal named `<event>.<METHOD> <path>`, so the issue list shows which API failed, e.g.
`api_failed.POST /mobile/water/intake` or `sync_failed.DELETE /mobile/water/intake/{syncedId}` (numeric ids in
`api_failed` paths are folded to `{id}`). The message reads `<event>: <endpoint> · user <user_id> · <error_type>`;
the original exception and its stack trace are under **Caused by**. Open an issue → events: **User ID** shows the
user, **Keys** / **Logs** show `api_query`, `request_body` and the record payload. Search by user id to see all
failures of one user.

## Value lengths

Analytics cuts every string value to 100 chars, so long `request_body` / `error_message` values are incomplete
there. The same failure in Crashlytics (Non-fatals → event → **Keys** / **Logs**) keeps values up to 1024 chars:
use Crashlytics to read the full request body.

Every failure event (`sync_failed`, `api_failed`, `water_intake_save_failed`) also carries `user_id`.

## Crashlytics user id and custom keys

- User id (Crashlytics and Analytics): the server user id of the logged-in account (stored at login / OTP verify,
  or fetched once from `/mobile/auth/me` at app start for sessions logged in before this was added). When nobody is logged in,
  an anonymous install UUID that survives sign-out. Accounts logged in before this change report the install id
  until their next login.
- `app_flavor`: `dev` or `prod`.
- `server_url_host`: host of `BuildConfig.SERVER_URL`.
- Per-failure keys: the params listed above. Keys a failure does not use are set to an empty string so values
  from an earlier failure do not show on a later report.

## Privacy

Failure events carry the record payload, the user id and, for `api_failed`, the request body of data calls
(water, urine, weight, symptoms), so failed data can be re-entered by hand (decisions of the product owner,
2026-10-05 and 2026-10-07). Never send auth tokens or headers, `/mobile/auth/*` bodies (credentials, OTP,
profile), or a name, phone or email.

## Register custom params as custom dimensions

Analytics only shows event params in reports after they are registered:

1. Firebase console → **Analytics** → **Custom definitions** (in Google Analytics: **Admin** → **Custom definitions**).
2. **Create custom dimension**, scope **Event**, and for each text param enter the param name exactly
   (`data_type`, `operation`, `api_method`, `api_path`, `api_query`, `error_body`, `error_type`, `error_message`, `record_id`, `record_created_at`, `drink_name`,
   `logged_at`, `note`, `client_id`).
3. **Create custom metric**, scope **Event**, for each number param (`http_code`, `attempt`, `pending_count`,
   `amount_ml`, `success_count`, `failed_count`), unit **Standard**.
4. Data shows in reports for events received after registration (it can take up to 24-48 hours).
   For quick checks, enable debug mode on a device
   (`adb shell setprop debug.firebase.analytics.app com.dialysis.app`) and use **DebugView**.

Analytics limits: string values over 100 chars are cut (the app truncates them first), and an event can have
at most 25 params.
