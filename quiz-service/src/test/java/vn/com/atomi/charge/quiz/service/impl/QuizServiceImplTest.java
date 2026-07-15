package vn.com.atomi.charge.quiz.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.quiz.mapper.QuizMapper;
import vn.com.atomi.charge.quiz.model.dto.QuizDto;
import vn.com.atomi.charge.quiz.model.entity.QuizEntity;
import vn.com.atomi.charge.quiz.repository.QuizRepository;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuizServiceImplTest {

    @Mock
    private QuizRepository repository;

    @Mock
    private QuizMapper mapper;

    @Mock
    private QuizOwnershipService ownershipService;

    @Test
    void reviewerListIsRestrictedToCourseServiceVisibleIds() {
        QuizServiceImpl service = service();
        Pageable pageable = PageRequest.of(0, 20);
        QuizEntity quiz = new QuizEntity();
        quiz.setCourseId("course-visible");
        QuizDto dto = new QuizDto();
        when(ownershipService.canReviewCourses()).thenReturn(true);
        when(ownershipService.getVisibleCourseIds()).thenReturn(List.of("course-visible"));
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(quiz)));
        when(mapper.toDto(quiz)).thenReturn(dto);

        BaseResponse<org.springframework.data.domain.Page<QuizDto>> response =
                service.getAll(Map.of("status", "DRAFT"), pageable);

        assertEquals(List.of(dto), response.getData().getContent());
        verify(ownershipService).getVisibleCourseIds();
        verify(repository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void reviewerListIsEmptyWithoutAnyVisibleCourse() {
        QuizServiceImpl service = service();
        Pageable pageable = PageRequest.of(0, 20);
        when(ownershipService.canReviewCourses()).thenReturn(true);
        when(ownershipService.getVisibleCourseIds()).thenReturn(List.of());

        BaseResponse<org.springframework.data.domain.Page<QuizDto>> response =
                service.getAll(Map.of(), pageable);

        assertEquals(0, response.getData().getTotalElements());
        verify(repository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    private QuizServiceImpl service() {
        QuizServiceImpl service = new QuizServiceImpl();
        ReflectionTestUtils.setField(service, "repository", repository);
        ReflectionTestUtils.setField(service, "mapper", mapper);
        ReflectionTestUtils.setField(service, "ownershipService", ownershipService);
        return service;
    }
}
