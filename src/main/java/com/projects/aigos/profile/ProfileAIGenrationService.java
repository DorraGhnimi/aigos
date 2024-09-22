package com.projects.aigos.profile;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.projects.aigos.conversation.ConversationRepository;
import com.projects.aigos.match.MatchRepository;

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
import java.util.Map;
import java.util.function.Function;

@Configuration
public class ProfileAIGenrationService {

    private final String PROFILES_FILE_PATH = "profiles.json";

    // define ranges for profiles properties to be generated
    private final List<Gender> genders = new ArrayList<>(List.of(Gender.Male, Gender.Female));
    private final List<String> ethnicities = new ArrayList<>(List.of("Arab", "Black", "Amazigh", "Asian", "White", "Hispanic", "Indian", "Pacific Islander", "American Indian"));
    private final List<String> religions = new ArrayList<>(List.of("Muslim", "Christian", "Jew", "Atheist", "Agnostic"));
    private final List<String> mbtiTypes = new ArrayList<>(List.of("ENTJ", "ENFJ", "ESFJ", "ESTJ", "ENTP", "ENFP", "ESFP", "ESTP", "INTJ", "INFJ", "ISFJ", "ISTJ", "INTP", "INFP", "ISFP", "ISTP"));

    private final OllamaChatModel chatModel;
    private final ImageGenerationService imageGenerationService;
    private final ProfileRepository profileRepository;
    private final ConversationRepository conversationRepository;
    private final MatchRepository matchRepository;

    private final List<Profile> generatedProfiles = new ArrayList<>();
    @Value("${start-up.actions.doGenerateProfiles}")
    private boolean doGenerateProfiles;
    @Value("${start-up.actions.nbProfiles}")
    private int nbProfiles;

    @Value("#{${tinderai.character.user}}")
    private Map<String, String> userProfileProperties;

    public ProfileAIGenrationService(OllamaChatModel chatClient, ImageGenerationService imageGenerationService, ProfileRepository profileRepository, ConversationRepository conversationRepository, MatchRepository matchRepository) {
        this.chatModel = chatClient;
        this.imageGenerationService = imageGenerationService;
        this.profileRepository = profileRepository;
        this.conversationRepository = conversationRepository;
        this.matchRepository = matchRepository;
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
            Collections.shuffle(mbtiTypes);

            String promptString = STR."Create a tinder like profile persona for a person with the following properties:age = \{(int) (Math.random() * (70 - 20 + 1) + 20)}, gender = \{genders.getFirst().toString()}, ethnicity = \{ethnicities.getFirst()}, religion = \{religions.getFirst()}, mbti type= \{mbtiTypes.getFirst()}, including first name and last name, profession and a bio. Execute the addGeneratedProfileToList function";
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
    }

    public void saveProfilesToDB() {
        profileRepository.deleteAll();
        matchRepository.deleteAll();
        conversationRepository.deleteAll();
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


        System.out.println("userProfileProperties=======" + userProfileProperties);

        Profile userProfile = new Profile(
                userProfileProperties.get("id"),
                userProfileProperties.get("firstName"),
                userProfileProperties.get("lastname"),
                Integer.parseInt(userProfileProperties.get("age")),
                userProfileProperties.get("ethnicity"),
                userProfileProperties.get("religion"),
                Gender.valueOf(userProfileProperties.get("gender")),
                userProfileProperties.get("bio"),
                userProfileProperties.get("imageUrl"),
                userProfileProperties.get("profession"),
                userProfileProperties.get("myersBriggsPersonalityType")
        );
        profileRepository.save(userProfile);
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
