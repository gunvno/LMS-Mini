package vn.com.atomi.charge.quiz.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import vn.com.atomi.charge.base.model.dto.BaseDto;
import vn.com.atomi.charge.quiz.model.enums.EnrollmentStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "Enrollment data")
public class EnrollmentDto extends BaseDto<String> {
    @NotBlank(groups = Create.class)
    @Schema(description = "UserId display", example = "UUID")
    private String userId;

    @NotBlank(groups = Create.class)
    @Schema(description = "CourseId display", example = "UUID")
    private String courseId;

    @Schema(description = "enrolledAt display", example = "Time")
    private LocalDateTime enrolledAt;

    @Schema(description = "completedAt display", example = "Time")
    private LocalDateTime completedAt;

    @Schema(description = "progressPercent display", example = "20%")
    private Double progressPercent;

    @NotNull(groups = Create.class)
    @Schema(description = "status display", example = "ACTIVE")
    @Enumerated(EnumType.STRING)
    private EnrollmentStatus status;
}
