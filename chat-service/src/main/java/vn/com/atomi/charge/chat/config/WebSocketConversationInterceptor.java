package vn.com.atomi.charge.chat.config;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import vn.com.atomi.charge.chat.model.exception.ChatException;
import vn.com.atomi.charge.chat.service.impl.ConversationAccessService;
import vn.com.atomi.charge.chat.service.impl.AuthenticatedChatUserService;
import vn.com.atomi.charge.chat.service.impl.SupportConversationAccessService;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class WebSocketConversationInterceptor implements ChannelInterceptor {

    private static final Pattern CONVERSATION_TOPIC =
            Pattern.compile("^/topic/chat/conversations/([^/]+)$");
    private static final Pattern SUPPORT_TOPIC =
            Pattern.compile("^/topic/support/conversations/([^/]+)$");
    private static final String AI_CHAT_AUTHORITY = "AI_CHAT";
    private static final String SUPPORT_CHAT_AUTHORITY = "SUPPORT_CHAT";

    private final ConversationAccessService accessService;
    private final SupportConversationAccessService supportAccessService;
    private final AuthenticatedChatUserService authenticatedUserService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            accessor = StompHeaderAccessor.wrap(message);
        }
        if (accessor.getCommand() == StompCommand.CONNECT) {
            authenticate(accessor);
        } else if (accessor.getCommand() == StompCommand.SUBSCRIBE) {
            authorizeSubscription(accessor);
        } else if (accessor.getCommand() == StompCommand.SEND) {
            throw new ChatException(HttpStatus.FORBIDDEN, "WebSocket SEND bị tắt; hãy gửi lệnh qua REST");
        }
        return message;
    }

    private void authenticate(StompHeaderAccessor accessor) {
        String conversationId = accessor.getFirstNativeHeader("X-Conversation-Id");
        String accessToken = accessor.getFirstNativeHeader("X-Chat-Token");
        if (accessToken != null && !accessToken.isBlank()) {
            accessService.require(conversationId, accessToken);
            accessor.setUser(new UsernamePasswordAuthenticationToken(
                    conversationId,
                    null,
                    List.of(new SimpleGrantedAuthority(AI_CHAT_AUTHORITY))));
            return;
        }
        String userId = authenticatedUserService.requireUserId(
                accessor.getFirstNativeHeader("Authorization"));
        accessor.setUser(new UsernamePasswordAuthenticationToken(
                userId,
                null,
                List.of(new SimpleGrantedAuthority(SUPPORT_CHAT_AUTHORITY))));
    }

    private void authorizeSubscription(StompHeaderAccessor accessor) {
        if (accessor.getUser() == null) {
            throw new ChatException(HttpStatus.UNAUTHORIZED, "WebSocket chưa được xác thực");
        }
        String destination = accessor.getDestination() == null ? "" : accessor.getDestination();
        Matcher aiMatcher = CONVERSATION_TOPIC.matcher(destination);
        if (aiMatcher.matches() && hasAuthority(accessor, AI_CHAT_AUTHORITY)) {
            if (!aiMatcher.group(1).equals(accessor.getUser().getName())) {
                throw new ChatException(HttpStatus.FORBIDDEN, "Không được subscribe hội thoại khác");
            }
            return;
        }
        Matcher supportMatcher = SUPPORT_TOPIC.matcher(destination);
        if (supportMatcher.matches() && hasAuthority(accessor, SUPPORT_CHAT_AUTHORITY)) {
            supportAccessService.require(supportMatcher.group(1), accessor.getUser().getName());
            return;
        }
        throw new ChatException(HttpStatus.FORBIDDEN, "Không được subscribe topic này");
    }

    private boolean hasAuthority(StompHeaderAccessor accessor, String authority) {
        return accessor.getUser() instanceof UsernamePasswordAuthenticationToken authentication
                && authentication.getAuthorities().stream()
                .anyMatch(item -> authority.equals(item.getAuthority()));
    }
}
