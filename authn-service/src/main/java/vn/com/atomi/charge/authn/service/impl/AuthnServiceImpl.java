package vn.com.atomi.charge.authn.service.impl;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.atomi.charge.authn.client.AuthorClient;
import vn.com.atomi.charge.authn.model.dto.AuthenticationResult;
import vn.com.atomi.charge.authn.model.entity.AuthnUserEntity;
import vn.com.atomi.charge.authn.model.entity.RefreshTokenEntity;
import vn.com.atomi.charge.authn.model.enums.AuthnUserStatus;
import vn.com.atomi.charge.authn.model.enums.ClientPortal;
import vn.com.atomi.charge.authn.model.enums.ErrorCode;
import vn.com.atomi.charge.authn.model.enums.RefreshTokenStatus;
import vn.com.atomi.charge.authn.model.enums.UserLanguage;
import vn.com.atomi.charge.authn.model.request.AuthenticationRequest;
import vn.com.atomi.charge.authn.model.request.ChangePasswordRequest;
import vn.com.atomi.charge.authn.model.request.ForgotPasswordResetRequest;
import vn.com.atomi.charge.authn.model.request.IntrospectRequest;
import vn.com.atomi.charge.authn.model.request.OtpVerifyRequest;
import vn.com.atomi.charge.authn.model.request.RegistrationRequest;
import vn.com.atomi.charge.authn.model.response.AuthenticationResponse;
import vn.com.atomi.charge.authn.model.response.IntrospectResponse;
import vn.com.atomi.charge.authn.model.response.UserInfoResponse;
import vn.com.atomi.charge.authn.repository.AuthnRepo;
import vn.com.atomi.charge.authn.repository.AuthnUserRepository;
import vn.com.atomi.charge.authn.service.interfaces.AuthnService;
import vn.com.atomi.charge.authn.service.interfaces.MailService;
import vn.com.atomi.charge.base.model.exception.BusinessException;
import vn.com.atomi.charge.base.model.response.BaseResponse;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class AuthnServiceImpl implements AuthnService {

	private static final String TOKEN_ISSUER = "lms.authn-service";
	private static final Duration OTP_VALIDITY = Duration.ofMinutes(5);
	private static final Duration REGISTRATION_VERIFIED_VALIDITY = Duration.ofMinutes(10);

	private final AuthnUserRepository userRepository;
	private final AuthnRepo refreshTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final RedisTemplate<String, String> redisTemplate;
	private final MailService mailService;
	private final AuthorClient authorClient;

	@Value("${jwt.signerKey}")
	private String signerKey;

	@Value("${jwt.valid-duration}")
	private long validDuration;

	@Value("${jwt.refreshable-duration}")
	private long refreshableDuration;

	public AuthnServiceImpl(AuthnUserRepository userRepository,
							AuthnRepo refreshTokenRepository,
							PasswordEncoder passwordEncoder,
							RedisTemplate<String, String> redisTemplate,
							MailService mailService,
							AuthorClient authorClient) {
		this.userRepository = userRepository;
		this.refreshTokenRepository = refreshTokenRepository;
		this.passwordEncoder = passwordEncoder;
		this.redisTemplate = redisTemplate;
		this.mailService = mailService;
		this.authorClient = authorClient;
	}

	@Override
	public AuthenticationResult authenticate(AuthenticationRequest request, ClientPortal portal) {
		AuthnUserEntity user = userRepository.findByUsernameOrEmail(request.getUsername(), request.getUsername())
				.orElseThrow(() -> businessException(ErrorCode.LOGIN_FAILED));

		if (user.getStatus() != AuthnUserStatus.ACTIVE || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
			throw businessException(ErrorCode.LOGIN_FAILED);
		}
		validatePortalAccess(user.getId(), portal);

		user.setLastLoginAt(LocalDateTime.now());
		userRepository.save(user);
		return buildAuthResponse(user, portal);
	}

	@Override
	public IntrospectResponse introspect(IntrospectRequest request, ClientPortal portal) {
		try {
			verifyToken(request.getToken(), portal);
			return IntrospectResponse.builder().valid(true).build();
		} catch (Exception exception) {
			return IntrospectResponse.builder().valid(false).build();
		}
	}

	@Override
	public AuthenticationResult refreshToken(String refreshToken, ClientPortal portal) {
		try {
			SignedJWT jwt = verifyToken(refreshToken, portal);
			invalidateToken(jwt);

			String userId = jwt.getJWTClaimsSet().getSubject();
			AuthnUserEntity user = userRepository.findEntityById(userId)
					.orElseThrow(this::unauthenticatedException);

			if (user.getStatus() != AuthnUserStatus.ACTIVE) {
				throw unauthenticatedException();
			}
			validatePortalAccess(user.getId(), portal);

			return buildAuthResponse(user, portal);
		} catch (BusinessException exception) {
			throw exception;
		} catch (Exception exception) {
			throw unauthenticatedException();
		}
	}

	@Override
	public void logout(String... tokens) {
		if (tokens == null) {
			return;
		}
		for (String token : tokens) {
			if (token == null || token.isBlank()) {
				continue;
			}
			try {
				invalidateToken(verifyToken(token));
			} catch (Exception exception) {
				log.debug("Ignoring an already invalid session token during logout");
			}
		}
	}

	@Override
	@Transactional
	public void changePassword(String token, ChangePasswordRequest request, ClientPortal portal) {
		try {
			SignedJWT jwt = verifyToken(token, portal);
			AuthnUserEntity user = userRepository.findEntityById(jwt.getJWTClaimsSet().getSubject())
					.orElseThrow(() -> businessException(ErrorCode.USER_NOT_FOUND));

			if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
				throw businessException(ErrorCode.INVALID_OLD_PASSWORD);
			}
			if (!request.getNewPassword().equals(request.getConfirmPassword())) {
				throw businessException(ErrorCode.INVALID_CONFIRM_PASSWORD);
			}
			if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
				throw businessException(ErrorCode.OLD_PASSWORD_EQUAL_NEW_PASSWORD);
			}

			user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
			user.setPasswordChangeAt(LocalDateTime.now());
			user.setLastModifiedBy(user.getUsername());
			user.setLastModifiedDate(LocalDateTime.now());
			userRepository.saveAndFlush(user);
			invalidateToken(jwt);
		} catch (BusinessException exception) {
			throw exception;
		} catch (Exception exception) {
			throw unauthenticatedException();
		}
	}

	@Override
	public String register(RegistrationRequest request) {
		String email = normalizeEmail(request.getEmail());
		if (!Boolean.TRUE.equals(redisTemplate.hasKey(registrationVerifiedKey(email)))) {
			throw new BusinessException("OTP_REQUIRED", "auth.otp_required");
		}

		AuthnUserEntity user = userRepository.findByEmail(email).orElse(null);
		if (user != null && user.getStatus() != AuthnUserStatus.INACTIVE) {
			throw new BusinessException("EMAIL_EXISTED", "auth.email_existed");
		}

		AuthnUserEntity usernameOwner = userRepository.findByUsername(request.getUsername()).orElse(null);
		if (usernameOwner != null && (user == null || !usernameOwner.getId().equals(user.getId()))) {
			throw new BusinessException("USER_EXISTED", "auth.username_existed");
		}
		if (user == null) {
			user = new AuthnUserEntity();
			user.setCreatedBy("system");
			user.setCreatedDate(LocalDateTime.now());
		}
		user.setUsername(request.getUsername());
		user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
		user.setEmail(email);
		user.setPhone(request.getPhone());
		user.setFullName(request.getFirstName() + " " + request.getLastName());
		user.setLanguage(UserLanguage.VI);
		user.setStatus(AuthnUserStatus.ACTIVE);
		user.setFailedLoginAttempts(0);
		user.setLastModifiedBy(request.getUsername());
		user.setLastModifiedDate(LocalDateTime.now());
		AuthnUserEntity saved = userRepository.save(user);
		redisTemplate.delete(registrationVerifiedKey(email));
		assignStudentRole(saved.getId());
		return saved.getId();
	}

	@Override
	public void sendRegistrationOtp(String email) {
		String normalizedEmail = normalizeEmail(email);
		userRepository.findByEmail(normalizedEmail).ifPresent(user -> {
			if (user.getStatus() != AuthnUserStatus.INACTIVE) {
				throw new BusinessException("EMAIL_EXISTED", "auth.email_existed");
			}
		});
		String otp = createOtp(normalizedEmail, "REGISTER");
		try {
			mailService.sendOtp(normalizedEmail, "EduFlow - Xác thực đăng ký", otp, "Xác thực đăng ký tài khoản");
		} catch (RuntimeException exception) {
			redisTemplate.delete(otpKey(normalizedEmail, "REGISTER"));
			throw exception;
		}
	}

	@Override
	public void sendForgotPasswordOtp(String email) {
		AuthnUserEntity user = userRepository.findByUsernameOrEmail(email, email)
				.orElseThrow(() -> businessException(ErrorCode.USER_NOT_FOUND));
		if (user.getStatus() == AuthnUserStatus.DELETED) {
			throw businessException(ErrorCode.USER_NOT_FOUND);
		}
		String normalizedEmail = normalizeEmail(email);
		String otp = createOtp(normalizedEmail, "RESET_PASSWORD");
		try {
			mailService.sendOtp(normalizedEmail, "EduFlow - Đặt lại mật khẩu", otp, "Đặt lại mật khẩu");
		} catch (RuntimeException exception) {
			redisTemplate.delete(otpKey(normalizedEmail, "RESET_PASSWORD"));
			throw exception;
		}
	}

	@Override
	public void resetPassword(ForgotPasswordResetRequest request) {
		if (!request.getNewPassword().equals(request.getConfirmPassword())) {
			throw businessException(ErrorCode.INVALID_CONFIRM_PASSWORD);
		}

		if (!verifyOtpValue(request.getEmail(), request.getInputOtp(), "RESET_PASSWORD", true)) {
			throw new BusinessException("OTP_INVALID", "auth.otp_invalid");
		}

		AuthnUserEntity user = userRepository.findByUsernameOrEmail(request.getEmail(), request.getEmail())
				.orElseThrow(() -> businessException(ErrorCode.USER_NOT_FOUND));
		user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
		user.setPasswordChangeAt(LocalDateTime.now());
		user.setLastModifiedBy(user.getUsername());
		user.setLastModifiedDate(LocalDateTime.now());
		if (user.getStatus() == AuthnUserStatus.INACTIVE) {
			user.setStatus(AuthnUserStatus.ACTIVE);
		}
		userRepository.save(user);
	}

	@Override
	public boolean verifyOtp(OtpVerifyRequest request) {
		boolean matched = verifyOtpValue(request.getEmail(), request.getInputOtp(), request.getExpectedType(), true);
		if (!matched) {
			throw new BusinessException("OTP_INVALID", "auth.otp_invalid");
		}
		if ("REGISTER".equals(request.getExpectedType())) {
			redisTemplate.opsForValue().set(
					registrationVerifiedKey(normalizeEmail(request.getEmail())),
					"true",
					REGISTRATION_VERIFIED_VALIDITY);
		}
		return true;
	}

	private void assignStudentRole(String userId) {
		try {
			authorClient.assignStudentRole(userId);
		} catch (Exception ex) {
			log.warn("Could not assign STUDENT role for userId={}", userId, ex);
		}
	}

	private String createOtp(String email, String purpose) {
		String otp = String.format("%06d", new Random().nextInt(900000) + 100000);
		redisTemplate.opsForValue().set(otpKey(email, purpose), otp, OTP_VALIDITY);
		return otp;
	}

	private boolean verifyOtpValue(String email, String inputOtp, String expectedType, boolean deleteOnMatch) {
		String key = otpKey(email, expectedType);
		String cached = redisTemplate.opsForValue().get(key);
		if (cached == null) {
			return false;
		}
		boolean matched = cached.equals(inputOtp);
		if (matched && deleteOnMatch) {
			redisTemplate.delete(key);
		}
		return matched;
	}

	@Override
	public UserInfoResponse getInfoByToken(String token, ClientPortal portal) {
		try {
			SignedJWT jwt = verifyToken(token, portal);
			AuthnUserEntity user = userRepository.findEntityById(jwt.getJWTClaimsSet().getSubject())
					.orElseThrow(this::unauthenticatedException);

			return UserInfoResponse.builder()
					.sub(user.getId())
					.email(user.getEmail())
					.preferred_username(user.getUsername())
					.given_name(user.getFullName())
					.family_name("")
					.build();
		} catch (Exception exception) {
			throw unauthenticatedException();
		}
	}

	private AuthenticationResult buildAuthResponse(AuthnUserEntity user, ClientPortal portal) {
		try {
			AuthenticationResponse response = AuthenticationResponse.builder()
					.id(user.getId())
					.userName(user.getUsername())
					.email(user.getEmail())
					.firstName(extractFirstName(user.getFullName()))
					.lastName(extractLastName(user.getFullName()))
					.build();
			return new AuthenticationResult(
					generateToken(user, validDuration, portal),
					generateToken(user, refreshableDuration, portal),
					response);
		} catch (JOSEException exception) {
			throw new BusinessException("INVALID_KEY", "auth.invalid_key");
		}
	}

	private String generateToken(AuthnUserEntity user, long durationSeconds, ClientPortal portal) throws JOSEException {
		Instant now = Instant.now();
		Set<String> authorities = new LinkedHashSet<>();
		JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
				.subject(user.getId())
				.issuer(TOKEN_ISSUER)
				.issueTime(Date.from(now))
				.expirationTime(Date.from(now.plusSeconds(durationSeconds)))
				.jwtID(UUID.randomUUID().toString())
				.claim("username", user.getUsername())
				.claim("email", user.getEmail())
				.claim("portal", portal.name())
				.claim("scope", String.join(" ", authorities))
				.build();

		JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.HS512), new Payload(claimsSet.toJSONObject()));
		jwsObject.sign(new MACSigner(signerKey.getBytes()));
		return jwsObject.serialize();
	}

	private SignedJWT verifyToken(String token) throws Exception {
		SignedJWT signedJWT = SignedJWT.parse(normalizeToken(token));
		JWSVerifier verifier = new MACVerifier(signerKey.getBytes());
		if (!signedJWT.verify(verifier)) {
			throw unauthenticatedException();
		}
		Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
		if (expirationTime == null || expirationTime.before(new Date()) || refreshTokenRepository.existsByTokenId(signedJWT.getJWTClaimsSet().getJWTID())) {
			throw unauthenticatedException();
		}
		return signedJWT;
	}

	private SignedJWT verifyToken(String token, ClientPortal portal) throws Exception {
		SignedJWT signedJWT = verifyToken(token);
		String tokenPortal = signedJWT.getJWTClaimsSet().getStringClaim("portal");
		if (portal == null || !portal.name().equalsIgnoreCase(tokenPortal)) {
			throw unauthenticatedException();
		}
		return signedJWT;
	}

	private void invalidateToken(SignedJWT signedJWT) throws Exception {
		String jwtId = signedJWT.getJWTClaimsSet().getJWTID();
		RefreshTokenEntity entity = new RefreshTokenEntity();
		entity.setTokenId(jwtId);
		entity.setUserId(signedJWT.getJWTClaimsSet().getSubject());
		entity.setRefreshTokenHash(jwtId);
		entity.setIssuedAt(LocalDateTime.now());
		entity.setExpiredAt(LocalDateTime.now());
		entity.setStatus(RefreshTokenStatus.REVOKED);
		refreshTokenRepository.save(entity);
	}

	private String normalizeToken(String token) {
		return token != null && token.startsWith("Bearer ") ? token.substring(7) : token;
	}

	private String otpKey(String email, String purpose) {
		return "otp:" + purpose + ":" + normalizeEmail(email);
	}

	private String registrationVerifiedKey(String email) {
		return "otp:REGISTER_VERIFIED:" + normalizeEmail(email);
	}

	private String normalizeEmail(String email) {
		return email == null ? "" : email.trim().toLowerCase();
	}

	private BusinessException businessException(ErrorCode errorCode) {
		return new BusinessException(errorCode.getErrorCode(), errorCode.getMessageKey());
	}

	private BusinessException unauthenticatedException() {
		return new BusinessException("UNAUTHENTICATED", "security.invalid_token");
	}

	private void validatePortalAccess(String userId, ClientPortal portal) {
		if (portal == null) {
			throw new BusinessException("INVALID_PORTAL", "auth.portal_required");
		}
		try {
			BaseResponse<List<String>> response = authorClient.getUserRoles(userId);
			List<String> roles = response == null || response.getData() == null
					? List.of()
					: response.getData().stream()
							.filter(Objects::nonNull)
							.map(role -> role.trim().toUpperCase(Locale.ROOT))
							.toList();
			boolean allowed = portal == ClientPortal.STUDENT
					? roles.contains("STUDENT")
					: roles.contains("ADMIN") || roles.contains("INSTRUCTOR");
			if (!allowed) {
				String messageKey = portal == ClientPortal.STUDENT
						? "auth.student_portal_forbidden"
						: "auth.admin_portal_forbidden";
				throw new BusinessException("FORBIDDEN", messageKey);
			}
		} catch (BusinessException exception) {
			throw exception;
		} catch (Exception exception) {
			log.error("Could not verify portal access for userId={}", userId, exception);
			throw new BusinessException("FORBIDDEN", "auth.portal_access_check_failed");
		}
	}

	private String extractFirstName(String fullName) {
		if (fullName == null || fullName.isBlank()) {
			return null;
		}
		String[] parts = fullName.trim().split("\\s+");
		return parts.length > 0 ? parts[0] : fullName;
	}

	private String extractLastName(String fullName) {
		if (fullName == null || fullName.isBlank()) {
			return null;
		}
		String[] parts = fullName.trim().split("\\s+");
		return parts.length > 1 ? parts[parts.length - 1] : "";
	}
}
