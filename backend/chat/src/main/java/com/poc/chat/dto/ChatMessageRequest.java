package com.poc.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChatMessageRequest {

    @NotNull(message = "L'id de conversation est obligatoire")
    private Long conversationId;

    @NotBlank(message = "Le message ne peut pas être vide")
    private String message;
}
