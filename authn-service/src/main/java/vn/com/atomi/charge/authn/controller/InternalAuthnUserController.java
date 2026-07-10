package vn.com.atomi.charge.authn.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import vn.com.atomi.charge.authn.model.dto.AuthnUserDto;
import vn.com.atomi.charge.authn.model.request.InternalCreateUserRequest;
import vn.com.atomi.charge.authn.model.request.InternalResetPasswordRequest;
import vn.com.atomi.charge.authn.model.request.InternalUpdateUserStatusRequest;
import vn.com.atomi.charge.authn.model.request.InternalMailRequest;
import vn.com.atomi.charge.authn.service.interfaces.AuthnUserService;
import vn.com.atomi.charge.authn.service.interfaces.MailService;
import vn.com.atomi.charge.base.model.response.BaseResponse;

import java.util.List;

@RestController
@RequestMapping("/internal/v1/AuthnUser")
public class InternalAuthnUserController {

    private final AuthnUserService authnUserService;
    private final MailService mailService;

    public InternalAuthnUserController(AuthnUserService authnUserService, MailService mailService) {
        this.authnUserService = authnUserService;
        this.mailService = mailService;
    }

    @PostMapping("/{id}/check")
    public Boolean checkUser(@PathVariable String id) {
        return authnUserService.checkUser(id);
    }

    @GetMapping("/{userId}/info")
    public BaseResponse<AuthnUserDto> getUserById(@PathVariable String userId) {
        return authnUserService.getUserById(userId);
    }

    @PostMapping("/mail")
    public BaseResponse<Void> sendMail(@RequestBody @Valid InternalMailRequest request) {
        mailService.send(request.getEmail(), request.getSubject(), request.getContent());
        return BaseResponse.success(org.springframework.http.HttpStatus.OK, null);
    }

    @GetMapping("/username/{username}/info")
    public BaseResponse<AuthnUserDto> getUserByUsername(@PathVariable String username) {
        return authnUserService.getUserByUsername(username);
    }

    @PostMapping("/bulk")
    public BaseResponse<List<AuthnUserDto>> getUsersByIds(@RequestBody List<String> userIds) {
        return authnUserService.getUsersByIds(userIds);
    }

    @PostMapping("/staff")
    public BaseResponse<AuthnUserDto> createStaffUser(@RequestBody @Valid InternalCreateUserRequest request) {
        return authnUserService.createStaffUser(request);
    }

    @PostMapping("/{userId}/status")
    public BaseResponse<AuthnUserDto> updateUserStatus(@PathVariable String userId,
                                                       @RequestBody @Valid InternalUpdateUserStatusRequest request) {
        return authnUserService.updateUserStatus(userId, request);
    }

    @PostMapping("/{userId}/reset-password")
    public BaseResponse<AuthnUserDto> resetPassword(@PathVariable String userId,
                                                    @RequestBody InternalResetPasswordRequest request) {
        return authnUserService.resetPassword(userId, request);
    }
}
