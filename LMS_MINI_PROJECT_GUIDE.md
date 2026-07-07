# LMS Mini Project Guide

Tài liệu này là bản đồ nhanh cho repo `LMS-Mini-GitHub`. Mục tiêu là giúp bạn đọc code nhanh hơn và hỏi đúng chỗ khi cần sửa hoặc giải thích một tính năng.

## 1. Tổng quan kiến trúc

Repo này là một backend microservice cho hệ thống LMS mini.

Luồng chính:

1. Frontend gọi `api-gateway`.
2. Gateway xác thực JWT, thêm header người dùng, rồi route sang service phù hợp.
3. Mỗi service giữ database riêng và không đọc thẳng DB của service khác.
4. Khi cần dữ liệu liên service, code dùng Feign/RestTemplate.

## 2. Các service chính

### `api-gateway`

Vai trò:

- Cửa vào hệ thống.
- Validate JWT.
- Route request sang đúng service.
- Gom Swagger qua một đầu mối.
- Chạy các filter bảo mật và logging.

Điểm nên đọc trước:

- `api-gateway/src/main/java/vn/com/atomi/charge/gateway/ApiGatewayApplication.java`
- `api-gateway/src/main/java/vn/com/atomi/charge/gateway/config/AuthenticationFilter.java`
- `api-gateway/src/main/java/vn/com/atomi/charge/gateway/config/SecurityConfig.java`
- `api-gateway/src/main/java/vn/com/atomi/charge/gateway/config/ApiConfig.java`

### `base-service`

Vai trò:

- Chứa code dùng chung cho toàn hệ thống.
- Có base entity, base request/response, service CRUD, exception, i18n, MinIO, config chung.

Điểm nên đọc trước:

- `base-service/src/main/java/vn/com/atomi/charge/base/service/BaseService.java`
- `base-service/src/main/java/vn/com/atomi/charge/base/service/MinIOService.java`
- `base-service/src/main/java/vn/com/atomi/charge/base/model/exception/BusinessException.java`
- `base-service/src/main/java/vn/com/atomi/charge/base/i18n/MessageConfig.java`

### `authn-service`

Vai trò:

- Đăng nhập, refresh token, trạng thái tài khoản, bảo mật tài khoản.

Khi hỏi code về đăng nhập, token, khóa tài khoản thì đọc service này trước.

### `author-service`

Vai trò:

- Quản lý role và gán role cho user.

Điểm nên đọc trước:

- `author-service/src/main/java/vn/com/atomi/charge/authorization/AuthorizationServiceApplication.java`
- `author-service/src/main/java/vn/com/atomi/charge/authorization/controller/AuthorController.java`
- `author-service/src/main/java/vn/com/atomi/charge/authorization/service/impl/AuthorServiceImpl.java`
- `author-service/src/main/java/vn/com/atomi/charge/authorization/model/entity/RoleEntity.java`

### `course-service`

Vai trò:

- Danh mục khóa học.
- Course.
- Lesson.
- Resource và image.
- Upload file qua MinIO.

Điểm nên đọc trước:

- `course-service/src/main/java/vn/com/atomi/charge/course/CourseServiceApplication.java`
- `course-service/src/main/java/vn/com/atomi/charge/course/service/impl/CourseServiceImpl.java`
- `course-service/src/main/java/vn/com/atomi/charge/course/service/impl/LessonServiceImpl.java`
- `course-service/src/main/java/vn/com/atomi/charge/course/service/impl/MinioStorageServiceImpl.java`

### `learning-service`

Vai trò:

- Đăng ký học.
- Theo dõi tiến độ học.
- Cấp chứng chỉ.
- Gọi sang course/authn khi cần dữ liệu liên quan.

Điểm nên đọc trước:

- `learning-service/src/main/java/vn/com/atomi/charge/learning/LearningServiceApplication.java`
- `learning-service/src/main/java/vn/com/atomi/charge/learning/controller/EnrollmentController.java`
- `learning-service/src/main/java/vn/com/atomi/charge/learning/controller/LearningProgressController.java`
- `learning-service/src/main/java/vn/com/atomi/charge/learning/controller/CertificateController.java`
- `learning-service/src/main/java/vn/com/atomi/charge/learning/service/impl/EnrollmentServiceImpl.java`

