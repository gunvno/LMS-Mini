package vn.com.atomi.charge.course.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;
import vn.com.atomi.charge.base.model.enums.BaseErrorCode;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.base.repository.BaseSpecification;
import vn.com.atomi.charge.base.service.BaseService;
import vn.com.atomi.charge.base.util.StringUtil;
import vn.com.atomi.charge.course.mapper.LessonResourceMapper;
import vn.com.atomi.charge.course.client.LearningClient;
import vn.com.atomi.charge.course.model.entity.CourseEntity;
import vn.com.atomi.charge.course.model.entity.LessonEntity;
import vn.com.atomi.charge.course.model.dto.LessonResourceDto;
import vn.com.atomi.charge.course.model.entity.LessonResourceEntity;
import vn.com.atomi.charge.course.model.enums.LessonResourceStatus;
import vn.com.atomi.charge.course.model.enums.LessonResourceType;
import vn.com.atomi.charge.course.model.storage.StorageUploadResult;
import vn.com.atomi.charge.course.model.storage.StorageFile;
import vn.com.atomi.charge.course.repository.LessonRepository;
import vn.com.atomi.charge.course.repository.LessonResourceRepository;
import vn.com.atomi.charge.course.repository.CourseRepository;
import vn.com.atomi.charge.course.security.CourseVisibilityPolicy;
import vn.com.atomi.charge.course.service.interfaces.LessonResourceService;
import vn.com.atomi.charge.course.service.interfaces.StorageService;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Locale;
import java.nio.charset.StandardCharsets;

