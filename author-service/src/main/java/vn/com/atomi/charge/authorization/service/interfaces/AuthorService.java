package vn.com.atomi.charge.authorization.service.interfaces;

import org.springframework.data.domain.Pageable;
import vn.com.atomi.charge.authorization.model.dto.PermissionDto;
import vn.com.atomi.charge.authorization.model.dto.RoleDto;
import vn.com.atomi.charge.authorization.model.dto.StaffActivityDto;
import vn.com.atomi.charge.authorization.model.dto.StaffAccountDto;
import vn.com.atomi.charge.authorization.model.request.RolePermissionRequest;
import vn.com.atomi.charge.authorization.model.request.StaffPermissionUpdateRequest;
import vn.com.atomi.charge.authorization.model.request.StaffResetPasswordRequest;
import vn.com.atomi.charge.authorization.model.request.StaffAccountCreationRequest;
import vn.com.atomi.charge.authorization.model.request.StaffStatusUpdateRequest;
import vn.com.atomi.charge.authorization.model.request.UserRoleRequest;
import vn.com.atomi.charge.base.model.request.BaseRequest;
import vn.com.atomi.charge.base.model.response.BaseResponse;

import java.util.List;

public interface AuthorService {
	BaseResponse<List<RoleDto>> getRoles();

	BaseResponse<List<PermissionDto>> getPermissions();

	BaseResponse<RoleDto> getRole(String roleCode);

	BaseResponse<RoleDto> updateRolePermissions(String roleCode, BaseRequest<RolePermissionRequest> request);

	BaseResponse<RoleDto> assignRoleToUser(String userId, BaseRequest<UserRoleRequest> request);

	BaseResponse<StaffAccountDto> createInstructorStaff(BaseRequest<StaffAccountCreationRequest> request);

	BaseResponse<List<StaffAccountDto>> getStaffAccounts();

	BaseResponse<StaffAccountDto> getStaffAccount(String accountId);

	BaseResponse<StaffAccountDto> updateStaffPermissions(String accountId, BaseRequest<StaffPermissionUpdateRequest> request);

	BaseResponse<StaffAccountDto> updateStaffStatus(String accountId, BaseRequest<StaffStatusUpdateRequest> request);

	BaseResponse<StaffAccountDto> resetStaffPassword(String accountId, BaseRequest<StaffResetPasswordRequest> request);

	BaseResponse<StaffActivityDto> getMyStaffActivity();

	BaseResponse<List<StaffActivityDto>> getStaffActivities();

	BaseResponse<List<String>> getUserRoles(String userId);

	BaseResponse<List<String>> getUserPermissions(String userId);

	BaseResponse<List<String>> getUsersByRole(String roleCode);
}
