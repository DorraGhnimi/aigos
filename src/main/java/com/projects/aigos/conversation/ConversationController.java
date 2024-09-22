package com.projects.aigos.conversation;

import com.projects.aigos.aiChat.AIChatService;
import com.projects.aigos.profile.Profile;
import com.projects.aigos.profile.ProfileRepository;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@RestController
public class ConversationController {

    private ConversationRepository conversationRepository;
    private ProfileRepository profileRepository;
    private AIChatService aiChatService;

    public ConversationController(ConversationRepository conversationRepository, ProfileRepository profileRepository, AIChatService aiChatService) {
        this.conversationRepository = conversationRepository;
        this.profileRepository = profileRepository;
        this.aiChatService = aiChatService;
    }

    @GetMapping("/conversations/{conversationId}")
    public Conversation fetchConversation(@PathVariable String conversationId){
        return conversationRepository.findById(conversationId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found for id " + conversationId));
    }

    @PostMapping("/conversations/{conversationId}")
    public Conversation addMessageToConversation(@PathVariable String conversationId, @RequestBody(required = true) ChatMessage message){
        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found for id " + conversationId));
        profileRepository.findById(message.authorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found for author of the message " + message.authorId()));

        if(!message.authorId().equals(conversation.profileId()) && !message.authorId().equals("user")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "message author is incompatible with requested conversations profiles");
        }
        ChatMessage validChatMessage = new ChatMessage(
                message.messageText(),
                message.authorId(),
                LocalDateTime.now()
        );
        conversation.messages().add(validChatMessage);

        String matchProfileId = conversation.profileId();

        Profile profile = profileRepository.findById(matchProfileId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Unable to find a profile with ID " + matchProfileId
                ));
        Profile user = profileRepository.findById(validChatMessage.authorId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Unable to find a profile with ID " + validChatMessage.authorId()
                ));

        aiChatService.chatWithAi(conversation, profile, user);
        return conversationRepository.save(conversation);
    }

    public record ConversationRequest(String profileId) {};
}
