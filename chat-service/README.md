# LMS Mini Chat Service

Service quản lý hai luồng chat độc lập:

- Trợ lý AI công khai để tư vấn khóa học.
- Chat realtime giữa học viên đã đăng ký khóa học và giảng viên phụ trách.

## Endpoints

- `POST /api/v1/chat/conversations`: tạo hội thoại và access token.
- `GET /api/v1/chat/conversations/{id}`: lấy trạng thái hội thoại.
- `GET /api/v1/chat/conversations/{id}/messages`: lấy lịch sử.
- `POST /api/v1/chat/conversations/{id}/messages`: gửi lệnh chat qua REST.
- `GET /ws` (WebSocket upgrade): STOMP endpoint.
- Topic: `/topic/chat/conversations/{id}`.

Các request sau khi tạo hội thoại dùng header `X-Chat-Token`. STOMP CONNECT dùng cả
`X-Conversation-Id` và `X-Chat-Token`. STOMP SEND bị chặn; frontend chỉ subscribe event.

## Instructor chat

- `POST /api/v1/support/conversations/courses/{courseId}`: học viên tạo/lấy hội thoại theo khóa học.
- `GET /api/v1/support/conversations`: lấy các hội thoại mà user hiện tại tham gia.
- `GET /api/v1/support/conversations/{id}/messages`: lấy lịch sử và đánh dấu đã đọc.
- `POST /api/v1/support/conversations/{id}/messages`: gửi tin nhắn.
- `POST /api/v1/support/conversations/{id}/read`: đánh dấu tin nhắn đã đọc.
- Topic: `/topic/support/conversations/{id}`.

Instructor chat yêu cầu JWT ở REST và header `Authorization: Bearer <token>` khi STOMP CONNECT.
Học viên chỉ tạo được hội thoại khi enrollment còn quyền truy cập; chỉ học viên và đúng instructor
của khóa học được đọc hoặc subscribe hội thoại.

## Local setup

Chạy `database/chat-service.sql` cho database mới hoặc `database/support-chat.sql` để nâng cấp database
đang có, cấu hình `GEMINI_API_KEY`, rồi khởi động:

```bash
mvn -f chat-service/pom.xml spring-boot:run
```
