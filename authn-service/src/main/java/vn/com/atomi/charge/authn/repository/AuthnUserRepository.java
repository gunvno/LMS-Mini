package vn.com.atomi.charge.authn.repository;

import org.springframework.stereotype.Repository;
import vn.com.atomi.charge.authn.model.entity.AuthnUserEntity;
import vn.com.atomi.charge.base.repository.BaseRepository;
@Repository
public interface AuthnUserRepository extends BaseRepository<AuthnUserEntity, String> {
}
