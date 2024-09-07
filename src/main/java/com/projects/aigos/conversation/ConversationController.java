package com.projects.aigos.conversation;

import com.projects.aigos.profile.ProfileRepository;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

@RestController
public class ConversationController {

    private ConversationRepository conversationRepository;
    private ProfileRepository profileRepository;

    public ConversationController(ConversationRepository conversationRepository, ProfileRepository profileRepository) {
        this.conversationRepository = conversationRepository;
        this.profileRepository = profileRepository;
    }

    @PostMapping("/conversations")
    public Conversation createNewConversation(@RequestBody ConversationRequest request) {
        profileRepository.findById(request.profileId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found for id " + request.profileId()));

        Conversation conversation = new Conversation(
                UUID.randomUUID().toString(),
                request.profileId(),
                new ArrayList<>()
        );
        return conversationRepository.save(conversation);
    }

    @PostMapping("/conversations/{conversationId}")
    public Conversation addMessageToConversation(@PathVariable String conversationId, @RequestBody ChatMessage message){
        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found for id " + conversationId));
        profileRepository.findById(message.authorId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found for author of the message " + message.authorId()));
        ChatMessage validChatMessage = new ChatMessage(
                message.messageText(),
                message.authorId(),
                LocalDateTime.now()
        );
        conversation.messages().add(validChatMessage);
        conversationRepository.save(conversation);
    }

    public record ConversationRequest(String profileId) {};
}
