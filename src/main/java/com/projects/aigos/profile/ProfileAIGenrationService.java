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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@Configuration
public class ProfileAIGenrationService {

    private final String PROFILES_FILE_PATH = "profiles.json";

    // define ranges for profiles properties to be generated
    private final List<Gender> genders = new ArrayList<>(List.of(Gender.Male, Gender.Female));
    private final List<String> ethnicities = new ArrayList<>(List.of("Arab", "Black", "Amazigh", "Asian", "White", "Hispanic", "Indian", "Pacific Islander", "American Indian"));
    private final List<String> religions = new ArrayList<>(List.of("Muslim", "Christian", "Jew", "Atheist", "Agnostic"));


    private final OllamaChatModel chatModel;
    private final ImageGenerationService imageGenerationService;
    private final ProfileRepository profileRepository;

    private final List<Profile> generatedProfiles = new ArrayList<>();
    @Value("${start-up.actions.doGenerateProfiles}")
    private boolean doGenerateProfiles;
    @Value("${start-up.actions.nbProfiles}")
    private int nbProfiles;

    public ProfileAIGenrationService(OllamaChatModel chatClient, ImageGenerationService imageGenerationService, ProfileRepository profileRepository) {
        this.chatModel = chatClient;
        this.imageGenerationService = imageGenerationService;
        this.profileRepository = profileRepository;
    }

    public void generateProfiles() {
        if (!doGenerateProfiles) {
            return;
        }
        for (int i = 0; i < nbProfiles; i++) {
            System.out.println("Generating " + (i+1) + "/" + nbProfiles);

            // for random combinations:
            Collections.shuffle(genders);
            Collections.shuffle(ethnicities);
            Collections.shuffle(religions);

            String promptString = STR."Create a tinder like profile persona for a person with the following properties:age = \{(int) (Math.random() * (70 - 20 + 1) + 20)}, gender = \{genders.getFirst().toString()}, ethnicity = \{ethnicities.getFirst()}, religion = \{religions.getFirst()} including first name and last name, mbti type, profession and a bio. Execute the addGeneratedProfileToList function";
            // make a call to AI to generate sample profile
            ChatResponse profileResponse = chatModel.call(
                    new Prompt(
                            promptString,
                            OllamaOptions.builder().withFunction("addGeneratedProfileToList").build() // this is function to be called after the call to AI, so make the response coherent with it
                    )
            );
        }
        // generate images for new generated profiles
        List<Profile> newGeneratedProfilesWithImages = new ArrayList<>();
        for (Profile profile : generatedProfiles) {
            Profile profileWithImage = imageGenerationService.generateImage(profile);
            newGeneratedProfilesWithImages.add(profileWithImage);
        }
        // save profile in a json file
        saveProfilesToJsonFile(newGeneratedProfilesWithImages);
        // open resulted json file and save content in DB
        saveProfilesToDB();

    }

    private void saveProfilesToDB() {
        profileRepository.deleteAll();
        Gson gson = new Gson();
        try {
            List<Profile> profiles = gson.fromJson(
                    new FileReader(PROFILES_FILE_PATH),
                    new TypeToken<ArrayList<Profile>>() {
                    }.getType()
            );
            profileRepository.saveAll(profiles);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    @Description("Save a profile, a function that will be called by spring AI")
    public Function<Profile, Boolean> addGeneratedProfileToList() {
        return (profile) -> {
            System.out.println(profile);
            this.generatedProfiles.add(profile);
            return true;
        };
    }

    private void saveProfilesToJsonFile(List<Profile> newGeneratedProfiles) {
        try {
            // keep existing profiles
            Gson gson = new Gson();
            List<Profile> existingProfiles = gson.fromJson(
                    new FileReader(PROFILES_FILE_PATH),
                    new TypeToken<ArrayList<Profile>>() {
                    }.getType()
            );
            if (existingProfiles != null) {
                newGeneratedProfiles.addAll(existingProfiles);
            }

            // add all profiles to the file
            writeProfilesListToFile(newGeneratedProfiles);
            System.out.println("Done generating profiles");
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeProfilesListToFile(List<Profile> newGeneratedProfilesWithImages) throws IOException {
        String jsonString = new Gson().toJson(newGeneratedProfilesWithImages);
        FileWriter fileWriter = new FileWriter(PROFILES_FILE_PATH);
        fileWriter.write(jsonString);
        fileWriter.close();
    }
}
