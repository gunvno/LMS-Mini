package vn.com.atomi.charge.base.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import vn.com.atomi.charge.base.mapper.EntityMapper;
import vn.com.atomi.charge.base.model.dto.BaseDto;
import vn.com.atomi.charge.base.model.enums.BaseErrorCode;
import vn.com.atomi.charge.base.model.request.BaseRequest;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.base.model.entity.BaseEntity;
import vn.com.atomi.charge.base.repository.BaseRepository;
import vn.com.atomi.charge.base.repository.BaseSpecification;
import vn.com.atomi.charge.base.util.StringUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class BaseService<
    R extends BaseRepository, D extends BaseDto, E extends BaseEntity, M extends EntityMapper>
    implements IBaseService<R, D, E, M> {

  @Autowired
  protected R repository;

  @Autowired
  protected M mapper;

  @Autowired
  protected I18nService i18n;

  protected BaseResponse<D> response;

  protected BaseResponse<Page<D>> responsePage;

  protected HttpServletRequest request;

  protected void getRequest() {
    request = ((ServletRequestAttributes) RequestContextHolder
        .getRequestAttributes()).getRequest();
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public BaseResponse<D> create(BaseRequest<D> dto) {
    response = new BaseResponse<>();
    try {
      getRequest();
      if (isDuplicate(dto)) {
//        String langHeader = request.getHeader("Accept-Language");
//        Locale locale = request.getLocale();
        String localizedMsg = i18n.getMessage("common.already_exists");
        return BaseResponse.fail(HttpStatus.BAD_REQUEST, localizedMsg);
      }
      E saved = (E) repository.save(mapper.toEntity(dto.getData()));
      response.setStatus(HttpStatus.OK);
      response.setData((D) mapper.toDto(saved));
    } catch (Exception ex) {
      response.setStatus(HttpStatus.BAD_REQUEST);
      response.setErrorCode(BaseErrorCode.FAILURE.getErrorCode());
      response.setMessage(StringUtil.beautyError(ex));
    }
    return response;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public BaseResponse<D> update(BaseRequest<D> dto) {
    response = new BaseResponse<>();
    try {
      getRequest();
      if (!repository.existsById(dto.getData().getId())) {
        String localizedMsg = i18n.getMessage("common.not_found");
        return BaseResponse.fail(HttpStatus.BAD_REQUEST, localizedMsg);
      }
      if (isDuplicate(dto)) {
        String localizedMsg = i18n.getMessage("common.already_exists");
        return BaseResponse.fail(HttpStatus.BAD_REQUEST, localizedMsg);
      }
      Optional<E> result = repository.findEntityById(dto.getData().getId());
      if (result.isPresent()) {
        E entity = result.get();
        mapper.updateFromDTO(dto.getData(), entity);
        E updated = (E) repository.save(entity);
        response.setStatus(HttpStatus.OK);
        response.setData((D) mapper.toDto(updated));
      }
    } catch (Exception ex) {
      response.setStatus(HttpStatus.BAD_REQUEST);
      response.setErrorCode(BaseErrorCode.FAILURE.getErrorCode());
      response.setMessage(StringUtil.beautyError(ex));
    }
    return response;
  }

  @Override
  public BaseResponse<Page<D>> getAll(Map<String, String> params, Pageable pageable) {
    responsePage = new BaseResponse<>();
    try {
      BaseSpecification<E> filters = new BaseSpecification<>(params);
      Sort sort = Sort.by(Sort.Direction.DESC, "lastModifiedDate", "createdDate");
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
      Page<E> result = repository.findAll(filters, sortedPageable);
      responsePage.setStatus(HttpStatus.OK);
      if(!result.getContent().isEmpty()){
        responsePage.setData((Page<D>) result.map(mapper::toDto));
      }
    } catch (Exception ex) {
      responsePage.setStatus(HttpStatus.BAD_REQUEST);
      response.setErrorCode(BaseErrorCode.FAILURE.getErrorCode());
      responsePage.setMessage(StringUtil.beautyError(ex));
    }
    return responsePage;
  }

  @Override
  public BaseResponse<D> getDetails(String id) {
    response = new BaseResponse<>();
    try {
      getRequest();
      if (!repository.existsById(id)) {
        String localizedMsg = i18n.getMessage("common.not_found");
        return BaseResponse.fail(HttpStatus.BAD_REQUEST, localizedMsg);
      }
      Optional<E> result = repository.findEntityById(id);
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      if (result.isEmpty()) {
        response.setStatus(HttpStatus.NOT_FOUND);
        return response;
      }
      response.setStatus(HttpStatus.OK);
      response.setData((D) mapper.toDto(result.get()));
    } catch (Exception ex) {
      response.setStatus(HttpStatus.BAD_REQUEST);
      response.setErrorCode(BaseErrorCode.FAILURE.getErrorCode());
      response.setMessage(StringUtil.beautyError(ex));
    }
    return response;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public BaseResponse<D> delete(String id) {
    response = new BaseResponse<>();
    try {
      getRequest();
      if (!repository.existsById(id)) {
        String localizedMsg = i18n.getMessage("common.not_found");
        return BaseResponse.fail(HttpStatus.BAD_REQUEST, localizedMsg);
      }
      String username = currentUsername();
      repository.softDelete(id, LocalDateTime.now(), username, LocalDateTime.now());
      response.setStatus(HttpStatus.OK);
    } catch (Exception ex) {
      response.setStatus(HttpStatus.BAD_REQUEST);
      response.setErrorCode(BaseErrorCode.FAILURE.getErrorCode());
      response.setMessage(StringUtil.beautyError(ex));
    }
    return response;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public BaseResponse<D> delete(List<String> ids) {
    response = new BaseResponse<>();
    try {
      String username = currentUsername();
      repository.softDelete(ids, LocalDateTime.now(), username, LocalDateTime.now());
      response.setStatus(HttpStatus.OK);
    } catch (Exception ex) {
      response.setStatus(HttpStatus.BAD_REQUEST);
      response.setErrorCode(BaseErrorCode.FAILURE.getErrorCode());
      response.setMessage(StringUtil.beautyError(ex));
    }
    return response;
  }

  protected boolean isDuplicate(BaseRequest<D> dto) {
    return false; // Override trong subclass nếu cần kiểm tra trùng lặp
  }

  private String currentUsername() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication != null && authentication.getName() != null
        ? authentication.getName()
        : "system";
  }
}
