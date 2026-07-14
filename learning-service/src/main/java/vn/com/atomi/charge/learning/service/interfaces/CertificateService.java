package vn.com.atomi.charge.learning.service.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.base.service.IBaseService;
import vn.com.atomi.charge.learning.mapper.CertificateMapper;
import vn.com.atomi.charge.learning.model.dto.CertificateDto;
import vn.com.atomi.charge.learning.model.entity.CertificateEntity;
import vn.com.atomi.charge.learning.repository.CertificateRepository;

public interface CertificateService extends IBaseService<CertificateRepository, CertificateDto, CertificateEntity, CertificateMapper> {
    BaseResponse<Page<CertificateDto>> getMyCertificate(Pageable pageable);
    BaseResponse<Page<CertificateDto>> getCertificates(Pageable pageable);
    BaseResponse<CertificateDto> verifyCertificate(String certificateCode);
}
