package vn.com.atomi.charge.learning.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.atomi.charge.base.model.enums.BaseErrorCode;
import vn.com.atomi.charge.base.model.request.BaseRequest;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.base.service.BaseService;
import vn.com.atomi.charge.base.util.StringUtil;
import vn.com.atomi.charge.learning.mapper.EnrollmentMapper;
import vn.com.atomi.charge.learning.model.dto.AuthnUserDto;
import vn.com.atomi.charge.learning.model.dto.EnrollmentDto;
import vn.com.atomi.charge.learning.model.dto.LessonDto;
import vn.com.atomi.charge.learning.model.entity.CertificateEntity;
import vn.com.atomi.charge.learning.model.entity.EnrollmentEntity;
import vn.com.atomi.charge.learning.model.entity.LearningProgressEntity;
import vn.com.atomi.charge.learning.model.enums.CertificateStatus;
import vn.com.atomi.charge.learning.model.enums.EnrollmentStatus;
import vn.com.atomi.charge.learning.model.enums.LearningProgressStatus;
import vn.com.atomi.charge.learning.repository.CertificateRepository;
import vn.com.atomi.charge.learning.repository.Client.AuthnClient;
import vn.com.atomi.charge.learning.repository.Client.CourseClient;
import vn.com.atomi.charge.learning.repository.Client.QuizClient;
import vn.com.atomi.charge.learning.repository.EnrollmentRepository;
import vn.com.atomi.charge.learning.repository.LearningProgressRepository;
import vn.com.atomi.charge.learning.service.interfaces.EnrollmentService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl extends BaseService<EnrollmentRepository,
        EnrollmentDto, EnrollmentEntity, EnrollmentMapper> implements EnrollmentService {

    private final CourseClient courseClient;
    private final AuthnClient authnClient;
    private final LearningProgressRepository learningProgressRepository;
    private final CertificateRepository certificateRepository;
    private final QuizClient quizClient;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<EnrollmentDto> enrollCourse(BaseRequest<EnrollmentDto> dto, String courseId){
        response = new BaseResponse<>();
        try {
            getRequest();
            String userId = currentUserId();
            if (userId == null || userId.isBlank()) {
                return BaseResponse.fail(HttpStatus.UNAUTHORIZED, i18n.getMessage("user.not_found"));
            }

            if (!Boolean.TRUE.equals(authnClient.checkUser(userId))) {
                return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("user.not_found"));
            }

            if (dto == null) {
                dto = new BaseRequest<>();
            }
            if (dto.getData() == null) {
                dto.setData(new EnrollmentDto());
            }

            dto.getData().setUserId(userId);
            dto.getData().setCourseId(courseId);

            if (isDuplicate(dto)) {
                String localizedMsg = i18n.getMessage("common.already_exists");
                return BaseResponse.fail(HttpStatus.BAD_REQUEST, localizedMsg);
            }

            Boolean courseExists = courseClient.existsPublishedCourseById(courseId);
            if (!Boolean.TRUE.equals(courseExists)) {
                return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("course.not_found"));
            }

            dto.getData().setEnrolledAt(LocalDateTime.now());
            dto.getData().setCompletedAt(null);
            dto.getData().setProgressPercent(0.0);
            dto.getData().setStatus(EnrollmentStatus.ACTIVE);
            EnrollmentEntity saved = repository.save(mapper.toEntity(dto.getData()));
            response.setStatus(HttpStatus.OK);
            response.setData(mapper.toDto(saved));
            String enrollmentId = saved.getId();
            List<LessonDto> lessons = courseClient.getLessonByCourseId(courseId);
            List<LearningProgressEntity> progresses = new ArrayList<>();

            for (LessonDto lesson : lessons) {
                LearningProgressEntity progress = new LearningProgressEntity();
                progress.setEnrollmentId(enrollmentId);
                progress.setLessonId(lesson.getId());
                progress.setStatus(LearningProgressStatus.NOT_STARTED);
                progress.setStartedAt(null);
                progress.setCompletedAt(null);

                progresses.add(progress);
            }

            learningProgressRepository.saveAll(progresses);
        } catch (Exception ex) {
            response.setStatus(HttpStatus.BAD_REQUEST);
            response.setErrorCode(BaseErrorCode.FAILURE.getErrorCode());
            response.setMessage(StringUtil.beautyError(ex));
        }


        return response;
    }

    private String currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? null : authentication.getName();
    }
    @Override
    protected boolean isDuplicate(BaseRequest<EnrollmentDto> request) {
        EnrollmentDto dto = request.getData();
        if (dto.getCourseId() == null || dto.getUserId() == null || dto.getUserId().isBlank()) {
            return false;
        }
        if (dto.getId() == null) {
            return repository.existsByCourseIdAndUserIdAndDeletedAtIsNull(dto.getCourseId(), dto.getUserId());
        }
        return repository.existsByCourseIdAndUserIdAndIdNotAndDeletedAtIsNull(
                dto.getCourseId(), dto.getUserId(), dto.getId());
    }
    @Override
    public BaseResponse<Page<EnrollmentDto>> getMyEnroll(Pageable pageable){
        responsePage = new BaseResponse<>();

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication == null ? null : authentication.getName();

            if (userId == null || userId.isBlank()) {
                String localizedMsg = i18n.getMessage("user.not_found");
                return BaseResponse.fail(HttpStatus.BAD_REQUEST, localizedMsg);
            }

            if (!Boolean.TRUE.equals(authnClient.checkUser(userId))) {
                String localizedMsg = i18n.getMessage("user.not_found");
                return BaseResponse.fail(HttpStatus.BAD_REQUEST, localizedMsg);
            }
            Sort sort = Sort.by(Sort.Direction.ASC, "createdDate");
            Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
            Page<EnrollmentEntity> enrollments =
                    repository.findByUserIdAndDeletedAtIsNull(userId, sortedPageable);

            responsePage.setStatus(HttpStatus.OK);
            responsePage.setData(enrollments.map(mapper::toDto));
        } catch (Exception ex) {
            responsePage.setStatus(HttpStatus.BAD_REQUEST);
            responsePage.setErrorCode(BaseErrorCode.FAILURE.getErrorCode());
            responsePage.setMessage(StringUtil.beautyError(ex));
        }
        return responsePage;
    }
    @Override
    public BaseResponse<EnrollmentDto> finishCourse(String courseId){
        response = new BaseResponse<>();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        BaseResponse<AuthnUserDto> userDto = authnClient.getUserById(userId);
        AuthnUserDto user = userDto.getData();
        Optional<EnrollmentEntity> optionalEnrollment = repository.findByUserIdAndCourseIdAndDeletedAtIsNull(userId, courseId);
        if(optionalEnrollment.isEmpty()){
            String localizedMsg = i18n.getMessage("learning.not_found");
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, localizedMsg);
        }
        if(optionalEnrollment.get().getProgressPercent() < 100.0 && optionalEnrollment.get().getStatus() != EnrollmentStatus.COMPLETED){
            String localizedMsg = i18n.getMessage("course.not_finish");
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, localizedMsg);
        }
        boolean quizCompleted = quizClient.completeQuizRequiredInCourse(courseId);

        if (!quizCompleted) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, "quiz.required_not_passed");
        }
        EnrollmentEntity enrollment = optionalEnrollment.get();
        enrollment.setCompletedAt(LocalDateTime.now());
        enrollment.setLastModifiedBy(user.getUsername());
        enrollment.setStatus(EnrollmentStatus.COMPLETED);
        enrollment.setLastModifiedDate(LocalDateTime.now());
        EnrollmentEntity save = repository.save(enrollment);

        CertificateEntity certificate = new CertificateEntity();
        certificate.setCourseId(courseId);
        certificate.setStatus(CertificateStatus.ISSUED);
        certificate.setEnrollmentId(optionalEnrollment.get().getId());
        certificate.setUserId(userId);
        certificate.setIssuedAt(LocalDateTime.now());
        certificate.setCertificateCode(generateCertificateCode());

        certificateRepository.save(certificate);
        response.setData(mapper.toDto(save));
        response.setStatus(HttpStatus.OK);

        return response;
    }

    private String generateCertificateCode() {
        String certificateCode;
        do {
            String random = UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 8)
                    .toUpperCase();
            certificateCode = "LMS-" + LocalDateTime.now().getYear() + "-" + random;
        } while (certificateRepository.existsByCertificateCodeAndDeletedAtIsNull(certificateCode));

        return certificateCode;
    }

    @Override
    public BaseResponse<EnrollmentDto> findEnrollmentByCourseIdAndUserId(String courseId){
        response = new BaseResponse<>();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        if(courseId.isEmpty())
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("course.not_found"));
        Optional<EnrollmentEntity> optionalEnrollment = repository.findByUserIdAndCourseIdAndDeletedAtIsNull(userId,courseId);
        if(optionalEnrollment.isEmpty())
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("course.not_found"));
        EnrollmentEntity enrollment = optionalEnrollment.get();
        response.setData(mapper.toDto(enrollment));
        response.setStatus(HttpStatus.OK);
        return response;
    }
}
