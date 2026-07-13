package vn.com.atomi.charge.chat.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.com.atomi.charge.chat.model.dto.AiAnswer;
import vn.com.atomi.charge.chat.model.entity.ChatConversationEntity;
import vn.com.atomi.charge.chat.model.entity.ChatMessageEntity;
import vn.com.atomi.charge.chat.model.enums.ChatSenderType;
import vn.com.atomi.charge.chat.model.response.ChatMessageResponse;
import vn.com.atomi.charge.chat.repository.ChatConversationRepository;
import vn.com.atomi.charge.chat.repository.ChatMessageRepository;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AssistantMessageWriter {

    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final ChatMessageMapper messageMapper;
    private final ChatRealtimePublisher realtimePublisher;

    @Transactional
    public void writeAnswer(String conversationId, AiAnswer answer) {
        write(conversationId, answer.content(), answer.recommendedCourses(), false);
    }

    @Transactional
    public void writeError(String conversationId) {
        write(
                conversationId,
                "Mình đang gặp sự cố khi tìm khóa học. Bạn vui lòng thử lại sau nhé.",
                List.of(),
                true);
    }

    private void write(
            String conversationId,
            String content,
            List<vn.com.atomi.charge.chat.model.dto.RecommendedCourseDto> recommendations,
            boolean error) {
        ChatConversationEntity conversation = conversationRepository.findEntityById(conversationId)
                .orElse(null);
        if (conversation == null) return;

        ChatMessageEntity message = new ChatMessageEntity();
        message.setConversationId(conversationId);
        message.setSenderType(ChatSenderType.ASSISTANT);
        message.setContent(content);
        message.setRecommendationsJson(messageMapper.writeRecommendations(recommendations));
        message.setErrorMessage(error);
        message = messageRepository.save(message);

        conversation.setAssistantProcessing(false);
        conversation.setLastMessage(content.length() > 500 ? content.substring(0, 500) : content);
        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        ChatMessageResponse response = messageMapper.toResponse(message);
        realtimePublisher.publish(
                error ? "ASSISTANT_ERROR" : "ASSISTANT_MESSAGE_CREATED",
                conversationId,
                response);
    }
}
