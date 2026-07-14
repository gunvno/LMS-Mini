package vn.com.atomi.charge.chat.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import vn.com.atomi.charge.chat.model.entity.SupportConversationEntity;
import vn.com.atomi.charge.chat.model.exception.ChatException;
import vn.com.atomi.charge.chat.repository.SupportConversationRepository;

@Service
@RequiredArgsConstructor
public class SupportConversationAccessService {

    private final SupportConversationRepository conversationRepository;

    public SupportConversationEntity require(String conversationId, String userId) {
        SupportConversationEntity conversation = conversationRepository.findEntityById(conversationId)
                .orElseThrow(() -> new ChatException(HttpStatus.NOT_FOUND, "Không tìm thấy hội thoại"));
        if (!userId.equals(conversation.getStudentId()) && !userId.equals(conversation.getInstructorId())) {
            throw new ChatException(HttpStatus.FORBIDDEN, "Bạn không thuộc hội thoại này");
        }
        return conversation;
    }
}
