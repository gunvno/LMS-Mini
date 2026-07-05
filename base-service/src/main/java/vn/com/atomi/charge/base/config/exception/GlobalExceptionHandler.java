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
                "Invalid parameter: " + fieldName, HttpStatus.OK);
    }

    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<BaseResponse<Void>> handleServerWebInputException(ServerWebInputException ex) {
        log.warn("Invalid request: {}", ex.getReason());
        return createErrorResponse(BaseErrorCode.BAD_REQUEST,
                "Invalid request: " + ex.getReason(), HttpStatus.OK);
    }

    @ExceptionHandler(MethodNotAllowedException.class)
    public ResponseEntity<BaseResponse<Void>> handleMethodNotAllowed(MethodNotAllowedException ex) {
        log.warn("Method not allowed: {}", ex.getHttpMethod());
        return createErrorResponse(BaseErrorCode.METHOD_NOT_ALLOWED,
                "HTTP method not allowed: " + ex.getHttpMethod(), HttpStatus.OK);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<BaseResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        log.warn("Method not supported: {}", ex.getMethod());
        return createErrorResponse(BaseErrorCode.METHOD_NOT_ALLOWED,
                "HTTP method not supported: " + ex.getMethod(), HttpStatus.OK);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<BaseResponse<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Message not readable: {}", Util.beautyError(ex));
        return createErrorResponse(BaseErrorCode.BAD_REQUEST,
                "Invalid request", HttpStatus.OK);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<BaseResponse<Void>> handleHttpMessageNotReadable(NoResourceFoundException ex) {
        log.warn("resource not found: {}", Util.beautyError(ex));
        return createErrorResponse(BaseErrorCode.BAD_REQUEST,
                "Invalid request", HttpStatus.OK);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<BaseResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", Util.beautyError(ex));
        return createErrorResponse(BaseErrorCode.UNAUTHORIZED,
                messageService.getMessage("common.access_denied"), HttpStatus.OK);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        FieldError errorField = ex.getBindingResult().getFieldErrors().get(0);
        log.warn("invalid param: {}", Util.beautyError(ex));
        return createErrorResponse(BaseErrorCode.BAD_REQUEST,
                messageService.getMessage(errorField.getDefaultMessage()), HttpStatus.OK);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<BaseResponse<Void>> handleMaxSizeExceedException(MaxUploadSizeExceededException ex) {
        log.warn("upload file error: {}", Util.beautyError(ex));
        return createErrorResponse(BaseErrorCode.BAD_REQUEST,
                messageService.getMessage("common.max_file_size_exceed"), HttpStatus.OK);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<BaseResponse<Void>> handleBusinessException(Exception ex) {
        BusinessException businessEx = (BusinessException) ex;
        log.error("handle business exception: {}, {}", businessEx.getCode(), Util.beautyError(businessEx));
        return createErrorResponse(businessEx.getCode(), businessEx.getMessage(), HttpStatus.OK);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Void>> handleAllUncaughtException(Exception ex) {
        if (ex instanceof BusinessException businessEx) {
            return createErrorResponse(businessEx.getCode(), businessEx.getMessage(), HttpStatus.OK);
        }
        log.error("Unhandled exception: {}", Util.beautyError(ex));
        return createErrorResponse(BaseErrorCode.INTERNAL_ERROR, HttpStatus.OK);
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