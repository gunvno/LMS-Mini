package vn.com.atomi.charge.quiz.mapper;

import org.mapstruct.Mapper;
import vn.com.atomi.charge.base.mapper.EntityMapper;
import vn.com.atomi.charge.quiz.model.dto.QuestionDto;
import vn.com.atomi.charge.quiz.model.entity.QuestionEntity;

@Mapper(componentModel = "spring")
public interface QuestionMapper extends EntityMapper<String, QuestionDto, QuestionEntity> {
}
