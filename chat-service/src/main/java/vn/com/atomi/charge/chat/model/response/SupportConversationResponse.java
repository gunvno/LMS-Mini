package vn.com.atomi.charge.chat.model.response;

import vn.com.atomi.charge.chat.model.enums.SupportConversationStatus;

import java.time.LocalDateTime;

public record SupportConversationResponse(
        String id,
        String courseId,
        String courseName,
        String studentId,
        String studentName,
        String instructorId,
        String instructorName,
        SupportConversationStatus status,
        String lastMessage,
        LocalDateTime lastMessageAt,
        long unreadCount,
        LocalDateTime createdAt
) {
}
