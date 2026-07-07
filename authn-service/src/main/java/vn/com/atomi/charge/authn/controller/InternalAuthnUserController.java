package vn.com.atomi.charge.authn.controller;

import org.springframework.web.bind.annotation.*;
import vn.com.atomi.charge.authn.model.dto.AuthnUserDto;
import vn.com.atomi.charge.authn.service.interfaces.AuthnUserService;
import vn.com.atomi.charge.base.model.response.BaseResponse;

@RestController
@RequestMapping("/internal/v1/AuthnUser")
public class InternalAuthnUserController{
    private final AuthnUserService authnUserService;

    public InternalAuthnUserController(AuthnUserService authnUserService) {
        this.authnUserService = authnUserService;
    }

    @PostMapping("/{id}/check")
    public Boolean checkUser(@PathVariable String id) {
        return authnUserService.checkUser(id);
    }
    @GetMapping("/{userId}/info")
    public BaseResponse<AuthnUserDto> getUserById(@PathVariable String userId){
        return authnUserService.getUserById(userId);
    }
}
