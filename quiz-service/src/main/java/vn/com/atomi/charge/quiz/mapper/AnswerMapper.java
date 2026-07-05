package vn.com.atomi.charge.quiz.mapper;

import org.mapstruct.Mapper;
import vn.com.atomi.charge.base.mapper.EntityMapper;
import vn.com.atomi.charge.quiz.model.dto.AnswerDto;
import vn.com.atomi.charge.quiz.model.entity.AnswerEntity;

@Mapper(componentModel = "spring")
public interface AnswerMapper extends EntityMapper<String, AnswerDto, AnswerEntity> {
}
