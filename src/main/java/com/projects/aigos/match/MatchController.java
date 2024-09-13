package com.projects.aigos.match;

import com.projects.aigos.conversation.Conversation;
import com.projects.aigos.conversation.ConversationRepository;
import com.projects.aigos.profile.Profile;
import com.projects.aigos.profile.ProfileRepository;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
public class MatchController {

    private ProfileRepository profileRepository;
    private ConversationRepository conversationRepository;
    private MatchRepository matchRepository;

    public MatchController(ProfileRepository profileRepository, ConversationRepository conversationRepository, MatchRepository matchRepository) {
        this.profileRepository = profileRepository;
        this.conversationRepository = conversationRepository;
        this.matchRepository = matchRepository;
    }

    public record MatchRequest(String profileId){};

    @PostMapping("/matches")
    public Match createNewMatch(@RequestBody MatchRequest request) {
        Profile profile = profileRepository.findById(request.profileId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found for id " + request.profileId()));

        Optional<Match> ExistingMatchOptional = matchRepository.findByProfile(profile);
        if (!ExistingMatchOptional.isEmpty()) {
            return ExistingMatchOptional.get();
        }

        Conversation conversation = new Conversation(
                UUID.randomUUID().toString(),
                request.profileId(),
                new ArrayList<>()
        );
        conversationRepository.save(conversation);
        Match match  = new Match(UUID.randomUUID().toString(), profile, conversation.id());
        return matchRepository.save(match);
    }


    @GetMapping("/matches")
    public List<Match> getAllMatch() {
        return matchRepository.findAll();
    }
}
