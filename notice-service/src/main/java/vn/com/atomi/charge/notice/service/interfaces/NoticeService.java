package vn.com.atomi.charge.notice.service.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.com.atomi.charge.base.model.request.BaseRequest;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.notice.model.dto.NoticeDto;
import vn.com.atomi.charge.notice.model.request.NoticeSendAllRequest;
import vn.com.atomi.charge.notice.model.request.NoticeSendRoleRequest;
import vn.com.atomi.charge.notice.model.request.NoticeSendUserRequest;
import vn.com.atomi.charge.notice.model.request.NoticeSendUsersRequest;

public interface NoticeService {

    BaseResponse<Void> sendUser(BaseRequest<NoticeSendUserRequest> request);

    BaseResponse<Void> sendUsers(BaseRequest<NoticeSendUsersRequest> request);

    BaseResponse<Void> sendRole(BaseRequest<NoticeSendRoleRequest> request);

    BaseResponse<Void> sendAll(BaseRequest<NoticeSendAllRequest> request);

    BaseResponse<Page<NoticeDto>> getMyNotices(Pageable pageable);

    BaseResponse<Long> getMyUnreadCount();

    BaseResponse<Void> markRead(String recipientId);

    BaseResponse<Void> markAllRead();
}
