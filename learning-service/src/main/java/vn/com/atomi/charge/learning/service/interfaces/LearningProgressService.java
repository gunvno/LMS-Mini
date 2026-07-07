package vn.com.atomi.charge.learning.service.interfaces;

import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.base.service.IBaseService;
import vn.com.atomi.charge.learning.mapper.LearningProgressMapper;
import vn.com.atomi.charge.learning.model.dto.EnrollmentDto;
import vn.com.atomi.charge.learning.model.dto.LearningProgressDto;
import vn.com.atomi.charge.learning.model.entity.LearningProgressEntity;
import vn.com.atomi.charge.learning.repository.LearningProgressRepository;

public interface LearningProgressService extends IBaseService<LearningProgressRepository, LearningProgressDto, LearningProgressEntity, LearningProgressMapper> {
    BaseResponse<LearningProgressDto> startLesson(String lessonId);
    BaseResponse<LearningProgressDto> finishLesson(String lessonId);
}
