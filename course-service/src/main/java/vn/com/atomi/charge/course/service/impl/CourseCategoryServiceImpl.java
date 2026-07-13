package vn.com.atomi.charge.course.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import vn.com.atomi.charge.base.model.request.BaseRequest;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.base.service.BaseService;
import vn.com.atomi.charge.course.mapper.CourseCategoryMapper;
import vn.com.atomi.charge.course.model.dto.CourseCategoryDto;
import vn.com.atomi.charge.course.model.entity.CourseCategoryEntity;
import vn.com.atomi.charge.course.model.enums.CourseCategoryStatus;
import vn.com.atomi.charge.course.model.enums.CourseStatus;
import vn.com.atomi.charge.course.repository.CourseCategoryRepository;
import vn.com.atomi.charge.course.service.interfaces.CourseCategoryService;

import java.util.List;

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

    @Override
    public BaseResponse<List<CourseCategoryDto>> getCatalogCategories() {
        List<CourseCategoryDto> categories = repository
                .findCatalogCategories(CourseCategoryStatus.ACTIVE, CourseStatus.PUBLISHED)
                .stream()
                .map(mapper::toDto)
                .toList();
        return BaseResponse.success(HttpStatus.OK, categories);
    }
}
