package vn.com.atomi.charge.course.service.impl;

import org.springframework.http.HttpStatus;
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

import java.util.Optional;

@Service
public class CourseServiceImpl
    extends BaseService<CourseRepository, CourseDto, CourseEntity, CourseMapper>
    implements CourseService {

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

}
