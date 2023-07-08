package com.example.hello_there;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaAuditing
@SpringBootApplication
@EntityScan("com.example.hello_there")
@EnableJpaRepositories("com.example.hello_there")
public class HelloThereApplication {
	public static void main(String[] args) {
		SpringApplication.run(HelloThereApplication.class, args);
	}
}
