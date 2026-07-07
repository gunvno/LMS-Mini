package vn.com.atomi.charge.quiz.mapper;

import org.mapstruct.Mapper;
import vn.com.atomi.charge.base.mapper.EntityMapper;
import vn.com.atomi.charge.quiz.model.dto.QuizAttemptDto;
import vn.com.atomi.charge.quiz.model.entity.QuizAttemptEntity;

@Mapper(componentModel = "spring")
public interface QuizAttemptMapper extends EntityMapper<String, QuizAttemptDto, QuizAttemptEntity> {
}
