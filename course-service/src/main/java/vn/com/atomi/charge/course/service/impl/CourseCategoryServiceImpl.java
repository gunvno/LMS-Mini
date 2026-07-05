package vn.com.atomi.charge.course.service.impl;

import org.springframework.stereotype.Service;
import vn.com.atomi.charge.base.model.request.BaseRequest;
import vn.com.atomi.charge.base.service.BaseService;
import vn.com.atomi.charge.course.mapper.CourseCategoryMapper;
import vn.com.atomi.charge.course.model.dto.CourseCategoryDto;
import vn.com.atomi.charge.course.model.entity.CourseCategoryEntity;
import vn.com.atomi.charge.course.repository.CourseCategoryRepository;
import vn.com.atomi.charge.course.service.interfaces.CourseCategoryService;

@Service
public class CourseCategoryServiceImpl
    extends BaseService<
        CourseCategoryRepository,
        CourseCategoryDto,
        CourseCategoryEntity,
        CourseCategoryMapper>
    implements CourseCategoryService {

    @Override
    protected boolean isDuplicate(BaseRequest<CourseCategoryDto> request) {
        CourseCategoryDto dto = request.getData();
        if (dto.getCode() == null || dto.getCode().isBlank()) {
            return false;
        }
        if (dto.getId() == null) {
            return repository.existsByCodeAndDeletedAtIsNull(dto.getCode());
        }
        return repository.existsByCodeAndIdNotAndDeletedAtIsNull(dto.getCode(), dto.getId());
    }
}
