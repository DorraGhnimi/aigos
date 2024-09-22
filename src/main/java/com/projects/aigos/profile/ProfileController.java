package com.projects.aigos.profile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class ProfileController {

    @Autowired private ProfileRepository profileRepository;

    @GetMapping("/profiles/random")
    public Profile getRandomProfile() {
        Profile profile;
        do {
            profile = profileRepository.getRandomProfile();
        } while ("user".equals(profile.id()));
        return profile;
    }

    @GetMapping("/profiles/{profileId}")
    public Profile getRandomProfile(@PathVariable String profileId) {
        return profileRepository.findById(profileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found for id " + profileId));
    }

    @GetMapping("/profiles")
    public List<Profile> getAllProfile() {
        return profileRepository.findAll();
    }
}
