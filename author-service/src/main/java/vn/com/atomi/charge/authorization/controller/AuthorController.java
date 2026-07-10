package vn.com.atomi.charge.authorization.controller;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.com.atomi.charge.authorization.model.dto.PermissionDto;
import vn.com.atomi.charge.authorization.model.dto.RoleDto;
import vn.com.atomi.charge.authorization.model.dto.StaffActivityDto;
import vn.com.atomi.charge.authorization.model.dto.StaffAccountDto;
import vn.com.atomi.charge.authorization.model.request.RolePermissionRequest;
import vn.com.atomi.charge.authorization.model.request.StaffAccountCreationRequest;
import vn.com.atomi.charge.authorization.model.request.StaffPermissionUpdateRequest;
import vn.com.atomi.charge.authorization.model.request.StaffResetPasswordRequest;
import vn.com.atomi.charge.authorization.model.request.StaffStatusUpdateRequest;
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
	@PreAuthorize("hasAuthority('ROLE_VIEW')")
	public BaseResponse<List<RoleDto>> getRoles() {
		return authorService.getRoles();
	}

	@GetMapping("/api/v1/roles/{roleCode}")
	@PreAuthorize("hasAuthority('ROLE_VIEW')")
	public BaseResponse<RoleDto> getRole(@PathVariable String roleCode) {
		return authorService.getRole(roleCode);
	}

	@GetMapping("/api/v1/permissions")
	@PreAuthorize("hasAuthority('PERMISSION_VIEW')")
	public BaseResponse<List<PermissionDto>> getPermissions() {
		return authorService.getPermissions();
	}

	@PostMapping("/api/v1/permissions/staff")
	@PreAuthorize("hasAuthority('STAFF_CREATE')")
	public BaseResponse<StaffAccountDto> createStaffAccount(
			@RequestBody @Valid BaseRequest<StaffAccountCreationRequest> request) {
		return authorService.createInstructorStaff(request);
	}

	@GetMapping("/api/v1/permissions/staff")
	@PreAuthorize("hasAuthority('STAFF_VIEW')")
	public BaseResponse<List<StaffAccountDto>> getStaffAccounts() {
		return authorService.getStaffAccounts();
	}

	@GetMapping("/api/v1/permissions/staff/{accountId}")
	@PreAuthorize("hasAuthority('STAFF_VIEW')")
	public BaseResponse<StaffAccountDto> getStaffAccount(@PathVariable String accountId) {
		return authorService.getStaffAccount(accountId);
	}

	@PutMapping("/api/v1/permissions/staff/{accountId}")
	@PreAuthorize("hasAuthority('STAFF_UPDATE')")
	public BaseResponse<StaffAccountDto> updateStaffPermissions(@PathVariable String accountId,
																@RequestBody @Valid BaseRequest<StaffPermissionUpdateRequest> request) {
		return authorService.updateStaffPermissions(accountId, request);
	}

	@PutMapping("/api/v1/permissions/staff/{accountId}/status")
	@PreAuthorize("hasAuthority('STAFF_STATUS_UPDATE')")
	public BaseResponse<StaffAccountDto> updateStaffStatus(@PathVariable String accountId,
														   @RequestBody @Valid BaseRequest<StaffStatusUpdateRequest> request) {
		return authorService.updateStaffStatus(accountId, request);
	}

	@PutMapping("/api/v1/permissions/staff/{accountId}/reset-password")
	@PreAuthorize("hasAuthority('STAFF_PASSWORD_RESET')")
	public BaseResponse<StaffAccountDto> resetStaffPassword(@PathVariable String accountId,
															@RequestBody BaseRequest<StaffResetPasswordRequest> request) {
		return authorService.resetStaffPassword(accountId, request);
	}

	@PostMapping("/api/v1/roles/{roleCode}/permissions")
	@PreAuthorize("hasAuthority('PERMISSION_MANAGE')")
	public BaseResponse<RoleDto> updateRolePermissions(@PathVariable String roleCode,
													   @RequestBody @Valid BaseRequest<RolePermissionRequest> request) {
		return authorService.updateRolePermissions(roleCode, request);
	}

	@PostMapping("/api/v1/user-roles")
	@PreAuthorize("hasAuthority('ROLE_MANAGE')")
	public BaseResponse<RoleDto> assignRoleToUser(@RequestBody @Valid BaseRequest<UserRoleRequest> request) {
		String userId = request != null && request.getData() != null ? request.getData().getUserId() : null;
		return authorService.assignRoleToUser(userId, request);
	}

	@PostMapping("/api/v1/users/{userId}/roles")
	@PreAuthorize("hasAuthority('ROLE_MANAGE')")
	public BaseResponse<RoleDto> assignRoleToUser(@PathVariable String userId,
												  @RequestBody @Valid BaseRequest<UserRoleRequest> request) {
		return authorService.assignRoleToUser(userId, request);
	}

	@GetMapping("/api/v1/users/{userId}/roles")
	@PreAuthorize("hasAuthority('USER_VIEW')")
	public BaseResponse<List<String>> getUserRoles(@PathVariable String userId) {
		return authorService.getUserRoles(userId);
	}

	@GetMapping("/api/v1/users/me/roles")
	@PreAuthorize("hasAuthority('USER_PROFILE_VIEW')")
	public BaseResponse<List<String>> getMyRoles() {
		return authorService.getMyRoles();
	}

	@GetMapping("/api/v1/users/me/permissions")
	@PreAuthorize("hasAuthority('USER_PROFILE_VIEW')")
	public BaseResponse<List<String>> getMyPermissions() {
		return authorService.getMyPermissions();
	}

	@GetMapping("/internal/v1/users/{userId}/permissions")
	public BaseResponse<List<String>> getUserPermissions(@PathVariable String userId) {
		return authorService.getUserPermissions(userId);
	}

	@GetMapping("/internal/v1/roles/{roleCode}/users")
	public BaseResponse<List<String>> getUsersByRole(@PathVariable String roleCode) {
		return authorService.getUsersByRole(roleCode);
	}

	@PostMapping("/internal/v1/users/{userId}/roles/student")
	public BaseResponse<RoleDto> assignStudentRole(@PathVariable String userId) {
		return authorService.assignStudentRole(userId);
	}

	@GetMapping("/staff-activity/me")
	@PreAuthorize("hasAuthority('STAFF_ACTIVITY_VIEW')")
	public BaseResponse<StaffActivityDto> getMyStaffActivity() {
		return authorService.getMyStaffActivity();
	}

	@GetMapping("/staff-activity")
	@PreAuthorize("hasAuthority('STAFF_ACTIVITY_VIEW')")
	public BaseResponse<List<StaffActivityDto>> getStaffActivities() {
		return authorService.getStaffActivities();
	}
}
