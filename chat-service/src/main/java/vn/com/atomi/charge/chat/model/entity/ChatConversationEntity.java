package vn.com.atomi.charge.chat.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.com.atomi.charge.base.model.entity.BaseEntity;
import vn.com.atomi.charge.chat.model.enums.ConversationStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "tbl_chat_conversations")
public class ChatConversationEntity extends BaseEntity {

    @Column(name = "access_token_hash", nullable = false, length = 64)
    private String accessTokenHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ConversationStatus status;

    @Column(name = "assistant_processing", nullable = false)
    private boolean assistantProcessing;

    @Column(name = "last_message", length = 500)
    private String lastMessage;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;
}
