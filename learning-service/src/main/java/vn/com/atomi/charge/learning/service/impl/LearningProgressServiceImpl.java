package vn.com.atomi.charge.learning.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.com.atomi.charge.base.model.request.BaseRequest;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.base.service.BaseService;
import vn.com.atomi.charge.learning.mapper.LearningProgressMapper;
import vn.com.atomi.charge.learning.model.dto.EnrollmentDto;
import vn.com.atomi.charge.learning.model.dto.LearningProgressDto;
import vn.com.atomi.charge.learning.model.entity.EnrollmentEntity;
import vn.com.atomi.charge.learning.model.entity.LearningProgressEntity;
import vn.com.atomi.charge.learning.model.enums.LearningProgressStatus;
import vn.com.atomi.charge.learning.repository.Client.CourseClient;
import vn.com.atomi.charge.learning.repository.EnrollmentRepository;
import vn.com.atomi.charge.learning.repository.LearningProgressRepository;
import vn.com.atomi.charge.learning.service.interfaces.LearningProgressService;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LearningProgressServiceImpl extends BaseService<LearningProgressRepository, LearningProgressDto, LearningProgressEntity, LearningProgressMapper>
implements LearningProgressService {

    private final CourseClient courseClient;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    public BaseResponse<LearningProgressDto> startLesson(String lessonId){
        response = new BaseResponse<>();
        if(!courseClient.existsLessonById(lessonId)){
            String localizedMsg = i18n.getMessage("lesson.not_found");
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, localizedMsg);
        }
        String courseId = courseClient.getCourseByLessonId(lessonId);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        Optional<EnrollmentEntity> optionalEnrollment = enrollmentRepository.findByUserIdAndCourseIdAndDeletedAtIsNull(userId, courseId);
        if(optionalEnrollment.isEmpty()){
            String localizedMsg = i18n.getMessage("learning.not_found");
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, localizedMsg);
        }
        Optional<LearningProgressEntity> optionalProgress =
                repository.findByEnrollmentIdAndLessonIdAndDeletedAtIsNull(optionalEnrollment.get().getId(), lessonId);
        if (optionalProgress.isEmpty()) {
            String localizedMsg = i18n.getMessage("learning.not_found");
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, localizedMsg);
        }
        LearningProgressEntity entity = optionalProgress.get();
        if (entity.getStatus() != LearningProgressStatus.NOT_STARTED) {
            String localizedMsg = i18n.getMessage("learning.invalid_status");
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, localizedMsg);
        }
        entity.setStatus(LearningProgressStatus.IN_PROGRESS);
        entity.setStartedAt(LocalDateTime.now());
        LearningProgressEntity saved = repository.save(entity);
        response.setStatus(HttpStatus.OK);
        response.setData(mapper.toDto(saved));
        return response;
    }

    @Override
    public BaseResponse<LearningProgressDto> finishLesson(String lessonId){
        response = new BaseResponse<>();
        if(!courseClient.existsLessonById(lessonId)){
            String localizedMsg = i18n.getMessage("lesson.not_found");
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, localizedMsg);
        }
        String courseId = courseClient.getCourseByLessonId(lessonId);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        Optional<EnrollmentEntity> optionalEnrollment = enrollmentRepository.findByUserIdAndCourseIdAndDeletedAtIsNull(userId, courseId);
        if(optionalEnrollment.isEmpty()){
            String localizedMsg = i18n.getMessage("learning.not_found");
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, localizedMsg);
        }
        Optional<LearningProgressEntity> optionalProgress =
                repository.findByEnrollmentIdAndLessonIdAndDeletedAtIsNull(optionalEnrollment.get().getId(), lessonId);
        if (optionalProgress.isEmpty()) {
            String localizedMsg = i18n.getMessage("learning.not_found");
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, localizedMsg);
        }
        LearningProgressEntity entity = optionalProgress.get();
        if (entity.getStatus() == LearningProgressStatus.COMPLETED) {
            response.setStatus(HttpStatus.OK);
            response.setData(mapper.toDto(entity));
            return response;
        }

        double countLesson = courseClient.countLessonInCourse(courseId);
        if (countLesson <= 0) {
            String localizedMsg = i18n.getMessage("lesson.not_found");
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, localizedMsg);
        }

        entity.setStatus(LearningProgressStatus.COMPLETED);
        entity.setCompletedAt(LocalDateTime.now());
        LearningProgressEntity saved = repository.save(entity);
        EnrollmentEntity enrollment = optionalEnrollment.get();
        double progressPercentAfter = enrollment.getProgressPercent() == null
                ? 0.0
                : enrollment.getProgressPercent();
        double progressPercentBefore = Math.min(100.0, progressPercentAfter + 100.0 / countLesson);
        enrollment.setProgressPercent(progressPercentBefore);
        enrollmentRepository.save(enrollment);


        response.setStatus(HttpStatus.OK);
        response.setData(mapper.toDto(saved));
        return response;
    }

}
