package vn.com.atomi.charge.chat.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.chat.model.request.SendChatMessageRequest;
import vn.com.atomi.charge.chat.model.response.ChatConversationResponse;
import vn.com.atomi.charge.chat.model.response.ChatMessageResponse;
import vn.com.atomi.charge.chat.service.internal.ChatRateLimiter;
import vn.com.atomi.charge.chat.service.interfaces.ChatService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat/conversations")
@RequiredArgsConstructor
@Tag(name = "AI Chat", description = "REST commands for anonymous AI course conversations")
public class ChatController {

    private final ChatService chatService;
    private final ChatRateLimiter rateLimiter;

    @PostMapping
    public ResponseEntity<BaseResponse<ChatConversationResponse>> create(HttpServletRequest request) {
        if (!rateLimiter.allow(clientIp(request))) {
            return tooManyRequests();
        }
        return ResponseEntity.ok(BaseResponse.success(HttpStatus.OK, chatService.createConversation()));
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<BaseResponse<ChatConversationResponse>> getConversation(
            @PathVariable String conversationId,
            @RequestHeader(value = "X-Chat-Token", required = false) String accessToken) {
        return ResponseEntity.ok(BaseResponse.success(
                HttpStatus.OK,
                chatService.getConversation(conversationId, accessToken)));
    }

    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<BaseResponse<List<ChatMessageResponse>>> getMessages(
            @PathVariable String conversationId,
            @RequestHeader(value = "X-Chat-Token", required = false) String accessToken) {
        return ResponseEntity.ok(BaseResponse.success(
                HttpStatus.OK,
                chatService.getMessages(conversationId, accessToken)));
    }

    @PostMapping("/{conversationId}/messages")
    public ResponseEntity<BaseResponse<ChatMessageResponse>> sendMessage(
            @PathVariable String conversationId,
            @RequestHeader(value = "X-Chat-Token", required = false) String accessToken,
            @RequestBody @Valid SendChatMessageRequest request,
            HttpServletRequest servletRequest) {
        if (!rateLimiter.allow(clientIp(servletRequest))) {
            return tooManyRequests();
        }
        return ResponseEntity.ok(BaseResponse.success(
                HttpStatus.OK,
                chatService.sendMessage(conversationId, accessToken, request)));
    }

    private <T> ResponseEntity<BaseResponse<T>> tooManyRequests() {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(BaseResponse.fail(
                        HttpStatus.TOO_MANY_REQUESTS,
                        "Bạn gửi tin nhắn quá nhanh, vui lòng thử lại sau một phút"));
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",", 2)[0].trim();
        }
        return request.getRemoteAddr() == null ? "unknown" : request.getRemoteAddr();
    }
}
