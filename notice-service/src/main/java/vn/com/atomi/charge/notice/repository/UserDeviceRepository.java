package vn.com.atomi.charge.notice.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.com.atomi.charge.base.repository.BaseRepository;
import vn.com.atomi.charge.notice.model.entity.UserDeviceEntity;
import vn.com.atomi.charge.notice.model.enums.UserDeviceStatus;

import java.util.List;

public interface UserDeviceRepository extends BaseRepository<UserDeviceEntity, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select device
            from UserDeviceEntity device
            where device.installationId = :installationId
              and device.deletedAt is null
            """)
    List<UserDeviceEntity> findAllByInstallationIdForUpdate(
            @Param("installationId") String installationId);

    List<UserDeviceEntity> findByUserIdAndStatusAndDeletedAtIsNull(String userId, UserDeviceStatus status);

    List<UserDeviceEntity> findByStatusAndDeletedAtIsNull(UserDeviceStatus status);
}
