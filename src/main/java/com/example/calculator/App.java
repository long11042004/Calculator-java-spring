package com.example.calculator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.calculator")
@EntityScan(basePackages = "com.example.calculator")
public class App {

	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}

}
