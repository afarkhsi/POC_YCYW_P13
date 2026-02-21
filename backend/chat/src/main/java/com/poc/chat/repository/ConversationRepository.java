package com.poc.chat.repository;


import com.poc.chat.model.Conversation;
import com.poc.chat.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    Optional<Conversation> findByCustomer(User customer);
    Optional<Conversation> findTopByCustomerOrderByCreatedAtDesc(User customer);
    List<Conversation> findByAgent(User agent);
    List<Conversation> findByAgentIsNull();
}
