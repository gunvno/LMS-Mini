package vn.com.atomi.charge.learning.service.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.com.atomi.charge.base.model.request.BaseRequest;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.base.service.IBaseService;
import vn.com.atomi.charge.learning.mapper.EnrollmentMapper;
import vn.com.atomi.charge.learning.model.dto.EnrollmentDto;
import vn.com.atomi.charge.learning.model.entity.EnrollmentEntity;
import vn.com.atomi.charge.learning.repository.EnrollmentRepository;


public interface EnrollmentService extends IBaseService<EnrollmentRepository, EnrollmentDto, EnrollmentEntity, EnrollmentMapper> {
    BaseResponse<EnrollmentDto> enrollCourse(BaseRequest<EnrollmentDto> dto, String courseId);
    BaseResponse<Page<EnrollmentDto>> getMyEnroll(Pageable pageable);
    BaseResponse<EnrollmentDto> finishCourse(String courseId);
    BaseResponse<EnrollmentDto> findEnrollmentByCourseIdAndUserId(String courseId);
}
