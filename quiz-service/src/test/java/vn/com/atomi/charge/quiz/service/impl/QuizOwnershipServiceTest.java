package vn.com.atomi.charge.quiz.service.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import vn.com.atomi.charge.quiz.client.CourseClient;
import vn.com.atomi.charge.quiz.client.LearningClient;
import vn.com.atomi.charge.quiz.model.entity.QuizEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuizOwnershipServiceTest {

    @Mock
    private CourseClient courseClient;

    @Mock
    private LearningClient learningClient;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void reviewerCannotViewOrManageQuizFromInstructorDraft() {
        authenticate("reviewer-1", "COURSE_REVIEW", "QUIZ_MANAGE");
        QuizOwnershipService service = service();
        QuizEntity quiz = quiz("course-private");
        when(courseClient.isVisibleToReviewer("course-private")).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> service.assertCanViewQuiz(quiz));
        assertThrows(AccessDeniedException.class,
                () -> service.assertCanManageCourse("course-private"));
    }

    @Test
    void reviewerCanViewAndManageQuizFromVisibleCourse() {
        authenticate("reviewer-1", "COURSE_REVIEW", "QUIZ_MANAGE");
        QuizOwnershipService service = service();
        QuizEntity quiz = quiz("course-pending");
        when(courseClient.isVisibleToReviewer("course-pending")).thenReturn(true);

        assertDoesNotThrow(() -> service.assertCanViewQuiz(quiz));
        assertDoesNotThrow(() -> service.assertCanManageCourse("course-pending"));
    }

    @Test
    void instructorOwnerRetainsPrivateDraftQuizAccessWithoutReviewPermission() {
        authenticate("instructor-1", "QUIZ_MANAGE");
        QuizOwnershipService service = service();
        QuizEntity quiz = quiz("course-private");
        when(courseClient.isInstructorOwner("course-private", "instructor-1"))
                .thenReturn(true);

        assertDoesNotThrow(() -> service.assertCanViewQuiz(quiz));
        assertDoesNotThrow(() -> service.assertCanManageCourse("course-private"));
    }

    @Test
    void reviewerVisibleCourseIdsComeFromCoursePrivacyBoundary() {
        authenticate("reviewer-1", "COURSE_REVIEW");
        QuizOwnershipService service = service();
        when(courseClient.getReviewerVisibleCourseIds())
                .thenReturn(List.of("course-draft", "course-pending"));

        assertEquals(List.of("course-draft", "course-pending"),
                service.getVisibleCourseIds());
    }

    private QuizOwnershipService service() {
        return new QuizOwnershipService(courseClient, learningClient);
    }

    private static QuizEntity quiz(String courseId) {
        QuizEntity quiz = new QuizEntity();
        quiz.setCourseId(courseId);
        return quiz;
    }

    private static void authenticate(String userId, String... authorities) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userId,
                "n/a",
                Arrays.stream(authorities).map(SimpleGrantedAuthority::new).toList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
