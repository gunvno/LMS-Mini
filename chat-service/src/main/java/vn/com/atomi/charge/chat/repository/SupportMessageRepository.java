package vn.com.atomi.charge.chat.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.com.atomi.charge.base.repository.BaseRepository;
import vn.com.atomi.charge.chat.model.entity.SupportMessageEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface SupportMessageRepository extends BaseRepository<SupportMessageEntity, String> {

    List<SupportMessageEntity> findByConversationIdAndDeletedAtIsNullOrderByCreatedDateAsc(String conversationId);

    long countByConversationIdAndSenderIdNotAndReadAtIsNullAndDeletedAtIsNull(
            String conversationId, String senderId);

    @Modifying
    @Query("""
            update SupportMessageEntity message
               set message.readAt = :readAt
             where message.conversationId = :conversationId
               and message.senderId <> :readerId
               and message.readAt is null
               and message.deletedAt is null
            """)
    int markRead(
            @Param("conversationId") String conversationId,
            @Param("readerId") String readerId,
            @Param("readAt") LocalDateTime readAt);
}
