package vn.com.atomi.charge.learning.model.dto;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import vn.com.atomi.charge.base.model.dto.BaseDto;
import vn.com.atomi.charge.learning.model.enums.CertificateStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class CertificateDto extends BaseDto<String> {
    private String userId;

    private String courseId;

    private String enrollmentId;

    private String certificateCode;

    private LocalDateTime issuedAt;

    @Enumerated(EnumType.STRING)
    private CertificateStatus status;
}
