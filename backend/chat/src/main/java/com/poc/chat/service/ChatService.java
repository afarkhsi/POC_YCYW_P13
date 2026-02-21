package com.poc.chat.service;

import com.poc.chat.dto.MessageDTO;
import com.poc.chat.exception.ConversationNotFoundException;
import com.poc.chat.exception.UnauthorizedActionException;
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
    private final UserService userService;
    // 🟡 FIX : UserService à la place de UserRepository directement

    public Conversation getOrCreateConversation(Long customerId) {
        User customer = userService.getById(customerId);
        // 🟠 FIX : on cherche la conversation OPEN, on n'en crée une nouvelle
        // que s'il n'en existe pas déjà une ouverte
        return conversationRepository
                .findTopByCustomerAndStatusOrderByCreatedAtDesc(customer, "OPEN")
                .orElseGet(() -> {
                    Conversation conv = new Conversation();
                    conv.setCustomer(customer);
                    conv.setStatus("OPEN");
                    return conversationRepository.save(conv);
                });
    }

    // 🔴 FIX : senderEmail extrait du Principal, plus du payload
    // Vérification que l'expéditeur appartient bien à la conversation
    public Chat saveMessage(Long conversationId, String senderEmail, String messageContent) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException(conversationId));

        User sender = userService.getByEmail(senderEmail);

        // 🔴 Vérification d'ownership : seul le client ou l'agent assigné peut écrire
        boolean isCustomer = conversation.getCustomer().getId().equals(sender.getId());
        boolean isAgent = conversation.getAgent() != null
                && conversation.getAgent().getId().equals(sender.getId());

        if (!isCustomer && !isAgent) {
            throw new UnauthorizedActionException(
                "Vous n'êtes pas autorisé à envoyer un message dans cette conversation"
            );
        }

        Chat chat = new Chat();
        chat.setConversation(conversation);
        chat.setSender(sender);
        chat.setMessage(messageContent);
        return chatRepository.save(chat);
    }

    public List<MessageDTO> getHistory(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException(conversationId));

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
        // filtre sur le statut OPEN
        return conversationRepository.findByAgentIsNullAndStatus("OPEN");
    }

    public Conversation assignAgent(Long conversationId, Long agentId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException(conversationId));

        User agent = userService.getById(agentId);

        // ne pas réassigner une conversation déjà prise
        if (conversation.getAgent() != null) {
            throw new UnauthorizedActionException(
                "Cette conversation est déjà assignée à un agent"
            );
        }

        conversation.setAgent(agent);
        return conversationRepository.save(conversation);
    }

    public List<Conversation> getAgentConversations(Long agentId) {
        User agent = userService.getById(agentId);
        // filtre sur le statut OPEN
        return conversationRepository.findByAgentAndStatus(agent, "OPEN");
    }
}
