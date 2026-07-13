# LMS Mini Chat Service

Service quản lý hội thoại AI công khai, lưu lịch sử MySQL và phát câu trả lời realtime bằng WebSocket/STOMP.

## Endpoints

- `POST /api/v1/chat/conversations`: tạo hội thoại và access token.
- `GET /api/v1/chat/conversations/{id}`: lấy trạng thái hội thoại.
- `GET /api/v1/chat/conversations/{id}/messages`: lấy lịch sử.
- `POST /api/v1/chat/conversations/{id}/messages`: gửi lệnh chat qua REST.
- `GET /ws` (WebSocket upgrade): STOMP endpoint.
- Topic: `/topic/chat/conversations/{id}`.

Các request sau khi tạo hội thoại dùng header `X-Chat-Token`. STOMP CONNECT dùng cả
`X-Conversation-Id` và `X-Chat-Token`. STOMP SEND bị chặn; frontend chỉ subscribe event.

## Local setup

Chạy `database/chat-service.sql` một lần, cấu hình `GEMINI_API_KEY`, rồi khởi động:

```bash
mvn -f chat-service/pom.xml spring-boot:run
```
