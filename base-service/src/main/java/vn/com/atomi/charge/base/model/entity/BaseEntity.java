package vn.com.atomi.charge.base.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.CreatedDate;
import vn.com.atomi.charge.base.util.DateUtil;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public abstract class BaseEntity implements Serializable {

    @Id
    @UuidGenerator
    String id;

    @Version
    @Column(name = "version", nullable = false)
    Long version;

    @CreatedBy
    @Column(name = "created_by", length = 50, updatable = false)
    String createdBy;

    @CreatedDate
    @Column(name = "created_date", nullable = false, length = 50, updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtil.YMD_HMS_DASH_PATTERN)
    LocalDateTime createdDate = LocalDateTime.now();

    @LastModifiedBy
    @Column(name = "last_modified_by", length = 50)
    @JsonIgnore
    String lastModifiedBy;

    @LastModifiedDate
    @Column(name = "last_modified_date", length = 50)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtil.YMD_HMS_DASH_PATTERN)
    @JsonIgnore
    LocalDateTime lastModifiedDate = LocalDateTime.now();

    @Column(name = "deleted_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtil.YMD_HMS_DASH_PATTERN)
    @JsonIgnore
    LocalDateTime deletedAt;

}
