package com.projects.aigos.profile;

public record Profile(
        String id,
        String firstname,
        String lastname,
        int age,
        String ethnicity,
        String religion,
        Gender gender,
        String bio,
        String imageUrl,
        String profession,
        String mtbi
) {
}
