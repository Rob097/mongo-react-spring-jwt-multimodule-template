package com.authentication.jwt.runnable.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * @author Roberto97
 * Main class of the project.
 * We said to it to Scan all the packages that are under com.authentication.jwt
 * We said to it to Scan all the entities that are under com.authentication.jwt
 * We said to it to Enable all the mongo repositories that are under com.authentication.jwt
 */
@SpringBootApplication
@ComponentScan(basePackages = { "com.authentication.jwt.*" })
@EntityScan(basePackages = { "com.authentication.jwt.*" })
@EnableMongoRepositories(basePackages = { "com.authentication.jwt.*" })
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class);
	}

}
