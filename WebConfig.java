package edu.rec.campusconnect.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS: lets the Vercel-hosted frontend (a different website) call this API.
 * Only the origins listed in CORS_ALLOWED_ORIGINS are allowed.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AppConfig config;

    public WebConfig(AppConfig config) {
        this.config = config;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (config.allowedOrigins().isEmpty()) {
            return;
        }
        registry.addMapping("/api/**")
                .allowedOrigins(config.allowedOrigins().toArray(new String[0]))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Content-Type", "Authorization")
                .maxAge(3600);
    }
}
