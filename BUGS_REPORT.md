# Bug Report: REC CampusConnect (original project → fixed version)

Scope: every file in `campus-connect-1-main.zip` (`Code.gs`, `index.html`, `README.md`, `appsscript.json`).
Severity: **Critical** = security or the main flow broken · **High** = feature broken or data risk · **Medium** = wrong behaviour in edge cases · **Low** = clean-up or maintenance.

Status column: **Fixed** = changed in the new Java + frontend code. **Verified** = covered by an automated test or a browser test (see the "Testing" section).

---

## 1. Security

| # | Sev | Where (original) | Problem | Fix in new version | Status |
|---|---|---|---|---|---|
| S1 | Critical | `Code.gs` `setupPortal()` and the zip | The admin registration key `Ars@2008` is hardcoded and written into the script. Anyone with the code can create an admin account. | The key is only read from the `ADMIN_REGISTRATION_KEY` environment variable. Admin registration is disabled when it is empty. Compared in constant time. **Action: rotate the key** (SETUP_GUIDE Step 0). | Fixed, Verified |
| S2 | Critical | `Code.gs` `hash_()` | Passwords are hashed with plain, unsalted SHA-256. Identical passwords give identical hashes, so they are fast to crack. | PBKDF2-HMAC-SHA256, 210,000 iterations, 16-byte random salt, constant-time comparison. Stored as `pbkdf2_sha256$iterations$salt$hash`. | Fixed, Verified |
| S3 | High | `Code.gs` `sessionResponse_()`, `auth_()` | Login tokens are stored in CacheService (shared, can be evicted early) and the raw token is the cache key. | Tokens are 256-bit random values. Only their SHA-256 hash is stored, in MongoDB, with a TTL index so they expire automatically. | Fixed, Verified |
| S4 | High | `Code.gs` `changePassword_()` | After a password change, only the current session is removed. Other devices stay logged in. | All sessions for the user are deleted when the password changes. | Fixed, Verified (service test) |
| S5 | High | `Code.gs` `savePoster_()`, `saveProfilePhoto_()` | Posters and profile photos are stored in Google Drive and shared as "anyone with the link". Upload errors are silently swallowed (`catch(err){}`). The file type is trusted from the browser. | Files are stored in MongoDB. Type is checked by magic bytes (PNG/JPEG/WEBP), size is limited (5 MB posters, 3 MB photos), filenames are sanitised, and `X-Content-Type-Options: nosniff` is sent. Failures are returned to the user. | Fixed, Verified |
| S6 | High | `Code.gs` `doPost` | Internal error messages (stack/system text) are sent back to the browser. | Unexpected errors return a generic message; details go only to the server log (`GlobalExceptionHandler`). | Fixed, Verified (by code review) |
| S7 | Medium | `Code.gs` `safeUser_()` | The user object sent to the browser includes `settings`, which holds the Google account ID (`googleSub`). | The response type (`UserView`) never includes password hashes or Google IDs. | Fixed, Verified |
| S8 | Medium | `Code.gs` `updateEvent_()` | Event `status` is written as typed (any text). The date is not checked. | Status must be `draft` or `published`; the date must be a real `YYYY-MM-DD` date. | Fixed, Verified |
| S9 | Medium | `Code.gs` `createComplaint_()` | Category and priority accept any text. | Category and priority must come from fixed lists (`Validation.java`). | Fixed, Verified |
| S10 | Medium | `Code.gs` `updateProfile_()` | Student year is not checked. | Year must be 1st–4th Year or empty. | Fixed, Verified |
| S11 | Medium | (none) | No limit on login attempts. | Not yet implemented (listed as a known limitation in SETUP_GUIDE). | Open |
| S12 | Low | `index.html` `api()` | The session token is kept in `localStorage`. | Kept for simplicity; documented as a limitation. Use HTTPS only (Render and Vercel do this). | Known limitation |

## 2. Data and logic

