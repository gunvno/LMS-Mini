package vn.com.atomi.charge.chat.config;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import vn.com.atomi.charge.chat.model.exception.ChatException;
import vn.com.atomi.charge.chat.model.entity.ChatConversationEntity;
import vn.com.atomi.charge.chat.service.impl.ConversationAccessService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WebSocketConversationInterceptorTest {

    private final ConversationAccessService accessService = mock(ConversationAccessService.class);
    private final WebSocketConversationInterceptor interceptor =
            new WebSocketConversationInterceptor(accessService);

    @Test
    void connectAuthenticatesConversationToken() {
        ChatConversationEntity conversation = new ChatConversationEntity();
        conversation.setId("conversation-a");
        when(accessService.require("conversation-a", "secret-token")).thenReturn(conversation);
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeader("X-Conversation-Id", "conversation-a");
        accessor.setNativeHeader("X-Chat-Token", "secret-token");

        Message<?> result = interceptor.preSend(message(accessor), mock(org.springframework.messaging.MessageChannel.class));

        assertThat(StompHeaderAccessor.wrap(result).getUser())
                .extracting(java.security.Principal::getName)
                .isEqualTo("conversation-a");
    }

    @Test
    void subscriptionToAnotherConversationIsRejected() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/chat/conversations/conversation-b");
        accessor.setUser(new UsernamePasswordAuthenticationToken("conversation-a", null, List.of()));

        assertThatThrownBy(() -> interceptor.preSend(
                message(accessor), mock(org.springframework.messaging.MessageChannel.class)))
                .isInstanceOf(ChatException.class);
    }

    @Test
    void stompSendIsRejected() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        accessor.setDestination("/app/chat");

        assertThatThrownBy(() -> interceptor.preSend(
                message(accessor), mock(org.springframework.messaging.MessageChannel.class)))
                .isInstanceOf(ChatException.class);
    }

    private Message<byte[]> message(StompHeaderAccessor accessor) {
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}
