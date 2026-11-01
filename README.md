# REC CampusConnect

A college portal for Rajalakshmi Engineering College (REC). Students log in, report campus problems (complaints), track their status, and read campus events. Administrators review complaints, update their status, and publish events.

- **Frontend:** one static page (`index.html`, `config.js`, `assets/`), hosted on Vercel.
- **Backend:** a Java 17 + Spring Boot API with MongoDB as the database, in `backend/`.

Live site: https://campus-connect-1-mu.vercel.app/

---

## Project structure

```
campus-connect/
├── index.html              Web page (login, student portal, admin portal)
├── config.js               The 3 public settings the page needs (API address, Google client ID, admin contact)
├── assets/                 Images used by the page (hero photo, REC logo)
├── .vercelignore           Files Vercel should NOT publish (backend, docs)
├── docker-compose.yml      Runs MongoDB + the Java API together on your computer
├── .env.example            Template for the backend settings. Copy to .env
├── scripts/                Simple commands for faculty (see below)
├── SETUP_GUIDE.md          Step-by-step setup and deployment guide
├── BUGS_REPORT.md          Every bug found in the original project and how it was fixed
└── backend/
    ├── pom.xml             Maven build file (Java 17, Spring Boot 3.3.5, MongoDB driver)
    ├── Dockerfile          Builds the backend into a container
    └── src/
        ├── main/java/edu/rec/campusconnect/
        │   ├── controller/   HTTP endpoints (the "commands" the page calls)
        │   ├── service/      Business logic (accounts, complaints, events, files, Google sign-in)
        │   ├── rules/        Small pure rules (status labels, event ordering, image checks)
        │   ├── security/     Password hashing and session tokens
        │   ├── model/        Database documents (users, complaints, events, sessions, files)
        │   ├── repository/   MongoDB queries
        │   ├── dto/          Shapes of the JSON the page sends
        │   ├── view/         Shapes of the JSON the page receives (never includes passwords)
        │   ├── config/       Settings and CORS (which websites may call the API)
        │   └── common/       Errors, validation, response helpers
        └── test/java/        Automated tests
```

## Commands for faculty (what each script does)

| Command | What it does |
|---|---|
| `scripts/build.sh` | Compiles the backend, runs all tests, and creates `backend/target/campus-connect-api.jar` |
| `scripts/test.sh` | Runs the automated tests only |
| `scripts/run-local.sh` | Starts the API on your computer (MongoDB must be running) |
| `scripts/run-docker.sh` | Starts MongoDB and the API together with Docker (easiest) |
| `scripts/health-check.sh [URL]` | Checks that the API answers at `/api/health` |

On Windows, run these commands in Git Bash or WSL, or use the equivalent `mvn` / `docker compose` commands shown in `SETUP_GUIDE.md`.

## API endpoints (the backend "commands")

All endpoints return JSON with `"ok": true` on success or `"ok": false, "message": "..."` on error. Logged-in requests send the header `Authorization: Bearer <session token>`.

| Method and path | Who | Purpose |
|---|---|---|
| `GET /api/health` | anyone | Check that the server is alive |
| `POST /api/auth/register/student` | anyone | Create a student account |
| `POST /api/auth/register/admin` | anyone with the admin registration key | Create an admin account (email must be on the admin list) |
| `POST /api/auth/login` | anyone | Log in with email and password (choose `student` or `admin`) |
| `POST /api/auth/google` | anyone | Log in with a Google account |
| `POST /api/auth/logout` | logged in | End the current session |
| `GET /api/users/me` | logged in | Show the current user |
| `PUT /api/users/me` | logged in | Update name, phone, department, roll/employee ID, year, profile photo |
| `PUT /api/users/me/settings` | logged in | Save notification and privacy switches |
| `POST /api/users/me/password` | logged in | Change password (ends all other sessions) |
| `GET /api/dashboard` | logged in | Counts and recent items for the home page |
| `GET /api/complaints?status=all\|open\|inprogress\|resolved` | logged in | List complaints (students see only their own) |
| `GET /api/complaints/search?q=text` | logged in | Search complaints |
| `GET /api/complaints/summary` | admin | Totals and counts by category |
| `POST /api/complaints` | student | Submit a complaint |
| `GET /api/complaints/{id}` | logged in | One complaint with its history |
| `PUT /api/complaints/{id}` | admin | Change status and add a note |
| `GET /api/events` | logged in | List events (students see published events only) |
| `GET /api/events/{id}` | logged in | One event |
| `POST /api/events` | admin | Create an event (optionally with a poster) |
| `PUT /api/events/{id}` | admin | Edit an event or publish it |
| `DELETE /api/events/{id}` | admin | Delete an event and its poster |
| `GET /api/files/{id}` | anyone | Download a poster or profile photo |

## Settings (environment variables)

These are read by the backend. See `.env.example` for the full list with examples.

| Variable | Required | Meaning |
|---|---|---|
| `MONGODB_URI` | yes | Where the database is |
| `PORT` | no (8080) | Port the API listens on |
| `GOOGLE_CLIENT_ID` | for Google sign-in | Google OAuth Web client ID |
| `ADMIN_EMAILS` | yes | Comma-separated admin email list |
| `ADMIN_REGISTRATION_KEY` | yes (for admin sign-up) | Secret needed to create admin accounts |
| `CORS_ALLOWED_ORIGINS` | yes | Websites allowed to call the API |
| `SESSION_HOURS` | no (6) | Login duration |
| `APP_TIMEZONE` | no (Asia/Kolkata) | Time zone for event dates |

## Security notes

- Passwords are stored as PBKDF2-SHA256 hashes (210,000 rounds, random salt). They are never returned by the API.
- Login tokens are random, and only their SHA-256 hash is stored in the database. Tokens expire after `SESSION_HOURS`.
- Uploads (posters, photos) are checked by file type and content (magic bytes), and limited in size.
- Secrets live only in environment variables, never in `index.html` or `config.js`.
- Admin accounts require the admin registration key. The old hardcoded key `Ars@2008` has been removed and must be rotated (see `SETUP_GUIDE.md`, step 1).

## License and ownership

Built for REC CampusConnect. Replace contact details in `config.js` with the college's own addresses before going live.
