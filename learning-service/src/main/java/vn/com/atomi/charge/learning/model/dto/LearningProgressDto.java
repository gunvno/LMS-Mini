package vn.com.atomi.charge.learning.model.dto;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;
import vn.com.atomi.charge.base.model.dto.BaseDto;
import vn.com.atomi.charge.learning.model.enums.LearningProgressStatus;

import java.time.LocalDateTime;
@Getter
@Setter
public class LearningProgressDto extends BaseDto<String> {
    private String enrollmentId;

    private String lessonId;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    @Enumerated(EnumType.STRING)
    private LearningProgressStatus status;
}
