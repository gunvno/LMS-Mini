package vn.com.atomi.charge.authn.service.interfaces;

import vn.com.atomi.charge.authn.model.request.AuthenticationRequest;
import vn.com.atomi.charge.authn.model.request.ChangePasswordRequest;
import vn.com.atomi.charge.authn.model.request.IntrospectRequest;
import vn.com.atomi.charge.authn.model.request.LogoutRequest;
import vn.com.atomi.charge.authn.model.request.OtpRegisterRequest;
import vn.com.atomi.charge.authn.model.request.OtpVerifyRequest;
import vn.com.atomi.charge.authn.model.request.RefreshRequest;
import vn.com.atomi.charge.authn.model.request.RegistrationRequest;
import vn.com.atomi.charge.authn.model.response.AuthenticationResponse;
import vn.com.atomi.charge.authn.model.response.IntrospectResponse;
import vn.com.atomi.charge.authn.model.response.UserInfoResponse;

public interface AuthnService {
	AuthenticationResponse authenticate(AuthenticationRequest request);

	IntrospectResponse introspect(IntrospectRequest request);

	AuthenticationResponse refreshToken(RefreshRequest request);

	void logout(LogoutRequest request);

	void changePassword(String token, ChangePasswordRequest request);

	String register(RegistrationRequest request);

	void sendRegistrationOtp(String email);

	boolean verifyOtp(OtpVerifyRequest request);

	UserInfoResponse getInfoByToken(String token);
}
