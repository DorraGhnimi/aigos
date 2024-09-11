package com.projects.aigos.profile;

import static com.projects.aigos.Utils.selfieTypes;

import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ImageGenerationService {
    private final String STABLE_DIFUSION_URL = "http://127.0.0.1:7860/sdapi/v1/txt2img";
    private HttpClient httpClient;
    private HttpRequest.Builder stableDifussionRequestBuilder;

    public ImageGenerationService() {
        this.httpClient = HttpClient.newHttpClient();
        this.stableDifussionRequestBuilder = HttpRequest.newBuilder()
                .setHeader("Content-type", "application/json")
                .uri(URI.create(STABLE_DIFUSION_URL));
    }

    public Profile generateImage(Profile profile) {
        String uuid = UUID.randomUUID().toString();
        profile = new Profile(
                uuid,
                profile.firstname(),
                profile.lastName(),
                profile.age(),
                profile.ethnicity(),
                profile.religion(),
                profile.gender(),
                profile.bio(),
                uuid + ".jpg",
                profile.mtbi(),
                profile.profession()
        );
        try {
            // make request to stable diffusion
            System.out.println(STR."Creating image for \{profile.firstname()} \{profile.lastName()}(\{profile.ethnicity()})");
            HttpRequest request = buildRequest(profile);
            HttpResponse response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            // Save the generated image in the resources folder
            saveImageinFile(response, profile);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        return profile;
    }

    private HttpRequest buildRequest(Profile profile) {
        String randomSelfieType = getRandomElement(selfieTypes());
        // build request from profile
        String prompt = STR."Selfie of a \{profile.age()} year old \{profile.mtbi()} \{profile.ethnicity()} \{profile.religion()} \{profile.gender()}, \{randomSelfieType}, photorealistic skin texture and details, individual hairs and pores visible, highly detailed, photorealistic, hyperrealistic, subsurface scattering, 4k DSLR, ultrarealistic, best quality, masterpiece. Bio- \{profile.bio()}";
        String negativePrompt = "multiple faces, lowres, text, error, cropped, worst quality, low quality, jpeg artifacts, ugly, duplicate, morbid, mutilated, out of frame, extra fingers, mutated hands, poorly drawn hands, poorly drawn face, mutation, deformed, blurry, dehydrated, bad anatomy, bad proportions, extra limbs, cloned face, disfigured, gross proportions, malformed limbs, missing arms, missing legs, extra arms, extra legs, fused fingers, too many fingers, long neck, username, watermark, signature";
        String jsonString = STR."""
        { "prompt": "\{prompt}", "negative_prompt": "\{negativePrompt}", "steps":40 }
        """;
        return stableDifussionRequestBuilder.POST(
                    HttpRequest.BodyPublishers.ofString(
                            jsonString
                    )
                ).build();
    }


    private void saveImageinFile(HttpResponse response, Profile profile) {
        record ImageResponse(List<String> images) {}
        Gson gson = new Gson();
        ImageResponse imageResponse = gson.fromJson(response.body().toString(), ImageResponse.class);
        if (imageResponse.images() != null && !imageResponse.images().isEmpty()) {
            String base64Image = imageResponse.images().getFirst();

            // Decode Base64 to binary
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
            String directoryPath = "src/main/resources/static/images/";
            String filePath = directoryPath + profile.imageUrl();
            Path directory = Paths.get(directoryPath);
            if (!Files.exists(directory)) {
                try {
                    Files.createDirectories(directory);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            // Save the image to a file
            try (FileOutputStream imageOutFile = new FileOutputStream(filePath)) {
                imageOutFile.write(imageBytes);
            } catch (IOException e) {
                System.out.println("Error saving generated image in a file!");
            }
        }
    }

    private static <T> T getRandomElement(List<T> list) {
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }
}
