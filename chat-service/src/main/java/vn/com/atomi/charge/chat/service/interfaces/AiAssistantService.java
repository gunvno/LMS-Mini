package vn.com.atomi.charge.chat.service.interfaces;

import vn.com.atomi.charge.chat.model.dto.AiAnswer;

public interface AiAssistantService {
    AiAnswer answer(String conversationId);
}
