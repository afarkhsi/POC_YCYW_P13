package com.poc.chat.exception;

public class ConversationNotFoundException extends RuntimeException {
    public ConversationNotFoundException(Long id) {
        super("Conversation introuvable avec l'id : " + id);
    }
}
