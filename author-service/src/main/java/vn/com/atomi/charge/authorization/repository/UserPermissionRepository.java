package vn.com.atomi.charge.authorization.repository;

import org.springframework.stereotype.Repository;
import vn.com.atomi.charge.authorization.model.entity.UserPermissionEntity;
import vn.com.atomi.charge.base.repository.BaseRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPermissionRepository extends BaseRepository<UserPermissionEntity, String> {
    List<UserPermissionEntity> findByUserIdAndDeletedAtIsNull(String userId);

    Optional<UserPermissionEntity> findByUserIdAndPermissionIdAndDeletedAtIsNull(String userId, String permissionId);
}
