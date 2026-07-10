package vn.com.atomi.charge.course.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import vn.com.atomi.charge.base.model.request.BaseRequest;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.base.service.BaseService;
import vn.com.atomi.charge.course.mapper.CourseMapper;
import vn.com.atomi.charge.course.model.dto.CourseDto;
import vn.com.atomi.charge.course.model.entity.CourseEntity;
import vn.com.atomi.charge.course.model.enums.CourseStatus;
import vn.com.atomi.charge.course.model.request.RejectCourseRequest;
import vn.com.atomi.charge.course.repository.CourseRepository;
import vn.com.atomi.charge.course.service.interfaces.CourseService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CourseServiceImpl
    extends BaseService<CourseRepository, CourseDto, CourseEntity, CourseMapper>
    implements CourseService {

    @Override
    public BaseResponse<Page<CourseDto>> getAll(Map<String, String> params, Pageable pageable) {
        if (canReviewCourses()) {
            return super.getAll(params, pageable);
        }
        Sort sort = Sort.by(Sort.Direction.DESC, "lastModifiedDate", "createdDate");
        Pageable sorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        Page<CourseEntity> result = canManageCourses()
                ? repository.findByInstructorIdAndDeletedAtIsNull(currentUserId(), sorted)
                : repository.findByStatusAndDeletedAtIsNull(CourseStatus.PUBLISHED, sorted);
        return BaseResponse.success(HttpStatus.OK, result.map(mapper::toDto));
    }

    @Override
    public BaseResponse<CourseDto> getDetails(String id) {
        if (canReviewCourses()) {
            return super.getDetails(id);
        }
        CourseEntity course = repository.findEntityById(id)
                .orElseThrow(() -> new AccessDeniedException("common.access_denied"));
        if (canManageCourses()) {
            assertInstructorOwns(course);
        } else if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new AccessDeniedException("common.access_denied");
        }
        return BaseResponse.success(HttpStatus.OK, mapper.toDto(course));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<CourseDto> create(BaseRequest<CourseDto> request) {
        if (!canReviewCourses()) {
            request.getData().setStatus(CourseStatus.DRAFT);
            request.getData().setInstructorId(currentUserId());
        }
        return super.create(request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<CourseDto> update(BaseRequest<CourseDto> request) {
        if (!canReviewCourses()) {
            CourseEntity existing = repository.findEntityById(request.getData().getId())
                    .orElseThrow(() -> new AccessDeniedException("common.access_denied"));
            assertInstructorOwns(existing);
            request.getData().setInstructorId(existing.getInstructorId());
            request.getData().setStatus(existing.getStatus());
            request.getData().setPublishedAt(existing.getPublishedAt());
            request.getData().setRejectReason(existing.getRejectReason());
        }
        return super.update(request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<CourseDto> delete(String id) {
        if (!canReviewCourses()) {
            CourseEntity course = repository.findEntityById(id)
                    .orElseThrow(() -> new AccessDeniedException("common.access_denied"));
            assertInstructorOwns(course);
        }
        return super.delete(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<CourseDto> delete(List<String> ids) {
        if (!canReviewCourses()) {
            ids.stream()
                    .map(id -> repository.findEntityById(id)
                            .orElseThrow(() -> new AccessDeniedException("common.access_denied")))
                    .forEach(this::assertInstructorOwns);
        }
        return super.delete(ids);
    }

    @Override
    protected boolean isDuplicate(BaseRequest<CourseDto> request) {
        CourseDto dto = request.getData();
        if (dto.getCode() == null || dto.getCode().isBlank()) {
            return false;
        }
        if (dto.getId() == null) {
            return repository.existsByCodeAndDeletedAtIsNull(dto.getCode());
        }
        return repository.existsByCodeAndIdNotAndDeletedAtIsNull(dto.getCode(), dto.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<CourseDto> submitReview(String id) {
        if (!StringUtils.hasText(id)) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("course.not_found"));
        }

        Optional<CourseEntity> optionalCourse = repository.findEntityById(id);

        if (optionalCourse.isEmpty()) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("course.not_found"));
        }

        CourseEntity course = optionalCourse.get();

        assertInstructorOwns(course);

        if (!canSubmitReview(course)) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("course.invalid_submit_review_status"));
        }

        course.setStatus(CourseStatus.PENDING_REVIEW);
        course.setRejectReason(null);

        CourseEntity saved = repository.save(course);
        return BaseResponse.success(HttpStatus.OK, mapper.toDto(saved));
    }

    private boolean canSubmitReview(CourseEntity course) {
        return course.getStatus() == CourseStatus.DRAFT || course.getStatus() == CourseStatus.REJECTED;
    }

    private boolean canReviewCourses() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "COURSE_REVIEW".equals(authority.getAuthority()));
    }

    private boolean canManageCourses() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "COURSE_MANAGE".equals(authority.getAuthority()));
    }

    private String currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !StringUtils.hasText(authentication.getName())) {
            throw new AccessDeniedException("common.access_denied");
        }
        return authentication.getName();
    }

    private void assertInstructorOwns(CourseEntity course) {
        if (!canReviewCourses() && !currentUserId().equals(course.getInstructorId())) {
            throw new AccessDeniedException("common.access_denied");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<CourseDto> approveCourse(String id) {
        if (!StringUtils.hasText(id)) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("course.not_found"));
        }

        Optional<CourseEntity> optionalCourse = repository.findEntityById(id);

        if (optionalCourse.isEmpty()) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("course.not_found"));
        }

        CourseEntity course = optionalCourse.get();

        if (!course.getStatus().equals(CourseStatus.PENDING_REVIEW)) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("course.invalid_approve_course_status"));
        }

        course.setStatus(CourseStatus.PUBLISHED);
        course.setRejectReason(null);

        CourseEntity saved = repository.save(course);
        return BaseResponse.success(HttpStatus.OK, mapper.toDto(saved));
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<CourseDto> rejectCourse(RejectCourseRequest request) {
        if (!StringUtils.hasText(request.getCourseId())) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("course.not_found"));
        }

        Optional<CourseEntity> optionalCourse = repository.findEntityById(request.getCourseId());

        if (optionalCourse.isEmpty()) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("course.not_found"));
        }

        CourseEntity course = optionalCourse.get();

        if (!course.getStatus().equals(CourseStatus.PENDING_REVIEW)) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("course.invalid_approve_course_status"));
        }

        course.setStatus(CourseStatus.REJECTED);
        course.setRejectReason(request.getReasonReject());

        CourseEntity saved = repository.save(course);
        return BaseResponse.success(HttpStatus.OK, mapper.toDto(saved));
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<CourseDto> archiveCourse(String id) {
        if (!StringUtils.hasText(id)) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("course.not_found"));
        }

        Optional<CourseEntity> optionalCourse = repository.findEntityById(id);

        if (optionalCourse.isEmpty()) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("course.not_found"));
        }

        CourseEntity course = optionalCourse.get();

        if (!course.getStatus().equals(CourseStatus.PUBLISHED)) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("course.invalid_archive_course_status"));
        }

        course.setStatus(CourseStatus.ARCHIVED);
        course.setRejectReason(null);

        CourseEntity saved = repository.save(course);
        return BaseResponse.success(HttpStatus.OK, mapper.toDto(saved));
    }

    @Override
    public Boolean checkCourse(String courseId){
        if(courseId.isBlank()){
            return false;
        }
        Optional<CourseEntity> optionalCourse = repository.findEntityById(courseId);
        return optionalCourse.isPresent();
    }

    @Override
    public Boolean checkPublishedCourse(String courseId) {
        if (!StringUtils.hasText(courseId)) {
            return false;
        }
        return repository.existsByIdAndStatusAndDeletedAtIsNull(courseId, CourseStatus.PUBLISHED);
    }

    @Override
    public Boolean isInstructorOwner(String courseId, String userId) {
        if (!StringUtils.hasText(courseId) || !StringUtils.hasText(userId)) {
            return false;
        }
        return repository.findEntityById(courseId)
                .map(course -> userId.equals(course.getInstructorId()))
                .orElse(false);
    }

    @Override
    public BaseResponse<Page<CourseDto>> getPublishedCourses(Pageable pageable) {
        Page<CourseEntity> courses = repository.findByStatusAndDeletedAtIsNull(CourseStatus.PUBLISHED, pageable);
        return BaseResponse.success(HttpStatus.OK, courses.map(mapper::toDto));
    }

    @Override
    public BaseResponse<CourseDto> getPublishedCourseDetails(String id) {
        if (!StringUtils.hasText(id)) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("course.not_found"));
        }
        Optional<CourseEntity> course = repository.findByIdAndStatusAndDeletedAtIsNull(id, CourseStatus.PUBLISHED);
        if (course.isEmpty()) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("course.not_found"));
        }
        return BaseResponse.success(HttpStatus.OK, mapper.toDto(course.get()));
    }

}
