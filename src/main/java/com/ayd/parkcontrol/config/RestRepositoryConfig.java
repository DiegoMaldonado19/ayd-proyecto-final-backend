package com.ayd.parkcontrol.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

/**
 * Configuration for Spring Data REST
 * Disables automatic REST endpoints to avoid conflicts with custom controllers
 */
@Configuration
public class RestRepositoryConfig implements RepositoryRestConfigurer {

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors) {
        // Disable Spring Data REST automatic endpoint exposure
        // This prevents conflicts with Swagger and custom controllers
        config.setBasePath("/api/data-rest");
        config.setReturnBodyOnCreate(true);
        config.setReturnBodyOnUpdate(true);
    }
}
