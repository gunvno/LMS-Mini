package vn.com.atomi.charge.authorization.repository;

import org.springframework.stereotype.Repository;
import vn.com.atomi.charge.authorization.model.entity.RolePermissionEntity;
import vn.com.atomi.charge.base.repository.BaseRepository;

import java.util.List;

@Repository
public interface RolePermissionRepo extends BaseRepository<RolePermissionEntity, String> {
	List<RolePermissionEntity> findByRoleIdAndDeletedAtIsNull(String roleId);
}