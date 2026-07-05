package vn.com.atomi.charge.quiz.mapper;

import org.mapstruct.Mapper;
import vn.com.atomi.charge.base.mapper.EntityMapper;
import vn.com.atomi.charge.quiz.model.dto.QuizDto;
import vn.com.atomi.charge.quiz.model.entity.QuizEntity;

@Mapper(componentModel = "spring")
public interface QuizMapper extends EntityMapper<String, QuizDto, QuizEntity> {
}
