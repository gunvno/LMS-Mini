package vn.com.atomi.charge.chat.model.response;

import java.time.LocalDateTime;

public record ChatRealtimeEventResponse(
        String eventType,
        String conversationId,
        ChatMessageResponse message,
        LocalDateTime occurredAt
) {
}
