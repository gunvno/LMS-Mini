package vn.com.atomi.charge.authorization.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import vn.com.atomi.charge.authorization.model.dto.AuthnUserDto;
import vn.com.atomi.charge.authorization.model.request.StaffResetPasswordRequest;
import vn.com.atomi.charge.authorization.model.request.StaffAccountCreationRequest;
import vn.com.atomi.charge.authorization.model.request.StaffStatusUpdateRequest;
import vn.com.atomi.charge.base.model.response.BaseResponse;

import java.util.List;

@FeignClient(name = "lms-authn-service")
public interface AuthnClient {
    @PostMapping("/internal/v1/AuthnUser/{userId}/check")
    Boolean checkUser(@PathVariable("userId") String userId);

    @PostMapping("/internal/v1/AuthnUser/staff")
    BaseResponse<AuthnUserDto> createStaffUser(@RequestBody StaffAccountCreationRequest request);

    @GetMapping("/internal/v1/AuthnUser/{userId}/info")
    BaseResponse<AuthnUserDto> getUserById(@PathVariable String userId);

    @PostMapping("/internal/v1/AuthnUser/bulk")
    BaseResponse<List<AuthnUserDto>> getUsersByIds(@RequestBody List<String> userIds);

    @PostMapping("/internal/v1/AuthnUser/{userId}/status")
    BaseResponse<AuthnUserDto> updateUserStatus(@PathVariable String userId,
                                                @RequestBody StaffStatusUpdateRequest request);

    @PostMapping("/internal/v1/AuthnUser/{userId}/reset-password")
    BaseResponse<AuthnUserDto> resetPassword(@PathVariable String userId,
                                             @RequestBody StaffResetPasswordRequest request);
}
