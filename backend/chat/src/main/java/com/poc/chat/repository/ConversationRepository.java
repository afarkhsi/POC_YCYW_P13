package com.poc.chat.repository;

import com.poc.chat.model.Conversation;
import com.poc.chat.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    // cherche la conversation OPEN la plus récente d'un client
    Optional<Conversation> findTopByCustomerAndStatusOrderByCreatedAtDesc(User customer, String status);

    List<Conversation> findByAgentAndStatus(User agent, String status);

    List<Conversation> findByAgentIsNullAndStatus(String status);
}
