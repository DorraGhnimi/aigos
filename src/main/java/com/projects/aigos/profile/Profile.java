package com.projects.aigos.profile;

public record Profile(
        String id,
        String firstname,
        String lastName,
        int age,
        String ethnicity,
        Gender gender,
        String bio,
        String imageUrl,
        String mtbi
) {
}
