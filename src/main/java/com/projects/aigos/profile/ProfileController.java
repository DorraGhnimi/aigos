package com.projects.aigos.profile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProfileController {

    @Autowired private ProfileRepository profileRepository;

    @GetMapping("/profiles/random")
    public Profile getRandomProfile() {
        return profileRepository.getRandomProfile();
    }


    @GetMapping("/profiles")
    public List<Profile> getAllProfile() {
        return profileRepository.findAll();
    }
}
