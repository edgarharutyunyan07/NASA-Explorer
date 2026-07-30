package com.tumo.finalproject;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The entry point of the application. This file is complete — you do not need to change it.
 *
 * <p>{@code @SpringBootApplication} tells Spring to scan this package and every
 * package below it ({@code controller}, {@code service}, {@code repository},
 * {@code model}) for classes it should manage, then start an embedded web server
 * on the port set in {@code application.properties}.
 *
 * <p>Run it with {@code ./mvnw spring-boot:run} and open http://localhost:8080
 */
@SpringBootApplication
public class TumoFinalProjectApplication {

    public static void main(String[] args) {
        // Load .env file before Spring starts
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        // Set environment variables so Spring can read them
        dotenv.entries().forEach(entry -> {
            if (System.getenv(entry.getKey()) == null) {
                System.setProperty(entry.getKey(), entry.getValue());
            }
        });

        SpringApplication.run(TumoFinalProjectApplication.class, args);
    }
}
