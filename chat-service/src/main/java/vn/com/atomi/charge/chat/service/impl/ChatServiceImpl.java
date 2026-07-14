package vn.com.atomi.charge.chat.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.atomi.charge.chat.mapper.ChatMapper;
import vn.com.atomi.charge.chat.model.entity.ChatConversationEntity;
import vn.com.atomi.charge.chat.model.entity.ChatMessageEntity;
import vn.com.atomi.charge.chat.model.enums.ChatSenderType;
import vn.com.atomi.charge.chat.model.enums.ConversationStatus;
import vn.com.atomi.charge.chat.model.event.UserMessageCreatedEvent;
import vn.com.atomi.charge.chat.model.exception.ChatException;
import vn.com.atomi.charge.chat.model.request.SendChatMessageRequest;
import vn.com.atomi.charge.chat.model.response.ChatConversationResponse;
import vn.com.atomi.charge.chat.model.response.ChatMessageResponse;
import vn.com.atomi.charge.chat.repository.ChatConversationRepository;
import vn.com.atomi.charge.chat.repository.ChatMessageRepository;
import vn.com.atomi.charge.chat.service.interfaces.ChatService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final ConversationTokenService tokenService;
    private final ConversationAccessService accessService;
    private final ChatMapper chatMapper;
    private final ChatRealtimePublisher realtimePublisher;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public ChatConversationResponse createConversation() {
        String accessToken = tokenService.createToken();
        ChatConversationEntity entity = new ChatConversationEntity();
        entity.setAccessTokenHash(tokenService.hash(accessToken));
        entity.setStatus(ConversationStatus.ACTIVE);
        entity.setAssistantProcessing(false);
        entity = conversationRepository.save(entity);
        return chatMapper.toConversationResponse(entity, accessToken);
    }

    @Override
    public ChatConversationResponse getConversation(String conversationId, String accessToken) {
        return chatMapper.toConversationResponse(accessService.require(conversationId, accessToken), null);
    }

    @Override
    public List<ChatMessageResponse> getMessages(String conversationId, String accessToken) {
        accessService.require(conversationId, accessToken);
        return messageRepository
                .findByConversationIdAndDeletedAtIsNullOrderByCreatedDateAsc(conversationId)
                .stream()
                .map(chatMapper::toMessageResponse)
                .toList();
    }

    @Override
    @Transactional
    public ChatMessageResponse sendMessage(
            String conversationId,
            String accessToken,
            SendChatMessageRequest request) {
        ChatConversationEntity conversation = accessService.require(conversationId, accessToken);
        if (conversation.getStatus() != ConversationStatus.ACTIVE) {
            throw new ChatException(HttpStatus.CONFLICT, "Hội thoại đã đóng");
        }
        if (conversation.isAssistantProcessing()
                && conversation.getLastMessageAt() != null
                && conversation.getLastMessageAt().isAfter(LocalDateTime.now().minusMinutes(2))) {
            throw new ChatException(HttpStatus.CONFLICT, "Trợ lý đang trả lời tin nhắn trước đó");
        }

        LocalDateTime now = LocalDateTime.now();
        ChatMessageEntity message = new ChatMessageEntity();
        message.setConversationId(conversationId);
        message.setSenderType(ChatSenderType.USER);
        message.setContent(request.content().strip());
        message.setErrorMessage(false);
        message = messageRepository.save(message);

        conversation.setAssistantProcessing(true);
        conversation.setLastMessage(message.getContent().length() > 500
                ? message.getContent().substring(0, 500)
                : message.getContent());
        conversation.setLastMessageAt(now);
        conversationRepository.save(conversation);

        ChatMessageResponse response = chatMapper.toMessageResponse(message);
        realtimePublisher.publish("MESSAGE_CREATED", conversationId, response);
        eventPublisher.publishEvent(new UserMessageCreatedEvent(conversationId));
        return response;
    }
}
