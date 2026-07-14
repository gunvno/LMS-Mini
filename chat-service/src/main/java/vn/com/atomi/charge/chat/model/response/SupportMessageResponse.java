package vn.com.atomi.charge.chat.model.response;

import java.time.LocalDateTime;

public record SupportMessageResponse(
        String id,
        String conversationId,
        String senderId,
        String content,
        LocalDateTime readAt,
        LocalDateTime createdAt
) {
}
