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
import org.springframework.stereotype.Component;
import vn.com.atomi.charge.chat.exception.ChatException;
import vn.com.atomi.charge.chat.service.impl.ConversationAccessService;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class WebSocketConversationInterceptor implements ChannelInterceptor {

    private static final Pattern CONVERSATION_TOPIC =
            Pattern.compile("^/topic/chat/conversations/([^/]+)$");

    private final ConversationAccessService accessService;

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
        accessService.require(conversationId, accessToken);
        accessor.setUser(new UsernamePasswordAuthenticationToken(conversationId, null, List.of()));
    }

    private void authorizeSubscription(StompHeaderAccessor accessor) {
        Matcher matcher = CONVERSATION_TOPIC.matcher(
                accessor.getDestination() == null ? "" : accessor.getDestination());
        if (!matcher.matches()
                || accessor.getUser() == null
                || !matcher.group(1).equals(accessor.getUser().getName())) {
            throw new ChatException(HttpStatus.FORBIDDEN, "Không được subscribe hội thoại khác");
        }
    }
}
