package vn.com.atomi.charge.base.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import vn.com.atomi.charge.base.model.dto.BaseDto;
import vn.com.atomi.charge.base.model.entity.BaseEntity;

import java.util.List;

public interface EntityMapper<I, D extends BaseDto<I>, E extends BaseEntity> {

  E toEntity(D dto);

  D toDto(E entity);

  List<E> toEntity(List<D> dtoList);

  List<D> toDto(List<E> entityList);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateFromDTO(D dto, @MappingTarget E entity);
}
