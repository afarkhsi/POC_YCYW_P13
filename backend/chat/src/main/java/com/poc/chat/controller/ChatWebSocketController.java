package com.poc.chat.controller;

import com.poc.chat.dto.ChatMessageRequest;
import com.poc.chat.dto.MessageDTO;
import com.poc.chat.model.Chat;
import com.poc.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

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
