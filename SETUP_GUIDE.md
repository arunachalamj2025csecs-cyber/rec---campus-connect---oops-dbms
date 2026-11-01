# REC CampusConnect: Step-by-Step Setup Guide

This guide takes you from zero to a working site. It has two parts:

- **Part A: Run it on your own computer** (test first, about 30 minutes).
- **Part B: Put it online** (MongoDB Atlas database, Render for the Java API, Google sign-in, Vercel for the website).

Read each step before you do it. Items marked **SECRET** must never be shared or uploaded to GitHub.

---

## Before you start: what you need

| Item | Why | Where to get it |
|---|---|---|
| Java 17 (JDK) | Runs the backend | https://adoptium.net (choose Temurin 17) |
| Maven 3.9+ | Builds the backend | https://maven.apache.org/download.cgi |
| Docker Desktop (optional, easiest) | Runs MongoDB and the API together | https://www.docker.com/products/docker-desktop |
| A GitHub account | Hosts the code for Render and Vercel | https://github.com |
| A MongoDB Atlas account (free) | Cloud database | https://www.mongodb.com/cloud/atlas |
| A Render account (free) | Hosts the Java API | https://render.com |
| A Vercel account (free) | Hosts the website | https://vercel.com |
| A Google Cloud project (free) | Enables "Continue with Google" | https://console.cloud.google.com |

Check Java and Maven in a terminal:

```bash
java -version      # should show version 17
mvn -version       # should show Maven 3.9 or newer
```

---

## Step 0: Rotate the leaked admin key (do this first)

The original project contained a hardcoded admin registration key (`Ars@2008`) in `Code.gs`, in the ZIP file, and in the GitHub history. Treat it as public.

1. Choose a new random key. In a terminal run `openssl rand -base64 24` (or use any password manager). Copy the result. **SECRET**
2. Use this new value as `ADMIN_REGISTRATION_KEY` in every place below. Never reuse `Ars@2008`.
3. If the old key was ever used to create a real admin account, change that account's password after setup.
4. The GitHub history still contains the old key. Deleting it from history needs a separate step (a history rewrite). Ask the repository owner to do this if you want it fully removed.

---

## Step 1: Put the project in your GitHub repository

1. Unzip `campus-connect.zip`. You will get a folder named `campus-connect`.
2. Open your GitHub repository `campus-connect-1` in the browser.
3. Replace its contents with the files from the `campus-connect` folder. The repository root must contain `index.html`, `config.js`, `assets/`, `backend/`, `scripts/`, `docker-compose.yml`, `.env.example`, `.vercelignore`, `.gitignore`.
4. Make sure a file named `.env` is **not** uploaded. `.gitignore` already blocks it.

If you prefer the command line:

```bash
cd campus-connect
# inside your local copy of the repository (cloned with: git clone <repo-url>)
cp -r ../campus-connect/. .          # copy the new files over the old ones
git add -A
git commit -m "REC CampusConnect: Java + MongoDB backend and fixed frontend"
git push
```

Do not use `git push --force` unless the repository owner has agreed to replace the history.

---

## PART A: Run on your own computer

### A1. Create your settings file

From the project folder:

```bash
cp .env.example .env
```

Open `.env` in a text editor. For now, change only:

- `ADMIN_REGISTRATION_KEY=` the new key from Step 0. **SECRET**
- Leave `MONGODB_URI` as it is for local use.

### A2. Start the backend (choose one option)

**Option 1: Docker (easiest).** Make sure Docker Desktop is running, then:

```bash
scripts/run-docker.sh
```

This starts MongoDB and the Java API. Keep this window open. The first run takes a few minutes.

**Option 2: Java and Maven without Docker.** You need a local MongoDB. Install MongoDB Community Server from https://www.mongodb.com/try/download/community and start it. Then:

```bash
scripts/run-local.sh
```

### A3. Test the backend

Open a second terminal:

```bash
scripts/health-check.sh
```

Expected output: `{"ok":true,"service":"campus-connect-api",...}` followed by `API is running.`

To run the automated tests:

```bash
scripts/test.sh
```

You should see `BUILD SUCCESS`. To build the jar file as well, run `scripts/build.sh`.

### A4. Run the website on your computer

1. Open `config.js` and set:

   ```js
   API_BASE_URL: "http://localhost:8080",
   ```

2. In the project folder, start a simple web server **on port 5500** (this matches the allowed origins in `.env`):

   ```bash
   python3 -m http.server 5500
   ```

   Windows users without Python can use the "Live Server" extension in VS Code, which also uses port 5500.

3. Open http://localhost:5500 in your browser.

You should see the REC CampusConnect welcome screen with Student Login and Admin Login.

---

## PART B: Put it online

### B1. Create the MongoDB Atlas database

1. Go to https://cloud.mongodb.com and sign in. Choose **Build a Database** → **M0 (Free)**. Pick a region near India (for example Mumbai) and click **Create**.
2. **Database Access** (left menu) → **Add New Database User**:
   - Authentication: Password.
   - Username: for example `campusapp`.
   - Password: click **Autogenerate**, then copy it. **SECRET**
   - Role: **Read and write to any database**. Click **Add User**.
