package com.ayd.parkcontrol.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Properties;

public class EnvironmentConfig implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Resource resource = new FileSystemResource(".env");
        if (resource.exists()) {
            try {
                Properties properties = new Properties();
                properties.load(resource.getInputStream());

                environment.getPropertySources().addFirst(
                        new PropertiesPropertySource("envFile", properties));

                System.out.println("Loaded .env file with " + properties.size() + " properties");

                String dbUrl = properties.getProperty("SPRING_DATASOURCE_URL");
                String dbUser = properties.getProperty("SPRING_DATASOURCE_USERNAME");
                String dbPass = properties.getProperty("SPRING_DATASOURCE_PASSWORD");

                System.out.println("Database URL: " + dbUrl);
                System.out.println("Database User: " + dbUser);
                System.out.println("Database Password: " + (dbPass == null || dbPass.isEmpty() ? "[EMPTY]" : "[SET]"));

            } catch (IOException e) {
                System.err.println("Error loading .env file: " + e.getMessage());
            }
        } else {
            System.out.println("No .env file found at: " + resource.getDescription());
        }
    }
}