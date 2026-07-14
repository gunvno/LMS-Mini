package vn.com.atomi.charge.chat.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.chat.client.AuthnClient;
import vn.com.atomi.charge.chat.client.CourseClient;
import vn.com.atomi.charge.chat.client.LearningClient;
import vn.com.atomi.charge.chat.mapper.SupportChatMapper;
import vn.com.atomi.charge.chat.model.dto.CourseDetailsDto;
import vn.com.atomi.charge.chat.model.dto.UserSummaryDto;
import vn.com.atomi.charge.chat.model.entity.SupportConversationEntity;
import vn.com.atomi.charge.chat.model.entity.SupportMessageEntity;
import vn.com.atomi.charge.chat.model.enums.SupportConversationStatus;
import vn.com.atomi.charge.chat.model.exception.ChatException;
import vn.com.atomi.charge.chat.model.request.SendSupportMessageRequest;
import vn.com.atomi.charge.chat.model.response.SupportConversationResponse;
import vn.com.atomi.charge.chat.model.response.SupportMessageResponse;
import vn.com.atomi.charge.chat.repository.SupportConversationRepository;
import vn.com.atomi.charge.chat.repository.SupportMessageRepository;
import vn.com.atomi.charge.chat.service.interfaces.SupportChatService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SupportChatServiceImpl implements SupportChatService {

    private final SupportConversationRepository conversationRepository;
    private final SupportMessageRepository messageRepository;
    private final SupportConversationAccessService accessService;
    private final CourseClient courseClient;
    private final LearningClient learningClient;
    private final AuthnClient authnClient;
    private final SupportChatMapper mapper;
    private final SupportChatRealtimePublisher realtimePublisher;

    @Override
    @Transactional
    public SupportConversationResponse createOrGet(String courseId) {
        String studentId = currentUserId();
        if (!StringUtils.hasText(courseId)) {
            throw new ChatException(HttpStatus.BAD_REQUEST, "Khóa học không hợp lệ");
        }
        if (!Boolean.TRUE.equals(learningClient.hasCourseAccess(studentId, courseId))) {
            throw new ChatException(HttpStatus.FORBIDDEN, "Bạn cần đăng ký khóa học trước khi chat với giảng viên");
        }
        SupportConversationEntity existing = conversationRepository
                .findFirstByCourseIdAndStudentIdAndDeletedAtIsNull(courseId, studentId)
                .orElse(null);
        if (existing != null) {
            return toResponse(existing, studentId);
        }

        CourseDetailsDto course = data(courseClient.getCourse(courseId));
        if (course == null || !StringUtils.hasText(course.instructorId())) {
            throw new ChatException(HttpStatus.NOT_FOUND, "Không tìm thấy giảng viên của khóa học");
        }
        LocalDateTime now = LocalDateTime.now();
        conversationRepository.insertIgnore(
                UUID.randomUUID().toString(),
                studentId,
                now,
                courseId,
                defaultText(course.name(), "Khóa học"),
                studentId,
                userName(studentId, "Học viên"),
                course.instructorId(),
                userName(course.instructorId(), "Giảng viên"),
                SupportConversationStatus.ACTIVE.name());

        SupportConversationEntity conversation = conversationRepository
                .findFirstByCourseIdAndStudentIdAndDeletedAtIsNull(courseId, studentId)
                .orElseThrow(() -> new ChatException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Không thể tạo hội thoại với giảng viên"));
        return toResponse(conversation, studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportConversationResponse> getMyConversations() {
        String userId = currentUserId();
        return conversationRepository.findParticipantConversations(userId).stream()
                .map(conversation -> toResponse(conversation, userId))
                .toList();
    }

    @Override
    @Transactional
    public List<SupportMessageResponse> getMessages(String conversationId) {
        String userId = currentUserId();
        accessService.require(conversationId, userId);
        messageRepository.markRead(conversationId, userId, LocalDateTime.now());
        return messageRepository.findByConversationIdAndDeletedAtIsNullOrderByCreatedDateAsc(conversationId)
                .stream()
                .map(mapper::toMessageResponse)
                .toList();
    }

    @Override
    @Transactional
    public SupportMessageResponse sendMessage(String conversationId, SendSupportMessageRequest request) {
        String userId = currentUserId();
        SupportConversationEntity conversation = accessService.require(conversationId, userId);
        if (conversation.getStatus() != SupportConversationStatus.ACTIVE) {
            throw new ChatException(HttpStatus.CONFLICT, "Hội thoại đã đóng");
        }
        String content = request.content().strip();
        SupportMessageEntity message = new SupportMessageEntity();
        message.setConversationId(conversationId);
        message.setSenderId(userId);
        message.setContent(content);
        message = messageRepository.save(message);

        conversation.setLastMessage(content.length() > 500 ? content.substring(0, 500) : content);
        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        SupportMessageResponse response = mapper.toMessageResponse(message);
        realtimePublisher.publish("MESSAGE_CREATED", conversationId, response);
        return response;
    }

    @Override
    @Transactional
    public void markRead(String conversationId) {
        String userId = currentUserId();
        accessService.require(conversationId, userId);
        messageRepository.markRead(conversationId, userId, LocalDateTime.now());
        realtimePublisher.publish("MESSAGES_READ", conversationId, null);
    }

    private SupportConversationResponse toResponse(SupportConversationEntity conversation, String userId) {
        long unread = messageRepository.countByConversationIdAndSenderIdNotAndReadAtIsNullAndDeletedAtIsNull(
                conversation.getId(), userId);
        return mapper.toConversationResponse(conversation, unread);
    }

    private String currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication == null ? null : authentication.getName();
        if (!StringUtils.hasText(userId) || "anonymousUser".equals(userId)) {
            throw new ChatException(HttpStatus.UNAUTHORIZED, "Bạn cần đăng nhập để sử dụng chat giảng viên");
        }
        return userId;
    }

    private String userName(String userId, String fallback) {
        try {
            UserSummaryDto user = data(authnClient.getUser(userId));
            if (user != null) {
                return defaultText(user.fullName(), defaultText(user.username(), fallback));
            }
        } catch (Exception ignored) {
            // The conversation remains usable when the profile service is temporarily unavailable.
        }
        return fallback;
    }

    private String defaultText(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private <T> T data(BaseResponse<T> response) {
        return response == null ? null : response.getData();
    }
}
