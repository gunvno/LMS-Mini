package vn.com.atomi.charge.quiz.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.com.atomi.charge.base.service.BaseService;
import vn.com.atomi.charge.quiz.mapper.QuizMapper;
import vn.com.atomi.charge.quiz.model.dto.QuizDto;
import vn.com.atomi.charge.quiz.model.entity.QuizEntity;
import vn.com.atomi.charge.quiz.model.enums.QuizStatus;
import vn.com.atomi.charge.quiz.repository.QuizAttemptRepository;
import vn.com.atomi.charge.quiz.repository.QuizRepository;
import vn.com.atomi.charge.quiz.client.CourseClient;
import vn.com.atomi.charge.quiz.service.interfaces.QuizService;
import vn.com.atomi.charge.quiz.service.internal.QuizConfigurationValidator;
import vn.com.atomi.charge.base.model.request.BaseRequest;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class QuizServiceImpl extends BaseService<QuizRepository, QuizDto, QuizEntity, QuizMapper>
implements QuizService {

    @Autowired
    private QuizAttemptRepository attemptRepository;
    @Autowired
    private CourseClient courseClient;
    @Autowired
    private QuizOwnershipService ownershipService;
    @Autowired
    private QuizConfigurationValidator configurationValidator;

    @Override
    public BaseResponse<Page<QuizDto>> getAll(Map<String, String> params, Pageable pageable) {
        if (ownershipService.canReviewCourses()) {
            return super.getAll(params, pageable);
        }

        QuizEntity scopedQuiz = resolveScopedQuiz(params);
        if (scopedQuiz != null) {
            ownershipService.assertCanViewQuiz(scopedQuiz);
            return super.getAll(params, pageable);
        }
        if (params != null && (params.containsKey("courseId") || params.containsKey("lessonId"))) {
            return super.getAll(params, pageable);
        }

        List<String> courseIds = ownershipService.getVisibleCourseIds();
        Page<QuizEntity> result = courseIds.isEmpty()
                ? Page.empty(pageable)
                : ownershipService.isStudentRequest()
                ? repository.findByCourseIdInAndStatusAndDeletedAtIsNull(courseIds, QuizStatus.ACTIVE, pageable)
                : repository.findByCourseIdInAndDeletedAtIsNull(courseIds, pageable);
        return BaseResponse.success(HttpStatus.OK, result.map(mapper::toDto));
    }

    @Override
    public BaseResponse<QuizDto> getDetails(String id) {
        QuizEntity quiz = repository.findEntityById(id)
                .orElseThrow(() -> new AccessDeniedException("common.access_denied"));
        ownershipService.assertCanViewQuiz(quiz);
        return BaseResponse.success(HttpStatus.OK, mapper.toDto(quiz));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<QuizDto> create(BaseRequest<QuizDto> request) {
        assertCanManageCourse(request.getData().getCourseId());
        if (request.getData().getStatus() == QuizStatus.ACTIVE) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("quiz.invalid_configuration"));
        }
        return super.create(request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<QuizDto> update(BaseRequest<QuizDto> request) {
        QuizEntity existing = repository.findEntityById(request.getData().getId())
                .orElseThrow(() -> new AccessDeniedException("common.access_denied"));
        assertCanManageCourse(existing.getCourseId());
        assertCanManageCourse(request.getData().getCourseId());
        if (request.getData().getStatus() == QuizStatus.ACTIVE
                && !configurationValidator.isValid(existing.getId())) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("quiz.invalid_configuration"));
        }
        return super.update(request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<QuizDto> delete(String id) {
        QuizEntity existing = repository.findEntityById(id)
                .orElseThrow(() -> new AccessDeniedException("common.access_denied"));
        assertCanManageCourse(existing.getCourseId());
        return super.delete(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<QuizDto> delete(List<String> ids) {
        ids.stream()
                .map(id -> repository.findEntityById(id)
                        .orElseThrow(() -> new AccessDeniedException("common.access_denied")))
                .map(QuizEntity::getCourseId)
                .distinct()
                .forEach(this::assertCanManageCourse);
        return super.delete(ids);
    }
    @Override
    public boolean completeQuizRequiredInCourse(String courseId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        if (courseId == null || courseId.isBlank() || userId == null || userId.isBlank()) {
            return false;
        }

        List<QuizEntity> quizRequiredList =
                repository.findByCourseIdAndRequiredToCompleteTrueAndStatusAndDeletedAtIsNull(
                        courseId,
                        QuizStatus.ACTIVE
                );

        for (QuizEntity quiz : quizRequiredList) {
            boolean passed = attemptRepository
                    .existsByQuizIdAndUserIdAndPassedTrueAndDeletedAtIsNull(quiz.getId(), userId);

            if (!passed) {
                return false;
            }
        }

        return true;
    }

    private void assertCanManageCourse(String courseId) {
        if (canReviewCourses()) {
            return;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication == null ? null : authentication.getName();
        if (userId == null || !Boolean.TRUE.equals(courseClient.isInstructorOwner(courseId, userId))) {
            throw new AccessDeniedException("common.access_denied");
        }
    }

    private boolean canReviewCourses() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "COURSE_REVIEW".equals(authority.getAuthority()));
    }

    private QuizEntity resolveScopedQuiz(Map<String, String> params) {
        if (params == null) {
            return null;
        }
        String courseId = params.get("courseId");
        if (courseId != null && !courseId.isBlank()) {
            return repository.findByCourseIdInAndDeletedAtIsNull(List.of(courseId), Pageable.ofSize(1))
                    .stream().findFirst().orElse(null);
        }
        String lessonId = params.get("lessonId");
        if (lessonId != null && !lessonId.isBlank()) {
            return repository.findFirstByLessonIdAndDeletedAtIsNull(lessonId).orElse(null);
        }
        return null;
    }
}
