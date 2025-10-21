package com.ayd.parkcontrol.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(getApiInfo())
                .servers(getServers())
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT token for authentication. Format: Bearer {token}")));
    }

    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("00-all-endpoints")
                .pathsToMatch("/**")
                .build();
    }

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("01-authentication")
                .pathsToMatch("/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("02-administration")
                .pathsToMatch("/rates/**", "/branches/**", "/subscription-plans/**")
                .build();
    }

    @Bean
    public GroupedOpenApi subscriptionApi() {
        return GroupedOpenApi.builder()
                .group("03-subscriptions")
                .pathsToMatch("/subscriptions/**")
                .build();
    }

    @Bean
    public GroupedOpenApi operationsApi() {
        return GroupedOpenApi.builder()
                .group("04-operations")
                .pathsToMatch("/tickets/**", "/occupancy/**")
                .build();
    }

    @Bean
    public GroupedOpenApi commerceApi() {
        return GroupedOpenApi.builder()
                .group("05-commerce")
                .pathsToMatch("/commerce/**", "/settlements/**")
                .build();
    }

    @Bean
    public GroupedOpenApi validationApi() {
        return GroupedOpenApi.builder()
                .group("06-validation")
                .pathsToMatch("/plate-changes/**", "/temporal-permits/**")
                .build();
    }

    @Bean
    public GroupedOpenApi incidentsApi() {
        return GroupedOpenApi.builder()
                .group("07-incidents")
                .pathsToMatch("/incidents/**")
                .build();
    }

    @Bean
    public GroupedOpenApi fleetsApi() {
        return GroupedOpenApi.builder()
                .group("08-fleets")
                .pathsToMatch("/fleets/**")
                .build();
    }

    @Bean
    public GroupedOpenApi reportsApi() {
        return GroupedOpenApi.builder()
                .group("09-reports")
                .pathsToMatch("/reports/**")
                .build();
    }

    private Info getApiInfo() {
        return new Info()
                .title("ParkControl S.A. API")
                .description("RESTful API for comprehensive parking management system including user management, " +
                        "subscription plans, vehicle entry/exit tracking, commerce affiliations, and fleet management.\n\n"
                        +
                        "**Base URL Structure:**\n" +
                        "- Authentication endpoints: `/api/v1/auth/**`\n" +
                        "- Administration endpoints: `/api/v1/rates/**`, `/api/v1/branches/**`, `/api/v1/subscription-plans/**`\n"
                        +
                        "- Subscription endpoints: `/api/v1/subscriptions/**`\n" +
                        "- Operations endpoints: `/api/v1/tickets/**`, `/api/v1/occupancy/**`\n" +
                        "- Commerce endpoints: `/api/v1/commerce/**`, `/api/v1/settlements/**`\n" +
                        "- Validation endpoints: `/api/v1/plate-changes/**`, `/api/v1/temporal-permits/**`\n" +
                        "- Incident endpoints: `/api/v1/incidents/**`\n" +
                        "- Fleet endpoints: `/api/v1/fleets/**`\n" +
                        "- Report endpoints: `/api/v1/reports/**`")
                .version("1.0.0")
                .contact(new Contact()
                        .name("Diego Maldonado")
                        .email("dmaldonado1920@gmail.com")
                        .url("https://github.com/djmaldonado19"))
                .license(new License()
                        .name("Academic Project License")
                        .url("https://www.example.com/license"));
    }

    private List<Server> getServers() {
        return List.of(
                new Server()
                        .url("http://localhost:8080/api/v1")
                        .description("Development Server (Local)"),
                new Server()
                        .url("http://172.190.44.206:8080/api/v1")
                        .description("Production Server (Azure HTTP)"));
    }
}