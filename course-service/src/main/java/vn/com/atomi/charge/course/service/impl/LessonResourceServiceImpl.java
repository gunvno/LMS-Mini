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
import vn.com.atomi.charge.course.mapper.LessonResourceMapper;
import vn.com.atomi.charge.course.model.dto.LessonResourceDto;
import vn.com.atomi.charge.course.model.entity.LessonResourceEntity;
import vn.com.atomi.charge.course.model.enums.LessonResourceStatus;
import vn.com.atomi.charge.course.model.enums.LessonResourceType;
import vn.com.atomi.charge.course.model.storage.StorageUploadResult;
import vn.com.atomi.charge.course.repository.LessonRepository;
import vn.com.atomi.charge.course.repository.LessonResourceRepository;
import vn.com.atomi.charge.course.service.interfaces.LessonResourceService;
import vn.com.atomi.charge.course.service.interfaces.StorageService;

import java.util.Map;
import java.util.Set;

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
        "application/msword"
    );

    private static final Map<String, LessonResourceType> RESOURCE_TYPE_BY_CONTENT_TYPE = Map.of(
        "application/pdf", LessonResourceType.PDF,
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document", LessonResourceType.DOCX,
        "application/msword", LessonResourceType.DOCX
    );

    private final LessonRepository lessonRepository;
    private final StorageService storageService;

    public LessonResourceServiceImpl(LessonRepository lessonRepository, StorageService storageService) {
        this.lessonRepository = lessonRepository;
        this.storageService = storageService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<LessonResourceDto> uploadLessonResource(
        String lessonId,
        MultipartFile file,
        String title,
        String externalUrl
    ) {
        response = new BaseResponse<>();
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

    private LessonResourceEntity buildFileResource(String lessonId, MultipartFile file, String title) {
        String contentType = file.getContentType();
        if (!SUPPORTED_FILE_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(i18n.getMessage("lesson_resource.invalid_content_type"));
        }

        StorageUploadResult uploaded = storageService.upload(file, "lessons/" + lessonId + "/resources");
        return LessonResourceEntity.builder()
            .lessonId(lessonId)
            .title(title)
            .resourceType(RESOURCE_TYPE_BY_CONTENT_TYPE.get(contentType))
            .filePath(uploaded.filePath())
            .status(LessonResourceStatus.ACTIVE)
            .build();
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
