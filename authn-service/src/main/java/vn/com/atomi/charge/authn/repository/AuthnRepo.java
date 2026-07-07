package vn.com.atomi.charge.authn.repository;

import vn.com.atomi.charge.authn.model.entity.RefreshTokenEntity;
import vn.com.atomi.charge.base.repository.BaseRepository;

import java.util.List;
import java.util.Optional;

public interface AuthnRepo extends BaseRepository<RefreshTokenEntity, String> {
	Optional<RefreshTokenEntity> findByTokenId(String tokenId);

	boolean existsByTokenId(String tokenId);

	List<RefreshTokenEntity> findByUserIdAndDeletedAtIsNull(String userId);
}
