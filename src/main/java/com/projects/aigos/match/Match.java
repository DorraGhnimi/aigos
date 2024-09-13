package com.projects.aigos.match;

import com.projects.aigos.profile.Profile;

public record Match (
        String id,
        Profile profile,
        String conversationId
){
}
