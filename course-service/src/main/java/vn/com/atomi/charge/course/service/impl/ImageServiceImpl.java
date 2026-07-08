package vn.com.atomi.charge.course.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
        response = new BaseResponse<>();
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
    public BaseResponse<List<ImageDto>> getCourseImages(String courseId) {
        if (!StringUtils.hasText(courseId) || courseRepository.findEntityById(courseId).isEmpty()) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("course.not_found"));
        }

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
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "image.not_found"));

        return buildImageResponse(image, attachment);
    }

    @Override
    public ResponseEntity<byte[]> viewPrimaryCourseImage(String courseId) {
        if (!StringUtils.hasText(courseId) || courseRepository.findEntityById(courseId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, i18n.getMessage("course.not_found"));
        }

        ImageEntity image = repository.findFirstByObjectTypeAndObjectIdAndPrimaryImageTrueAndDeletedAtIsNull(
                ImageObjectType.COURSE,
                courseId
            )
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "image.not_found"));

        return buildImageResponse(image, false);
    }

    private ResponseEntity<byte[]> buildImageResponse(ImageEntity image, boolean attachment) {
        if (image.getStatus() != ImageStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "image.not_found");
        }

        StorageFile file = storageService.download(image.getFilePath());
        String contentType = StringUtils.hasText(image.getContentType())
            ? image.getContentType()
            : file.contentType();
        if (!StringUtils.hasText(contentType)) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        String dispositionType = attachment ? "attachment" : "inline";
        String safeFileName = sanitizeFileName(image.getFileName());

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, dispositionType + "; filename=\"" + safeFileName + "\"")
            .body(file.content());
    }

    private String sanitizeFileName(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "image";
        }
        return new String(fileName.replace("\"", "").getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }
}
