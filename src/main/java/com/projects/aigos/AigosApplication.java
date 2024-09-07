package com.projects.aigos;

import com.projects.aigos.profile.Gender;
import com.projects.aigos.profile.Profile;
import com.projects.aigos.profile.ProfileRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AigosApplication implements CommandLineRunner {

	@Autowired private ProfileRepository profileRepository;

	public static void main(String[] args) {
		SpringApplication.run(AigosApplication.class, args);
	}

	public void run(String ... args) {
		Profile profile =  new Profile("id", "Naruto", "Usumaki", 18, "Japneese", Gender.Male, "Shinubi", "naruto.jpg", "ESFP");
		profileRepository.save(profile);
		profileRepository.findAll().forEach(System.out::println);
	}
}
