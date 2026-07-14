package vn.com.atomi.charge.chat.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.com.atomi.charge.base.model.entity.BaseEntity;
import vn.com.atomi.charge.chat.model.enums.SupportConversationStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "tbl_support_conversations")
public class SupportConversationEntity extends BaseEntity {

    @Column(name = "course_id", nullable = false, length = 36)
    private String courseId;

    @Column(name = "course_name", nullable = false, length = 255)
    private String courseName;

    @Column(name = "student_id", nullable = false, length = 36)
    private String studentId;

    @Column(name = "student_name", nullable = false, length = 255)
    private String studentName;

    @Column(name = "instructor_id", nullable = false, length = 36)
    private String instructorId;

    @Column(name = "instructor_name", nullable = false, length = 255)
    private String instructorName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SupportConversationStatus status;

    @Column(name = "last_message", length = 500)
    private String lastMessage;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;
}
