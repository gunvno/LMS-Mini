package vn.com.atomi.charge.base.config.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import vn.com.atomi.charge.base.i18n.IMessageService;
import vn.com.atomi.charge.base.model.enums.BaseErrorCode;
import vn.com.atomi.charge.base.model.response.BaseResponse;

/** Resolves i18n keys left by service-layer BaseResponse.fail calls before serializing the API response. */
@ControllerAdvice
@RequiredArgsConstructor
public class I18nResponseAdvice implements ResponseBodyAdvice<Object> {
    private final IMessageService messageService;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  org.springframework.http.server.ServerHttpRequest request,
                                  org.springframework.http.server.ServerHttpResponse response) {
        if (!(body instanceof BaseResponse<?> api) || BaseErrorCode.SUCCESS.getErrorCode().equals(api.getErrorCode())) {
            return body;
        }
        String message = api.getMessage();
        if (message != null && message.matches("[a-z][a-z0-9_]*(\\.[a-z0-9_]+)+")) {
            String localized = messageService.getMessage(message);
            api.setMessage(localized.equals(message)
                    ? messageService.getMessage("common.operation_failed")
                    : localized);
        }
        if (api.getMessage() == null || api.getMessage().isBlank()) {
            api.setMessage(messageService.getMessage("common.operation_failed"));
        }
        return api;
    }
}
