package vn.com.atomi.charge.notice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.com.atomi.charge.base.model.request.BaseRequest;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.notice.model.request.NoticeSendAllRequest;
import vn.com.atomi.charge.notice.model.request.NoticeSendRoleRequest;
import vn.com.atomi.charge.notice.model.request.NoticeSendUserRequest;
import vn.com.atomi.charge.notice.model.request.NoticeSendUsersRequest;
import vn.com.atomi.charge.notice.service.interfaces.NoticeService;

@RestController
@RequestMapping("/internal/v1/notices")
@RequiredArgsConstructor
public class InternalNoticeController {

    private final NoticeService service;

    @PostMapping("/send-user")
    public BaseResponse<Void> sendUser(@RequestBody @Valid BaseRequest<NoticeSendUserRequest> request) {
        return service.sendUser(request);
    }

    @PostMapping("/send-users")
    public BaseResponse<Void> sendUsers(@RequestBody @Valid BaseRequest<NoticeSendUsersRequest> request) {
        return service.sendUsers(request);
    }

    @PostMapping("/send-role")
    public BaseResponse<Void> sendRole(@RequestBody @Valid BaseRequest<NoticeSendRoleRequest> request) {
        return service.sendRole(request);
    }

    @PostMapping("/send-all")
    public BaseResponse<Void> sendAll(@RequestBody @Valid BaseRequest<NoticeSendAllRequest> request) {
        return service.sendAll(request);
    }
}
