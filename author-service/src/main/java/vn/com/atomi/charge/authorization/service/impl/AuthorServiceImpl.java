package vn.com.atomi.charge.authorization.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import vn.com.atomi.charge.authorization.client.AuthnClient;
import vn.com.atomi.charge.authorization.model.dto.AuthnUserDto;
import vn.com.atomi.charge.authorization.model.dto.PermissionDto;
import vn.com.atomi.charge.authorization.model.dto.RoleDto;
import vn.com.atomi.charge.authorization.model.dto.StaffActivityDto;
import vn.com.atomi.charge.authorization.model.dto.StaffAccountDto;
import vn.com.atomi.charge.authorization.model.entity.PermissionEntity;
import vn.com.atomi.charge.authorization.model.entity.RoleEntity;
import vn.com.atomi.charge.authorization.model.entity.RolePermissionEntity;
import vn.com.atomi.charge.authorization.model.entity.UserRoleEntity;
import vn.com.atomi.charge.authorization.model.enums.RoleCode;
import vn.com.atomi.charge.authorization.model.request.RolePermissionRequest;
import vn.com.atomi.charge.authorization.model.request.StaffPermissionUpdateRequest;
import vn.com.atomi.charge.authorization.model.request.StaffResetPasswordRequest;
import vn.com.atomi.charge.authorization.model.request.StaffAccountCreationRequest;
import vn.com.atomi.charge.authorization.model.request.StaffStatusUpdateRequest;
import vn.com.atomi.charge.authorization.model.request.UserRoleRequest;
import vn.com.atomi.charge.authorization.repository.AuthorRepo;
import vn.com.atomi.charge.authorization.repository.PermissionRepo;
import vn.com.atomi.charge.authorization.repository.RolePermissionRepo;
import vn.com.atomi.charge.authorization.repository.UserRoleRepository;
import vn.com.atomi.charge.authorization.service.interfaces.AuthorService;
import vn.com.atomi.charge.base.model.request.BaseRequest;
import vn.com.atomi.charge.base.model.response.BaseResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AuthorServiceImpl implements AuthorService {

	private final AuthorRepo roleRepository;
	private final PermissionRepo permissionRepository;
	private final RolePermissionRepo rolePermissionRepository;
	private final UserRoleRepository userRoleRepository;
	private final AuthnClient authnClient;

	public AuthorServiceImpl(AuthorRepo roleRepository,
							 PermissionRepo permissionRepository,
							 RolePermissionRepo rolePermissionRepository,
							 UserRoleRepository userRoleRepository,
							 AuthnClient authnClient) {
		this.roleRepository = roleRepository;
		this.permissionRepository = permissionRepository;
		this.rolePermissionRepository = rolePermissionRepository;
		this.userRoleRepository = userRoleRepository;
		this.authnClient = authnClient;
	}

	@Override
	public BaseResponse<List<RoleDto>> getRoles() {
		List<RoleDto> roles = roleRepository.getAll().stream().map(this::mapRole).toList();
		return BaseResponse.success(HttpStatus.OK, roles);
	}

	@Override
	public BaseResponse<List<PermissionDto>> getPermissions() {
		List<PermissionDto> permissions = permissionRepository.getAll().stream().map(this::mapPermission).toList();
		return BaseResponse.success(HttpStatus.OK, permissions);
	}

	@Override
	public BaseResponse<RoleDto> getRole(String roleCode) {
		RoleEntity role = findRole(roleCode);
		return BaseResponse.success(HttpStatus.OK, mapRole(role));
	}

	@Override
	@Transactional
	public BaseResponse<RoleDto> updateRolePermissions(String roleCode, BaseRequest<RolePermissionRequest> request) {
		RoleEntity role = findRole(roleCode);
		List<String> permissionCodes = request != null && request.getData() != null && request.getData().getPermissionCodes() != null
				? request.getData().getPermissionCodes()
				: List.of();

		List<RolePermissionEntity> existing = rolePermissionRepository.findByRoleIdAndDeletedAtIsNull(role.getId());
		if (!existing.isEmpty()) {
			rolePermissionRepository.softDelete(existing.stream().map(RolePermissionEntity::getId).toList(), LocalDateTime.now(), Optional.ofNullable(currentUserId()).orElse("system"), LocalDateTime.now());
		}

		for (String permissionCode : permissionCodes) {
			if (!StringUtils.hasText(permissionCode)) {
				continue;
			}
			PermissionEntity permission = findPermission(permissionCode);
			RolePermissionEntity mapping = new RolePermissionEntity();
			mapping.setRoleId(role.getId());
			mapping.setPermissionId(permission.getId());
			mapping.setCreatedBy("system");
			mapping.setLastModifiedBy("system");
			rolePermissionRepository.save(mapping);
		}

		return BaseResponse.success(HttpStatus.OK, mapRole(roleRepository.findEntityById(role.getId()).orElse(role)));
	}

	@Override
	@Transactional
	public BaseResponse<RoleDto> assignRoleToUser(String userId, BaseRequest<UserRoleRequest> request) {
		if (!StringUtils.hasText(userId)) {
			return BaseResponse.fail(HttpStatus.BAD_REQUEST, "user.not_found");
		}
		Boolean userExists = authnClient.checkUser(userId);
		if (userExists == null || !userExists) {
			return BaseResponse.fail(HttpStatus.BAD_REQUEST, "user.not_found");
		}

		String roleCode = request != null && request.getData() != null ? request.getData().getRoleCode() : null;
		RoleEntity role = findRole(roleCode);

		Optional<UserRoleEntity> existing = userRoleRepository.findByUserIdAndRoleIdAndDeletedAtIsNull(userId, role.getId());
		if (existing.isEmpty()) {
			UserRoleEntity entity = new UserRoleEntity();
			entity.setUserId(userId);
			entity.setRoleId(role.getId());
			entity.setCreatedBy("system");
			entity.setLastModifiedBy("system");
			userRoleRepository.save(entity);
		}

		return BaseResponse.success(HttpStatus.OK, mapRole(role));
	}

	@Override
	@Transactional
	public BaseResponse<RoleDto> assignStudentRole(String userId) {
		if (!StringUtils.hasText(userId)) {
			return BaseResponse.fail(HttpStatus.BAD_REQUEST, "user.invalid_request");
		}
		RoleEntity role = findRole(RoleCode.STUDENT.name());
		assignRoleIfMissing(userId, role);
		return BaseResponse.success(HttpStatus.OK, mapRole(role));
	}

	@Override
	@Transactional
	public BaseResponse<StaffAccountDto> createInstructorStaff(BaseRequest<StaffAccountCreationRequest> request) {
		if (request == null || request.getData() == null) {
			return BaseResponse.fail(HttpStatus.BAD_REQUEST, "user.invalid_request");
		}

		BaseResponse<AuthnUserDto> createdUserResponse = authnClient.createStaffUser(request.getData());
		if (createdUserResponse == null || createdUserResponse.getData() == null) {
			String message = createdUserResponse != null && StringUtils.hasText(createdUserResponse.getMessage())
					? createdUserResponse.getMessage()
					: "user.create_failed";
			return BaseResponse.fail(HttpStatus.BAD_REQUEST, message);
		}

		AuthnUserDto user = createdUserResponse.getData();
		RoleEntity instructorRole = findRole(RoleCode.INSTRUCTOR.name());
		assignRoleIfMissing(user.getId(), instructorRole);

		StaffAccountDto staff = new StaffAccountDto();
		staff.setUserId(user.getId());
		staff.setUsername(user.getUsername());
		staff.setEmail(user.getEmail());
		staff.setPhone(user.getPhone());
		staff.setFullName(user.getFullName());
		staff.setRoleCode(RoleCode.INSTRUCTOR.name());
		staff.setPermissionCodes(getEffectivePermissionCodes(user.getId()));

		return BaseResponse.success(HttpStatus.OK, staff);
	}

	@Override
	public BaseResponse<List<StaffAccountDto>> getStaffAccounts() {
		RoleEntity instructorRole = findRole(RoleCode.INSTRUCTOR.name());
		List<String> staffIds = userRoleRepository.findByRoleIdAndDeletedAtIsNull(instructorRole.getId()).stream()
				.map(UserRoleEntity::getUserId)
				.distinct()
				.toList();
		if (staffIds.isEmpty()) {
			return BaseResponse.success(HttpStatus.OK, List.of());
		}

		BaseResponse<List<AuthnUserDto>> userResponse = authnClient.getUsersByIds(staffIds);
		List<AuthnUserDto> users = userResponse != null && userResponse.getData() != null
				? userResponse.getData()
				: List.of();
		List<StaffAccountDto> staff = users.stream()
				.map(user -> mapStaffAccount(user, RoleCode.INSTRUCTOR.name()))
				.toList();
		return BaseResponse.success(HttpStatus.OK, staff);
	}

	@Override
	public BaseResponse<StaffAccountDto> getStaffAccount(String accountId) {
		if (!isInstructor(accountId)) {
			return BaseResponse.fail(HttpStatus.BAD_REQUEST, "staff.not_found");
		}
		AuthnUserDto user = getAuthnUser(accountId);
		return BaseResponse.success(HttpStatus.OK, mapStaffAccount(user, RoleCode.INSTRUCTOR.name()));
	}

	@Override
	@Transactional
	public BaseResponse<StaffAccountDto> updateStaffPermissions(String accountId, BaseRequest<StaffPermissionUpdateRequest> request) {
		if (!isInstructor(accountId)) {
			return BaseResponse.fail(HttpStatus.BAD_REQUEST, "staff.not_found");
		}
		List<String> permissionCodes = request != null && request.getData() != null && request.getData().getPermissionCodes() != null
				? request.getData().getPermissionCodes()
				: List.of();

		RoleEntity instructorRole = findRole(RoleCode.INSTRUCTOR.name());
		List<RolePermissionEntity> existing = rolePermissionRepository.findByRoleIdAndDeletedAtIsNull(instructorRole.getId());
		if (!existing.isEmpty()) {
			rolePermissionRepository.softDelete(existing.stream().map(RolePermissionEntity::getId).toList(), LocalDateTime.now(), Optional.ofNullable(currentUserId()).orElse("system"), LocalDateTime.now());
		}

		for (String permissionCode : permissionCodes) {
			if (!StringUtils.hasText(permissionCode)) {
				continue;
			}
			PermissionEntity permission = findPermission(permissionCode);
			RolePermissionEntity mapping = new RolePermissionEntity();
			mapping.setRoleId(instructorRole.getId());
			mapping.setPermissionId(permission.getId());
			mapping.setCreatedBy("system");
			mapping.setLastModifiedBy("system");
			rolePermissionRepository.save(mapping);
		}

		return getStaffAccount(accountId);
	}

	@Override
	public BaseResponse<StaffAccountDto> updateStaffStatus(String accountId, BaseRequest<StaffStatusUpdateRequest> request) {
		if (!isInstructor(accountId)) {
			return BaseResponse.fail(HttpStatus.BAD_REQUEST, "staff.not_found");
		}
		if (request == null || request.getData() == null || !StringUtils.hasText(request.getData().getStatus())) {
			return BaseResponse.fail(HttpStatus.BAD_REQUEST, "staff.invalid_request");
		}

		BaseResponse<AuthnUserDto> response = authnClient.updateUserStatus(accountId, request.getData());
		if (response == null || response.getData() == null) {
			return BaseResponse.fail(HttpStatus.BAD_REQUEST, "staff.status_update_failed");
		}
		return BaseResponse.success(HttpStatus.OK, mapStaffAccount(response.getData(), RoleCode.INSTRUCTOR.name()));
	}

	@Override
	public BaseResponse<StaffAccountDto> resetStaffPassword(String accountId, BaseRequest<StaffResetPasswordRequest> request) {
		if (!isInstructor(accountId)) {
			return BaseResponse.fail(HttpStatus.BAD_REQUEST, "staff.not_found");
		}
		StaffResetPasswordRequest data = request != null ? request.getData() : null;
		BaseResponse<AuthnUserDto> response = authnClient.resetPassword(accountId, data);
		if (response == null || response.getData() == null) {
			return BaseResponse.fail(HttpStatus.BAD_REQUEST, "staff.reset_password_failed");
		}
		return BaseResponse.success(HttpStatus.OK, mapStaffAccount(response.getData(), RoleCode.INSTRUCTOR.name()));
	}

	@Override
	public BaseResponse<StaffActivityDto> getMyStaffActivity() {
		String userId = currentUserId();
		if (!StringUtils.hasText(userId)) {
			return BaseResponse.fail(HttpStatus.BAD_REQUEST, "user.not_found");
		}
		AuthnUserDto user = getAuthnUser(userId);
		return BaseResponse.success(HttpStatus.OK, mapStaffActivity(user));
	}

	@Override
	public BaseResponse<List<StaffActivityDto>> getStaffActivities() {
		BaseResponse<List<StaffAccountDto>> staffResponse = getStaffAccounts();
		List<String> staffIds = staffResponse.getData() == null
				? List.of()
				: staffResponse.getData().stream().map(StaffAccountDto::getUserId).toList();
		if (staffIds.isEmpty()) {
			return BaseResponse.success(HttpStatus.OK, List.of());
		}

		BaseResponse<List<AuthnUserDto>> userResponse = authnClient.getUsersByIds(staffIds);
		List<StaffActivityDto> activities = userResponse != null && userResponse.getData() != null
				? userResponse.getData().stream().map(this::mapStaffActivity).toList()
				: List.of();
		return BaseResponse.success(HttpStatus.OK, activities);
	}

	@Override
	public BaseResponse<List<String>> getUserRoles(String userId) {
		if (!StringUtils.hasText(userId)) {
			return BaseResponse.success(HttpStatus.OK, List.of());
		}
		List<String> roles = userRoleRepository.findByUserIdAndDeletedAtIsNull(userId).stream()
				.map(UserRoleEntity::getRoleId)
				.map(roleId -> roleRepository.findEntityById(roleId).orElse(null))
				.filter(item -> item != null)
				.map(item -> item.getCode().name())
				.toList();
		return BaseResponse.success(HttpStatus.OK, roles);
	}

	@Override
	public BaseResponse<List<String>> getMyRoles() {
		return getUserRoles(currentUserId());
	}

	@Override
	public BaseResponse<List<String>> getUserPermissions(String userId) {
		if (!StringUtils.hasText(userId)) {
			return BaseResponse.success(HttpStatus.OK, List.of());
		}

		return BaseResponse.success(HttpStatus.OK, getEffectivePermissionCodes(userId));
	}

	@Override
	public BaseResponse<List<String>> getMyPermissions() {
		return getUserPermissions(currentUserId());
	}

	@Override
	public BaseResponse<List<String>> getUsersByRole(String roleCode) {
		RoleEntity role = findRole(roleCode);
		List<String> users = userRoleRepository.findByRoleIdAndDeletedAtIsNull(role.getId()).stream()
				.map(UserRoleEntity::getUserId)
				.toList();
		return BaseResponse.success(HttpStatus.OK, users);
	}

	private RoleEntity findRole(String roleCode) {
		if (!StringUtils.hasText(roleCode)) {
			throw new IllegalArgumentException("Role code is required");
		}
		RoleCode code = RoleCode.valueOf(roleCode.trim().toUpperCase());
		return roleRepository.findByCodeAndDeletedAtIsNull(code)
				.orElseThrow(() -> new IllegalArgumentException("Role not found"));
	}

	private void assignRoleIfMissing(String userId, RoleEntity role) {
		Optional<UserRoleEntity> existing = userRoleRepository.findByUserIdAndRoleIdAndDeletedAtIsNull(userId, role.getId());
		if (existing.isPresent()) {
			return;
		}

		UserRoleEntity entity = new UserRoleEntity();
		entity.setUserId(userId);
		entity.setRoleId(role.getId());
		entity.setCreatedBy("system");
		entity.setLastModifiedBy("system");
		userRoleRepository.save(entity);
	}

	private boolean isInstructor(String userId) {
		if (!StringUtils.hasText(userId)) {
			return false;
		}
		RoleEntity instructor = findRole(RoleCode.INSTRUCTOR.name());
		return userRoleRepository.findByUserIdAndRoleIdAndDeletedAtIsNull(userId, instructor.getId()).isPresent();
	}

	private AuthnUserDto getAuthnUser(String userId) {
		BaseResponse<AuthnUserDto> response = authnClient.getUserById(userId);
		if (response == null || response.getData() == null) {
			throw new IllegalArgumentException("User not found");
		}
		return response.getData();
	}

	private StaffAccountDto mapStaffAccount(AuthnUserDto user, String roleCode) {
		StaffAccountDto staff = new StaffAccountDto();
		staff.setUserId(user.getId());
		staff.setUsername(user.getUsername());
		staff.setEmail(user.getEmail());
		staff.setPhone(user.getPhone());
		staff.setFullName(user.getFullName());
		staff.setRoleCode(roleCode);
		staff.setPermissionCodes(getEffectivePermissionCodes(user.getId()));
		return staff;
	}

	private StaffActivityDto mapStaffActivity(AuthnUserDto user) {
		StaffActivityDto activity = new StaffActivityDto();
		activity.setUserId(user.getId());
		activity.setUsername(user.getUsername());
		activity.setFullName(user.getFullName());
		activity.setStatus(user.getStatus());
		activity.setLastLoginAt(user.getLastLoginAt());
		activity.setCreatedDate(user.getCreatedDate());
		activity.setLastModifiedDate(user.getLastModifiedDate());
		return activity;
	}

	private List<String> getEffectivePermissionCodes(String userId) {
		return userRoleRepository.findByUserIdAndDeletedAtIsNull(userId).stream()
				.map(UserRoleEntity::getRoleId)
				.flatMap(roleId -> rolePermissionRepository.findByRoleIdAndDeletedAtIsNull(roleId).stream())
				.map(RolePermissionEntity::getPermissionId)
				.map(permissionId -> permissionRepository.findEntityById(permissionId).orElse(null))
				.filter(item -> item != null)
				.map(PermissionEntity::getCode)
				.distinct()
				.toList();
	}

	private String currentUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || authentication.getPrincipal() == null) {
			return null;
		}
		return authentication.getPrincipal().toString();
	}

	private PermissionEntity findPermission(String permissionCode) {
		if (!StringUtils.hasText(permissionCode)) {
			throw new IllegalArgumentException("Permission code is required");
		}
		return permissionRepository.findByCodeAndDeletedAtIsNull(permissionCode.trim().toUpperCase())
				.orElseThrow(() -> new IllegalArgumentException("Permission not found"));
	}

	private RoleDto mapRole(RoleEntity role) {
		List<String> permissions = rolePermissionRepository.findByRoleIdAndDeletedAtIsNull(role.getId()).stream()
				.map(RolePermissionEntity::getPermissionId)
				.map(permissionId -> permissionRepository.findEntityById(permissionId).orElse(null))
				.filter(item -> item != null)
				.map(PermissionEntity::getCode)
				.toList();

		RoleDto dto = new RoleDto();
		dto.setId(role.getId());
		dto.setName(role.getName());
		dto.setCode(role.getCode() != null ? role.getCode().name() : null);
		dto.setDescription(role.getDescription());
		dto.setStatus(role.getStatus() != null ? role.getStatus().name() : null);
		dto.setPermissionCodes(permissions);
		return dto;
	}

	private PermissionDto mapPermission(PermissionEntity permission) {
		PermissionDto dto = new PermissionDto();
		dto.setId(permission.getId());
		dto.setName(permission.getName());
		dto.setCode(permission.getCode());
		dto.setDescription(permission.getDescription());
		dto.setStatus(permission.getStatus() != null ? permission.getStatus().name() : null);
		return dto;
	}
}
