package vn.com.atomi.charge.chat.service.interfaces;

import vn.com.atomi.charge.chat.model.request.SendChatMessageRequest;
import vn.com.atomi.charge.chat.model.response.ChatConversationResponse;
import vn.com.atomi.charge.chat.model.response.ChatMessageResponse;

import java.util.List;

public interface ChatService {
    ChatConversationResponse createConversation();
    ChatConversationResponse getConversation(String conversationId, String accessToken);
    List<ChatMessageResponse> getMessages(String conversationId, String accessToken);
    ChatMessageResponse sendMessage(
            String conversationId,
            String accessToken,
            SendChatMessageRequest request);
}
