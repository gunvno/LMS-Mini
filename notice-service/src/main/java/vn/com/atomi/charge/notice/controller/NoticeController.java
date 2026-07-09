package vn.com.atomi.charge.notice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.notice.service.interfaces.NoticeService;

@RestController
@RequestMapping("/api/v1/notices")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('NOTICE_VIEW')")
public class NoticeController {

    private final NoticeService service;

    @GetMapping("/me")
    public BaseResponse<?> getMyNotices(Pageable pageable) {
        return service.getMyNotices(pageable);
    }

    @GetMapping("/me/unread-count")
    public BaseResponse<Long> getMyUnreadCount() {
        return service.getMyUnreadCount();
    }

    @PostMapping("/{id}/read")
    public BaseResponse<Void> markRead(@PathVariable String id) {
        return service.markRead(id);
    }

    @PostMapping("/read-all")
    public BaseResponse<Void> markAllRead() {
        return service.markAllRead();
    }
}
