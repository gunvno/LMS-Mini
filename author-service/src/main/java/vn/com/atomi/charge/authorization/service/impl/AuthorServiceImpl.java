package vn.com.atomi.charge.authorization.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import vn.com.atomi.charge.authorization.client.AuthnClient;
import vn.com.atomi.charge.authorization.model.dto.PermissionDto;
import vn.com.atomi.charge.authorization.model.dto.RoleDto;
import vn.com.atomi.charge.authorization.model.entity.PermissionEntity;
import vn.com.atomi.charge.authorization.model.entity.RoleEntity;
import vn.com.atomi.charge.authorization.model.entity.RolePermissionEntity;
import vn.com.atomi.charge.authorization.model.entity.UserRoleEntity;
import vn.com.atomi.charge.authorization.model.enums.RoleCode;
import vn.com.atomi.charge.authorization.model.request.RolePermissionRequest;
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
			rolePermissionRepository.softDelete(existing.stream().map(RolePermissionEntity::getId).toList(), LocalDateTime.now(), LocalDateTime.now());
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
	public BaseResponse<List<String>> getUserRoles(String userId) {
		List<String> roles = userRoleRepository.findByUserIdAndDeletedAtIsNull(userId).stream()
				.map(UserRoleEntity::getRoleId)
				.map(roleId -> roleRepository.findEntityById(roleId).orElse(null))
				.filter(item -> item != null)
				.map(item -> item.getCode().name())
				.toList();
		return BaseResponse.success(HttpStatus.OK, roles);
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
