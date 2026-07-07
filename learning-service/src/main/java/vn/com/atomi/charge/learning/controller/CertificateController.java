package vn.com.atomi.charge.learning.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.com.atomi.charge.base.controller.BaseController;
import vn.com.atomi.charge.learning.model.dto.CertificateDto;
import vn.com.atomi.charge.learning.service.interfaces.CertificateService;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Certificate", description = "APIs for course certificate")
@PreAuthorize("hasAuthority('CERTIFICATE_VIEW')")
public class CertificateController extends BaseController<CertificateService, CertificateDto> {
    @GetMapping("/my-certificates")
    @PreAuthorize("hasAuthority('CERTIFICATE_VIEW')")
    public ResponseEntity<?> getMyCertificate(Pageable pageable){
        return ResponseEntity.ok(service.getMyCertificate(pageable));
    }
    @GetMapping("/certificates/{code}")
    @PreAuthorize("hasAuthority('CERTIFICATE_VERIFY')")
    public ResponseEntity<?> verifyCertificate(@PathVariable String code){
        return ResponseEntity.ok(service.verifyCertificate(code));
    }
}
