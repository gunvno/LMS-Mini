package vn.com.atomi.charge.chat.repository;

import vn.com.atomi.charge.base.repository.BaseRepository;
import vn.com.atomi.charge.chat.model.entity.SupportConversationEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SupportConversationRepository extends BaseRepository<SupportConversationEntity, String> {

    Optional<SupportConversationEntity> findFirstByCourseIdAndStudentIdAndDeletedAtIsNull(
            String courseId, String studentId);

    @Modifying
    @Query(value = """
            INSERT IGNORE INTO tbl_support_conversations (
                id, version, created_by, created_date, last_modified_by, last_modified_date,
                course_id, course_name, student_id, student_name,
                instructor_id, instructor_name, status
            ) VALUES (
                :id, 0, :createdBy, :createdDate, :createdBy, :createdDate,
                :courseId, :courseName, :studentId, :studentName,
                :instructorId, :instructorName, :status
            )
            """, nativeQuery = true)
    int insertIgnore(
            @Param("id") String id,
            @Param("createdBy") String createdBy,
            @Param("createdDate") LocalDateTime createdDate,
            @Param("courseId") String courseId,
            @Param("courseName") String courseName,
            @Param("studentId") String studentId,
            @Param("studentName") String studentName,
            @Param("instructorId") String instructorId,
            @Param("instructorName") String instructorName,
            @Param("status") String status);

    @Query("""
            select conversation
              from SupportConversationEntity conversation
             where conversation.deletedAt is null
               and (conversation.studentId = :userId or conversation.instructorId = :userId)
             order by coalesce(conversation.lastMessageAt, conversation.createdDate) desc
            """)
    List<SupportConversationEntity> findParticipantConversations(@Param("userId") String userId);
}
