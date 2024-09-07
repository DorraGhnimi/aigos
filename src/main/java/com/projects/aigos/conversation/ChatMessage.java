package com.projects.aigos.conversation;

import java.time.LocalDateTime;

public record ChatMessage(
        String messageText,
        String authorId,
        LocalDateTime messageTime

) {
}
