package vn.com.atomi.charge.gateway.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import vn.com.atomi.charge.gateway.util.DateUtil;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDto {
    String id;

    String organizationId;

    String username;

    String phone;

    String fullName;

    String identityNumber;

    String displayName;

    String userType;

    String roleCode;

    String status;

    String resetPasswordKey;

    Integer failedLoginAttempts;

    String loginIp;

    String loginDevice;

    boolean twoFactorEnabled;

    String twoFactorMethod;

    String twoFactorSecret;

    boolean twoFactorVerified;

    String twoFactorEmail;

    String twoFactorPhone;

    Integer otpRetryCount;

    List<String> permissions;

    List<String> permissionSets;

    String bankAccountId;

    String profilePhotoUrl;

    String language;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtil.YMD_HMS_DASH_PATTERN)
    LocalDateTime activeSince;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtil.YMD_HMS_DASH_PATTERN)
    LocalDateTime resetPasswordExpiredAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtil.YMD_HMS_DASH_PATTERN)
    LocalDateTime lastLoginAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtil.YMD_HMS_DASH_PATTERN)
    LocalDateTime lastActiveAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtil.YMD_HMS_DASH_PATTERN)
    LocalDateTime accountLockedUntil;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtil.YMD_HMS_DASH_PATTERN)
    LocalDateTime passwordChangedAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtil.YMD_HMS_DASH_PATTERN)
    LocalDateTime createdDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtil.YMD_HMS_DASH_PATTERN)
    LocalDateTime otpLastSentAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtil.YMD_HMS_DASH_PATTERN)
    LocalDateTime identityNumberExpiredAt;
}
