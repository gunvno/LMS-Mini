package vn.com.atomi.charge.notice.repository;

import vn.com.atomi.charge.base.repository.BaseRepository;
import vn.com.atomi.charge.notice.model.entity.UserDeviceEntity;
import vn.com.atomi.charge.notice.model.enums.UserDeviceStatus;

import java.util.List;
import java.util.Optional;

public interface UserDeviceRepository extends BaseRepository<UserDeviceEntity, String> {

    Optional<UserDeviceEntity> findByUserIdAndFcmTokenAndDeletedAtIsNull(String userId, String fcmToken);

    List<UserDeviceEntity> findByUserIdAndStatusAndDeletedAtIsNull(String userId, UserDeviceStatus status);

    List<UserDeviceEntity> findByStatusAndDeletedAtIsNull(UserDeviceStatus status);
}