### `quiz-service`

Vai trò:

- Quiz.
- Question.
- Answer.
- Attempt.

Khi hỏi code về làm bài quiz hoặc chấm điểm thì đọc service này trước.

### `notice-service`

Vai trò:

- In-app notification.
- Firebase Web Push.

Hiện tại đây là service thông báo riêng, thường được dùng khi cần đẩy tin nhắn hệ thống hoặc sự kiện học tập.

## 3. Database theo service

Quy tắc chung:

- Mỗi service có database riêng.
- Không tạo foreign key vật lý giữa database khác nhau.
- Dữ liệu liên service chỉ tham chiếu bằng ID và lấy chi tiết qua API.

Mapping nhanh:

- `authn-service` -> `lms_authn_service`
- `author-service` -> `lms_author_service`
- `course-service` -> `lms_course_service`
- `learning-service` -> `lms_learning_service`
- `quiz-service` -> `lms_quiz_service`
- `notice-service` -> `lms_notice_service`

## 4. Luồng nghiệp vụ nên nhớ

### Đăng nhập

1. User login qua `authn-service`.
2. Gateway xác thực JWT cho request tiếp theo.
3. Header user được gắn thêm khi đi qua gateway.

### Tạo khóa học

1. Instructor tạo course ở `course-service`.
2. Lesson/resource/image cũng ở `course-service`.
3. Nếu có duyệt hoặc từ chối khóa học, logic thường đi qua role/admin flow.

### Học và hoàn thành

1. Student enroll trong `learning-service`.
2. `learning-service` theo dõi progress.
3. Nếu yêu cầu quiz, `quiz-service` xử lý phần làm bài.
4. Khi đủ điều kiện, `learning-service` cấp certificate.

### Thông báo

1. Sự kiện nghiệp vụ tạo notice.
2. `notice-service` lưu in-app notice.
3. Nếu có token Firebase hợp lệ, service gửi push notification.

## 5. Quy ước khi đọc hoặc hỏi code

Nếu muốn hỏi nhanh về một tính năng, hãy nêu theo mẫu này:

- Service: service nào.
- Mục tiêu: feature nào.
- Entry point: controller hoặc endpoint nào.
- Vấn đề: lỗi, logic, hoặc phần cần refactor.

Ví dụ:

- “Trong `learning-service`, luồng enroll course chạy từ controller nào đến repository nào?”
- “Trong `course-service`, upload image đi qua MinIO ở đâu?”
- “Trong `api-gateway`, JWT được verify ở filter nào?”

## 6. Gợi ý thứ tự đọc code

Nếu bạn mới vào repo này, nên đọc theo thứ tự:

1. `README.md`
2. `LMS_MINI_PROJECT_PLAN.md`
3. `base-service`
4. `api-gateway`
5. `authn-service`
6. `author-service`
7. `course-service`
8. `learning-service`
9. `quiz-service`
10. `notice-service`

## 7. Tình trạng hiện tại của repo

- Một số service đã có code khá đầy đủ, đặc biệt là gateway, auth, author, course, learning, quiz.
- `notice-service` hiện có vẻ còn nhẹ hơn các service khác.
- Repo đã có tài liệu tổng quan và kế hoạch, nhưng chưa có một file “map code” ngắn như tài liệu này.

## 8. Nếu muốn tôi hỗ trợ tiếp

Bạn chỉ cần hỏi theo một trong các kiểu sau:

- “Giải thích luồng login trong repo này.”
- “Đọc giúp tôi `course-service` từ controller xuống repository.”
- “Chỉ cho tôi nơi cần sửa nếu muốn thêm API enroll course.”
- “Mô tả kiến trúc các service bằng sơ đồ.”

Tôi sẽ bám theo đúng service và file liên quan để giải thích hoặc sửa code cho bạn.