package vn.com.atomi.charge.notice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.com.atomi.charge.base.model.request.BaseRequest;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.notice.model.request.DeviceDeactivateRequest;
import vn.com.atomi.charge.notice.model.request.DeviceRegisterRequest;
import vn.com.atomi.charge.notice.service.interfaces.DeviceService;

@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('DEVICE_MANAGE')")
public class DeviceController {

    private final DeviceService service;

    @PostMapping("/register")
    public BaseResponse<Void> register(@RequestBody @Valid BaseRequest<DeviceRegisterRequest> request) {
        return service.registerDevice(request);
    }

    @PostMapping("/deactivate")
    public BaseResponse<Void> deactivate(@RequestBody @Valid BaseRequest<DeviceDeactivateRequest> request) {
        return service.deactivateDevice(request);
    }
}