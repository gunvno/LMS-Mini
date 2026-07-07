package vn.com.atomi.charge.authn.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.com.atomi.charge.authn.model.request.AuthenticationRequest;
import vn.com.atomi.charge.authn.model.request.ChangePasswordRequest;
import vn.com.atomi.charge.authn.model.request.IntrospectRequest;
import vn.com.atomi.charge.authn.model.request.LogoutRequest;
import vn.com.atomi.charge.authn.model.request.OtpRegisterRequest;
import vn.com.atomi.charge.authn.model.request.OtpVerifyRequest;
import vn.com.atomi.charge.authn.model.request.RefreshRequest;
import vn.com.atomi.charge.authn.model.request.RegistrationRequest;
import vn.com.atomi.charge.authn.service.interfaces.AuthnService;
import vn.com.atomi.charge.base.model.response.BaseResponse;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final AuthnService authnService;

	public AuthController(AuthnService authnService) {
		this.authnService = authnService;
	}

	@PostMapping("/login")
	public BaseResponse<?> authenticate(@RequestBody @Valid AuthenticationRequest request) {
		return BaseResponse.success(org.springframework.http.HttpStatus.OK, authnService.authenticate(request));
	}

	@PostMapping("/introspect")
	public BaseResponse<?> introspect(@RequestBody @Valid IntrospectRequest request) {
		return BaseResponse.success(org.springframework.http.HttpStatus.OK, authnService.introspect(request));
	}

	@PostMapping("/refresh-token")
	public BaseResponse<?> refresh(@RequestBody @Valid RefreshRequest request) {
		return BaseResponse.success(org.springframework.http.HttpStatus.OK, authnService.refreshToken(request));
	}

	@PostMapping("/logout")
	public BaseResponse<?> logout(@RequestBody @Valid LogoutRequest request) {
		authnService.logout(request);
		return BaseResponse.success(org.springframework.http.HttpStatus.OK, null);
	}

	@PostMapping("/change-password")
	@PreAuthorize("hasAuthority('USER_PASSWORD_CHANGE')")
	public BaseResponse<?> changePassword(@RequestHeader(HttpHeaders.AUTHORIZATION) String token,
										  @RequestBody @Valid ChangePasswordRequest request) {
		authnService.changePassword(token, request);
		return BaseResponse.success(org.springframework.http.HttpStatus.OK, null);
	}

	@PostMapping("/otp-register")
	public BaseResponse<?> otpRegister(@RequestBody @Valid OtpRegisterRequest request) {
		authnService.sendRegistrationOtp(request.getEmail());
		return BaseResponse.success(org.springframework.http.HttpStatus.OK, null);
	}

	@PostMapping("/otp-verify")
	public BaseResponse<?> otpVerify(@RequestBody @Valid OtpVerifyRequest request) {
		return BaseResponse.success(org.springframework.http.HttpStatus.OK, authnService.verifyOtp(request));
	}

	@PostMapping("/register")
	public BaseResponse<?> register(@RequestBody @Valid RegistrationRequest request) {
		return BaseResponse.success(org.springframework.http.HttpStatus.OK, authnService.register(request));
	}

	@PostMapping("/me")
	public BaseResponse<?> getUserInfo(@RequestBody String token) {
		return BaseResponse.success(org.springframework.http.HttpStatus.OK, authnService.getInfoByToken(token));
	}
}
