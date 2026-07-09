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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.com.atomi.charge.authn.model.entity.AuthnUserEntity;
import vn.com.atomi.charge.authn.model.entity.RefreshTokenEntity;
import vn.com.atomi.charge.authn.model.enums.AuthnUserStatus;
import vn.com.atomi.charge.authn.model.enums.ErrorCode;
import vn.com.atomi.charge.authn.model.enums.RefreshTokenStatus;
import vn.com.atomi.charge.authn.model.enums.UserLanguage;
import vn.com.atomi.charge.authn.model.request.AuthenticationRequest;
import vn.com.atomi.charge.authn.model.request.ChangePasswordRequest;
import vn.com.atomi.charge.authn.model.request.IntrospectRequest;
import vn.com.atomi.charge.authn.model.request.LogoutRequest;
import vn.com.atomi.charge.authn.model.request.OtpVerifyRequest;
import vn.com.atomi.charge.authn.model.request.RefreshRequest;
import vn.com.atomi.charge.authn.model.request.RegistrationRequest;
import vn.com.atomi.charge.authn.model.response.AuthenticationResponse;
import vn.com.atomi.charge.authn.model.response.IntrospectResponse;
import vn.com.atomi.charge.authn.model.response.UserInfoResponse;
import vn.com.atomi.charge.authn.repository.AuthnRepo;
import vn.com.atomi.charge.authn.repository.AuthnUserRepository;
import vn.com.atomi.charge.authn.service.interfaces.AuthnService;
import vn.com.atomi.charge.base.model.exception.BusinessException;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

@Service
public class AuthnServiceImpl implements AuthnService {

	private static final String TOKEN_ISSUER = "lms.authn-service";

	private final AuthnUserRepository userRepository;
	private final AuthnRepo refreshTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final RedisTemplate<String, String> redisTemplate;

	@Value("${jwt.signerKey}")
	private String signerKey;

	@Value("${jwt.valid-duration}")
	private long validDuration;

	@Value("${jwt.refreshable-duration}")
	private long refreshableDuration;

	public AuthnServiceImpl(AuthnUserRepository userRepository,
							AuthnRepo refreshTokenRepository,
							PasswordEncoder passwordEncoder,
							RedisTemplate<String, String> redisTemplate) {
		this.userRepository = userRepository;
		this.refreshTokenRepository = refreshTokenRepository;
		this.passwordEncoder = passwordEncoder;
		this.redisTemplate = redisTemplate;
	}

