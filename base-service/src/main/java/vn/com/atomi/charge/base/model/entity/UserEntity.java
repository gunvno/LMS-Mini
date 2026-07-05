package vn.com.atomi.charge.base.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.com.atomi.charge.base.util.DateUtil;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tbl_users")
public class UserEntity extends BaseEntity implements Serializable {

    @Column(name = "organization_id")
    String organizationId;

    @Column(name = "department_id")
    String departmentId;

    @Column(name = "position_id")
    String positionId;

    @Column(name = "address_id", length = 125)
    String addressId;

    @Column(name = "username")
    String username;

    @Column(name = "password_hash")
    String passwordHash;

    @Column(name = "email")
    String email;

    @Column(name = "phone")
    String phone;

    @Column(name = "full_name")
    String fullName;

    @Column(name = "date_of_birth")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtil.YMD_HMS_DASH_PATTERN)
    Date dateOfBirth;

    @Column(name = "gender")
    String gender;

    @Column(name = "profile_photo_url")
    String profilePhotoUrl;

    @Column(name = "language")
    String language;

    @Column(name = "status")
    String status;

    @Column(name = "notice_status")
    Boolean noticeStatus;

    @Column(name = "last_login_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtil.YMD_HMS_DASH_PATTERN)
    LocalDateTime lastLoginAt;

    @Column(name = "contact_id")
    String contactId;

    @Lob
    @Column(name = "access_token", columnDefinition = "TEXT")
    String accessToken;

    @Column(name = "reset_password_key")
    String resetPasswordKey;

    @Column(name = "reset_password_expired_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtil.YMD_HMS_DASH_PATTERN)
    LocalDateTime resetPasswordExpiredAt;

    @Column(name = "failed_login_attempts")
    Integer failedLoginAttempts;

    @Column(name = "account_locked_until")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtil.YMD_HMS_DASH_PATTERN)
    LocalDateTime accountLockedUntil;

    @Column(name = "otp_locked_until")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtil.YMD_HMS_DASH_PATTERN)
    LocalDateTime otpLockedUntil;

    @Column(name = "send_otp_locked_until")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtil.YMD_HMS_DASH_PATTERN)
    LocalDateTime sendOtpLockedUntil;

    @Column(name = "password_change_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtil.YMD_HMS_DASH_PATTERN)
    LocalDateTime passwordChangeAt;

    @Column(name = "otp_password")
    String otpPassword;

    @Column(name = "two_factor_enabled")
    Boolean twoFactorEnabled;

    @Column(name = "two_factor_method")
    String twoFactorMethod;

    @Column(name = "two_factor_secret")
    String twoFactorSecret;

    @Column(name = "two_factor_verified")
    Boolean twoFactorVerified;

    @Column(name = "two_factor_email")
    String twoFactorEmail;

    @Column(name = "two_factor_phone")
    String twoFactorPhone;

    @Column(name = "user_type", length = 50)
    String userType;

    @Column(name = "identity_number")
    String identityNumber;

    @Column(name = "identity_number_expired_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtil.YMD_HMS_DASH_PATTERN)
    LocalDateTime identityNumberExpiredAt;

}
