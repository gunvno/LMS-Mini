package vn.com.atomi.charge.quiz.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import vn.com.atomi.charge.quiz.client.CourseClient;
import vn.com.atomi.charge.quiz.client.LearningClient;
import vn.com.atomi.charge.quiz.model.entity.QuizEntity;
import vn.com.atomi.charge.quiz.model.enums.QuizStatus;

import java.util.List;

@Component
@RequiredArgsConstructor
public class QuizOwnershipService {

    private final CourseClient courseClient;
    private final LearningClient learningClient;

    public void assertCanManageCourse(String courseId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "COURSE_REVIEW".equals(authority.getAuthority()))) {
            if (Boolean.TRUE.equals(courseClient.isVisibleToReviewer(courseId))) {
                return;
            }
            throw new AccessDeniedException("common.access_denied");
        }
        String userId = authentication == null ? null : authentication.getName();
        if (userId == null || !Boolean.TRUE.equals(courseClient.isInstructorOwner(courseId, userId))) {
            throw new AccessDeniedException("common.access_denied");
        }
    }

    public void assertCanViewQuiz(QuizEntity quiz) {
        if (quiz == null) {
            throw new AccessDeniedException("common.access_denied");
        }
        if (canReviewCourses()) {
            if (Boolean.TRUE.equals(courseClient.isVisibleToReviewer(quiz.getCourseId()))) {
                return;
            }
            throw new AccessDeniedException("common.access_denied");
        }
        Authentication authentication = authentication();
        String userId = authentication == null ? null : authentication.getName();
        if (hasAuthority("QUIZ_MANAGE")) {
            if (userId != null && Boolean.TRUE.equals(courseClient.isInstructorOwner(quiz.getCourseId(), userId))) {
                return;
            }
        } else if (quiz.getStatus() == QuizStatus.ACTIVE
                && Boolean.TRUE.equals(learningClient.hasCourseAccess(quiz.getCourseId()))) {
            return;
        }
        throw new AccessDeniedException("common.access_denied");
    }

    public List<String> getVisibleCourseIds() {
        Authentication authentication = authentication();
        String userId = authentication == null ? null : authentication.getName();
        if (userId == null) {
            return List.of();
        }
        if (canReviewCourses()) {
            List<String> ids = courseClient.getReviewerVisibleCourseIds();
            return ids == null ? List.of() : ids;
        }
        if (hasAuthority("QUIZ_MANAGE") && !canReviewCourses()) {
            List<String> ids = courseClient.getInstructorCourseIds(userId);
            return ids == null ? List.of() : ids;
        }
        List<String> ids = learningClient.getAccessibleCourseIds();
        return ids == null ? List.of() : ids;
    }

    public boolean canReviewCourses() {
        return hasAuthority("COURSE_REVIEW");
    }

    public boolean isInstructorRequest() {
        return hasAuthority("QUIZ_MANAGE") && !canReviewCourses();
    }

    public boolean isStudentRequest() {
        return !hasAuthority("QUIZ_MANAGE") && !canReviewCourses();
    }

    private boolean hasAuthority(String authority) {
        Authentication authentication = authentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(item -> authority.equals(item.getAuthority()));
    }

    private Authentication authentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
