package vn.com.atomi.charge.authorization.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.com.atomi.charge.authorization.client.AuthnClient;
import vn.com.atomi.charge.authorization.model.dto.AuthnUserDto;
import vn.com.atomi.charge.authorization.model.entity.PermissionEntity;
import vn.com.atomi.charge.authorization.model.entity.RoleEntity;
import vn.com.atomi.charge.authorization.model.entity.RolePermissionEntity;
import vn.com.atomi.charge.authorization.model.entity.UserRoleEntity;
import vn.com.atomi.charge.authorization.model.enums.PermissionStatus;
import vn.com.atomi.charge.authorization.model.enums.RoleCode;
import vn.com.atomi.charge.authorization.model.enums.RoleStatus;
import vn.com.atomi.charge.authorization.repository.AuthorRepo;
import vn.com.atomi.charge.authorization.repository.PermissionRepo;
import vn.com.atomi.charge.authorization.repository.RolePermissionRepo;
import vn.com.atomi.charge.authorization.repository.UserRoleRepository;
import vn.com.atomi.charge.base.model.response.BaseResponse;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private static final String ADMIN_USERNAME = "admin";

    private static final String INSTRUCTOR_USERNAME = "instructor";

    private static final String STUDENT_USERNAME = "student";

    private static final Map<String, PermissionSeed> PERMISSIONS = new LinkedHashMap<>();

    private static final List<String> ADMIN_PERMISSIONS = List.of(
            "PERMISSION_VIEW",
            "PERMISSION_MANAGE",
            "ROLE_VIEW",
            "ROLE_MANAGE",
            "USER_VIEW",
            "USER_CREATE",
            "USER_UPDATE",
            "USER_PROFILE_VIEW",
            "USER_PASSWORD_CHANGE",
            "STAFF_VIEW",
            "STAFF_CREATE",
            "STAFF_UPDATE",
            "STAFF_STATUS_UPDATE",
            "STAFF_PASSWORD_RESET",
            "STAFF_ACTIVITY_VIEW",
            "COURSE_VIEW",
            "COURSE_MANAGE",
            "COURSE_REVIEW",
            "CATEGORY_VIEW",
            "CATEGORY_MANAGE",
            "LESSON_VIEW",
            "LESSON_MANAGE",
            "RESOURCE_VIEW",
            "RESOURCE_MANAGE",
            "IMAGE_VIEW",
            "IMAGE_MANAGE",
            "ENROLLMENT_VIEW",
            "ENROLLMENT_ENROLL",
            "ENROLLMENT_MANAGE",
            "LEARNING_PROGRESS_VIEW",
            "LEARNING_PROGRESS_UPDATE",
            "QUIZ_VIEW",
            "QUIZ_MANAGE",
            "QUESTION_MANAGE",
            "ANSWER_MANAGE",
            "QUIZ_ATTEMPT",
            "CERTIFICATE_VIEW",
            "CERTIFICATE_MANAGE",
            "CERTIFICATE_VERIFY",
            "PAYMENT_VIEW",
            "PAYMENT_CREATE",
            "PAYMENT_MANAGE",
            "NOTICE_VIEW",
            "NOTICE_SEND",
            "NOTICE_BROADCAST",
            "DEVICE_MANAGE"
    );

    private static final List<String> INSTRUCTOR_PERMISSIONS = List.of(
            "COURSE_VIEW",
            "COURSE_MANAGE",
            "CATEGORY_VIEW",
            "CATEGORY_MANAGE",
            "LESSON_VIEW",
            "LESSON_MANAGE",
            "RESOURCE_VIEW",
            "RESOURCE_MANAGE",
            "IMAGE_VIEW",
            "IMAGE_MANAGE",
            "ENROLLMENT_VIEW",
            "LEARNING_PROGRESS_VIEW",
            "QUIZ_VIEW",
            "QUIZ_MANAGE",
            "QUESTION_MANAGE",
            "ANSWER_MANAGE",
            "QUIZ_ATTEMPT",
            "CERTIFICATE_VIEW",
            "PAYMENT_VIEW",
            "NOTICE_VIEW",
            "NOTICE_SEND",
            "STAFF_ACTIVITY_VIEW",
            "USER_PROFILE_VIEW",
            "USER_PASSWORD_CHANGE"
    );

    private static final List<String> STUDENT_PERMISSIONS = List.of(
            "COURSE_VIEW",
            "CATEGORY_VIEW",
            "LESSON_VIEW",
            "RESOURCE_VIEW",
            "IMAGE_VIEW",
            "ENROLLMENT_VIEW",
            "ENROLLMENT_ENROLL",
            "LEARNING_PROGRESS_VIEW",
            "LEARNING_PROGRESS_UPDATE",
            "QUIZ_VIEW",
            "QUIZ_ATTEMPT",
            "CERTIFICATE_VIEW",
            "CERTIFICATE_VERIFY",
            "PAYMENT_VIEW",
            "PAYMENT_CREATE",
            "USER_PROFILE_VIEW",
            "USER_PASSWORD_CHANGE",
            "NOTICE_VIEW",
            "DEVICE_MANAGE"
    );

    static {
        permission("PERMISSION_VIEW", "Permission View", "View permissions");
        permission("PERMISSION_MANAGE", "Permission Manage", "Manage permissions and role mappings");
        permission("ROLE_VIEW", "Role View", "View roles");
        permission("ROLE_MANAGE", "Role Manage", "Manage roles");
        permission("USER_VIEW", "User View", "View users");
        permission("USER_CREATE", "User Create", "Create users");
        permission("USER_UPDATE", "User Update", "Update users");
        permission("USER_PROFILE_VIEW", "User Profile View", "View current user profile");
        permission("USER_PASSWORD_CHANGE", "User Password Change", "Change own password");
        permission("STAFF_VIEW", "Staff View", "View staff accounts");
        permission("STAFF_CREATE", "Staff Create", "Create instructor/staff accounts");
        permission("STAFF_UPDATE", "Staff Update", "Update staff permissions");
        permission("STAFF_STATUS_UPDATE", "Staff Status Update", "Lock/unlock staff accounts");
        permission("STAFF_PASSWORD_RESET", "Staff Password Reset", "Reset staff password");
        permission("STAFF_ACTIVITY_VIEW", "Staff Activity View", "View staff activity");
        permission("COURSE_VIEW", "Course View", "View courses");
        permission("COURSE_MANAGE", "Course Manage", "Create and update courses");
        permission("COURSE_REVIEW", "Course Review", "Approve/reject/archive courses");
        permission("CATEGORY_VIEW", "Category View", "View course categories");
        permission("CATEGORY_MANAGE", "Category Manage", "Manage course categories");
        permission("LESSON_VIEW", "Lesson View", "View lessons");
        permission("LESSON_MANAGE", "Lesson Manage", "Create and update lessons");
        permission("RESOURCE_VIEW", "Resource View", "View lesson resources");
        permission("RESOURCE_MANAGE", "Resource Manage", "Manage lesson resources");
        permission("IMAGE_VIEW", "Image View", "View/download images");
        permission("IMAGE_MANAGE", "Image Manage", "Upload/manage images");
        permission("ENROLLMENT_VIEW", "Enrollment View", "View enrollments");
        permission("ENROLLMENT_ENROLL", "Enrollment Enroll", "Enroll in courses");
        permission("ENROLLMENT_MANAGE", "Enrollment Manage", "Manage enrollments");
        permission("LEARNING_PROGRESS_VIEW", "Learning Progress View", "View learning progress");
        permission("LEARNING_PROGRESS_UPDATE", "Learning Progress Update", "Start and complete lessons");
        permission("QUIZ_VIEW", "Quiz View", "View quizzes");
        permission("QUIZ_MANAGE", "Quiz Manage", "Manage quizzes, questions and answers");
        permission("QUESTION_MANAGE", "Question Manage", "Manage quiz questions");
        permission("ANSWER_MANAGE", "Answer Manage", "Manage quiz answers");
        permission("QUIZ_ATTEMPT", "Quiz Attempt", "Start and submit quiz attempts");
        permission("CERTIFICATE_VIEW", "Certificate View", "View certificates");
        permission("CERTIFICATE_MANAGE", "Certificate Manage", "Manage certificates");
        permission("CERTIFICATE_VERIFY", "Certificate Verify", "Verify certificates by code");
        permission("PAYMENT_VIEW", "Payment View", "View payments");
        permission("PAYMENT_CREATE", "Payment Create", "Create payment requests");
        permission("PAYMENT_MANAGE", "Payment Manage", "Manage payments");
        permission("NOTICE_VIEW", "Notice View", "View notices");
        permission("NOTICE_SEND", "Notice Send", "Send notices");
        permission("NOTICE_BROADCAST", "Notice Broadcast", "Send role and system-wide notices");
        permission("DEVICE_MANAGE", "Device Manage", "Register/deactivate user devices");
    }

    private final AuthorRepo roleRepository;

    private final PermissionRepo permissionRepository;

    private final RolePermissionRepo rolePermissionRepository;

    private final UserRoleRepository userRoleRepository;

    private final AuthnClient authnClient;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        RoleEntity admin = ensureRole(RoleCode.ADMIN, "Admin", "System administrator");
        RoleEntity instructor = ensureRole(RoleCode.INSTRUCTOR, "Instructor", "Course instructor");
        RoleEntity student = ensureRole(RoleCode.STUDENT, "Student", "Course student");

        ensurePermissions();

        assignPermissions(admin, ADMIN_PERMISSIONS);
        assignPermissions(instructor, INSTRUCTOR_PERMISSIONS);
        assignPermissions(student, STUDENT_PERMISSIONS);

        assignUserRoleByUsername(ADMIN_USERNAME, admin);
        assignUserRoleByUsername(INSTRUCTOR_USERNAME, instructor);
        assignUserRoleByUsername(STUDENT_USERNAME, student);
    }

    private RoleEntity ensureRole(RoleCode code, String name, String description) {
        return roleRepository.findByCodeAndDeletedAtIsNull(code)
                .orElseGet(() -> {
                    RoleEntity role = new RoleEntity();
                    role.setCode(code);
                    role.setName(name);
                    role.setDescription(description);
                    role.setStatus(RoleStatus.ACTIVE);
                    fillAudit(role);
                    RoleEntity saved = roleRepository.save(role);
                    log.warn("Created default LMS role. code={}", code);
                    return saved;
                });
    }

    private void ensurePermissions() {
        PERMISSIONS.values().forEach(permission ->
                ensurePermission(permission.code(), permission.name(), permission.description()));
    }

    private PermissionEntity ensurePermission(String code, String name, String description) {
        return permissionRepository.findByCodeAndDeletedAtIsNull(code)
                .orElseGet(() -> {
                    PermissionEntity permission = new PermissionEntity();
                    permission.setCode(code);
                    permission.setName(name);
                    permission.setDescription(description);
                    permission.setStatus(PermissionStatus.ACTIVE);
                    fillAudit(permission);
                    PermissionEntity saved = permissionRepository.save(permission);
                    log.warn("Created default LMS permission. code={}", code);
                    return saved;
                });
    }

    private void assignPermissions(RoleEntity role, List<String> permissionCodes) {
        List<RolePermissionEntity> existing = rolePermissionRepository.findByRoleIdAndDeletedAtIsNull(role.getId());
        for (String permissionCode : permissionCodes) {
            PermissionEntity permission = ensurePermission(permissionCode, permissionCode, permissionCode);
            boolean mapped = existing.stream()
                    .anyMatch(item -> permission.getId().equals(item.getPermissionId()));
            if (mapped) {
                continue;
            }

            RolePermissionEntity mapping = new RolePermissionEntity();
            mapping.setRoleId(role.getId());
            mapping.setPermissionId(permission.getId());
            fillAudit(mapping);
            rolePermissionRepository.save(mapping);
        }
    }

    private void assignUserRole(String userId, RoleEntity role) {
        if (userRoleRepository.findByUserIdAndRoleIdAndDeletedAtIsNull(userId, role.getId()).isPresent()) {
            return;
        }

        UserRoleEntity userRole = new UserRoleEntity();
        userRole.setUserId(userId);
        userRole.setRoleId(role.getId());
        fillAudit(userRole);
        userRoleRepository.save(userRole);
        log.warn("Assigned default LMS role. userId={}, role={}", userId, role.getCode());
    }

    private void assignUserRoleByUsername(String username, RoleEntity role) {
        try {
            BaseResponse<AuthnUserDto> response = authnClient.getUserByUsername(username);
            if (response == null
                    || response.getStatus() == null
                    || !HttpStatus.OK.equals(response.getStatus())
                    || response.getData() == null
                    || response.getData().getId() == null) {
                log.warn("Skipped assigning default LMS role. username={}, role={}", username, role.getCode());
                return;
            }
            assignUserRole(response.getData().getId(), role);
        } catch (Exception ex) {
            log.warn("Could not assign default LMS role yet. username={}, role={}, reason={}",
                    username, role.getCode(), ex.getMessage());
        }
    }

    private void fillAudit(vn.com.atomi.charge.base.model.entity.BaseEntity entity) {
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedBy("system");
        entity.setCreatedDate(now);
        entity.setLastModifiedBy("system");
        entity.setLastModifiedDate(now);
    }

    private static void permission(String code, String name, String description) {
        PERMISSIONS.put(code, new PermissionSeed(code, name, description));
    }

    private record PermissionSeed(String code, String name, String description) {
    }
}
