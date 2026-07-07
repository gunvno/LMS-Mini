package vn.com.atomi.charge.authorization.repository;

import org.springframework.stereotype.Repository;
import vn.com.atomi.charge.authorization.model.entity.UserRoleEntity;
import vn.com.atomi.charge.base.repository.BaseRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoleRepository extends BaseRepository<UserRoleEntity, String> {
    List<UserRoleEntity> findByUserIdAndDeletedAtIsNull(String userId);

    List<UserRoleEntity> findByRoleIdAndDeletedAtIsNull(String roleId);

    Optional<UserRoleEntity> findByUserIdAndRoleIdAndDeletedAtIsNull(String userId, String roleId);
}