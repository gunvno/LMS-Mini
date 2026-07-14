package vn.com.atomi.charge.chat.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.chat.model.request.SendSupportMessageRequest;
import vn.com.atomi.charge.chat.model.response.SupportConversationResponse;
import vn.com.atomi.charge.chat.model.response.SupportMessageResponse;
import vn.com.atomi.charge.chat.service.interfaces.SupportChatService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/support/conversations")
@RequiredArgsConstructor
@Tag(name = "Instructor Chat", description = "Realtime chat between enrolled students and instructors")
public class SupportChatController {

    private final SupportChatService supportChatService;

    @PostMapping("/courses/{courseId}")
    public ResponseEntity<BaseResponse<SupportConversationResponse>> createOrGet(@PathVariable String courseId) {
        return ResponseEntity.ok(BaseResponse.success(HttpStatus.OK, supportChatService.createOrGet(courseId)));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<SupportConversationResponse>>> getMyConversations() {
        return ResponseEntity.ok(BaseResponse.success(HttpStatus.OK, supportChatService.getMyConversations()));
    }

    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<BaseResponse<List<SupportMessageResponse>>> getMessages(
            @PathVariable String conversationId) {
        return ResponseEntity.ok(BaseResponse.success(HttpStatus.OK, supportChatService.getMessages(conversationId)));
    }

    @PostMapping("/{conversationId}/messages")
    public ResponseEntity<BaseResponse<SupportMessageResponse>> sendMessage(
            @PathVariable String conversationId,
            @RequestBody @Valid SendSupportMessageRequest request) {
        return ResponseEntity.ok(BaseResponse.success(
                HttpStatus.OK,
                supportChatService.sendMessage(conversationId, request)));
    }

    @PostMapping("/{conversationId}/read")
    public ResponseEntity<BaseResponse<Void>> markRead(@PathVariable String conversationId) {
        supportChatService.markRead(conversationId);
        return ResponseEntity.ok(BaseResponse.success(HttpStatus.OK, null));
    }
}
