package com.poc.chat.repository;

import com.poc.chat.model.Chat;
import com.poc.chat.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    List<Chat> findByConversationOrderByCreatedAtAsc(Conversation conversation);
}
