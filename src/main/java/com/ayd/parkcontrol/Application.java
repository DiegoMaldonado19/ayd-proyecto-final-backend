package com.ayd.parkcontrol;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableJpaRepositories(basePackages = "com.ayd.parkcontrol.infrastructure.persistence.repository")
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
