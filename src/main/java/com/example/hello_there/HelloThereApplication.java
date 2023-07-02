package com.example.hello_there;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class HelloThereApplication {
	public static void main(String[] args) {
		SpringApplication.run(HelloThereApplication.class, args);
	}
}
