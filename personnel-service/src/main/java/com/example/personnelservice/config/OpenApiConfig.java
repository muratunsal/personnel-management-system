package com.example.personnelservice.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI personnelServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Personnel Service API")
                        .description("People, departments, meetings and tasks endpoints")
                )
                .externalDocs(new ExternalDocumentation().description("Project Repo"));
    }
}


