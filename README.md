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
Consul UI:        http://localhost:8500
MinIO Console:    http://localhost:9001
```

Swagger qua gateway:

```text
http://localhost:8080/swagger-ui.html
```
