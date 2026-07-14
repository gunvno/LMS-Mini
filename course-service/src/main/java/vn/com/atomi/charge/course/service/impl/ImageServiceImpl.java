package vn.com.atomi.charge.course.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import vn.com.atomi.charge.base.service.BaseService;
import vn.com.atomi.charge.base.util.StringUtil;
import vn.com.atomi.charge.course.mapper.ImageMapper;
import vn.com.atomi.charge.course.model.dto.ImageDto;
import vn.com.atomi.charge.course.model.entity.ImageEntity;
import vn.com.atomi.charge.course.model.entity.CourseEntity;
import vn.com.atomi.charge.course.model.enums.CourseStatus;
import vn.com.atomi.charge.course.model.enums.ImageObjectType;
import vn.com.atomi.charge.course.model.enums.ImageStatus;
import vn.com.atomi.charge.course.model.storage.StorageFile;
import vn.com.atomi.charge.course.model.storage.StorageUploadResult;
import vn.com.atomi.charge.course.repository.CourseRepository;
import vn.com.atomi.charge.course.repository.ImageRepository;
import vn.com.atomi.charge.course.service.interfaces.ImageService;
import vn.com.atomi.charge.course.service.interfaces.StorageService;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class ImageServiceImpl
    extends BaseService<ImageRepository, ImageDto, ImageEntity, ImageMapper>
    implements ImageService {

    private final CourseRepository courseRepository;
    private final StorageService storageService;

    public ImageServiceImpl(CourseRepository courseRepository, StorageService storageService) {
        this.courseRepository = courseRepository;
        this.storageService = storageService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<ImageDto> uploadCourseImage(String courseId, MultipartFile file) {
        assertCanManageCourse(courseId);
        BaseResponse<ImageDto> response = new BaseResponse<>();
        try {
            if (!StringUtils.hasText(courseId) || courseRepository.findEntityById(courseId).isEmpty()) {
                return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("course.not_found"));
            }

            if (file == null || file.isEmpty()) {
                return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("image.file_required"));
            }

            String contentType = file.getContentType();
            if (!StringUtils.hasText(contentType) || !contentType.startsWith("image/")) {
                return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("image.invalid_content_type"));
            }

            StorageUploadResult uploaded = storageService.upload(file, "courses/" + courseId);
            boolean hasPrimaryImage = repository.existsByObjectTypeAndObjectIdAndPrimaryImageTrueAndDeletedAtIsNull(
                ImageObjectType.COURSE,
                courseId
            );

            ImageEntity image = ImageEntity.builder()
                .objectType(ImageObjectType.COURSE)
                .objectId(courseId)
                .fileName(uploaded.fileName())
                .filePath(uploaded.filePath())
                .fileUrl(uploaded.fileUrl())
                .contentType(uploaded.contentType())
                .fileSize(uploaded.fileSize())
                .primaryImage(!hasPrimaryImage)
                .status(ImageStatus.ACTIVE)
                .build();

            ImageEntity saved = repository.save(image);
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
    public BaseResponse<Page<ImageDto>> getAll(Map<String, String> params, Pageable pageable) {
        if (canReviewCourses()) {
            return super.getAll(params, pageable);
        }
        if (!hasAuthority("IMAGE_MANAGE")) {
            throw new AccessDeniedException("common.access_denied");
        }
        return BaseResponse.success(HttpStatus.OK,
                repository.findCourseImagesByInstructorId(currentUserId(), pageable).map(mapper::toDto));
    }

    @Override
    public BaseResponse<ImageDto> getDetails(String id) {
        ImageEntity image = repository.findEntityById(id)
                .orElseThrow(() -> new AccessDeniedException("common.access_denied"));
        assertCanViewImage(image);
        return BaseResponse.success(HttpStatus.OK, mapper.toDto(image));
    }

    @Override
    public BaseResponse<ImageDto> create(vn.com.atomi.charge.base.model.request.BaseRequest<ImageDto> request) {
        assertCanManageImageDto(request.getData());
        return super.create(request);
    }

    @Override
    public BaseResponse<ImageDto> update(vn.com.atomi.charge.base.model.request.BaseRequest<ImageDto> request) {
        ImageEntity existing = repository.findEntityById(request.getData().getId())
                .orElseThrow(() -> new AccessDeniedException("common.access_denied"));
        assertCanManageImage(existing);
        assertCanManageImageDto(request.getData());
        return super.update(request);
    }

    @Override
    public BaseResponse<ImageDto> delete(String id) {
        ImageEntity image = repository.findEntityById(id)
                .orElseThrow(() -> new AccessDeniedException("common.access_denied"));
        assertCanManageImage(image);
        return super.delete(id);
    }

    @Override
    public BaseResponse<ImageDto> delete(List<String> ids) {
        ids.stream()
                .map(id -> repository.findEntityById(id)
                        .orElseThrow(() -> new AccessDeniedException("common.access_denied")))
                .forEach(this::assertCanManageImage);
        return super.delete(ids);
    }

    @Override
    public BaseResponse<List<ImageDto>> getCourseImages(String courseId) {
        CourseEntity course = StringUtils.hasText(courseId)
                ? courseRepository.findEntityById(courseId).orElse(null)
                : null;
        if (course == null) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("course.not_found"));
        }
        assertCanViewCourseImages(course);

        List<ImageDto> images = repository.findByObjectTypeAndObjectIdAndDeletedAtIsNull(
                ImageObjectType.COURSE,
                courseId
            ).stream()
            .map(mapper::toDto)
            .toList();

        return BaseResponse.success(HttpStatus.OK, images);
    }

    @Override
    public ResponseEntity<byte[]> viewImage(String imageId, boolean attachment) {
        ImageEntity image = repository.findEntityById(imageId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, i18n.getMessage("image.not_found")));

        assertCanViewImage(image);

        return buildImageResponse(image, attachment);
    }

    @Override
    public ResponseEntity<byte[]> viewPrimaryCourseImage(String courseId) {
        CourseEntity course = StringUtils.hasText(courseId)
                ? courseRepository.findEntityById(courseId).orElse(null)
                : null;
        if (course == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, i18n.getMessage("course.not_found"));
        }
        assertCanViewCourseImages(course);

        ImageEntity image = repository.findFirstByObjectTypeAndObjectIdAndPrimaryImageTrueAndDeletedAtIsNull(
                ImageObjectType.COURSE,
                courseId
            )
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, i18n.getMessage("image.not_found")));

        return buildImageResponse(image, false);
    }

    private ResponseEntity<byte[]> buildImageResponse(ImageEntity image, boolean attachment) {
        if (image.getStatus() != ImageStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, i18n.getMessage("image.not_found"));
        }

        StorageFile file = storageService.download(image.getFilePath());
        String contentType = StringUtils.hasText(image.getContentType())
            ? image.getContentType()
            : file.contentType();
        if (!StringUtils.hasText(contentType)) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        String safeFileName = sanitizeFileName(image.getFileName());
        ContentDisposition contentDisposition = ContentDisposition
            .builder(attachment ? "attachment" : "inline")
            .filename(safeFileName, StandardCharsets.UTF_8)
            .build();

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
            .body(file.content());
    }

    private void assertCanManageImageDto(ImageDto image) {
        if (image == null || image.getObjectType() != ImageObjectType.COURSE) {
            throw new AccessDeniedException("common.access_denied");
        }
        assertCanManageCourse(image.getObjectId());
    }

    private void assertCanManageImage(ImageEntity image) {
        if (image == null || image.getObjectType() != ImageObjectType.COURSE) {
            throw new AccessDeniedException("common.access_denied");
        }
        assertCanManageCourse(image.getObjectId());
    }

    private void assertCanManageCourse(String courseId) {
        if (canReviewCourses()) {
            return;
        }
        CourseEntity course = StringUtils.hasText(courseId)
                ? courseRepository.findEntityById(courseId).orElse(null)
                : null;
        if (course == null || !currentUserId().equals(course.getInstructorId())) {
            throw new AccessDeniedException("common.access_denied");
        }
    }

    private void assertCanViewImage(ImageEntity image) {
        if (image.getObjectType() != ImageObjectType.COURSE) {
            throw new AccessDeniedException("common.access_denied");
        }
        CourseEntity course = courseRepository.findEntityById(image.getObjectId())
                .orElseThrow(() -> new AccessDeniedException("common.access_denied"));
        assertCanViewCourseImages(course);
    }

    private void assertCanViewCourseImages(CourseEntity course) {
        if (course.getStatus() == CourseStatus.PUBLISHED || canReviewCourses()) {
            return;
        }
        if (hasAuthority("IMAGE_MANAGE") && currentUserId().equals(course.getInstructorId())) {
            return;
        }
        throw new AccessDeniedException("common.access_denied");
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

    private String sanitizeFileName(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "image";
        }
        return new String(fileName.replace("\"", "").getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }
}