3. **Network Access** → **Add IP Address** → **Allow Access from Anywhere** (`0.0.0.0/0`). Render uses changing addresses, so this is needed. Click **Confirm**.
4. **Database** → **Connect** → **Drivers**. Copy the connection string. It looks like:

   ```
   mongodb+srv://campusapp:<password>@cluster0.xxxxx.mongodb.net/?retryWrites=true&w=majority
   ```

5. Replace `<password>` with your real password, and add the database name `campusconnect` before the `?`:

   ```
   mongodb+srv://campusapp:YOUR_PASSWORD@cluster0.xxxxx.mongodb.net/campusconnect?retryWrites=true&w=majority
   ```

6. **Important:** if the password contains special characters, encode them. The most common ones:

   | Character | Write it as |
   |---|---|
   | `@` | `%40` |
   | `#` | `%23` |
   | `/` | `%2F` |
   | `:` | `%3A` |
   | `%` | `%25` |
   | `?` | `%3F` |

   Example: password `Ars@2008` becomes `Ars%402008`.

   The final string is your **MONGODB_URI**. **SECRET**

### B2. Create the Google sign-in client ID

1. Go to https://console.cloud.google.com, create a project called `REC CampusConnect`.
2. **APIs & Services** → **OAuth consent screen**. Choose **External** (or **Internal** if your college Google Workspace is the only audience). Fill in the app name and support email, then save.
3. **APIs & Services** → **Credentials** → **Create Credentials** → **OAuth client ID**.
4. Application type: **Web application**. Name: `CampusConnect Web`.
5. **Authorized JavaScript origins** → add both lines:

   ```
   https://campus-connect-1-mu.vercel.app
   http://localhost:5500
   ```

   (Add your own Vercel domain too if it is different.)

6. Click **Create**. Copy the **Client ID** (ends with `.apps.googleusercontent.com`). This is **GOOGLE_CLIENT_ID**. It is public, but keep it consistent everywhere.

### B3. Deploy the Java API on Render

1. Go to https://render.com → **New +** → **Web Service** → connect your GitHub account and choose the repository `campus-connect-1`.
2. Settings:
   - **Name:** `campusconnect-api`
   - **Region:** Singapore (closest free region to India)
   - **Branch:** `main`
   - **Root Directory:** `backend`  (important)
   - **Runtime:** **Docker** (Render reads `backend/Dockerfile`)
   - **Instance Type:** Free
   - **Health Check Path:** `/api/health`
3. Click **Advanced** → **Add Environment Variable**, then add each line below. Use your own values.

   | Key | Value |
   |---|---|
   | `MONGODB_URI` | the string from B1 step 6 **SECRET** |
   | `GOOGLE_CLIENT_ID` | the client ID from B2 |
   | `ADMIN_EMAILS` | `admin@rajalakshmi.edu.in` (see note below) |
   | `ADMIN_REGISTRATION_KEY` | the new key from Step 0 **SECRET** |
   | `CORS_ALLOWED_ORIGINS` | `https://campus-connect-1-mu.vercel.app,http://localhost:5500` |
   | `SESSION_HOURS` | `6` |
   | `APP_TIMEZONE` | `Asia/Kolkata` |

   Do **not** add `PORT`. Render sets it for you.

4. Click **Create Web Service**. Wait for the build to finish (5 to 10 minutes the first time). The status should change to **Live**.
5. Open `https://campusconnect-api.onrender.com/api/health` in your browser (use your real Render address). You should see `"ok":true`.

Note: the free Render plan sleeps after about 15 minutes without traffic. The first request afterwards takes about 30 to 60 seconds. This is normal.

**Note on the admin email:** The original code used `admin1@rajalakshmi.edu.in` in one place and `admin@rajalakshmi.edu.in` in the README. This version uses the value in `ADMIN_EMAILS`. Confirm the correct admin address with your department and set it here. Only emails on this list can be admins.

### B4. Connect the website to the API

1. Open `config.js` in your GitHub repository and set your Render address and client ID:

   ```js
   window.CAMPUS_CONFIG = {
     API_BASE_URL: "https://campusconnect-api.onrender.com",
     GOOGLE_CLIENT_ID: "123456789-abc.apps.googleusercontent.com",
     ADMIN_CONTACT_EMAIL: "admin@rajalakshmi.edu.in"
   };
   ```

   Only public values go here. Never put passwords, the admin key or the MongoDB string in this file.

2. Commit and push the change to GitHub.

### B5. Deploy the website on Vercel

1. Go to https://vercel.com → **Add New…** → **Project** → import `campus-connect-1` from GitHub.
2. Settings:
   - **Framework Preset:** Other
   - **Root Directory:** `./` (the repository root, which contains `index.html`)
   - **Build Command:** leave empty
   - **Output Directory:** leave empty or `.`
3. Click **Deploy**. Vercel will publish the site. Open the address shown (for example `https://campus-connect-1-mu.vercel.app`).

