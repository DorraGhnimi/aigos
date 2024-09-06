package com.projects.aigos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;

@SpringBootApplication(exclude={MongoAutoConfiguration.class})
public class AigosApplication {

	public static void main(String[] args) {
		SpringApplication.run(AigosApplication.class, args);
	}

}
