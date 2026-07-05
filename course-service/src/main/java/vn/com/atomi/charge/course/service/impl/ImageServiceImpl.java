package vn.com.atomi.charge.course.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
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
import vn.com.atomi.charge.course.model.storage.StorageUploadResult;
import vn.com.atomi.charge.course.repository.CourseRepository;
import vn.com.atomi.charge.course.repository.ImageRepository;
import vn.com.atomi.charge.course.service.interfaces.ImageService;
import vn.com.atomi.charge.course.service.interfaces.StorageService;

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
}
