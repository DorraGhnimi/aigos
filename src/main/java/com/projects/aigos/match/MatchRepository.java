package com.projects.aigos.match;

import com.projects.aigos.profile.Profile;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MatchRepository extends MongoRepository<Match, String> {
    Optional<Match> findByProfile(Profile profile);
}
