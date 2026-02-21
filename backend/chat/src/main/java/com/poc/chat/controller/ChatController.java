package com.poc.chat.controller;

import com.poc.chat.dto.ChatMessageRequest;
import com.poc.chat.dto.MessageDTO;
import com.poc.chat.model.Chat;
import com.poc.chat.model.Conversation;
import com.poc.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/api/conversations")
    public ResponseEntity<Conversation> getOrCreateConversation(@RequestParam Long customerId) {
        return ResponseEntity.ok(chatService.getOrCreateConversation(customerId));
    }

    @GetMapping("/api/conversations/{conversationId}/history")
    public ResponseEntity<List<MessageDTO>> getHistory(@PathVariable Long conversationId) {
        return ResponseEntity.ok(chatService.getHistory(conversationId));
    }

    @GetMapping("/api/conversations/pending")
    public ResponseEntity<List<Conversation>> getPendingConversations() {
        return ResponseEntity.ok(chatService.getPendingConversations());
    }

    @PostMapping("/api/conversations/{conversationId}/assign")
    public ResponseEntity<Conversation> assignAgent(
            @PathVariable Long conversationId,
            @RequestParam Long agentId) {
        return ResponseEntity.ok(chatService.assignAgent(conversationId, agentId));
    }

    @GetMapping("/api/conversations/my")
    public ResponseEntity<List<Conversation>> getMyConversations(@RequestParam Long agentId) {
        return ResponseEntity.ok(chatService.getAgentConversations(agentId));
    }

    // senderEmail extrait du JWT
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageRequest request, Principal principal) {
        String senderEmail = principal.getName();

        Chat savedChat = chatService.saveMessage(
                request.getConversationId(),
                senderEmail,
                request.getMessage()
        );

        MessageDTO dto = new MessageDTO(
                request.getConversationId(),
                savedChat.getSender().getId(),
                savedChat.getSender().getFirstname() + " " + savedChat.getSender().getLastname(),
                savedChat.getSender().getType(),
                savedChat.getMessage(),
                savedChat.getCreatedAt()
        );

        messagingTemplate.convertAndSend(
                "/topic/conversation." + request.getConversationId(),
                dto
        );
    }
}
