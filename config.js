/*
  REC CampusConnect - front-end settings.
  Edit these three values, then redeploy the static site (Vercel does this on git push).
  Do NOT put secrets (passwords, admin keys, database URLs) in this file. Everything here is public.
*/
window.CAMPUS_CONFIG = {
  // Public address of your Java backend (no trailing slash). Example: https://campusconnect-api.onrender.com
  API_BASE_URL: "http://localhost:8080",
  // OAuth 2.0 Web client ID from Google Cloud Console (ends with .apps.googleusercontent.com). Leave "" to hide Google sign-in.
  GOOGLE_CLIENT_ID: "280039600649-8b0jpi3idbuhkvrbnmnfuqhke8a6d6t1.apps.googleusercontent.com",
  // Shown when a student clicks "Contact Admin" and no admin has replied to a complaint yet.

};
