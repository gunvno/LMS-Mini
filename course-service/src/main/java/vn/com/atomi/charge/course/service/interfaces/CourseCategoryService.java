package vn.com.atomi.charge.course.service.interfaces;

import vn.com.atomi.charge.base.service.IBaseService;
import vn.com.atomi.charge.course.mapper.CourseCategoryMapper;
import vn.com.atomi.charge.course.model.dto.CourseCategoryDto;
import vn.com.atomi.charge.course.model.entity.CourseCategoryEntity;
import vn.com.atomi.charge.course.repository.CourseCategoryRepository;

public interface CourseCategoryService extends IBaseService<
    CourseCategoryRepository,
    CourseCategoryDto,
    CourseCategoryEntity,
    CourseCategoryMapper> {
}
