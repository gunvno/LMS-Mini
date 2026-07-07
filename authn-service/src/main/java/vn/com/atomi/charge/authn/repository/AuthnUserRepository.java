package vn.com.atomi.charge.authn.repository;

import org.springframework.stereotype.Repository;
import vn.com.atomi.charge.authn.model.entity.AuthnUserEntity;
import vn.com.atomi.charge.base.repository.BaseRepository;

import java.util.Optional;

@Repository
public interface AuthnUserRepository extends BaseRepository<AuthnUserEntity, String> {
	boolean existsByUsername(String username);

	boolean existsByEmail(String email);

	Optional<AuthnUserEntity> findByUsername(String username);

	Optional<AuthnUserEntity> findByUsernameOrEmail(String username, String email);
}
