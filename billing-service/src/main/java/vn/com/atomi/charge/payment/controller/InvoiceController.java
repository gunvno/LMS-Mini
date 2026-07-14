package vn.com.atomi.charge.payment.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.com.atomi.charge.payment.service.interfaces.InvoiceService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "Invoices", description = "Invoice APIs")
public class InvoiceController {

    private final InvoiceService invoiceService;

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
