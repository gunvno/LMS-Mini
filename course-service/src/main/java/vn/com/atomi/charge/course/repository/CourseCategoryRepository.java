package vn.com.atomi.charge.course.repository;

import vn.com.atomi.charge.base.repository.BaseRepository;
import vn.com.atomi.charge.course.model.entity.CourseCategoryEntity;

public interface CourseCategoryRepository extends BaseRepository<CourseCategoryEntity, String> {

    boolean existsByCodeAndDeletedAtIsNull(String code);

    boolean existsByCodeAndIdNotAndDeletedAtIsNull(String code, String id);
}
