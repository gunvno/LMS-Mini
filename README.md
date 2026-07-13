# LMS Mini

Workspace tách riêng cho LMS Mini.

## Services

- `api-gateway`: cổng vào hệ thống, route request và validate JWT.
- `base-service`: thư viện nền dùng chung.
- `authn-service`: xác thực, access token, refresh token.
- `author-service`: role/permission.
- `course-service`: course category, course, lesson, resource, image.
- `learning-service`: enrollment, learning progress, certificate.
- `quiz-service`: quiz, question, answer, attempt.
- `notice-service`: in-app notification và Firebase Web Push.
- `chat-service`: hội thoại AI, lịch sử chat và WebSocket/STOMP realtime.

## Ghi chú

Các service `api-gateway`, `authn-service`, `author-service`, `base-service` được copy từ base hiện tại và bỏ `.git`.
Các service LMS mới đã có `pom.xml`, `application.yaml`, application class và entity cơ bản.

## Docker Compose

Mặc định compose chạy các dependency còn thiếu và toàn bộ service, nhưng dùng
MySQL đang chạy trên máy host qua `host.docker.internal:3306`.

```bash
docker compose up -d --build
```

Nếu muốn Docker chạy cả MySQL:

```bash
docker compose --profile mysql up -d --build
```

Các database cần có trên MySQL local:

```text
lms_authn_service
lms_author_service
lms_course_service
lms_learning_service
lms_quiz_service
lms_notice_service
lms_chat_service
```

Port chính:

```text
API Gateway:      http://localhost:8080
Authn Service:    http://localhost:8081
Author Service:   http://localhost:8082
Course Service:   http://localhost:8083
Learning Service: http://localhost:8084
Quiz Service:     http://localhost:8085
Notice Service:   http://localhost:8086
Chat Service:     http://localhost:8089
Consul UI:        http://localhost:8500
MinIO Console:    http://localhost:9001
```

Swagger qua gateway:

```text
http://localhost:8080/swagger-ui.html
```

## AI chatbot tư vấn khóa học

Chatbot dùng Gemini ở `chat-service`; API key không được gửi xuống frontend.
Sao chép `.env.example` thành `.env` và điền:

```text
GEMINI_API_KEY=your-real-gemini-api-key
GEMINI_MODEL=gemini-3.1-flash-lite
```

Endpoint công khai qua API Gateway:

```text
POST http://localhost:8080/chat/api/v1/chat/conversations
Content-Type: application/json
```

Sau khi nhận `id` và `accessToken`, gửi tin nhắn:

```text
POST http://localhost:8080/chat/api/v1/chat/conversations/{id}/messages
X-Chat-Token: {accessToken}
Content-Type: application/json

{
  "content": "Bên mình có khóa Backend Java dưới 1,5 triệu không?"
}
```

Frontend kết nối STOMP tại `ws://localhost:8080/chat/ws`, truyền
`X-Conversation-Id` và `X-Chat-Token` trong CONNECT headers, sau đó subscribe
`/topic/chat/conversations/{id}`. REST dùng để gửi lệnh; WebSocket dùng nhận câu trả lời realtime.

Chatbot chỉ dùng tối đa 200 khóa học `PUBLISHED` lấy từ internal API của course-service,
kiểm tra lại ID gợi ý và mặc định giới hạn 20 yêu cầu/IP/phút. Nếu MySQL đã tồn tại
từ trước, chạy `database/chat-service.sql` một lần trước khi khởi động chat-service.
