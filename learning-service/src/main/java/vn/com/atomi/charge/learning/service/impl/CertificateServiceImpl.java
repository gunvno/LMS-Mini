package vn.com.atomi.charge.learning.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.base.service.BaseService;
import vn.com.atomi.charge.learning.mapper.CertificateMapper;
import vn.com.atomi.charge.learning.model.dto.CertificateDto;
import vn.com.atomi.charge.learning.model.entity.CertificateEntity;
import vn.com.atomi.charge.learning.repository.CertificateRepository;
import vn.com.atomi.charge.learning.service.interfaces.CertificateService;

import java.util.Optional;

@Service
public class CertificateServiceImpl extends BaseService<CertificateRepository, CertificateDto, CertificateEntity, CertificateMapper>
implements CertificateService {
    private Pageable newestFirst(Pageable pageable) {
        Sort sort = Sort.by(Sort.Direction.DESC, "issuedAt");
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    @Override
    public BaseResponse<Page<CertificateDto>> getMyCertificate(Pageable pageable){
        responsePage = new BaseResponse<>();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        Page<CertificateEntity> certificateEntity =
                repository.findByUserIdAndDeletedAtIsNull(userId, newestFirst(pageable));
        responsePage.setStatus(HttpStatus.OK);
        responsePage.setData(certificateEntity.map(mapper::toDto));
        return responsePage;
    }

    @Override
    public BaseResponse<Page<CertificateDto>> getCertificates(Pageable pageable) {
        responsePage = new BaseResponse<>();
        Page<CertificateEntity> certificateEntity = repository.findByDeletedAtIsNull(newestFirst(pageable));
        responsePage.setStatus(HttpStatus.OK);
        responsePage.setData(certificateEntity.map(mapper::toDto));
        return responsePage;
    }

    @Override
    public BaseResponse<CertificateDto> verifyCertificate(String certificateCode){
        response = new BaseResponse<>();
        if(certificateCode.isEmpty())
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("certificate.not_found"));
        Optional<CertificateEntity> optionalCertificate = repository.findByCertificateCodeAndDeletedAtIsNull(certificateCode);
        if(optionalCertificate.isEmpty())
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("certificate.not_found"));
        CertificateEntity entity = optionalCertificate.get();
        response.setData(mapper.toDto(entity));
        response.setStatus(HttpStatus.OK);
        return response;
    }

}
