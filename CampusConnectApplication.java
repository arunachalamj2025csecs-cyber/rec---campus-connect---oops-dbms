package edu.rec.campusconnect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Starting point of the backend. Running main() starts the web server on PORT (default 8080)
 * and connects to MongoDB using MONGODB_URI.
 */
@SpringBootApplication
public class CampusConnectApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusConnectApplication.class, args);
    }
}
