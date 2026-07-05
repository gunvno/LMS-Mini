package vn.com.atomi.charge.course.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.com.atomi.charge.base.model.entity.BaseEntity;
import vn.com.atomi.charge.course.model.enums.ImageObjectType;
import vn.com.atomi.charge.course.model.enums.ImageStatus;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tbl_images")
public class ImageEntity extends BaseEntity {

    @Column(name = "object_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ImageObjectType objectType;

    @Column(name = "object_id", nullable = false)
    private String objectId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_path", nullable = false, length = 1000)
    private String filePath;

    @Column(name = "file_url", length = 1000)
    private String fileUrl;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "is_primary")
    private Boolean primaryImage;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ImageStatus status;

}
