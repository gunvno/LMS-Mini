package vn.com.atomi.charge.course.repository;

import vn.com.atomi.charge.base.repository.BaseRepository;
import vn.com.atomi.charge.course.model.entity.ImageEntity;
import vn.com.atomi.charge.course.model.enums.ImageObjectType;

import java.util.List;
import java.util.Optional;

public interface ImageRepository extends BaseRepository<ImageEntity, String> {
    boolean existsByObjectTypeAndObjectIdAndPrimaryImageTrueAndDeletedAtIsNull(
        ImageObjectType objectType,
        String objectId
    );

    List<ImageEntity> findByObjectTypeAndObjectIdAndDeletedAtIsNull(
        ImageObjectType objectType,
        String objectId
    );

    Optional<ImageEntity> findFirstByObjectTypeAndObjectIdAndPrimaryImageTrueAndDeletedAtIsNull(
        ImageObjectType objectType,
        String objectId
    );
}
