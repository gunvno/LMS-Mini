package vn.com.atomi.charge.authorization.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.com.atomi.charge.authorization.model.dto.PermissionDto;
import vn.com.atomi.charge.authorization.model.dto.RoleDto;
import vn.com.atomi.charge.authorization.model.request.RolePermissionRequest;
import vn.com.atomi.charge.authorization.model.request.UserRoleRequest;
import vn.com.atomi.charge.authorization.service.interfaces.AuthorService;
import vn.com.atomi.charge.base.model.request.BaseRequest;
import vn.com.atomi.charge.base.model.response.BaseResponse;

import java.util.List;

@RestController
public class AuthorController {

	private final AuthorService authorService;

	public AuthorController(AuthorService authorService) {
		this.authorService = authorService;
	}

	@GetMapping("/api/v1/roles")
	public BaseResponse<List<RoleDto>> getRoles() {
		return authorService.getRoles();
	}

	@GetMapping("/api/v1/roles/{roleCode}")
	public BaseResponse<RoleDto> getRole(@PathVariable String roleCode) {
		return authorService.getRole(roleCode);
	}

	@GetMapping("/api/v1/permissions")
	public BaseResponse<List<PermissionDto>> getPermissions() {
		return authorService.getPermissions();
	}

	@PostMapping("/api/v1/roles/{roleCode}/permissions")
	public BaseResponse<RoleDto> updateRolePermissions(@PathVariable String roleCode,
													   @RequestBody @Valid BaseRequest<RolePermissionRequest> request) {
		return authorService.updateRolePermissions(roleCode, request);
	}

	@PostMapping("/api/v1/user-roles")
	public BaseResponse<RoleDto> assignRoleToUser(@RequestBody @Valid BaseRequest<UserRoleRequest> request) {
		String userId = request != null && request.getData() != null ? request.getData().getUserId() : null;
		return authorService.assignRoleToUser(userId, request);
	}

	@PostMapping("/api/v1/users/{userId}/roles")
	public BaseResponse<RoleDto> assignRoleToUser(@PathVariable String userId,
												  @RequestBody @Valid BaseRequest<UserRoleRequest> request) {
		return authorService.assignRoleToUser(userId, request);
	}

	@GetMapping("/api/v1/users/{userId}/roles")
	public BaseResponse<List<String>> getUserRoles(@PathVariable String userId) {
		return authorService.getUserRoles(userId);
	}

	@GetMapping("/internal/v1/roles/{roleCode}/users")
	public BaseResponse<List<String>> getUsersByRole(@PathVariable String roleCode) {
		return authorService.getUsersByRole(roleCode);
	}
}
