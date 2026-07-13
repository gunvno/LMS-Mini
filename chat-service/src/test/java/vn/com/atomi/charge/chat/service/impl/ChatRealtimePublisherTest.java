package vn.com.atomi.charge.chat.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import vn.com.atomi.charge.chat.model.enums.ChatSenderType;
import vn.com.atomi.charge.chat.model.response.ChatMessageResponse;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ChatRealtimePublisherTest {

    @Test
    void publishesOnlyToConversationTopic() {
        SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);
        ChatRealtimePublisher publisher = new ChatRealtimePublisher(template);
        ChatMessageResponse message = new ChatMessageResponse(
                "message-a", "conversation-a", ChatSenderType.ASSISTANT,
                "Xin chào", List.of(), false, LocalDateTime.now());

        publisher.publish("ASSISTANT_MESSAGE_CREATED", "conversation-a", message);

        verify(template).convertAndSend(
                org.mockito.ArgumentMatchers.eq("/topic/chat/conversations/conversation-a"),
                any(Object.class));
    }
}