	@Override
	public AuthenticationResponse authenticate(AuthenticationRequest request) {
		AuthnUserEntity user = userRepository.findByUsernameOrEmail(request.getUsername(), request.getUsername())
				.orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED.getErrorCode()));

		if (user.getStatus() != AuthnUserStatus.ACTIVE || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
			throw new BusinessException(ErrorCode.LOGIN_FAILED.getErrorCode());
		}

		user.setLastLoginAt(LocalDateTime.now());
		userRepository.save(user);
		return buildAuthResponse(user);
	}

	@Override
	public IntrospectResponse introspect(IntrospectRequest request) {
		try {
			verifyToken(request.getToken());
			return IntrospectResponse.builder().valid(true).build();
		} catch (Exception exception) {
			return IntrospectResponse.builder().valid(false).build();
		}
	}

	@Override
	public AuthenticationResponse refreshToken(RefreshRequest request) {
		try {
			SignedJWT jwt = verifyToken(request.getToken());
			invalidateToken(jwt);

			String userId = jwt.getJWTClaimsSet().getSubject();
			AuthnUserEntity user = userRepository.findEntityById(userId)
					.orElseThrow(() -> new BusinessException("UNAUTHENTICATED", "UNAUTHENTICATED"));

			if (user.getStatus() != AuthnUserStatus.ACTIVE) {
				throw new BusinessException("UNAUTHENTICATED", "UNAUTHENTICATED");
			}

			return buildAuthResponse(user);
		} catch (Exception exception) {
			throw new BusinessException("UNAUTHENTICATED", "UNAUTHENTICATED");
		}
	}

	@Override
	public void logout(LogoutRequest request) {
		try {
			SignedJWT jwt = verifyToken(request.getToken());
			invalidateToken(jwt);
		} catch (Exception exception) {
			throw new BusinessException("UNAUTHENTICATED", "UNAUTHENTICATED");
		}
	}

	@Override
	public void changePassword(String token, ChangePasswordRequest request) {
		try {
			SignedJWT jwt = verifyToken(token);
			AuthnUserEntity user = userRepository.findEntityById(jwt.getJWTClaimsSet().getSubject())
					.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND.getErrorCode(), "USER_NOT_FOUND"));

			if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
				throw new BusinessException(ErrorCode.INVALID_OLD_PASSWORD.getErrorCode(), "INVALID_OLD_PASSWORD");
			}
			if (!request.getNewPassword().equals(request.getConfirmPassword())) {
				throw new BusinessException(ErrorCode.INVALID_CONFIRM_PASSWORD.getErrorCode(), "INVALID_CONFIRM_PASSWORD");
			}
			if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
				throw new BusinessException(ErrorCode.OLD_PASSWORD_EQUAL_NEW_PASSWORD.getErrorCode(), "OLD_PASSWORD_EQUAL_NEW_PASSWORD");
			}

			user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
			user.setPasswordChangeAt(LocalDateTime.now());
			user.setLastModifiedBy(user.getUsername());
			user.setLastModifiedDate(LocalDateTime.now());
			userRepository.save(user);
			invalidateToken(jwt);
		} catch (BusinessException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new BusinessException("UNAUTHENTICATED", "UNAUTHENTICATED");
		}
	}

	@Override
	public String register(RegistrationRequest request) {
		if (userRepository.existsByUsername(request.getUsername())) {
			throw new BusinessException("USER_EXISTED", "USER_EXISTED");
		}
		if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
			throw new BusinessException("EMAIL_EXISTED", "EMAIL_EXISTED");
		}

		AuthnUserEntity user = new AuthnUserEntity();
		user.setUsername(request.getUsername());
		user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
		user.setEmail(request.getEmail());
		user.setPhone(request.getPhone());
		user.setFullName(request.getFirstName() + " " + request.getLastName());
		user.setLanguage(UserLanguage.VI);
		user.setStatus(AuthnUserStatus.ACTIVE);
		user.setFailedLoginAttempts(0);
		user.setCreatedBy("system");
		user.setCreatedDate(LocalDateTime.now());
		userRepository.save(user);
		return user.getId();
	}

	@Override
	public void sendRegistrationOtp(String email) {
		String otp = String.format("%06d", new Random().nextInt(900000) + 100000);
		redisTemplate.opsForValue().set(otpKey(email), "REGISTER|" + otp, Duration.ofMinutes(5));
	}

	@Override
	public boolean verifyOtp(OtpVerifyRequest request) {
		String cached = redisTemplate.opsForValue().get(otpKey(request.getEmail()));
		if (cached == null) {
			return false;
		}
		String[] parts = cached.split("\\|", 2);
		if (parts.length != 2 || !parts[0].equals(request.getExpectedType())) {
			return false;
		}
		boolean matched = parts[1].equals(request.getInputOtp());
		if (matched) {
			redisTemplate.delete(otpKey(request.getEmail()));
		}
		return matched;
	}

	@Override
	public UserInfoResponse getInfoByToken(String token) {
		try {
			SignedJWT jwt = verifyToken(token);
			AuthnUserEntity user = userRepository.findEntityById(jwt.getJWTClaimsSet().getSubject())
					.orElseThrow(() -> new BusinessException("UNAUTHENTICATED", "UNAUTHENTICATED"));

			return UserInfoResponse.builder()
					.sub(user.getId())
					.email(user.getEmail())
					.preferred_username(user.getUsername())
					.given_name(user.getFullName())
					.family_name("")
					.build();
		} catch (Exception exception) {
			throw new BusinessException("UNAUTHENTICATED", "UNAUTHENTICATED");
		}
	}

	private AuthenticationResponse buildAuthResponse(AuthnUserEntity user) {
		try {
			return AuthenticationResponse.builder()
					.token(generateToken(user, validDuration))
					.refreshToken(generateToken(user, refreshableDuration))
					.userName(user.getUsername())
					.email(user.getEmail())
					.firstName(extractFirstName(user.getFullName()))
					.lastName(extractLastName(user.getFullName()))
					.permissions(new String[0])
					.build();
		} catch (JOSEException exception) {
			throw new BusinessException("INVALID_KEY", "INVALID_KEY");
		}
	}

	private String generateToken(AuthnUserEntity user, long durationSeconds) throws JOSEException {
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
			throw new BusinessException("UNAUTHENTICATED", "UNAUTHENTICATED");
		}
		Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
		if (expirationTime == null || expirationTime.before(new Date()) || refreshTokenRepository.existsByTokenId(signedJWT.getJWTClaimsSet().getJWTID())) {
			throw new BusinessException("UNAUTHENTICATED", "UNAUTHENTICATED");
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

	private String otpKey(String email) {
		return "otp:" + email;
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
