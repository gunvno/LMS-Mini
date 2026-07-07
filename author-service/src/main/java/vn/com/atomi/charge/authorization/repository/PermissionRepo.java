package vn.com.atomi.charge.authorization.repository;

import org.springframework.stereotype.Repository;
import vn.com.atomi.charge.authorization.model.entity.PermissionEntity;
import vn.com.atomi.charge.base.repository.BaseRepository;

import java.util.Optional;

@Repository
public interface PermissionRepo extends BaseRepository<PermissionEntity, String> {
	Optional<PermissionEntity> findByCodeAndDeletedAtIsNull(String code);

	boolean existsByCodeAndDeletedAtIsNull(String code);
}