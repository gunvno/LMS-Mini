package vn.com.atomi.charge.course.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.com.atomi.charge.base.repository.BaseRepository;
import vn.com.atomi.charge.course.model.entity.CourseCategoryEntity;
import vn.com.atomi.charge.course.model.enums.CourseCategoryStatus;
import vn.com.atomi.charge.course.model.enums.CourseStatus;

import java.util.List;

public interface CourseCategoryRepository extends BaseRepository<CourseCategoryEntity, String> {

    boolean existsByCodeAndDeletedAtIsNull(String code);

    boolean existsByCodeAndIdNotAndDeletedAtIsNull(String code, String id);

    @Query("""
            select category
              from CourseCategoryEntity category
             where category.status = :categoryStatus
               and category.deletedAt is null
               and exists (
                   select course.id
                     from CourseEntity course
                    where course.categoryId = category.id
                      and course.status = :courseStatus
                      and course.deletedAt is null
               )
             order by category.name asc
            """)
    List<CourseCategoryEntity> findCatalogCategories(
            @Param("categoryStatus") CourseCategoryStatus categoryStatus,
            @Param("courseStatus") CourseStatus courseStatus
    );
}
