package vn.com.atomi.charge.chat.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import vn.com.atomi.charge.chat.model.dto.RecommendedCourseDto;
import vn.com.atomi.charge.chat.model.entity.ChatConversationEntity;
import vn.com.atomi.charge.chat.model.entity.ChatMessageEntity;
import vn.com.atomi.charge.chat.model.response.ChatConversationResponse;
import vn.com.atomi.charge.chat.model.response.ChatMessageResponse;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ChatMapper {

    private final ObjectMapper objectMapper;

    public ChatConversationResponse toConversationResponse(
            ChatConversationEntity entity,
            String accessToken) {
        return new ChatConversationResponse(
                entity.getId(),
                accessToken,
                entity.getStatus(),
                entity.isAssistantProcessing(),
                entity.getLastMessage(),
                entity.getLastMessageAt(),
                entity.getCreatedDate());
    }

    public ChatMessageResponse toMessageResponse(ChatMessageEntity entity) {
        return new ChatMessageResponse(
                entity.getId(),
                entity.getConversationId(),
                entity.getSenderType(),
                entity.getContent(),
                readRecommendations(entity.getRecommendationsJson()),
                entity.isErrorMessage(),
                entity.getCreatedDate());
    }

    public String writeRecommendations(List<RecommendedCourseDto> recommendations) {
        try {
            return objectMapper.writeValueAsString(recommendations == null ? List.of() : recommendations);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Không thể lưu gợi ý khóa học", exception);
        }
    }

    private List<RecommendedCourseDto> readRecommendations(String json) {
        if (!StringUtils.hasText(json)) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception exception) {
            return List.of();
        }
    }
}
