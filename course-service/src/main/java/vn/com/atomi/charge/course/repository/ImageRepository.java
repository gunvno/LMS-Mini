package vn.com.atomi.charge.course.repository;

import vn.com.atomi.charge.base.repository.BaseRepository;
import vn.com.atomi.charge.course.model.entity.ImageEntity;
import vn.com.atomi.charge.course.model.enums.ImageObjectType;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("""
            select i from ImageEntity i, CourseEntity c
            where i.objectType = vn.com.atomi.charge.course.model.enums.ImageObjectType.COURSE
              and i.objectId = c.id
              and c.instructorId = :instructorId
              and i.deletedAt is null
              and c.deletedAt is null
            """)
    Page<ImageEntity> findCourseImagesByInstructorId(
            @Param("instructorId") String instructorId, Pageable pageable);
}
