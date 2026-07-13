package vn.com.atomi.charge.chat.repository;

import org.springframework.data.domain.Pageable;
import vn.com.atomi.charge.base.repository.BaseRepository;
import vn.com.atomi.charge.chat.model.entity.ChatMessageEntity;

import java.util.List;

public interface ChatMessageRepository extends BaseRepository<ChatMessageEntity, String> {
    List<ChatMessageEntity> findByConversationIdAndDeletedAtIsNullOrderByCreatedDateAsc(String conversationId);

    List<ChatMessageEntity> findByConversationIdAndDeletedAtIsNullOrderByCreatedDateDesc(
            String conversationId, Pageable pageable);
}
