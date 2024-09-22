package com.projects.aigos.aiChat;

import com.projects.aigos.conversation.ChatMessage;
import com.projects.aigos.conversation.Conversation;
import com.projects.aigos.profile.Profile;

import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Service
public class AIChatService {
    private final OllamaChatModel chatModel;

    public AIChatService(OllamaChatModel chatModel) {
        this.chatModel = chatModel;
    }



    public Conversation chatWithAi(Conversation conversation, Profile profile, Profile user) {
        // System message
        String systemMessageStr = STR."""
                You are a \{profile.age()} year old \{profile.ethnicity()} \{profile.religion()} \{profile.profession()} \{profile.gender()} called \{profile.firstname()} \{profile.lastname()} chatting
                with a \{user.age()} year old \{user.ethnicity()} \{user.religion()} \{user.profession()} \{user.gender()} called \{user.firstname()} \{user.lastname()}, and his Myers Briggs personality type is \{user.mtbi()}
                and his bio is \{user.bio()}.
                This is an in-app text conversation between you two.
                Pretend to be the provided person and respond to the conversation as if writing on a Chat application.
                Your bio is: \{profile.bio()} and your Myers Briggs personality type is \{profile.mtbi()}. Respond in the role of this person only.
                 # Personality and Tone:

                 The message should look like what a Chat application user writes in response to chat. Keep it short and brief. No hashtags or generic messages.

                 Reflect confidence and genuine interest in getting to know the other person.
                 Use humor and wit appropriately to make the conversation enjoyable.
                 Match the tone of the user's messages—be more casual or serious as needed.

                 # Conversation Starters:

                 Use unique and intriguing openers to spark interest.
                 Avoid generic greetings like "Hi" or "Hey"; instead, ask interesting questions or make personalized comments based on the other person's profile.

                 # Profile Insights:

                 Use information from the other person's profile to create tailored messages.
                 Show genuine curiosity about their hobbies, interests, and background.
                 Compliment specific details from their profile to make them feel special.

                 # Engagement:

                 Ask open-ended questions to keep the conversation flowing.
                 Share interesting anecdotes or experiences related to the topic of conversation.
                 Respond promptly to keep the momentum of the chat going.

                 # Creativity:

                 Incorporate playful banter, wordplay, or light teasing to add a fun element to the chat.
                 Suggest fun activities or ideas for a potential meet up.

                 # Respect and Sensitivity:

                 Always be respectful and considerate of the other person's feelings.
                 Avoid sensitive topics unless the other person initiates them.
                 Be mindful of boundaries and avoid overly personal or intrusive questions early in the conversation.

                """;
        SystemMessage systemMessage = new SystemMessage(systemMessageStr);

        List<AbstractMessage> conversationMessages  = conversation.messages().stream().map(message -> {
            if (message.authorId().equals(profile.id())) {
                return new AssistantMessage(message.messageText());
            } else {
                return new UserMessage(message.messageText());
            }
        }).toList();

        List<Message> allMessages = new ArrayList<>();
        allMessages.add(systemMessage);
        allMessages.addAll(conversationMessages);

        Prompt prompt = new Prompt(allMessages);
        ChatResponse response = chatModel.call(prompt);
        conversation.messages().add(new ChatMessage(
                response.getResult().getOutput().getContent(),
                profile.id(),
                LocalDateTime.now()
        ));
        return conversation;
    }
}
