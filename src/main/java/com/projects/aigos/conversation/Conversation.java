package com.projects.aigos.conversation;

import com.projects.aigos.profile.Profile;

import java.util.List;

public record Conversation(
        String id,
        Profile profile,
        List<ChatMessage> messages
        ) {
}
