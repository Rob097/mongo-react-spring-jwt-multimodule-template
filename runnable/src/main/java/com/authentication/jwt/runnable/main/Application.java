package com.authentication.jwt.runnable.main;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.authentication.jwt.*"})
@EntityScan(basePackages = {"com.authentication.jwt.*"})
@EnableMongoRepositories(basePackages = {"com.authentication.jwt.*"})
public class Application {
	
	public static void main (String[] args) {
		SpringApplication.run(Application.class);
	}

}
