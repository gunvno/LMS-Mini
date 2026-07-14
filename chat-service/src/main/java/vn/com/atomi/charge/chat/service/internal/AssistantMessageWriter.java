package vn.com.atomi.charge.chat.service.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.com.atomi.charge.chat.mapper.ChatMapper;
import vn.com.atomi.charge.chat.model.dto.AiAnswer;
import vn.com.atomi.charge.chat.model.dto.RecommendedCourseDto;
import vn.com.atomi.charge.chat.model.entity.ChatConversationEntity;
import vn.com.atomi.charge.chat.model.entity.ChatMessageEntity;
import vn.com.atomi.charge.chat.model.enums.ChatSenderType;
import vn.com.atomi.charge.chat.model.response.ChatMessageResponse;
import vn.com.atomi.charge.chat.repository.ChatConversationRepository;
import vn.com.atomi.charge.chat.repository.ChatMessageRepository;
import vn.com.atomi.charge.chat.realtime.ChatRealtimePublisher;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AssistantMessageWriter {

    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final ChatMapper chatMapper;
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
            List<RecommendedCourseDto> recommendations,
            boolean error) {
        ChatConversationEntity conversation = conversationRepository.findEntityById(conversationId)
                .orElse(null);
        if (conversation == null) return;

        ChatMessageEntity message = new ChatMessageEntity();
        message.setConversationId(conversationId);
        message.setSenderType(ChatSenderType.ASSISTANT);
        message.setContent(content);
        message.setRecommendationsJson(chatMapper.writeRecommendations(recommendations));
        message.setErrorMessage(error);
        message = messageRepository.save(message);

        conversation.setAssistantProcessing(false);
        conversation.setLastMessage(content.length() > 500 ? content.substring(0, 500) : content);
        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        ChatMessageResponse response = chatMapper.toMessageResponse(message);
        realtimePublisher.publish(
                error ? "ASSISTANT_ERROR" : "ASSISTANT_MESSAGE_CREATED",
                conversationId,
                response);
    }
}
