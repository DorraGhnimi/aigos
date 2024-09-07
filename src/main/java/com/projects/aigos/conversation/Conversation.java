package com.projects.aigos.conversation;

import com.projects.aigos.profile.Profile;

import java.util.List;

public record Conversation(
        String id,
        String profileId,
        List<ChatMessage> messages
        ) {
}
