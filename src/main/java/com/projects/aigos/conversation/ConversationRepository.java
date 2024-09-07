package com.projects.aigos.conversation;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ConversationRepository extends MongoRepository<Conversation, String> {
}
