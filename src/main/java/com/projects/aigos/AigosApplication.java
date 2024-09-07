package com.projects.aigos;

import com.projects.aigos.conversation.ChatMessage;
import com.projects.aigos.conversation.Conversation;
import com.projects.aigos.conversation.ConversationRepository;
import com.projects.aigos.profile.Gender;
import com.projects.aigos.profile.Profile;
import com.projects.aigos.profile.ProfileRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDateTime;
import java.util.Arrays;

@SpringBootApplication
public class AigosApplication implements CommandLineRunner {

	@Autowired private ProfileRepository profileRepository;
    @Autowired private ConversationRepository conversationRepository;

	public static void main(String[] args) {
		SpringApplication.run(AigosApplication.class, args);
	}

	public void run(String ... args) {
		profileRepository.deleteAll();
		conversationRepository.deleteAll();

		Profile naruto =  new Profile("1", "Naruto", "Usumaki", 18, "Japneese", Gender.Male, "Shinubi", "naruto.jpg", "ESFP");
		profileRepository.save(naruto);
		Profile sasuke =  new Profile("2", "Sasuke", "Uchiha", 18, "Japneese", Gender.Male, "Shinubi", "sasuke.jpg", "ISTJ");
		profileRepository.save(sasuke);
		profileRepository.findAll().forEach(System.out::println);

		Conversation conversation = new Conversation(
				"1",
				naruto.id(),
				Arrays.asList(
						new ChatMessage("Hello",  naruto.id(), LocalDateTime.now())
				)
		);
		conversationRepository.save(conversation);
		conversationRepository.findAll().forEach(System.out::println);
	}
}
