package vn.com.atomi.charge.notice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.com.atomi.charge.notice.model.entity.UserDeviceEntity;
import vn.com.atomi.charge.notice.service.interfaces.DeviceService;

import java.util.List;

@RestController
@RequestMapping("/internal/v1/devices")
@RequiredArgsConstructor
public class InternalDeviceController {

    private final DeviceService service;

    @GetMapping("/users/{userId}/active")
    public List<UserDeviceEntity> getActiveDevices(@PathVariable String userId) {
        return service.getActiveDevices(userId);
    }
}