package edu.rec.campusconnect.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.rec.campusconnect.common.ApiException;
import edu.rec.campusconnect.common.Validation;
import edu.rec.campusconnect.config.AppConfig;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.springframework.stereotype.Component;

/**
 * Checks a Google sign-in token with Google's tokeninfo service.
 *
 * Checks made: the token is valid, it was issued for OUR client ID (aud),
 * Google issued it (iss), and the email is verified.
 * The README notes that tokeninfo is fine for a prototype; for high-security use
 * a server-side library that verifies the signature locally is recommended.
 */
@Component
public class GoogleTokenVerifier {

    private static final String TOKENINFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token=";

    /** The verified details we need from Google. */
    public record GoogleProfile(String sub, String email, String name) {
    }

    private final AppConfig config;
    private final ObjectMapper json;
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public GoogleTokenVerifier(AppConfig config, ObjectMapper json) {
        this.config = config;
        this.json = json;
    }

    public GoogleProfile verify(String credential) {
        String token = Validation.trimmed(credential);
        if (token.isEmpty()) {
            throw ApiException.badRequest("Google credential is required.");
        }
        if (config.googleClientId().isEmpty()) {
            throw new ApiException(503, "Google sign-in is not configured on the server. Set GOOGLE_CLIENT_ID.");
        }
        String body = fetchTokenInfo(token);
        try {
            JsonNode info = json.readTree(body);
            if (!config.googleClientId().equals(text(info, "aud"))) {
                throw ApiException.unauthorized("This Google sign-in client is not configured for REC CampusConnect.");
            }
            String issuer = text(info, "iss");
            if (!"accounts.google.com".equals(issuer) && !"https://accounts.google.com".equals(issuer)) {
                throw ApiException.unauthorized("Google credential could not be verified.");
            }
            if (!"true".equalsIgnoreCase(text(info, "email_verified"))) {
                throw ApiException.unauthorized("Your Google email must be verified.");
            }
            String sub = text(info, "sub");
            String email = Validation.normalizeEmail(text(info, "email"));
            if (sub.isEmpty() || email.isEmpty()) {
                throw ApiException.unauthorized("Google did not return a valid account.");
            }
            String name = text(info, "name");
            return new GoogleProfile(sub, email, name.isEmpty() ? "Google Student" : name);
        } catch (JsonProcessingException ex) {
            throw ApiException.unauthorized("Google credential could not be verified.");
        }
    }

    private String fetchTokenInfo(String token) {
        URI uri = URI.create(TOKENINFO_URL + URLEncoder.encode(token, StandardCharsets.UTF_8));
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        try {
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw ApiException.unauthorized("Google credential could not be verified.");
            }
            return response.body();
        } catch (IOException ex) {
            throw new ApiException(502, "Could not reach Google to verify your sign-in. Please try again.");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ApiException(503, "Sign-in was interrupted. Please try again.");
        }
    }

    private static String text(JsonNode node, String field) {
        return node.path(field).asText("");
    }
}
