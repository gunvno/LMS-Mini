package vn.com.atomi.charge.invoice.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import vn.com.atomi.charge.invoice.model.dto.InvoiceDto;
import vn.com.atomi.charge.invoice.service.interfaces.InvoiceService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "Invoices", description = "Invoice APIs")
public class InvoiceController {
    private final InvoiceService invoiceService;

    @Value("${internal.service-key}")
    private String internalServiceKey;

    @PostMapping("/internal/invoices")
    public ResponseEntity<?> createOrGet(@RequestBody InvoiceDto request,
                                         @RequestHeader(value = "X-Internal-Service-Key", required = false) String serviceKey) {
        if (serviceKey == null || !serviceKey.equals(internalServiceKey)) {
            return ResponseEntity.status(403).body("Forbidden");
        }
        return ResponseEntity.ok(invoiceService.createOrGet(request));
    }

    @GetMapping("/invoices/me")
    @PreAuthorize("hasAuthority('PAYMENT_VIEW')")
    public ResponseEntity<?> getMyInvoices(Pageable pageable, Authentication authentication) {
        return ResponseEntity.ok(invoiceService.getMyInvoices(authentication.getName(), pageable));
    }

    @GetMapping("/invoices/{invoiceCode}")
    @PreAuthorize("hasAuthority('PAYMENT_VIEW')")
    public ResponseEntity<?> getMyInvoice(@PathVariable String invoiceCode, Authentication authentication) {
        return ResponseEntity.ok(invoiceService.getMyInvoice(authentication.getName(), invoiceCode));
    }

    @GetMapping("/admin/invoices")
    @PreAuthorize("hasAuthority('PAYMENT_MANAGE')")
    public ResponseEntity<?> getAllInvoices(Pageable pageable) {
        return ResponseEntity.ok(invoiceService.getAllInvoices(pageable));
    }
}
