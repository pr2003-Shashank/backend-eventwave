package com.emp.eventmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.eventwave.model")
@EnableJpaRepositories("com.eventwave.repository")
@ComponentScan(basePackages = {
		"com.emp.eventmanagement",         // your main app package
		"com.eventwave.config",            // for SecurityConfig
		"com.eventwave.controller",        // if your controllers are here
		"com.eventwave.service",           // if your services are here
		"com.eventwave.repository"         // if you use any additional repositories
	})

public class EventWaveApplication {

	public static void main(String[] args) {
		SpringApplication.run(EventWaveApplication.class, args);
	}

}
