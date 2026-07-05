package vn.com.atomi.charge.course.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import vn.com.atomi.charge.base.model.dto.BaseDto;
import vn.com.atomi.charge.course.model.enums.CourseCategoryStatus;

@Getter
@Setter
@Schema(description = "Course category data")
public class CourseCategoryDto extends BaseDto<String> {

    @NotBlank(groups = Create.class)
    @Schema(description = "Category display name", example = "Backend")
    private String name;

    @NotBlank(groups = Create.class)
    @Schema(description = "Unique category code", example = "BACKEND")
    private String code;

    @Schema(description = "Category description", example = "Courses about backend development")
    private String description;

    @NotNull(groups = Create.class)
    @Schema(description = "Category status", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE"})
    private CourseCategoryStatus status;
}
