package vn.com.atomi.charge.authorization.repository;

import org.springframework.stereotype.Repository;
import vn.com.atomi.charge.authorization.model.entity.RoleEntity;
import vn.com.atomi.charge.authorization.model.enums.RoleCode;
import vn.com.atomi.charge.base.repository.BaseRepository;

import java.util.Optional;

@Repository
public interface AuthorRepo extends BaseRepository<RoleEntity, String> {
	Optional<RoleEntity> findByCodeAndDeletedAtIsNull(RoleCode code);

	boolean existsByCodeAndDeletedAtIsNull(RoleCode code);
}
