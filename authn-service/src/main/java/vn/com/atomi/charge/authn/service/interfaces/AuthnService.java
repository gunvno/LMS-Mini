package vn.com.atomi.charge.authn.service.interfaces;

import vn.com.atomi.charge.authn.model.dto.AuthenticationResult;
import vn.com.atomi.charge.authn.model.request.AuthenticationRequest;
import vn.com.atomi.charge.authn.model.request.ChangePasswordRequest;
import vn.com.atomi.charge.authn.model.request.ForgotPasswordResetRequest;
import vn.com.atomi.charge.authn.model.request.IntrospectRequest;
import vn.com.atomi.charge.authn.model.request.OtpVerifyRequest;
import vn.com.atomi.charge.authn.model.request.RegistrationRequest;
import vn.com.atomi.charge.authn.model.response.IntrospectResponse;
import vn.com.atomi.charge.authn.model.response.UserInfoResponse;

public interface AuthnService {
	AuthenticationResult authenticate(AuthenticationRequest request);

	IntrospectResponse introspect(IntrospectRequest request);

	AuthenticationResult refreshToken(String refreshToken);

	void logout(String... tokens);

	void changePassword(String token, ChangePasswordRequest request);

	String register(RegistrationRequest request);

	void sendRegistrationOtp(String email);

	void sendForgotPasswordOtp(String email);

	void resetPassword(ForgotPasswordResetRequest request);

	boolean verifyOtp(OtpVerifyRequest request);

	UserInfoResponse getInfoByToken(String token);
}
