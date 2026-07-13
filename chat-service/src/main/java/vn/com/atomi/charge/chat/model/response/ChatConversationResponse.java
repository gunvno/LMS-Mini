package vn.com.atomi.charge.chat.model.response;

import vn.com.atomi.charge.chat.model.enums.ConversationStatus;

import java.time.LocalDateTime;

public record ChatConversationResponse(
        String id,
        String accessToken,
        ConversationStatus status,
        boolean assistantProcessing,
        String lastMessage,
        LocalDateTime lastMessageAt,
        LocalDateTime createdAt
) {
}
