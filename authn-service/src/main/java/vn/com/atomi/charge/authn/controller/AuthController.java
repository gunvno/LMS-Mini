package vn.com.atomi.charge.authn.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.com.atomi.charge.authn.model.dto.AuthenticationResult;
import vn.com.atomi.charge.authn.model.request.AuthenticationRequest;
import vn.com.atomi.charge.authn.model.request.ChangePasswordRequest;
import vn.com.atomi.charge.authn.model.request.ForgotPasswordOtpRequest;
import vn.com.atomi.charge.authn.model.request.ForgotPasswordResetRequest;
import vn.com.atomi.charge.authn.model.request.IntrospectRequest;
import vn.com.atomi.charge.authn.model.request.LogoutRequest;
import vn.com.atomi.charge.authn.model.request.OtpRegisterRequest;
import vn.com.atomi.charge.authn.model.request.OtpVerifyRequest;
import vn.com.atomi.charge.authn.model.request.RefreshRequest;
import vn.com.atomi.charge.authn.model.request.RegistrationRequest;
import vn.com.atomi.charge.authn.service.interfaces.AuthnService;
import vn.com.atomi.charge.authn.service.internal.AuthCookieService;
import vn.com.atomi.charge.base.model.response.BaseResponse;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final AuthnService authnService;
	private final AuthCookieService authCookieService;

	public AuthController(AuthnService authnService, AuthCookieService authCookieService) {
		this.authnService = authnService;
		this.authCookieService = authCookieService;
	}

	@PostMapping("/login")
	public ResponseEntity<BaseResponse<?>> authenticate(@RequestBody @Valid AuthenticationRequest request) {
		AuthenticationResult result = authnService.authenticate(request);
		return ResponseEntity.ok()
				.headers(authCookieService.sessionHeaders(result))
				.body(BaseResponse.success(HttpStatus.OK, result.response()));
	}

	@PostMapping("/introspect")
	public BaseResponse<?> introspect(@RequestBody @Valid IntrospectRequest request) {
		return BaseResponse.success(HttpStatus.OK, authnService.introspect(request));
	}

	@PostMapping("/refresh-token")
	public ResponseEntity<BaseResponse<?>> refresh(
			@CookieValue(value = AuthCookieService.REFRESH_TOKEN_COOKIE, required = false) String cookieToken,
			@RequestBody(required = false) RefreshRequest request) {
		String refreshToken = resolveToken(cookieToken, request == null ? null : request.getToken());
		AuthenticationResult result = authnService.refreshToken(refreshToken);
		return ResponseEntity.ok()
				.headers(authCookieService.sessionHeaders(result))
				.body(BaseResponse.success(HttpStatus.OK, result.response()));
	}

	@PostMapping("/logout")
	public ResponseEntity<BaseResponse<?>> logout(
			@CookieValue(value = AuthCookieService.ACCESS_TOKEN_COOKIE, required = false) String accessToken,
			@CookieValue(value = AuthCookieService.REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
			@RequestBody(required = false) LogoutRequest request) {
		authnService.logout(accessToken, refreshToken, request == null ? null : request.getToken());
		return ResponseEntity.ok()
				.headers(authCookieService.clearSessionHeaders())
				.body(BaseResponse.success(HttpStatus.OK, null));
	}

	@PostMapping("/change-password")
	@PreAuthorize("hasAuthority('USER_PASSWORD_CHANGE')")
	public BaseResponse<?> changePassword(
			@CookieValue(value = AuthCookieService.ACCESS_TOKEN_COOKIE, required = false) String cookieToken,
			@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
			@RequestBody @Valid ChangePasswordRequest request) {
		authnService.changePassword(resolveToken(cookieToken, authorization), request);
		return BaseResponse.success(HttpStatus.OK, null);
	}

	@PostMapping("/otp-register")
	public BaseResponse<?> otpRegister(@RequestBody @Valid OtpRegisterRequest request) {
		authnService.sendRegistrationOtp(request.getEmail());
		return BaseResponse.success(HttpStatus.OK, null);
	}

	@PostMapping("/otp-verify")
	public BaseResponse<?> otpVerify(@RequestBody @Valid OtpVerifyRequest request) {
		return BaseResponse.success(HttpStatus.OK, authnService.verifyOtp(request));
	}

	@PostMapping("/register")
	public BaseResponse<?> register(@RequestBody @Valid RegistrationRequest request) {
		return BaseResponse.success(HttpStatus.OK, authnService.register(request));
	}

	@PostMapping("/forgot-password/otp")
	public BaseResponse<?> forgotPasswordOtp(@RequestBody @Valid ForgotPasswordOtpRequest request) {
		authnService.sendForgotPasswordOtp(request.getEmail());
		return BaseResponse.success(HttpStatus.OK, null);
	}

	@PostMapping("/forgot-password/reset")
	public BaseResponse<?> forgotPasswordReset(@RequestBody @Valid ForgotPasswordResetRequest request) {
		authnService.resetPassword(request);
		return BaseResponse.success(HttpStatus.OK, null);
	}

	@PostMapping("/me")
	public BaseResponse<?> getUserInfo(
			@CookieValue(value = AuthCookieService.ACCESS_TOKEN_COOKIE, required = false) String cookieToken,
			@RequestBody(required = false) String bodyToken) {
		return BaseResponse.success(HttpStatus.OK,
				authnService.getInfoByToken(resolveToken(cookieToken, bodyToken)));
	}

	private String resolveToken(String preferredToken, String fallbackToken) {
		String token = preferredToken == null || preferredToken.isBlank() ? fallbackToken : preferredToken;
		if (token != null && token.regionMatches(true, 0, "Bearer ", 0, 7)) {
			return token.substring(7).trim();
		}
		return token;
	}
}
