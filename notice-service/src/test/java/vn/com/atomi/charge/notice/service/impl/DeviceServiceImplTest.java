package vn.com.atomi.charge.notice.service.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import vn.com.atomi.charge.base.model.request.BaseRequest;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.notice.model.entity.UserDeviceEntity;
import vn.com.atomi.charge.notice.model.enums.UserDeviceStatus;
import vn.com.atomi.charge.notice.model.request.DeviceDeactivateRequest;
import vn.com.atomi.charge.notice.model.request.DeviceRegisterRequest;
import vn.com.atomi.charge.notice.repository.UserDeviceRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceServiceImplTest {

    private static final String USER_ID = "user-new";
    private static final String INSTALLATION_ID = "firebase-installation-id";

    @Mock
    private UserDeviceRepository repository;

    @InjectMocks
    private DeviceServiceImpl service;

    @BeforeEach
    void authenticate() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(USER_ID, "password"));
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void registerReassignsExistingInstallationToCurrentUser() {
        UserDeviceEntity previousOwner = device("user-old", UserDeviceStatus.ACTIVE);
        when(repository.findAllByInstallationIdForUpdate(INSTALLATION_ID))
                .thenReturn(List.of(previousOwner));

        BaseResponse<Void> response = service.registerDevice(registerRequest(INSTALLATION_ID, null));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK);
        assertThat(previousOwner.getUserId()).isEqualTo(USER_ID);
        assertThat(previousOwner.getInstallationId()).isEqualTo(INSTALLATION_ID);
        assertThat(previousOwner.getStatus()).isEqualTo(UserDeviceStatus.ACTIVE);
        verify(repository).saveAll(List.of(previousOwner));
    }

    @Test
    void registerKeepsOnlyOneActiveOwnerWhenDuplicateRowsAlreadyExist() {
        UserDeviceEntity currentOwner = device(USER_ID, UserDeviceStatus.INACTIVE);
        UserDeviceEntity otherOwner = device("user-old", UserDeviceStatus.ACTIVE);
        List<UserDeviceEntity> registrations = List.of(otherOwner, currentOwner);
        when(repository.findAllByInstallationIdForUpdate(INSTALLATION_ID)).thenReturn(registrations);

        service.registerDevice(registerRequest(null, INSTALLATION_ID));

        assertThat(currentOwner.getStatus()).isEqualTo(UserDeviceStatus.ACTIVE);
        assertThat(otherOwner.getStatus()).isEqualTo(UserDeviceStatus.INACTIVE);
        verify(repository).saveAll(registrations);
    }

    @Test
    void deactivateDoesNotDeactivateInstallationOwnedByAnotherUser() {
        UserDeviceEntity otherOwner = device("user-old", UserDeviceStatus.ACTIVE);
        when(repository.findAllByInstallationIdForUpdate(INSTALLATION_ID))
                .thenReturn(List.of(otherOwner));

        BaseResponse<Void> response = service.deactivateDevice(deactivateRequest(INSTALLATION_ID));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK);
        assertThat(otherOwner.getStatus()).isEqualTo(UserDeviceStatus.ACTIVE);
        verify(repository).saveAll(List.of());
    }

    @Test
    void deactivateMarksCurrentUsersRegistrationInactive() {
        UserDeviceEntity currentOwner = device(USER_ID, UserDeviceStatus.ACTIVE);
        when(repository.findAllByInstallationIdForUpdate(INSTALLATION_ID))
                .thenReturn(List.of(currentOwner));

        service.deactivateDevice(deactivateRequest(INSTALLATION_ID));

        assertThat(currentOwner.getStatus()).isEqualTo(UserDeviceStatus.INACTIVE);
        verify(repository).saveAll(List.of(currentOwner));
    }

    @Test
    void markInstallationInvalidInvalidatesEveryMatchingRegistration() {
        UserDeviceEntity active = device(USER_ID, UserDeviceStatus.ACTIVE);
        UserDeviceEntity staleDuplicate = device("user-old", UserDeviceStatus.INACTIVE);
        List<UserDeviceEntity> registrations = List.of(active, staleDuplicate);
        when(repository.findAllByInstallationIdForUpdate(INSTALLATION_ID)).thenReturn(registrations);

        service.markInstallationInvalid(INSTALLATION_ID);

        assertThat(registrations)
                .extracting(UserDeviceEntity::getStatus)
                .containsOnly(UserDeviceStatus.INVALID);
        verify(repository).saveAll(registrations);
    }

    private UserDeviceEntity device(String userId, UserDeviceStatus status) {
        UserDeviceEntity device = new UserDeviceEntity();
        device.setUserId(userId);
        device.setInstallationId(INSTALLATION_ID);
        device.setStatus(status);
        return device;
    }

    private BaseRequest<DeviceRegisterRequest> registerRequest(String installationId, String legacyToken) {
        DeviceRegisterRequest data = new DeviceRegisterRequest();
        data.setInstallationId(installationId);
        data.setToken(legacyToken);
        BaseRequest<DeviceRegisterRequest> request = new BaseRequest<>();
        request.setData(data);
        return request;
    }

    private BaseRequest<DeviceDeactivateRequest> deactivateRequest(String installationId) {
        DeviceDeactivateRequest data = new DeviceDeactivateRequest();
        data.setInstallationId(installationId);
        BaseRequest<DeviceDeactivateRequest> request = new BaseRequest<>();
        request.setData(data);
        return request;
    }
}
