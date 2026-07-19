package com.venkat.meditrack.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

// Swagger UI is served at /swagger-ui.html; the raw spec at /v3/api-docs.
// Declaring the tags here fixes their order and descriptions in the UI; the
// matching @Tag names on the controllers attach each endpoint to its group.
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI meditrackOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MediTrack API")
                        .description("Clinic appointment & billing API")
                        .version("v1"))
                .tags(List.of(
                        new Tag().name("Specialty Management").description("Medical specialties catalog"),
                        new Tag().name("Doctor Management").description("Doctors and their availability"),
                        new Tag().name("Patient Management").description("Patient registration and lookup"),
                        new Tag().name("Appointment Management").description("Booking and cancelling appointments")
                ));
    }
}
