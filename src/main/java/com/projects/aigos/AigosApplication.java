package com.projects.aigos;

import com.projects.aigos.profile.ProfileAIGenrationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AigosApplication implements CommandLineRunner {

	@Autowired private ProfileAIGenrationService profileAIGenrationService;
    public static void main(String[] args) {
		SpringApplication.run(AigosApplication.class, args);
	}


	@Override
	public void run(String... args) throws Exception {
		profileAIGenrationService.generateProfiles();
	}
}