@Service
public class LessonResourceServiceImpl
    extends BaseService<
        LessonResourceRepository,
        LessonResourceDto,
        LessonResourceEntity,
        LessonResourceMapper>
    implements LessonResourceService {

    private static final Set<String> SUPPORTED_FILE_TYPES = Set.of(
        "application/pdf",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/msword",
        "video/mp4",
        "video/webm",
        "video/quicktime",
        "video/x-matroska"
    );

    private static final Map<String, LessonResourceType> RESOURCE_TYPE_BY_CONTENT_TYPE = Map.of(
        "application/pdf", LessonResourceType.PDF,
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document", LessonResourceType.DOCX,
        "application/msword", LessonResourceType.DOCX,
        "video/mp4", LessonResourceType.VIDEO,
        "video/webm", LessonResourceType.VIDEO,
        "video/quicktime", LessonResourceType.VIDEO,
        "video/x-matroska", LessonResourceType.VIDEO
    );

    private static final Map<String, LessonResourceType> RESOURCE_TYPE_BY_EXTENSION = Map.of(
        "pdf", LessonResourceType.PDF,
        "doc", LessonResourceType.DOCX,
        "docx", LessonResourceType.DOCX,
        "mp4", LessonResourceType.VIDEO,
        "webm", LessonResourceType.VIDEO,
        "mov", LessonResourceType.VIDEO,
        "mkv", LessonResourceType.VIDEO
    );

    private final LessonRepository lessonRepository;
    private final StorageService storageService;
    private final CourseRepository courseRepository;
    private final LearningClient learningClient;

    public LessonResourceServiceImpl(LessonRepository lessonRepository,
                                     StorageService storageService,
                                     CourseRepository courseRepository,
                                     LearningClient learningClient) {
        this.lessonRepository = lessonRepository;
        this.storageService = storageService;
        this.courseRepository = courseRepository;
        this.learningClient = learningClient;
    }

    @Override
    public BaseResponse<Page<LessonResourceDto>> getAll(Map<String, String> params, Pageable pageable) {
        if (canReviewCourses()) {
            Sort sort = Sort.by(Sort.Direction.DESC, "lastModifiedDate", "createdDate");
            Pageable sorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
            BaseSpecification<LessonResourceEntity> filters = new BaseSpecification<>(
                    params == null ? Map.of() : params);
            Page<LessonResourceEntity> result = repository.findAll(
                    filters.and(CourseVisibilityPolicy.reviewerVisibleResources(currentUserId())),
                    sorted);
            return BaseResponse.success(HttpStatus.OK, result.map(mapper::toDto));
        }
        if (isInstructorRequest()) {
            String lessonId = params == null ? null : params.get("lessonId");
            if (StringUtils.hasText(lessonId)) {
                assertCanManageLesson(lessonId);
                return BaseResponse.success(HttpStatus.OK,
                        repository.findByInstructorIdAndLessonId(
                                currentUserId(), lessonId, pageable).map(mapper::toDto));
            }
            return BaseResponse.success(HttpStatus.OK,
                    repository.findByInstructorId(currentUserId(), pageable).map(mapper::toDto));
        }
        String lessonId = params == null ? null : params.get("lessonId");
        assertStudentCanAccessLesson(lessonId);
        return BaseResponse.success(HttpStatus.OK,
                repository.findByLessonIdAndStatusAndDeletedAtIsNull(
                        lessonId, LessonResourceStatus.ACTIVE, pageable).map(mapper::toDto));
    }

    @Override
    public BaseResponse<LessonResourceDto> getDetails(String id) {
        LessonResourceEntity resource = repository.findEntityById(id)
                .orElseThrow(() -> new AccessDeniedException("common.access_denied"));
        if (canReviewCourses()) {
            assertReviewerCanAccessLesson(resource.getLessonId());
            return BaseResponse.success(HttpStatus.OK, mapper.toDto(resource));
        }
        if (isInstructorRequest()) {
            assertCanManageLesson(resource.getLessonId());
        } else {
            if (resource.getStatus() != LessonResourceStatus.ACTIVE) {
                throw new AccessDeniedException("common.access_denied");
            }
            assertStudentCanAccessLesson(resource.getLessonId());
        }
        return BaseResponse.success(HttpStatus.OK, mapper.toDto(resource));
    }

    @Override
    public BaseResponse<LessonResourceDto> create(vn.com.atomi.charge.base.model.request.BaseRequest<LessonResourceDto> request) {
        assertCanManageLesson(request.getData().getLessonId());
        return super.create(request);
    }

    @Override
    public BaseResponse<LessonResourceDto> update(vn.com.atomi.charge.base.model.request.BaseRequest<LessonResourceDto> request) {
        LessonResourceEntity existing = repository.findEntityById(request.getData().getId())
                .orElseThrow(() -> new AccessDeniedException("common.access_denied"));
        assertCanManageLesson(existing.getLessonId());
        assertCanManageLesson(request.getData().getLessonId());
        return super.update(request);
    }

    @Override
    public BaseResponse<LessonResourceDto> delete(String id) {
        LessonResourceEntity existing = repository.findEntityById(id)
                .orElseThrow(() -> new AccessDeniedException("common.access_denied"));
        assertCanManageLesson(existing.getLessonId());
        return super.delete(id);
    }

    @Override
    public BaseResponse<LessonResourceDto> delete(List<String> ids) {
        ids.stream()
                .map(id -> repository.findEntityById(id)
                        .orElseThrow(() -> new AccessDeniedException("common.access_denied")))
                .map(LessonResourceEntity::getLessonId)
                .distinct()
                .forEach(this::assertCanManageLesson);
        return super.delete(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<LessonResourceDto> uploadLessonResource(
        String lessonId,
        MultipartFile file,
        String title,
        String externalUrl
    ) {
        assertCanManageLesson(lessonId);
        BaseResponse<LessonResourceDto> response = new BaseResponse<>();
        try {
            if (!StringUtils.hasText(lessonId) || lessonRepository.findEntityById(lessonId).isEmpty()) {
                return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("lesson.not_found"));
            }
            if (!StringUtils.hasText(title)) {
                return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("lesson_resource.title_required"));
            }

            boolean hasFile = file != null && !file.isEmpty();
            boolean hasExternalUrl = StringUtils.hasText(externalUrl);
            if (!hasFile && !hasExternalUrl) {
                return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("lesson_resource.file_or_url_required"));
            }

            if (hasFile && hasExternalUrl) {
                return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("lesson_resource.only_one_source_allowed"));
            }

            LessonResourceEntity resource = hasFile
                ? buildFileResource(lessonId, file, title)
                : buildLinkResource(lessonId, title, externalUrl);

            LessonResourceEntity saved = repository.save(resource);
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
    public ResponseEntity<byte[]> viewLessonResource(String id, boolean attachment) {
        LessonResourceEntity resource = repository.findEntityById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        assertCanViewResource(resource);

        if (!StringUtils.hasText(resource.getFilePath())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                i18n.getMessage("lesson_resource.file_or_url_required"));
        }

        StorageFile file = storageService.download(resource.getFilePath());
        String contentType = StringUtils.hasText(file.contentType())
            ? file.contentType()
            : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        String fileName = fileName(resource.getFilePath(), resource.getTitle());
        ContentDisposition contentDisposition = ContentDisposition
            .builder(attachment ? "attachment" : "inline")
            .filename(sanitizeFileName(fileName), StandardCharsets.UTF_8)
            .build();

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
            .body(file.content());
    }

    private void assertStudentCanAccessLesson(String lessonId) {
        LessonEntity lesson = StringUtils.hasText(lessonId)
                ? lessonRepository.findEntityById(lessonId).orElse(null)
                : null;
        if (lesson == null) {
            throw new AccessDeniedException("common.access_denied");
        }
        CourseEntity course = courseRepository.findEntityById(lesson.getCourseId()).orElse(null);
        List<String> accessibleIds = course == null ? List.of()
                : learningClient.getAccessibleLessonIds(course.getId());
        if (course == null
                || course.getStatus() != vn.com.atomi.charge.course.model.enums.CourseStatus.PUBLISHED
                || accessibleIds == null
                || !accessibleIds.contains(lessonId)) {
            throw new AccessDeniedException("common.access_denied");
        }
    }

    private void assertCanManageLesson(String lessonId) {
        if (canReviewCourses()) {
            assertReviewerCanAccessLesson(lessonId);
            return;
        }
        LessonEntity lesson = StringUtils.hasText(lessonId)
                ? lessonRepository.findEntityById(lessonId).orElse(null)
                : null;
        CourseEntity course = lesson == null ? null
                : courseRepository.findEntityById(lesson.getCourseId()).orElse(null);
        if (course == null || !currentUserId().equals(course.getInstructorId())) {
            throw new AccessDeniedException("common.access_denied");
        }
    }

    private boolean isInstructorRequest() {
        return hasAuthority("RESOURCE_MANAGE");
    }

    private boolean canReviewCourses() {
        return hasAuthority("COURSE_REVIEW");
    }

    private boolean hasAuthority(String authority) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(item -> authority.equals(item.getAuthority()));
    }

    private String currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !StringUtils.hasText(authentication.getName())) {
            throw new AccessDeniedException("common.access_denied");
        }
        return authentication.getName();
    }

    private LessonResourceEntity buildFileResource(String lessonId, MultipartFile file, String title) {
        String contentType = file.getContentType();
        LessonResourceType resourceType = resolveResourceType(contentType, file.getOriginalFilename());
        if (resourceType == null) {
            throw new IllegalArgumentException(i18n.getMessage("lesson_resource.invalid_content_type"));
        }

        StorageUploadResult uploaded = storageService.upload(file, "lessons/" + lessonId + "/resources");
        return LessonResourceEntity.builder()
            .lessonId(lessonId)
            .title(title)
            .resourceType(resourceType)
            .filePath(uploaded.filePath())
            .status(LessonResourceStatus.ACTIVE)
            .build();
    }

    private LessonResourceType resolveResourceType(String contentType, String originalFilename) {
        if (StringUtils.hasText(contentType) && SUPPORTED_FILE_TYPES.contains(contentType)) {
            return RESOURCE_TYPE_BY_CONTENT_TYPE.get(contentType);
        }
        if (!StringUtils.hasText(originalFilename) || !originalFilename.contains(".")) {
            return null;
        }
        String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1)
            .toLowerCase(Locale.ROOT);
        return RESOURCE_TYPE_BY_EXTENSION.get(extension);
    }

    private void assertCanViewResource(LessonResourceEntity resource) {
        if (canReviewCourses()) {
            assertReviewerCanAccessLesson(resource.getLessonId());
            return;
        }
        if (isInstructorRequest()) {
            assertCanManageLesson(resource.getLessonId());
            return;
        }
        if (resource.getStatus() != LessonResourceStatus.ACTIVE) {
            throw new AccessDeniedException("common.access_denied");
        }
        assertStudentCanAccessLesson(resource.getLessonId());
    }

    private void assertReviewerCanAccessLesson(String lessonId) {
        LessonEntity lesson = StringUtils.hasText(lessonId)
                ? lessonRepository.findEntityById(lessonId).orElse(null)
                : null;
        CourseEntity course = lesson == null
                ? null
                : courseRepository.findEntityById(lesson.getCourseId()).orElse(null);
        if (!CourseVisibilityPolicy.isVisibleToReviewer(course, currentUserId())) {
            throw new AccessDeniedException("common.access_denied");
        }
    }

    private String fileName(String filePath, String fallback) {
        int separator = filePath.lastIndexOf('/');
        String storedName = separator >= 0 ? filePath.substring(separator + 1) : filePath;
        int uuidSeparator = storedName.indexOf('-');
        return uuidSeparator >= 0 && uuidSeparator + 1 < storedName.length()
            ? storedName.substring(uuidSeparator + 1)
            : (StringUtils.hasText(storedName) ? storedName : fallback);
    }

    private String sanitizeFileName(String value) {
        String safe = StringUtils.hasText(value) ? value : "resource";
        return safe.replaceAll("[\\r\\n\\\"]", "_");
    }

    private LessonResourceEntity buildLinkResource(String lessonId, String title, String externalUrl) {
        return LessonResourceEntity.builder()
            .lessonId(lessonId)
            .title(title)
            .resourceType(LessonResourceType.LINK)
            .externalUrl(externalUrl)
            .status(LessonResourceStatus.ACTIVE)
            .build();
    }
}
