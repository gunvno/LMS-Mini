package vn.com.atomi.charge.authorization.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.com.atomi.charge.base.model.entity.BaseEntity;

@Getter
@Setter
@Entity
@Table(name = "tbl_role_permissions")
public class RolePermissionEntity extends BaseEntity {

    @Column(name = "role_id", nullable = false)
    private String roleId;

    @Column(name = "permission_id", nullable = false)
    private String permissionId;
}