package vn.com.atomi.charge.chat.model.response;

import java.time.LocalDateTime;

public record SupportRealtimeEventResponse(
        String eventType,
        String conversationId,
        SupportMessageResponse message,
        LocalDateTime occurredAt
) {
}