If the address is different from the one in CORS and Google settings, go back and update:
- `CORS_ALLOWED_ORIGINS` on Render (then **Manual Deploy** → **Deploy latest commit**),
- the Google **Authorized JavaScript origins** (B2 step 5).

### B6. Create the first administrator

1. Open the live website → **Admin Login** → **Create account**.
2. Fill in the name, the admin email (it must be on `ADMIN_EMAILS`), phone, and a password of at least 6 characters.
3. In **Admin Registration Password**, enter the value of `ADMIN_REGISTRATION_KEY`.
4. Click **Create Account**, then log in on the **Admin Login** page.

Students register on **Student Login** → **Create account** with a `@rajalakshmi.edu.in` email and a roll number.

---

## Testing checklist (run after deployment)

| # | Test | Expected result |
|---|---|---|
| 1 | Open the website | Welcome screen with two portal options |
| 2 | Student creates an account | "Account created. Please log in." |
| 3 | Student logs in | Dashboard with the student's name |
| 4 | Student submits a complaint | "Complaint Submitted Successfully" with an ID like `CMP-2026-…` |
| 5 | Student opens Profile → Edit | Fields become editable; Save shows "Profile updated." |
| 6 | Admin logs in | Admin dashboard with all complaints |
| 7 | Admin opens a complaint → sets **In Progress** → Save | Status changes; the student sees the admin contact |
| 8 | Admin adds an event with status **Published** | Event appears on the student's Events page |
| 9 | Admin adds an event as **Draft** | Students do not see it |
| 10 | Admin uploads an event poster | Poster shows on the event card |
| 11 | Student changes password | Must log in again afterwards |
| 12 | Logout | Returns to the welcome screen |
| 13 | Phone width (about 390 px) | Menu button opens navigation; no sideways scrolling |
| 14 | Open `https://…onrender.com/api/health` | `"ok":true` |

---

## Troubleshooting

| Problem | Likely cause | Fix |
|---|---|---|
| "The server address is not set" | `API_BASE_URL` still says `YOUR-BACKEND-ADDRESS` | Set it in `config.js` (B4) |
| "Cannot reach the server" | Render is asleep, wrong address, or the service failed | Wait 60 seconds and retry; check the address; check the Render logs |
| Browser console says "blocked by CORS policy" | Your website address is missing from `CORS_ALLOWED_ORIGINS` | Add it on Render and redeploy |
| Google button shows "could not load" | Blocked by a browser extension, or the origin is not authorized | Allow `accounts.google.com`; check B2 step 5 |
| Google sign-in reports the server is not configured (HTTP 503) | `GOOGLE_CLIENT_ID` missing on Render | Add it (B3 step 3) |
| Google sign-in fails (HTTP 502) | Render cannot reach Google | Retry in a minute |
| Render build fails | Root Directory is not `backend` | Set it to `backend` in Render settings |
| Render log says `Authentication failed` | Wrong database password, or special characters not encoded | Repeat B1 steps 5 and 6 |
| Render log says `Could not connect` / timeout | Atlas Network Access missing `0.0.0.0/0` | Add it (B1 step 3) |
| "Invalid admin registration password." | Key mismatch | Use exactly the `ADMIN_REGISTRATION_KEY` value |
| "Admin registration is turned off. The server needs ADMIN_REGISTRATION_KEY set." | Key not set on Render | Add the key (B3 step 3), then redeploy |
| "This email is not on the administrator list (ADMIN_EMAILS)." | Email not in `ADMIN_EMAILS` | Add the email to `ADMIN_EMAILS` on Render and redeploy |
| "Admin access is restricted to the administrator list (ADMIN_EMAILS)." | A non-admin account tried the Admin Login | Use the Student Login instead |
| "Invalid email, password or portal type." | Wrong email/password, or wrong portal chosen | Check the spelling; check you chose the right portal |
| "Use your college email address ending in @rajalakshmi.edu.in." | Student used a personal email | Register with the college email |
| "This roll / register number is already registered." | Duplicate roll number | Check whether the account already exists |
| Changes to Render variables do not take effect | Service not restarted | Click **Manual Deploy → Deploy latest commit** |
| Local: port 8080 already in use | Another program uses it | Change `PORT` in `.env` and in `config.js` |
| Local: `java` not recognised | Java not installed or not on PATH | Install JDK 17 and reopen the terminal |

## Where to look for errors

- **Backend errors:** Render dashboard → your service → **Logs**. Locally, read the terminal running `scripts/run-local.sh` or `docker compose`.
- **Website errors:** in the browser press **F12** → **Console** tab.
- **API check:** `scripts/health-check.sh https://your-api-address`.

## Known limitations (be honest with students and staff)

- There is no rate limiting on login yet. Adding a limit is a good next step.
- The login token is kept in the browser's local storage, which is common for simple portals but not the strongest option.
- Uploaded posters and profile photos are public links. Anyone with the link can view them (this is normal for public event posters).
- The notification switches are saved, but no email or push notifications are sent yet.
- Password reset is handled manually by the administrator.
