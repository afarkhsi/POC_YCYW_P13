package com.poc.chat.service;


import com.poc.chat.dto.MessageDTO;
import com.poc.chat.model.*;
import com.poc.chat.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;

    public Conversation getOrCreateConversation(Long customerId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return conversationRepository.findByCustomer(customer)
                .orElseGet(() -> conversationRepository.save(new Conversation(null, customer, null, null, null)));
    }

    public Chat saveMessage(Long conversationId, Long senderId, String message) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Chat chat = new Chat(null, conversation, sender, message, null);
        return chatRepository.save(chat);
    }

    public List<MessageDTO> getHistory(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        return chatRepository.findByConversationOrderByCreatedAtAsc(conversation)
                .stream()
                .map(chat -> new MessageDTO(
                        conversationId,
                        chat.getSender().getId(),
                        chat.getSender().getFirstname() + " " + chat.getSender().getLastname(),
                        chat.getSender().getType(),
                        chat.getMessage(),
                        chat.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    public List<Conversation> getPendingConversations() {
        return conversationRepository.findByAgentIsNull();
    }

    public Conversation assignAgent(Long conversationId, Long agentId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found"));
        conversation.setAgent(agent);
        return conversationRepository.save(conversation);
    }
    
    public List<Conversation> getAgentConversations(Long agentId) {
        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found"));
        return conversationRepository.findByAgent(agent);
    }

}
