package vn.com.atomi.charge.chat.config;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import vn.com.atomi.charge.chat.model.exception.ChatException;
import vn.com.atomi.charge.chat.model.entity.ChatConversationEntity;
import vn.com.atomi.charge.chat.security.ConversationAccessService;
import vn.com.atomi.charge.chat.security.AuthenticatedChatUserService;
import vn.com.atomi.charge.chat.security.SupportConversationAccessService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class WebSocketConversationInterceptorTest {

    private final ConversationAccessService accessService = mock(ConversationAccessService.class);
    private final SupportConversationAccessService supportAccessService = mock(SupportConversationAccessService.class);
    private final AuthenticatedChatUserService authenticatedUserService = mock(AuthenticatedChatUserService.class);
    private final WebSocketConversationInterceptor interceptor =
            new WebSocketConversationInterceptor(accessService, supportAccessService, authenticatedUserService);

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
        accessor.setUser(new UsernamePasswordAuthenticationToken(
                "conversation-a", null, List.of(new SimpleGrantedAuthority("AI_CHAT"))));

        assertThatThrownBy(() -> interceptor.preSend(
                message(accessor), mock(org.springframework.messaging.MessageChannel.class)))
                .isInstanceOf(ChatException.class);
    }

    @Test
    void supportConnectAcceptsVerifiedAuthorizationWithoutAuthorities() {
        when(authenticatedUserService.requireUserId("Bearer jwt-token")).thenReturn("user-a");
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeader("Authorization", "Bearer jwt-token");

        Message<?> result = interceptor.preSend(message(accessor), mock(org.springframework.messaging.MessageChannel.class));

        assertThat(StompHeaderAccessor.wrap(result).getUser())
                .isInstanceOfSatisfying(UsernamePasswordAuthenticationToken.class, authentication -> {
                    assertThat(authentication.getName()).isEqualTo("user-a");
                    assertThat(authentication.getAuthorities()).isEmpty();
                });
    }

    @Test
    void supportConnectPreservesAuthenticatedHttpHandshakePrincipal() {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "user-cookie",
                null,
                List.of(new SimpleGrantedAuthority("COURSE_VIEW")));
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setUser(authentication);

        Message<?> result = interceptor.preSend(message(accessor), mock(org.springframework.messaging.MessageChannel.class));

        assertThat(StompHeaderAccessor.wrap(result).getUser()).isSameAs(authentication);
        assertThat(((UsernamePasswordAuthenticationToken) StompHeaderAccessor.wrap(result).getUser())
                .getAuthorities())
                .extracting(org.springframework.security.core.GrantedAuthority::getAuthority)
                .containsExactly("COURSE_VIEW");
    }

    @Test
    void unauthenticatedSupportSubscriptionIsRejected() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/support/conversations/conversation-a");

        assertThatThrownBy(() -> interceptor.preSend(
                message(accessor), mock(org.springframework.messaging.MessageChannel.class)))
                .isInstanceOf(ChatException.class);
        verifyNoInteractions(supportAccessService);
    }

    @Test
    void anonymousPrincipalIsNotTreatedAsAuthenticatedHuman() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/support/conversations/conversation-a");
        accessor.setUser(new AnonymousAuthenticationToken(
                "anonymous-key",
                "anonymousUser",
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));

        assertThatThrownBy(() -> interceptor.preSend(
                message(accessor), mock(org.springframework.messaging.MessageChannel.class)))
                .isInstanceOf(ChatException.class);
        verifyNoInteractions(supportAccessService);
    }

    @Test
    void authenticatedSupportSubscriptionNeedsNoPermissionAndChecksOwnership() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/support/conversations/conversation-a");
        accessor.setUser(new UsernamePasswordAuthenticationToken(
                "user-a", null, List.of()));

        interceptor.preSend(message(accessor), mock(org.springframework.messaging.MessageChannel.class));

        verify(supportAccessService).require("conversation-a", "user-a");
    }

    @Test
    void aiConversationPrincipalCannotSubscribeToHumanSupportTopic() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/support/conversations/conversation-a");
        accessor.setUser(new UsernamePasswordAuthenticationToken(
                "conversation-a", null, List.of(new SimpleGrantedAuthority("AI_CHAT"))));

        assertThatThrownBy(() -> interceptor.preSend(
                message(accessor), mock(org.springframework.messaging.MessageChannel.class)))
                .isInstanceOf(ChatException.class);
        verifyNoInteractions(supportAccessService);
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
