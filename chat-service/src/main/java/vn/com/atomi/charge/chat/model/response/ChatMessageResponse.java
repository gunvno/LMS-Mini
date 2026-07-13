package vn.com.atomi.charge.chat.model.response;

import vn.com.atomi.charge.chat.model.dto.RecommendedCourseDto;
import vn.com.atomi.charge.chat.model.enums.ChatSenderType;

import java.time.LocalDateTime;
import java.util.List;

public record ChatMessageResponse(
        String id,
        String conversationId,
        ChatSenderType senderType,
        String content,
        List<RecommendedCourseDto> recommendedCourses,
        boolean error,
        LocalDateTime createdAt
) {
}
