package vn.com.atomi.charge.authorization.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import vn.com.atomi.charge.authorization.client.AuthnClient;
import vn.com.atomi.charge.authorization.model.dto.AuthnUserDto;
import vn.com.atomi.charge.authorization.model.dto.NoticeRecipientOptionDto;
import vn.com.atomi.charge.authorization.model.entity.RoleEntity;
import vn.com.atomi.charge.authorization.model.entity.UserRoleEntity;
import vn.com.atomi.charge.authorization.model.enums.RoleCode;
import vn.com.atomi.charge.authorization.repository.AuthorRepo;
import vn.com.atomi.charge.authorization.repository.PermissionRepo;
import vn.com.atomi.charge.authorization.repository.RolePermissionRepo;
import vn.com.atomi.charge.authorization.repository.UserRoleRepository;
import vn.com.atomi.charge.base.model.response.BaseResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorServiceImplTest {

    @Mock
    private AuthorRepo roleRepository;
    @Mock
    private PermissionRepo permissionRepository;
    @Mock
    private RolePermissionRepo rolePermissionRepository;
    @Mock
    private UserRoleRepository userRoleRepository;
    @Mock
    private AuthnClient authnClient;

    @InjectMocks
    private AuthorServiceImpl service;

    @Test
    void noticeRecipientOptionsExposeOnlySafeActiveUserFieldsAndRoles() {
        RoleEntity studentRole = role("role-student", RoleCode.STUDENT);
        RoleEntity instructorRole = role("role-instructor", RoleCode.INSTRUCTOR);
        when(roleRepository.getAll()).thenReturn(List.of(studentRole, instructorRole));
        when(userRoleRepository.findByDeletedAtIsNull()).thenReturn(List.of(
                mapping("active-user", studentRole.getId()),
                mapping("active-user", instructorRole.getId()),
                mapping("locked-user", studentRole.getId())
        ));

        AuthnUserDto active = user("active-user", "student.one", "student@example.com", "Học viên Một", "ACTIVE");
        AuthnUserDto locked = user("locked-user", "locked", "locked@example.com", "Đã khóa", "LOCKED");
        when(authnClient.getUsersByIds(anyList()))
                .thenReturn(BaseResponse.success(HttpStatus.OK, List.of(active, locked)));

        BaseResponse<List<NoticeRecipientOptionDto>> response = service.getNoticeRecipientOptions();

        assertEquals(1, response.getData().size());
        NoticeRecipientOptionDto option = response.getData().get(0);
        assertEquals("active-user", option.getUserId());
        assertEquals("student.one", option.getUsername());
        assertEquals("student@example.com", option.getEmail());
        assertTrue(option.getRoleCodes().containsAll(List.of("STUDENT", "INSTRUCTOR")));
    }

    private RoleEntity role(String id, RoleCode code) {
        RoleEntity role = new RoleEntity();
        role.setId(id);
        role.setCode(code);
        return role;
    }

    private UserRoleEntity mapping(String userId, String roleId) {
        UserRoleEntity mapping = new UserRoleEntity();
        mapping.setUserId(userId);
        mapping.setRoleId(roleId);
        return mapping;
    }

    private AuthnUserDto user(String id, String username, String email, String fullName, String status) {
        AuthnUserDto user = new AuthnUserDto();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setStatus(status);
        return user;
    }
}