| # | Sev | Where (original) | Problem | Fix in new version | Status |
|---|---|---|---|---|---|
| D1 | Critical | `Code.gs` email regex | The pattern is written as `/^[^\\s@]+@rajalakshmi\\.edu\\.in$/`. The doubled backslash makes the class exclude the letter `s`, so almost every real student address is rejected. | Correct pattern `^[^\s@]+@rajalakshmi\.edu\.in$` in `Validation.java`, with tests for valid and invalid addresses. | Fixed, Verified (unit test) |
| D2 | High | `Code.gs` `updateProfile_()` | Two students can share the same roll number. | Roll number has a unique index in MongoDB; the update returns HTTP 409 "already registered". | Fixed, Verified (service test) |
| D3 | High | `Code.gs` / `README.md` | The admin email conflicts: code uses `admin1@rajalakshmi.edu.in`, README uses `admin@rajalakshmi.edu.in`. | Admin emails come from `ADMIN_EMAILS` (default `admin@rajalakshmi.edu.in`). **Please confirm the correct address.** | Fixed, needs your confirmation |
| D4 | High | `Code.gs` `ensureSheets_()` | Sheet structure is checked and rewritten on every request, which is slow and can clash when requests run together. | Indexes are created once at startup (`auto-index-creation`). | Fixed |
| D5 | Medium | `Code.gs` `listComplaints_()` | An unknown status filter silently returns an empty list. | Filter must be `all`, `open`, `inprogress` or `resolved`; anything else returns HTTP 400. | Fixed, Verified |
| D6 | Medium | `Code.gs` `updateEvent_()` | When a poster is replaced, the old Drive file is left behind. | The old poster file is deleted when a new one is saved, and when the event is deleted. | Fixed, Verified (service test) |
| D7 | Medium | `Code.gs` `updateComplaint_()` | An invalid status is ignored silently. | Invalid status returns HTTP 400 "Status must be…". | Fixed |

## 3. Frontend (`index.html`)

| # | Sev | Where | Problem | Fix in new version | Status |
|---|---|---|---|---|---|
| F1 | Critical | `index.html` | The Apps Script address is hardcoded (`…/macros/s/AKfycby…/exec`). The site can only talk to that one old script, and the address cannot be changed without editing code. | Address moved to `config.js` (`API_BASE_URL`). | Fixed, Verified |
| F2 | High | `index.html` | Google client ID is the placeholder `YOUR_GOOGLE_CLIENT_ID`, so Google sign-in never works. | Client ID in `config.js`. The button shows a clear message when it is not set. | Fixed |
| F3 | High | `index.html` | Google sign-in button stays on "loading" forever if Google's script is blocked. | Retries for about 9 seconds, then shows "Google sign-in could not load". | Fixed |
| F4 | High | `index.html` | An expired session leaves the user on an error page. | HTTP 401 clears the saved login and returns to the welcome screen with a message. | Fixed, Verified (browser test) |
| F5 | High | `index.html` | Duplicate function definitions (`renderAuth`, `go`, `renderDashboard`, `renderSidebar`, `handleAuthSubmit` ×3, `initPortal`, `saveProfile`, `renderProfile`, and others). Only the last copy runs, so the code is confusing and easy to break. | One definition per function; a test checks for duplicates. | Fixed, Verified |
| F6 | High | `index.html` | The "Contact Admin" mailto link is hardcoded to `admin1@`. | Uses `ADMIN_CONTACT_EMAIL` from `config.js`. | Fixed |
| F7 | High | `index.html` | Profile photo upload was removed from the active version of the page. | Profile photo upload restored (PNG/JPG/WEBP, max 3 MB). | Fixed, Verified (browser test: profile edit) |
| F8 | High | `index.html` | Poster help text says images are stored in Google Drive. | Text updated to match the new storage. | Fixed |
| F9 | Medium | `index.html` `api()` | Requests are sent as `text/plain` to work around Apps Script, and there is a second code path that calls `google.script.run`. | One JSON API path with an `Authorization: Bearer` header. | Fixed, Verified |
| F10 | Medium | `index.html` edit-event form | Time value had a stray trailing space (`value="${e.time} "`), so edits changed the time text. | Value is escaped and not padded. | Fixed |
| F11 | Medium | `index.html` `saveSettings()` | Settings were read from the whole page with `document.querySelectorAll`, so switches on other screens could be saved too. | Only switches on the current settings page are read. | Fixed |
| F12 | Medium | `index.html` `showLoadingScreen()` | Had no effect after the loading element was removed, so the spinner could disappear at the wrong time. | Loading screen is shown and hidden with a class; always hidden in `finally`. | Fixed |
| F13 | Medium | `index.html` | Admin sign-in with Google was not allowed in the code (README promised it). | Admin Google sign-in allowed for emails on `ADMIN_EMAILS`. | Fixed |
| F14 | Low | `index.html` | 3.4 MB page. Seven base64 images are embedded. Two images (about 1.3 MB) are never used, and the hero photo is a 1.06 MB PNG. | Hero photo compressed to 132 KB JPEG and moved to `assets/`. Unused images removed. The page is now about 83 KB. | Fixed, Verified |
| F15 | Low | `index.html` | Unused constants `REC_LOADING_DATA`, `REC_LOGO_DATA` and a commented-out `API_URL`. | Removed. | Fixed |
| F16 | Low | `index.html` | Inline `onclick="…"` attributes spread across the page. | One click handler with a table of actions. | Fixed |

