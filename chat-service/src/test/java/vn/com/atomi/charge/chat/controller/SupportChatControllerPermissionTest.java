package vn.com.atomi.charge.chat.controller;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import vn.com.atomi.charge.chat.model.request.SendSupportMessageRequest;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class SupportChatControllerPermissionTest {

    @Test
    void everyHumanChatEndpointRequiresAuthenticationOnly() throws NoSuchMethodException {
        assertAuthenticated("getMyConversations");
        assertAuthenticated("getMessages", String.class);
        assertAuthenticated("markRead", String.class);
        assertAuthenticated("createOrGet", String.class);
        assertAuthenticated(
                "sendMessage",
                String.class,
                SendSupportMessageRequest.class);
    }

    private void assertAuthenticated(String methodName, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        Method method = SupportChatController.class.getMethod(methodName, parameterTypes);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        assertThat(annotation)
                .as("@PreAuthorize on %s", methodName)
                .isNotNull();
        assertThat(annotation.value()).isEqualTo("isAuthenticated()");
    }
}
