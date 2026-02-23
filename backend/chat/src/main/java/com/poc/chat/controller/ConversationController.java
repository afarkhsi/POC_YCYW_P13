package com.poc.chat.controller;

import com.poc.chat.dto.MessageDTO;
import com.poc.chat.model.Conversation;
import com.poc.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<Conversation> getOrCreateConversation(@RequestParam Long customerId) {
        return ResponseEntity.ok(chatService.getOrCreateConversation(customerId));
    }

    @GetMapping("/{conversationId}/history")
    public ResponseEntity<List<MessageDTO>> getHistory(@PathVariable Long conversationId) {
        return ResponseEntity.ok(chatService.getHistory(conversationId));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Conversation>> getPendingConversations() {
        return ResponseEntity.ok(chatService.getPendingConversations());
    }

    @PostMapping("/{conversationId}/assign")
    public ResponseEntity<Conversation> assignAgent(
            @PathVariable Long conversationId,
            @RequestParam Long agentId) {
        return ResponseEntity.ok(chatService.assignAgent(conversationId, agentId));
    }

    @GetMapping("/my")
    public ResponseEntity<List<Conversation>> getMyConversations(@RequestParam Long agentId) {
        return ResponseEntity.ok(chatService.getAgentConversations(agentId));
    }
}
