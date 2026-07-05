package vn.com.atomi.charge.base.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.com.atomi.charge.base.mapper.EntityMapper;
import vn.com.atomi.charge.base.model.dto.BaseDto;
import vn.com.atomi.charge.base.model.request.BaseRequest;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.base.model.entity.BaseEntity;
import vn.com.atomi.charge.base.repository.BaseRepository;

import java.util.List;
import java.util.Map;

public interface IBaseService <R extends BaseRepository, D extends BaseDto, E extends BaseEntity, M extends EntityMapper> {
  public abstract BaseResponse<D> create(BaseRequest<D> dto);
  public abstract BaseResponse<D> update(BaseRequest<D> dto);
  public abstract BaseResponse<Page<D>> getAll(Map<String, String> params, Pageable pageable);
  public abstract BaseResponse<D> getDetails(String id);
  public abstract BaseResponse<D> delete(String id);
  public abstract BaseResponse<D> delete(List<String> ids);
}
