package com.projects.aigos.profile;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@Configuration
public class ProfileAIGenrationService {

    private static final String PROFILES_FILE_PATH = "profiles.json";

    private final OllamaChatModel chatModel;
    private final List<Profile> generatedProfiles = new ArrayList<>();
    @Value("${start-up.actions.doGenerateProfiles}")
    private boolean doGenerateProfiles;
    @Value("${start-up.actions.nbProfiles}")
    private int nbProfiles;

    public ProfileAIGenrationService(OllamaChatModel chatClient) {
        this.chatModel = chatClient;
    }
    // define ranges for profiles properties to be generated
    private final List<Integer> ages = new ArrayList<>(List.of(20, 23, 25, 28, 30, 33, 35, 40, 45));
    private final List<Gender> genders = new ArrayList<>(List.of(Gender.Male, Gender.Female));
    private final List<String> ethnicities = new ArrayList<>(List.of("Arab", "Black", "Amazigh", "Asian", "White", "Hispanic"));


    public void generateProfiles() {
        if(!doGenerateProfiles) {
            return;
        }
        System.out.println("Generating " + nbProfiles + " profiles...");
        for(int i = 0; i < nbProfiles; i++) {
            // for random combinations:
            Collections.shuffle(ages);
            Collections.shuffle(genders);
            Collections.shuffle(ethnicities);

            String promptString = "create a tinder like profile persona for a person with the following properties:" +
                    "age = " +  (int)(Math.random()*(70-20+1)+20) +
                    ", gender = " + genders.getFirst().toString() +
                    ", ethnicity = " +  ethnicities.getFirst() +
                    " including first name and last name, mbti type and a bio. Execute the saveProfile function";
            // make a call to AI to generate sample profile
            ChatResponse profileResponse =  chatModel.call(
                    new Prompt(
                            promptString,
                            OllamaOptions.builder().withFunction("saveProfile").build() // this is function to be called after the call to AI, so make the response coherent with it
                    )
            );
        }
        // save profile in a json file
        saveProfilesToJsonFile(generatedProfiles);
        // open resulted json file and save content in DB

    }

    private void saveProfilesToJsonFile(List<Profile> generatedProfiles) {
        try {
            // keep existing profiles
            Gson gson = new Gson();
            List<Profile> existingProfiles = gson.fromJson(
                    new FileReader(PROFILES_FILE_PATH),
                    new TypeToken<ArrayList<Profile>>() {}.getType()
            );
            if (existingProfiles != null) {
                generatedProfiles.addAll(existingProfiles);
            }

            // add all profiles to the file
            String jsonString = new Gson().toJson(generatedProfiles);
            FileWriter fileWriter = new FileWriter(PROFILES_FILE_PATH);
            fileWriter.write(jsonString);
            fileWriter.close();
            System.out.println("Done generating profiles");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    @Description("save profile")
    public Function<Profile, Boolean> saveProfile() {
        return (profile) -> {
            System.out.println("^saveProfile^ function that is being called! (by spring ai)");
            System.out.println("generated profile: "+ profile);
            this.generatedProfiles.add(profile);
            return true;
        };
    }
}
