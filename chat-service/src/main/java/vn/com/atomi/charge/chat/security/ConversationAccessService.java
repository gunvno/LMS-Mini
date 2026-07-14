package vn.com.atomi.charge.chat.security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import vn.com.atomi.charge.chat.model.exception.ChatException;
import vn.com.atomi.charge.chat.model.entity.ChatConversationEntity;
import vn.com.atomi.charge.chat.repository.ChatConversationRepository;

@Component
@RequiredArgsConstructor
public class ConversationAccessService {

    private final ChatConversationRepository conversationRepository;
    private final ConversationTokenService tokenService;

    public ChatConversationEntity require(String conversationId, String accessToken) {
        if (!StringUtils.hasText(conversationId) || !StringUtils.hasText(accessToken)) {
            throw new ChatException(HttpStatus.FORBIDDEN, "Thiếu thông tin truy cập hội thoại");
        }
        ChatConversationEntity conversation = conversationRepository.findEntityById(conversationId)
                .orElseThrow(() -> new ChatException(HttpStatus.NOT_FOUND, "Không tìm thấy hội thoại"));
        if (!tokenService.matches(accessToken, conversation.getAccessTokenHash())) {
            throw new ChatException(HttpStatus.FORBIDDEN, "Token hội thoại không hợp lệ");
        }
        return conversation;
    }
}
