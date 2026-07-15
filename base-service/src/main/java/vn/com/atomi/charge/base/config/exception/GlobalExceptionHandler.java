package vn.com.atomi.charge.base.config.exception;

import vn.com.atomi.charge.base.i18n.IMessageService;
import vn.com.atomi.charge.base.model.enums.BaseErrorCode;
import vn.com.atomi.charge.base.model.exception.BusinessException;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.base.util.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.servlet.resource.NoResourceFoundException;


@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final IMessageService messageService;

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<BaseResponse<Void>> handleWebExchangeBindException(WebExchangeBindException ex) {
        String fieldName = ex.getFieldError() != null ?
                ex.getFieldError().getField() : "unknown";
        log.warn("Validation error on field {}: {}", fieldName, ex.getMessage());
        return createErrorResponse(BaseErrorCode.BAD_REQUEST,
                messageService.getMessage("validation.invalid_request"), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<BaseResponse<Void>> handleServerWebInputException(ServerWebInputException ex) {
        log.warn("Invalid request: {}", ex.getReason());
        return createErrorResponse(BaseErrorCode.BAD_REQUEST,
                messageService.getMessage("validation.invalid_request"), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodNotAllowedException.class)
    public ResponseEntity<BaseResponse<Void>> handleMethodNotAllowed(MethodNotAllowedException ex) {
        log.warn("Method not allowed: {}", ex.getHttpMethod());
        return createErrorResponse(BaseErrorCode.METHOD_NOT_ALLOWED,
                messageService.getMessage("common.method_not_allowed"), HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<BaseResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        log.warn("Method not supported: {}", ex.getMethod());
        return createErrorResponse(BaseErrorCode.METHOD_NOT_ALLOWED,
                messageService.getMessage("common.method_not_allowed"), HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<BaseResponse<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Message not readable: {}", Util.beautyError(ex));
        return createErrorResponse(BaseErrorCode.BAD_REQUEST,
                messageService.getMessage("validation.invalid_request"), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<BaseResponse<Void>> handleHttpMessageNotReadable(NoResourceFoundException ex) {
        log.warn("resource not found: {}", Util.beautyError(ex));
        return createErrorResponse(BaseErrorCode.NOT_FOUND,
                messageService.getMessage("common.not_found"), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<BaseResponse<Void>> handleResponseStatusException(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        BaseErrorCode errorCode = status == HttpStatus.NOT_FOUND
                ? BaseErrorCode.NOT_FOUND
                : status.is4xxClientError() ? BaseErrorCode.BAD_REQUEST : BaseErrorCode.INTERNAL_ERROR;
        if (status.is5xxServerError()) {
            log.error("Response status error {}: {}", status.value(), Util.beautyError(ex));
        } else {
            log.debug("Response status {}: {}", status.value(), ex.getReason());
        }
        return createErrorResponse(errorCode,
                StringUtils.defaultIfBlank(ex.getReason(), status.getReasonPhrase()), status);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<BaseResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", Util.beautyError(ex));
        return createErrorResponse(BaseErrorCode.FORBIDDEN,
                messageService.getMessage("security.access_denied"), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        FieldError errorField = ex.getBindingResult().getFieldErrors().get(0);
        log.warn("invalid param: {}", Util.beautyError(ex));
        String message = switch (errorField.getCode() == null ? "" : errorField.getCode()) {
            case "NotBlank", "NotNull", "NotEmpty" -> messageService.getMessage(
                    "validation.required_field", new Object[]{errorField.getField()});
            case "Email" -> messageService.getMessage("validation.email_invalid");
            case "Pattern" -> messageService.getMessage(
                    "validation.invalid_format", new Object[]{errorField.getField()});
            default -> messageService.getMessage(errorField.getDefaultMessage());
        };
        return createErrorResponse(BaseErrorCode.BAD_REQUEST,
                message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<BaseResponse<Void>> handleMaxSizeExceedException(MaxUploadSizeExceededException ex) {
        log.warn("upload file error: {}", Util.beautyError(ex));
        return createErrorResponse(BaseErrorCode.BAD_REQUEST,
                messageService.getMessage("common.max_file_size_exceed"), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<BaseResponse<Void>> handleBusinessException(Exception ex) {
        BusinessException businessEx = (BusinessException) ex;
        log.error("handle business exception: {}, {}", businessEx.getCode(), Util.beautyError(businessEx));
        return createErrorResponse(
                businessEx.getCode(), messageService.getMessage(businessEx.getMessage()), resolveBusinessStatus(businessEx.getCode()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Void>> handleAllUncaughtException(Exception ex) {
        if (ex instanceof BusinessException businessEx) {
            return createErrorResponse(
                    businessEx.getCode(), messageService.getMessage(businessEx.getMessage()), resolveBusinessStatus(businessEx.getCode()));
        }
        log.error("Unhandled exception: {}", Util.beautyError(ex));
        return createErrorResponse(BaseErrorCode.INTERNAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private HttpStatus resolveBusinessStatus(String code) {
        if (code == null) {
            return HttpStatus.BAD_REQUEST;
        }
        return switch (code) {
            case "UNAUTHENTICATED" -> HttpStatus.UNAUTHORIZED;
            case "FORBIDDEN" -> HttpStatus.FORBIDDEN;
            default -> HttpStatus.BAD_REQUEST;
        };
    }

    private ResponseEntity<BaseResponse<Void>> createErrorResponse(BaseErrorCode code, String message, HttpStatus httpStatus) {
        return ResponseEntity.status(httpStatus)
                .body(createResponse(code.getErrorCode(), message, httpStatus));
    }

    private ResponseEntity<BaseResponse<Void>> createErrorResponse(BaseErrorCode code, HttpStatus httpStatus) {
        return ResponseEntity.status(httpStatus)
                .body(createResponse(code.getErrorCode(), messageService.getMessage(code.getErrorCode()), httpStatus));
    }

    private ResponseEntity<BaseResponse<Void>> createErrorResponse(String errorCode, HttpStatus httpStatus) {
        return ResponseEntity.status(httpStatus)
                .body(createResponse(errorCode, messageService.getMessage(errorCode), httpStatus));
    }

    private ResponseEntity<BaseResponse<Void>> createErrorResponse(String errorCode, String message, HttpStatus httpStatus) {
        if (StringUtils.isBlank(message)) {
            return createErrorResponse(errorCode, httpStatus);
        }
        return ResponseEntity.status(httpStatus)
                .body(createResponse(errorCode, message, httpStatus));
    }

    BaseResponse<Void> createResponse(String errorCode, String errorMessage, HttpStatus httpStatus) {
        BaseResponse<Void> baseResponse = new BaseResponse<>();
        baseResponse.setErrorCode(errorCode);
        baseResponse.setMessage(errorMessage);
        baseResponse.setStatus(httpStatus);
        return baseResponse;
    }
}
