package vn.com.atomi.charge.course.repository;

import vn.com.atomi.charge.base.repository.BaseRepository;
import vn.com.atomi.charge.course.model.entity.ImageEntity;
import vn.com.atomi.charge.course.model.enums.ImageObjectType;

public interface ImageRepository extends BaseRepository<ImageEntity, String> {
    boolean existsByObjectTypeAndObjectIdAndPrimaryImageTrueAndDeletedAtIsNull(
        ImageObjectType objectType,
        String objectId
    );
}
