package vn.com.atomi.charge.learning.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import vn.com.atomi.charge.learning.repository.Client.NoticeClient;
import vn.com.atomi.charge.learning.model.dto.CourseNotificationDto;
import vn.com.atomi.charge.learning.model.request.NoticeRequest;
import vn.com.atomi.charge.learning.model.request.InternalMailRequest;
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
@Slf4j
public class EnrollmentServiceImpl extends BaseService<EnrollmentRepository,
        EnrollmentDto, EnrollmentEntity, EnrollmentMapper> implements EnrollmentService {

    private final CourseClient courseClient;
    private final AuthnClient authnClient;
    private final LearningProgressRepository learningProgressRepository;
    private final CertificateRepository certificateRepository;
    private final QuizClient quizClient;
    private final NoticeClient noticeClient;
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
            EnrollmentEntity saved = createEnrollmentWithProgress(userId, courseId);
            notifyEnrollment(saved, userId, courseId);
            response.setStatus(HttpStatus.OK);
            response.setData(mapper.toDto(saved));
        } catch (Exception ex) {
            response.setStatus(HttpStatus.BAD_REQUEST);
            response.setErrorCode(BaseErrorCode.FAILURE.getErrorCode());
            response.setMessage(StringUtil.beautyError(ex));
        }


        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<EnrollmentDto> enrollCourseForUser(String userId, String courseId) {
        response = new BaseResponse<>();
        try {
            if (userId == null || userId.isBlank()) {
                return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("user.not_found"));
            }
            if (courseId == null || courseId.isBlank()) {
                return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("course.not_found"));
            }
            if (!Boolean.TRUE.equals(authnClient.checkUser(userId))) {
                return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("user.not_found"));
            }

            Optional<EnrollmentEntity> existing =
                    repository.findByUserIdAndCourseIdAndDeletedAtIsNull(userId, courseId);
            if (existing.isPresent()) {
                response.setStatus(HttpStatus.OK);
                response.setData(mapper.toDto(existing.get()));
                return response;
            }

            Boolean courseExists = courseClient.existsPublishedCourseById(courseId);
            if (!Boolean.TRUE.equals(courseExists)) {
                return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("course.not_found"));
            }

            EnrollmentEntity saved = createEnrollmentWithProgress(userId, courseId);
            notifyEnrollment(saved, userId, courseId);

            response.setStatus(HttpStatus.OK);
            response.setData(mapper.toDto(saved));
        } catch (Exception ex) {
            response.setStatus(HttpStatus.BAD_REQUEST);
            response.setErrorCode(BaseErrorCode.FAILURE.getErrorCode());
            response.setMessage(StringUtil.beautyError(ex));
        }
        return response;
    }

    private EnrollmentEntity createEnrollmentWithProgress(String userId, String courseId) {
        EnrollmentEntity enrollment = new EnrollmentEntity();
        enrollment.setUserId(userId);
        enrollment.setCourseId(courseId);
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollment.setCompletedAt(null);
        enrollment.setProgressPercent(0.0);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        EnrollmentEntity saved = repository.save(enrollment);

        List<LearningProgressEntity> progresses = new ArrayList<>();
        List<LessonDto> lessons = courseClient.getLessonByCourseId(courseId);
        if (lessons != null) {
            for (LessonDto lesson : lessons) {
                LearningProgressEntity progress = new LearningProgressEntity();
                progress.setEnrollmentId(saved.getId());
                progress.setLessonId(lesson.getId());
                progress.setStatus(LearningProgressStatus.NOT_STARTED);
                progress.setStartedAt(null);
                progress.setCompletedAt(null);
                progresses.add(progress);
            }
            learningProgressRepository.saveAll(progresses);
        }
        return saved;
    }

    private void notifyEnrollment(EnrollmentEntity enrollment, String userId, String courseId) {
        try {
            CourseNotificationDto course = courseClient.getCourse(courseId).getData();
            AuthnUserDto student = authnClient.getUserById(userId).getData();
            String courseName = course == null || course.getName() == null ? courseId : course.getName();
            String studentName = student == null || student.getFullName() == null ? userId : student.getFullName();

            sendUserNotice(userId, i18n.getMessage("notice.enrollment_success.title"),
                    i18n.getMessage("notice.enrollment_success.student_content", new Object[]{courseName}), courseId);
            if (course != null && course.getInstructorId() != null && !course.getInstructorId().equals(userId)) {
                sendUserNotice(course.getInstructorId(), i18n.getMessage("notice.enrollment_created.title"),
                        i18n.getMessage("notice.enrollment_created.content", new Object[]{studentName, courseName}), courseId);
            }
            NoticeRequest adminNotice = notice("ADMIN", i18n.getMessage("notice.enrollment_created.title"),
                    i18n.getMessage("notice.enrollment_created.content", new Object[]{studentName, courseName}), courseId);
            noticeClient.sendRole(wrap(adminNotice));

            if (student != null && student.getEmail() != null) {
                InternalMailRequest mail = new InternalMailRequest();
                mail.setEmail(student.getEmail());
                mail.setSubject(i18n.getMessage("mail.enrollment.subject"));
                mail.setContent(i18n.getMessage("mail.enrollment.content", new Object[]{studentName, courseName}));
                authnClient.sendMail(mail);
            }
        } catch (Exception ex) {
            log.warn("Enrollment {} created but notification delivery failed: {}", enrollment.getId(), ex.getMessage());
        }
    }

    private void sendUserNotice(String userId, String title, String content, String courseId) {
        NoticeRequest request = notice(null, title, content, courseId);
        request.setUserId(userId);
        noticeClient.sendUser(wrap(request));
    }

    private NoticeRequest notice(String roleCode, String title, String content, String courseId) {
        NoticeRequest request = new NoticeRequest();
        request.setRoleCode(roleCode);
        request.setTitle(title);
        request.setContent(content);
        request.setNoticeType("ENROLLMENT_SUCCESS");
        request.setData(courseId);
        return request;
    }

    private BaseRequest<NoticeRequest> wrap(NoticeRequest data) {
        BaseRequest<NoticeRequest> request = new BaseRequest<>();
        request.setData(data);
        return request;
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
