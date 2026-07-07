package vn.com.atomi.charge.authorization.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.com.atomi.charge.authorization.model.enums.PermissionStatus;
import vn.com.atomi.charge.base.model.entity.BaseEntity;

@Getter
@Setter
@Entity
@Table(name = "tbl_permissions")
public class PermissionEntity extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private PermissionStatus status;
}