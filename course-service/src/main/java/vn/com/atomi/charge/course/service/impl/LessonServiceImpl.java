package vn.com.atomi.charge.course.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import vn.com.atomi.charge.base.model.enums.BaseErrorCode;
import vn.com.atomi.charge.base.model.request.BaseRequest;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.base.service.BaseService;
import vn.com.atomi.charge.base.util.StringUtil;
import vn.com.atomi.charge.course.mapper.LessonMapper;
import vn.com.atomi.charge.course.model.dto.LessonDto;
import vn.com.atomi.charge.course.model.entity.CourseEntity;
import vn.com.atomi.charge.course.model.entity.LessonEntity;
import vn.com.atomi.charge.course.repository.CourseRepository;
import vn.com.atomi.charge.course.repository.LessonRepository;
import vn.com.atomi.charge.course.service.interfaces.CourseService;
import vn.com.atomi.charge.course.service.interfaces.LessonService;

import java.util.List;
import java.util.Optional;

@Service
public class LessonServiceImpl
    extends BaseService<LessonRepository, LessonDto, LessonEntity, LessonMapper>
    implements LessonService {

    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private CourseService courseService;

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
    public BaseResponse<LessonDto> createLesson(BaseRequest<LessonDto> dto, String courseId) {
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
        responsePage = new BaseResponse<>();
        try {
            if (!StringUtils.hasText(courseId)) {
                return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("course.not_found"));
            }

            Optional<CourseEntity> optionalCourse = courseRepository.findEntityById(courseId);
            if (optionalCourse.isEmpty()) {
                return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("course.not_found"));
            }

            Sort sort = Sort.by(Sort.Direction.ASC, "orderIndex")
                .and(Sort.by(Sort.Direction.ASC, "createdDate"));
            Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
            Page<LessonEntity> lessons = repository.findByCourseIdAndDeletedAtIsNull(courseId, sortedPageable);

            responsePage.setStatus(HttpStatus.OK);
            responsePage.setData(lessons.map(mapper::toDto));
        } catch (Exception ex) {
            responsePage.setStatus(HttpStatus.BAD_REQUEST);
            responsePage.setErrorCode(BaseErrorCode.FAILURE.getErrorCode());
            responsePage.setMessage(StringUtil.beautyError(ex));
        }
        return responsePage;
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
}


