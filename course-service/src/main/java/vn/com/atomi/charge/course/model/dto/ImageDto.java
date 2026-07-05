package vn.com.atomi.charge.course.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import vn.com.atomi.charge.base.model.dto.BaseDto;
import vn.com.atomi.charge.course.model.enums.ImageObjectType;
import vn.com.atomi.charge.course.model.enums.ImageStatus;

@Getter
@Setter
@Schema(description = "Image metadata")
public class ImageDto extends BaseDto<String> {

    @NotNull(groups = Create.class)
    @Schema(example = "COURSE", allowableValues = {"COURSE", "LESSON", "USER", "CERTIFICATE"})
    private ImageObjectType objectType;

    @NotBlank(groups = Create.class)
    @Schema(example = "course-id")
    private String objectId;

    @NotBlank(groups = Create.class)
    @Schema(example = "thumbnail.png")
    private String fileName;

    @NotBlank(groups = Create.class)
    @Schema(example = "images/course/thumbnail.png")
    private String filePath;

    @Schema(example = "https://cdn.example.com/images/course/thumbnail.png")
    private String fileUrl;

    @Schema(example = "image/png")
    private String contentType;

    @Schema(example = "204800")
    private Long fileSize;

    @Schema(example = "true")
    private Boolean primaryImage;

    @NotNull(groups = Create.class)
    @Schema(example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE"})
    private ImageStatus status;
}
