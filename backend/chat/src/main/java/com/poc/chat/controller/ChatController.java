package com.poc.chat.controller;

import com.poc.chat.dto.MessageDTO;
import com.poc.chat.model.Chat;
import com.poc.chat.model.Conversation;
import com.poc.chat.model.User;
import com.poc.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    // REST - Démarrer ou récupérer une conversation
    @PostMapping("/api/conversations")
    public ResponseEntity<Conversation> getOrCreateConversation(@RequestParam Long customerId) {
        return ResponseEntity.ok(chatService.getOrCreateConversation(customerId));
    }

    // REST - Récupérer l'historique d'une conversation
    @GetMapping("/api/conversations/{conversationId}/history")
    public ResponseEntity<List<MessageDTO>> getHistory(@PathVariable Long conversationId) {
        return ResponseEntity.ok(chatService.getHistory(conversationId));
    }

    // REST - Récupérer les conversations en attente (pour les agents)
    @GetMapping("/api/conversations/pending")
    public ResponseEntity<List<Conversation>> getPendingConversations() {
        return ResponseEntity.ok(chatService.getPendingConversations());
    }

    // REST - Assigner un agent à une conversation
    @PostMapping("/api/conversations/{conversationId}/assign")
    public ResponseEntity<Conversation> assignAgent(
            @PathVariable Long conversationId,
            @RequestParam Long agentId) {
        return ResponseEntity.ok(chatService.assignAgent(conversationId, agentId));
    }

    // WebSocket - Envoyer un message
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload Map<String, Object> payload) {
        Long conversationId = Long.valueOf(payload.get("conversationId").toString());
        Long senderId = Long.valueOf(payload.get("senderId").toString());
        String message = payload.get("message").toString();

        Chat savedChat = chatService.saveMessage(conversationId, senderId, message);

        MessageDTO dto = new MessageDTO(
                conversationId,
                savedChat.getSender().getId(),
                savedChat.getSender().getFirstname() + " " + savedChat.getSender().getLastname(),
                savedChat.getSender().getType(),
                savedChat.getMessage(),
                savedChat.getCreatedAt()
        );

        // Diffuser le message à tous les abonnés de la conversation
        messagingTemplate.convertAndSend("/topic/conversation." + conversationId, dto);
    }
    
    // REST - Récupérer les conversations assignées à un agent
    @GetMapping("/api/conversations/my")
    public ResponseEntity<List<Conversation>> getMyConversations(@RequestParam Long agentId) {
        User agent = new User();
        agent.setId(agentId);
        return ResponseEntity.ok(chatService.getAgentConversations(agentId));
    }
}
