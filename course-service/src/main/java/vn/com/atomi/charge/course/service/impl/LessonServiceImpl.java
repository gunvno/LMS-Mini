package vn.com.atomi.charge.course.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
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
import vn.com.atomi.charge.base.model.enums.BaseErrorCode;
import vn.com.atomi.charge.base.model.request.BaseRequest;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.base.service.BaseService;
import vn.com.atomi.charge.base.util.StringUtil;
import vn.com.atomi.charge.course.mapper.LessonMapper;
import vn.com.atomi.charge.course.client.LearningClient;
import vn.com.atomi.charge.course.model.dto.LessonDto;
import vn.com.atomi.charge.course.model.entity.CourseEntity;
import vn.com.atomi.charge.course.model.entity.LessonEntity;
import vn.com.atomi.charge.course.model.enums.CourseStatus;
import vn.com.atomi.charge.course.repository.CourseRepository;
import vn.com.atomi.charge.course.repository.LessonRepository;
import vn.com.atomi.charge.course.service.interfaces.CourseService;
import vn.com.atomi.charge.course.service.interfaces.LessonService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class LessonServiceImpl
    extends BaseService<LessonRepository, LessonDto, LessonEntity, LessonMapper>
    implements LessonService {

    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private CourseService courseService;
    @Autowired
    private LearningClient learningClient;

    @Override
    public BaseResponse<Page<LessonDto>> getAll(Map<String, String> params, Pageable pageable) {
        if (canReviewCourses()) {
            return super.getAll(params, pageable);
        }
        if (!isInstructorRequest()) {
            throw new AccessDeniedException("common.access_denied");
        }
        Sort sort = Sort.by(Sort.Direction.ASC, "orderIndex")
                .and(Sort.by(Sort.Direction.ASC, "createdDate"));
        Pageable sorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        return BaseResponse.success(HttpStatus.OK,
                repository.findByInstructorId(currentUserId(), sorted).map(mapper::toDto));
    }

    @Override
    public BaseResponse<LessonDto> getDetails(String id) {
        LessonEntity lesson = repository.findEntityById(id)
                .orElseThrow(() -> new AccessDeniedException("common.access_denied"));
        LessonDto dto = mapper.toDto(lesson);
        if (canReviewCourses()) {
            dto.setLocked(false);
        } else if (isInstructorRequest()) {
            assertCanManageCourse(lesson.getCourseId());
            dto.setLocked(false);
        } else {
            assertStudentCanAccessCourse(lesson.getCourseId());
            List<String> accessibleLessonIds = learningClient.getAccessibleLessonIds(lesson.getCourseId());
            if (accessibleLessonIds == null || !accessibleLessonIds.contains(lesson.getId())) {
                throw new AccessDeniedException("common.access_denied");
            }
            dto.setLocked(false);
        }
        return BaseResponse.success(HttpStatus.OK, dto);
    }

    @Override
    protected boolean isDuplicate(BaseRequest<LessonDto> request) {
        LessonDto dto = request.getData();
        if (dto.getCourseId() == null || dto.getCode() == null || dto.getCode().isBlank()) {
            return false;
        }
        if (dto.getId() == null) {
            return repository.existsByCourseIdAndCodeAndDeletedAtIsNull(dto.getCourseId(), dto.getCode());
        }
        return repository.existsByCourseIdAndCodeAndIdNotAndDeletedAtIsNull(
            dto.getCourseId(), dto.getCode(), dto.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<LessonDto> create(BaseRequest<LessonDto> dto) {
        assertCanManageCourse(dto.getData().getCourseId());
        BaseResponse<LessonDto> result = super.create(dto);
        if (result != null && result.getData() != null) {
            syncCourseDuration(result.getData().getCourseId());
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<LessonDto> update(BaseRequest<LessonDto> dto) {
        String oldCourseId = null;
        if (dto != null && dto.getData() != null && StringUtils.hasText(dto.getData().getId())) {
            oldCourseId = repository.findEntityById(dto.getData().getId())
                .map(LessonEntity::getCourseId)
                .orElse(null);
        }

        if (StringUtils.hasText(oldCourseId)) {
            assertCanManageCourse(oldCourseId);
        }
        assertCanManageCourse(dto.getData().getCourseId());
        BaseResponse<LessonDto> result = super.update(dto);
        if (result != null && result.getData() != null) {
            syncCourseDuration(result.getData().getCourseId());
            if (StringUtils.hasText(oldCourseId) && !oldCourseId.equals(result.getData().getCourseId())) {
                syncCourseDuration(oldCourseId);
            }
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<LessonDto> delete(String id) {
        String courseId = repository.findEntityById(id)
            .map(LessonEntity::getCourseId)
            .orElse(null);

        assertCanManageCourse(courseId);
        BaseResponse<LessonDto> result = super.delete(id);
        if (result != null && result.getStatus() != null && result.getStatus().is2xxSuccessful()) {
            syncCourseDuration(courseId);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<LessonDto> delete(List<String> ids) {
        List<String> courseIds = ids == null
            ? List.of()
            : ids.stream()
                .map(id -> repository.findEntityById(id).map(LessonEntity::getCourseId).orElse(null))
                .filter(StringUtils::hasText)
                .distinct()
                .toList();

        courseIds.forEach(this::assertCanManageCourse);

        BaseResponse<LessonDto> result = super.delete(ids);
        if (result != null && result.getStatus() != null && result.getStatus().is2xxSuccessful()) {
            courseIds.forEach(this::syncCourseDuration);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<LessonDto> createLesson(BaseRequest<LessonDto> dto, String courseId) {
        assertCanManageCourse(courseId);
        response = new BaseResponse<>();
        try {
            getRequest();

            dto.getData().setCourseId(courseId);

            if (isDuplicate(dto)) {
                String localizedMsg = i18n.getMessage("common.already_exists");
                return BaseResponse.fail(HttpStatus.BAD_REQUEST, localizedMsg);
            }

            Optional<CourseEntity> optionalCourse = courseRepository.findEntityById(courseId);
            if(optionalCourse.isEmpty()){
                return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("course.not_found"));
            }
            LessonEntity saved = (LessonEntity) repository.save(mapper.toEntity(dto.getData()));
            syncCourseDuration(courseId);
            response.setStatus(HttpStatus.OK);
            response.setData((LessonDto) mapper.toDto(saved));
        } catch (Exception ex) {
            response.setStatus(HttpStatus.BAD_REQUEST);
            response.setErrorCode(BaseErrorCode.FAILURE.getErrorCode());
            response.setMessage(StringUtil.beautyError(ex));
        }
        return response;
    }
    @Override
    public BaseResponse<Page<LessonDto>> getLessonByCourseId(String courseId, Pageable pageable) {
        if (!StringUtils.hasText(courseId)) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("course.not_found"));
        }

        Optional<CourseEntity> optionalCourse = courseRepository.findEntityById(courseId);
        if (optionalCourse.isEmpty()) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("course.not_found"));
        }
        boolean studentRequest = !isInstructorRequest() && !canReviewCourses();
        List<String> accessibleLessonIds = List.of();
        if (isInstructorRequest()) {
            assertCanManageCourse(courseId);
        } else if (studentRequest) {
            assertPublishedCourse(courseId);
            if (Boolean.TRUE.equals(learningClient.hasCourseAccess(courseId))) {
                List<String> responseIds = learningClient.getAccessibleLessonIds(courseId);
                accessibleLessonIds = responseIds == null ? List.of() : responseIds;
            }
        }

        Sort sort = Sort.by(Sort.Direction.ASC, "orderIndex")
                .and(Sort.by(Sort.Direction.ASC, "createdDate"));
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        Page<LessonEntity> lessons = repository.findByCourseIdAndDeletedAtIsNull(courseId, sortedPageable);

        Page<LessonDto> result = lessons.map(mapper::toDto);
        if (studentRequest) {
            List<String> finalAccessibleLessonIds = accessibleLessonIds;
            result.forEach(dto -> {
                boolean locked = !finalAccessibleLessonIds.contains(dto.getId());
                dto.setLocked(locked);
                if (locked) {
                    dto.setContent(null);
                    dto.setVideoUrl(null);
                }
            });
        } else {
            result.forEach(dto -> dto.setLocked(false));
        }
        return BaseResponse.success(HttpStatus.OK, result);
    }
    @Override
    public Boolean checkLesson(String lessonId){
        if(lessonId.isBlank()){
            return false;
        }
        Optional<LessonEntity> optionalLesson = repository.findEntityById(lessonId);
        return optionalLesson.isPresent();
    }
    @Override
    public String getCourseByLessonId(String lessonId){
        if(!checkLesson(lessonId)){
            return i18n.getMessage("lesson.not_found");
        }
        Optional<LessonEntity> optionalLesson = repository.findEntityById(lessonId);
        if(optionalLesson.isEmpty()){
            return i18n.getMessage("lesson.not_found");
        }
        String courseId = optionalLesson.get().getCourseId();
        if(courseId.isEmpty()) return i18n.getMessage("course.not_found");
        return courseId;
    }

    @Override
    public List<LessonDto> getLessonsByCourseIdNoPage(String courseId){
            if (!StringUtils.hasText(courseId)) {
                return List.of();
            }

            Optional<CourseEntity> optionalCourse = courseRepository.findEntityById(courseId);
            if (optionalCourse.isEmpty()) {
                return List.of();
            }

            return mapper.toDto(repository.findByCourseIdAndDeletedAtIsNullOrderByOrderIndexAscCreatedDateAsc(courseId));
    }

    @Override
    public Double countLessonInCourse(String courseId) {
        if(!courseService.checkCourse(courseId)){
            return 0.0;
        }
        return (double) repository.countByCourseIdAndDeletedAtIsNull(courseId);
    }

    private void syncCourseDuration(String courseId) {
        if (!StringUtils.hasText(courseId)) {
            return;
        }
        Optional<CourseEntity> optionalCourse = courseRepository.findEntityById(courseId);
        if (optionalCourse.isEmpty()) {
            return;
        }
        CourseEntity course = optionalCourse.get();
        course.setDurationMinutes(repository.sumDurationMinutesByCourseId(courseId));
        courseRepository.save(course);
    }

    private void assertCanManageCourse(String courseId) {
        if (!isInstructorRequest() || canReviewCourses()) {
            return;
        }
        CourseEntity course = StringUtils.hasText(courseId)
                ? courseRepository.findEntityById(courseId).orElse(null)
                : null;
        if (course == null || !currentUserId().equals(course.getInstructorId())) {
            throw new AccessDeniedException("common.access_denied");
        }
    }

    private void assertStudentCanAccessCourse(String courseId) {
        CourseEntity course = courseRepository.findEntityById(courseId).orElse(null);
        if (course == null
                || course.getStatus() != CourseStatus.PUBLISHED
                || !Boolean.TRUE.equals(learningClient.hasCourseAccess(courseId))) {
            throw new AccessDeniedException("common.access_denied");
        }
    }

    private void assertPublishedCourse(String courseId) {
        CourseEntity course = courseRepository.findEntityById(courseId).orElse(null);
        if (course == null || course.getStatus() != CourseStatus.PUBLISHED) {
            throw new AccessDeniedException("common.access_denied");
        }
    }

    private boolean isInstructorRequest() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "LESSON_MANAGE".equals(authority.getAuthority()));
    }

    private boolean canReviewCourses() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "COURSE_REVIEW".equals(authority.getAuthority()));
    }

    private String currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !StringUtils.hasText(authentication.getName())) {
            throw new AccessDeniedException("common.access_denied");
        }
        return authentication.getName();
    }
}
