package vn.com.atomi.charge.quiz.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import vn.com.atomi.charge.quiz.repository.client.CourseClient;

@Component
@RequiredArgsConstructor
public class QuizOwnershipService {

    private final CourseClient courseClient;

    public void assertCanManageCourse(String courseId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "COURSE_REVIEW".equals(authority.getAuthority()))) {
            return;
        }
        String userId = authentication == null ? null : authentication.getName();
        if (userId == null || !Boolean.TRUE.equals(courseClient.isInstructorOwner(courseId, userId))) {
            throw new AccessDeniedException("common.access_denied");
        }
    }
}
