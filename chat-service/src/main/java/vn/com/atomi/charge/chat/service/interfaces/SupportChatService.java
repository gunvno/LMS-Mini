package vn.com.atomi.charge.chat.service.interfaces;

import vn.com.atomi.charge.chat.model.request.SendSupportMessageRequest;
import vn.com.atomi.charge.chat.model.response.SupportConversationResponse;
import vn.com.atomi.charge.chat.model.response.SupportMessageResponse;

import java.util.List;

public interface SupportChatService {

    SupportConversationResponse createOrGet(String courseId);

    List<SupportConversationResponse> getMyConversations();

    List<SupportMessageResponse> getMessages(String conversationId);

    SupportMessageResponse sendMessage(String conversationId, SendSupportMessageRequest request);

    void markRead(String conversationId);
}
