package vn.com.atomi.charge.chat.mapper;

import org.springframework.stereotype.Component;
import vn.com.atomi.charge.chat.model.entity.SupportConversationEntity;
import vn.com.atomi.charge.chat.model.entity.SupportMessageEntity;
import vn.com.atomi.charge.chat.model.response.SupportConversationResponse;
import vn.com.atomi.charge.chat.model.response.SupportMessageResponse;

@Component
public class SupportChatMapper {

    public SupportConversationResponse toConversationResponse(
            SupportConversationEntity entity,
            long unreadCount) {
        return new SupportConversationResponse(
                entity.getId(),
                entity.getCourseId(),
                entity.getCourseName(),
                entity.getStudentId(),
                entity.getStudentName(),
                entity.getInstructorId(),
                entity.getInstructorName(),
                entity.getStatus(),
                entity.getLastMessage(),
                entity.getLastMessageAt(),
                unreadCount,
                entity.getCreatedDate());
    }

    public SupportMessageResponse toMessageResponse(SupportMessageEntity entity) {
        return new SupportMessageResponse(
                entity.getId(),
                entity.getConversationId(),
                entity.getSenderId(),
                entity.getContent(),
                entity.getReadAt(),
                entity.getCreatedDate());
    }
}
