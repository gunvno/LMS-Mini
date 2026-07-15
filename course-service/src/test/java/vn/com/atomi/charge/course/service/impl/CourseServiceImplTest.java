package vn.com.atomi.charge.course.service.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import vn.com.atomi.charge.base.model.request.BaseRequest;
import vn.com.atomi.charge.course.mapper.CourseMapper;
import vn.com.atomi.charge.course.controller.CourseController;
import vn.com.atomi.charge.course.model.dto.CourseDto;
import vn.com.atomi.charge.course.model.entity.CourseEntity;
import vn.com.atomi.charge.course.model.enums.CourseStatus;
import vn.com.atomi.charge.course.repository.CourseRepository;
import vn.com.atomi.charge.course.security.CourseVisibilityPolicy;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceImplTest {

    @Mock
    private CourseRepository repository;

    @Mock
    private CourseMapper mapper;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void instructorCreateAlwaysBecomesOwnedInstructorDraft() {
        authenticate("instructor-1", "COURSE_MANAGE", "COURSE_SUBMIT_REVIEW");
        CourseServiceImpl service = service();
        CourseDto dto = new CourseDto();
        dto.setStatus(CourseStatus.PUBLISHED);
        dto.setInstructorId("another-user");
        CourseEntity entity = new CourseEntity();
        when(mapper.toEntity(dto)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(dto);

        service.create(request(dto));

        assertEquals(CourseStatus.INSTRUCTOR_DRAFT, dto.getStatus());
        assertEquals("instructor-1", dto.getInstructorId());
    }

    @Test
    void reviewerCannotCreateReservedInstructorDraftStatus() {
        authenticate("admin-1", "COURSE_MANAGE", "COURSE_REVIEW");
        CourseServiceImpl service = service();
        CourseDto dto = new CourseDto();
        dto.setStatus(CourseStatus.INSTRUCTOR_DRAFT);
        CourseEntity entity = new CourseEntity();
        when(mapper.toEntity(dto)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(dto);

        service.create(request(dto));

        assertEquals(CourseStatus.DRAFT, dto.getStatus());
    }

    @Test
    void ownerCanSubmitInstructorDraftForReview() {
        authenticate("instructor-1", "COURSE_SUBMIT_REVIEW");
        CourseServiceImpl service = service();
        CourseEntity course = course("course-1", "instructor-1", CourseStatus.INSTRUCTOR_DRAFT);
        CourseDto savedDto = new CourseDto();
        savedDto.setStatus(CourseStatus.PENDING_REVIEW);
        when(repository.findEntityById("course-1")).thenReturn(Optional.of(course));
        when(repository.save(course)).thenReturn(course);
        when(mapper.toDto(course)).thenReturn(savedDto);

        service.submitReview("course-1");

        assertEquals(CourseStatus.PENDING_REVIEW, course.getStatus());
    }

    @Test
    void submitReviewUsesStrictOwnershipEvenForReviewer() {
        authenticate("admin-1", "COURSE_REVIEW", "COURSE_SUBMIT_REVIEW");
        CourseServiceImpl service = service();
        CourseEntity course = course("course-1", "instructor-1", CourseStatus.INSTRUCTOR_DRAFT);
        when(repository.findEntityById("course-1")).thenReturn(Optional.of(course));

        assertThrows(AccessDeniedException.class, () -> service.submitReview("course-1"));
        assertEquals(CourseStatus.INSTRUCTOR_DRAFT, course.getStatus());
    }

    @Test
    void reviewerCannotOpenInstructorDraftDirectly() {
        authenticate("admin-1", "COURSE_VIEW", "COURSE_REVIEW");
        CourseServiceImpl service = service();
        CourseEntity course = course("course-1", "instructor-1", CourseStatus.INSTRUCTOR_DRAFT);
        when(repository.findEntityById("course-1")).thenReturn(Optional.of(course));

        assertThrows(AccessDeniedException.class, () -> service.getDetails("course-1"));
    }

    @Test
    void reviewerCannotApproveInstructorDraftByKnownId() {
        authenticate("admin-1", "COURSE_REVIEW");
        CourseServiceImpl service = service();
        CourseEntity course = course("course-1", "instructor-1", CourseStatus.INSTRUCTOR_DRAFT);
        when(repository.findEntityById("course-1")).thenReturn(Optional.of(course));

        assertThrows(AccessDeniedException.class, () -> service.approveCourse("course-1"));
    }

    @Test
    void adminDraftIsVisibleToEveryReviewer() {
        CourseEntity legacyDraft = course("course-1", "instructor-1", CourseStatus.DRAFT);
        legacyDraft.setCreatedBy("admin-1");

        assertTrue(CourseVisibilityPolicy.isVisibleToReviewer(legacyDraft, "admin-1"));
        assertTrue(CourseVisibilityPolicy.isVisibleToReviewer(legacyDraft, "admin-2"));
        assertFalse(CourseVisibilityPolicy.isVisibleToReviewer(
                course("course-private", "instructor-1", CourseStatus.INSTRUCTOR_DRAFT), "admin-2"));
        assertTrue(CourseVisibilityPolicy.isVisibleToReviewer(
                course("course-2", "instructor-1", CourseStatus.PENDING_REVIEW), "admin-2"));
    }

    @Test
    void submitReviewEndpointRequiresDedicatedPermission() throws NoSuchMethodException {
        PreAuthorize preAuthorize = CourseController.class
                .getMethod("submitReview", String.class)
                .getAnnotation(PreAuthorize.class);

        assertEquals("hasAuthority('COURSE_SUBMIT_REVIEW')", preAuthorize.value());
    }

    @Test
    void internalReviewerVisibilityExcludesInstructorDraftStatus() {
        CourseServiceImpl service = service();
        when(repository.existsByIdAndStatusNotAndDeletedAtIsNull(
                "course-visible", CourseStatus.INSTRUCTOR_DRAFT)).thenReturn(true);
        when(repository.findIdsByStatusNotAndDeletedAtIsNull(CourseStatus.INSTRUCTOR_DRAFT))
                .thenReturn(List.of("course-visible", "course-pending"));

        assertTrue(service.isVisibleToReviewer("course-visible"));
        assertFalse(service.isVisibleToReviewer(" "));
        assertEquals(List.of("course-visible", "course-pending"),
                service.getReviewerVisibleCourseIds());
        verify(repository).existsByIdAndStatusNotAndDeletedAtIsNull(
                "course-visible", CourseStatus.INSTRUCTOR_DRAFT);
        verify(repository).findIdsByStatusNotAndDeletedAtIsNull(CourseStatus.INSTRUCTOR_DRAFT);
    }

    private CourseServiceImpl service() {
        CourseServiceImpl service = new CourseServiceImpl();
        ReflectionTestUtils.setField(service, "repository", repository);
        ReflectionTestUtils.setField(service, "mapper", mapper);
        return service;
    }

    private static BaseRequest<CourseDto> request(CourseDto dto) {
        BaseRequest<CourseDto> request = new BaseRequest<>();
        request.setData(dto);
        return request;
    }

    private static CourseEntity course(String id, String instructorId, CourseStatus status) {
        CourseEntity course = new CourseEntity();
        course.setId(id);
        course.setInstructorId(instructorId);
        course.setStatus(status);
        return course;
    }

    private static void authenticate(String userId, String... authorities) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userId,
                "n/a",
                List.of(authorities).stream().map(SimpleGrantedAuthority::new).toList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