## 4. Project and documentation

| # | Sev | Where | Problem | Fix | Status |
|---|---|---|---|---|---|
| P1 | High | `README.md` | Setup steps refer to Google Sheets and Apps Script, which no longer apply. | Replaced by `README.md` and `SETUP_GUIDE.md` for the Java + MongoDB version. | Fixed |
| P2 | Medium | `appsscript.json` | Apps Script manifest (`USER_DEPLOYING`, `ANYONE_ANONYMOUS`) is not used by the Java backend. | Removed from the new project. | Fixed |
| P3 | Low | repository | No automated tests and no build file. | Maven build, JUnit 5 tests, `scripts/` commands, Dockerfile, docker-compose. | Fixed |

---

## What was checked and how

| Check | Result |
|---|---|
| Pure logic tests (validation, tokens, password hashing, complaint rules, event ordering, image signatures) | 25 of 25 passed (run with a JDK-only harness in this environment) |
| Service flow tests (register, login, Google login, complaints, events, uploads, profile, password change) using in-memory repositories | 64 of 64 passed |
| Backend source compiles against Spring Boot and MongoDB API signatures | Passed (compile check using stub versions of those APIs, not the real libraries) |
| Frontend browser test (Playwright, mocked API, 390 px phone width) | All 10 flows passed; no page or console errors; no horizontal scrolling |
| Frontend syntax and duplicate-function check | Passed |

### Not run in this environment (please run these yourself)

The sandbox used to build this project could not download Maven or MongoDB packages, so these were **not** run here:

1. `scripts/build.sh` (full Maven build with Spring Boot and the real JUnit runner)
2. `scripts/run-docker.sh` or `scripts/run-local.sh` with a real MongoDB
3. A real Google sign-in (needs your Google client ID)
4. Real deployments on Render and Vercel

Run the steps in `SETUP_GUIDE.md` Part A3 and the testing checklist before you rely on the system.

## Open decisions for you

1. **Admin email:** confirm `admin@rajalakshmi.edu.in` (or give the correct address). It is set in `ADMIN_EMAILS`.
2. **Admin key:** choose a new `ADMIN_REGISTRATION_KEY` (never reuse `Ars@2008`).
3. **GitHub history:** the old key still exists in past commits. Removing it needs a history rewrite, which you should approve.
