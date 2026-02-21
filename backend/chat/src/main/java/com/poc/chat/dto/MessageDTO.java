package com.poc.chat.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private Long conversationId;
    private Long senderId;
    private String senderName;
    private String senderType;
    private String message;
    private LocalDateTime createdAt;
}
