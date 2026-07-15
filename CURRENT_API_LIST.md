# LMS Mini - Danh Sách API Hiện Tại

File này chỉ liệt kê API đang có trong backend và chức năng của từng API. Frontend nên gọi qua API Gateway. Các API `/internal/v1/**` chỉ dành cho service gọi nhau, không dùng trực tiếp từ frontend.

## API Gateway

| Method | API | Chức năng |
|---|---|---|
| GET | `/api/v1/health-check` | Kiểm tra trạng thái API Gateway |
| POST | `/auth/token` | Alias đăng nhập, gateway chuyển tới authn login |
| POST | `/auth/refresh` | Alias refresh token |
| POST | `/auth/userinfo` | Alias lấy thông tin user hiện tại |
| ANY | `/auth/**` | Route chung tới authn-service `/api/v1/auth/**` |
| ANY | `/authn/**` | Route raw tới authn-service |
| ANY | `/author/**` | Route raw tới author-service |
| ANY | `/admin/permissions/**` | Alias quản lý permission/staff tới author-service |
| ANY | `/staff-activity/**` | Alias xem hoạt động staff tới author-service |
| ANY | `/course/**` | Route tới course-service |
| ANY | `/learning/**` | Route tới learning-service |
| ANY | `/quiz/**` | Route tới quiz-service |
| ANY | `/notice/**` | Route tới notice-service |

## Authn Service

Gateway prefix hay dùng: `/auth`. Raw prefix: `/authn/api/v1/auth`.

| Method | Gateway API | Raw service API | Chức năng |
|---|---|---|---|
| POST | `/auth/login` | `/api/v1/auth/login` | Đăng nhập bằng username/password |
| POST | `/auth/token` | `/api/v1/auth/login` | Đăng nhập alias theo gateway |
| POST | `/auth/introspect` | `/api/v1/auth/introspect` | Kiểm tra token còn hợp lệ |
| POST | `/auth/refresh-token` | `/api/v1/auth/refresh-token` | Refresh access token |
| POST | `/auth/refresh` | `/api/v1/auth/refresh-token` | Refresh token alias theo gateway |
| POST | `/auth/logout` | `/api/v1/auth/logout` | Đăng xuất/thu hồi token |
| POST | `/auth/change-password` | `/api/v1/auth/change-password` | User hiện tại đổi mật khẩu |
| POST | `/auth/otp-register` | `/api/v1/auth/otp-register` | Gửi OTP đăng ký |
| POST | `/auth/otp-verify` | `/api/v1/auth/otp-verify` | Xác thực OTP |
| POST | `/auth/register` | `/api/v1/auth/register` | Đăng ký tài khoản |
| POST | `/auth/me` | `/api/v1/auth/me` | Lấy thông tin user hiện tại |
| POST | `/auth/userinfo` | `/api/v1/auth/me` | Lấy thông tin user hiện tại alias theo gateway |

### Authn Internal API

| Method | Internal API | Chức năng |
|---|---|---|
| POST | `/internal/v1/AuthnUser/{id}/check` | Kiểm tra user có tồn tại không |
| GET | `/internal/v1/AuthnUser/{userId}/info` | Lấy thông tin user theo id |
| POST | `/internal/v1/AuthnUser/bulk` | Lấy thông tin nhiều user theo danh sách id |
| POST | `/internal/v1/AuthnUser/staff` | Tạo user staff/instructor từ author-service |
| POST | `/internal/v1/AuthnUser/{userId}/status` | Cập nhật trạng thái user |
| POST | `/internal/v1/AuthnUser/{userId}/reset-password` | Reset mật khẩu user |

## Author Service

Gateway prefix raw: `/author`. Alias admin: `/admin/permissions`.

| Method | Gateway API | Raw service API | Chức năng |
|---|---|---|---|
| GET | `/author/api/v1/roles` | `/api/v1/roles` | Danh sách role |
| GET | `/author/api/v1/roles/{roleCode}` | `/api/v1/roles/{roleCode}` | Chi tiết role theo mã role |
| GET | `/admin/permissions` | `/api/v1/permissions` | Danh sách permission |
| POST | `/admin/permissions/staff` | `/api/v1/permissions/staff` | Tạo tài khoản staff/instructor |
| GET | `/admin/permissions/staff` | `/api/v1/permissions/staff` | Danh sách staff account |
| GET | `/admin/permissions/staff/{accountId}` | `/api/v1/permissions/staff/{accountId}` | Chi tiết staff account và quyền |
| PUT | `/admin/permissions/staff/{accountId}` | `/api/v1/permissions/staff/{accountId}` | Cập nhật danh sách permission của staff |
| PUT | `/admin/permissions/staff/{accountId}/status` | `/api/v1/permissions/staff/{accountId}/status` | Khóa/mở trạng thái staff |
| PUT | `/admin/permissions/staff/{accountId}/reset-password` | `/api/v1/permissions/staff/{accountId}/reset-password` | Reset mật khẩu staff |
| POST | `/author/api/v1/roles/{roleCode}/permissions` | `/api/v1/roles/{roleCode}/permissions` | Gán permission cho role |
| POST | `/author/api/v1/user-roles` | `/api/v1/user-roles` | Gán role cho user bằng body |
| POST | `/author/api/v1/users/{userId}/roles` | `/api/v1/users/{userId}/roles` | Gán role cho user theo path userId |
| GET | `/author/api/v1/users/{userId}/roles` | `/api/v1/users/{userId}/roles` | Lấy role của một user |
| GET | `/author/api/v1/users/notice-recipients` | `/api/v1/users/notice-recipients` | Danh sách người nhận thông báo đang hoạt động; giao diện hiển thị tên, `userId` chỉ dùng làm key nội bộ |
| GET | `/author/api/v1/users/me/roles` | `/api/v1/users/me/roles` | Lấy role của user hiện tại từ token |
| GET | `/author/api/v1/users/me/permissions` | `/api/v1/users/me/permissions` | Lấy permission của user hiện tại từ token |
| GET | `/staff-activity/me` | `/staff-activity/me` | Xem hoạt động staff của user hiện tại |
| GET | `/staff-activity` | `/staff-activity` | Admin xem danh sách hoạt động staff |

### Author Internal API

| Method | Internal API | Chức năng |
|---|---|---|
| GET | `/internal/v1/users/{userId}/permissions` | Gateway lấy permission của user để build authority |
| GET | `/internal/v1/roles/{roleCode}/users` | Lấy danh sách user theo role |

## Course Service - Course

Gateway prefix: `/course`.

| Method | Gateway API | Raw service API | Chức năng |
|---|---|---|---|
| GET | `/course/api/v1/courses` | `/api/v1/courses` | Danh sách khóa học, phân trang/lọc |
| GET | `/course/api/v1/courses/{id}` | `/api/v1/courses/{id}` | Chi tiết khóa học |
| GET | `/course/api/v1/courses/published` | `/api/v1/courses/published` | Danh sách khóa học đã publish cho học sinh/khách xem |
| GET | `/course/api/v1/courses/{id}/published` | `/api/v1/courses/{id}/published` | Chi tiết khóa học đã publish cho học sinh/khách xem |
| POST | `/course/api/v1/courses` | `/api/v1/courses` | Tạo khóa học |
| POST | `/course/api/v1/courses/{id}` | `/api/v1/courses/{id}` | Cập nhật khóa học |
| DELETE | `/course/api/v1/courses/{id}` | `/api/v1/courses/{id}` | Xóa mềm khóa học |
| DELETE | `/course/api/v1/courses` | `/api/v1/courses` | Xóa mềm nhiều khóa học |
| POST | `/course/api/v1/courses/{id}/submit-review` | `/api/v1/courses/{id}/submit-review` | Gửi khóa học chờ duyệt |
| POST | `/course/api/v1/courses/{id}/approve` | `/api/v1/courses/{id}/approve` | Duyệt khóa học |
| POST | `/course/api/v1/courses/{id}/reject` | `/api/v1/courses/{id}/reject` | Từ chối khóa học kèm lý do |
| POST | `/course/api/v1/courses/{id}/archive` | `/api/v1/courses/{id}/archive` | Lưu trữ khóa học |
| POST | `/course/api/v1/courses/{id}/images` | `/api/v1/courses/{id}/images` | Upload ảnh cho khóa học |
| GET | `/course/api/v1/courses/{id}/images` | `/api/v1/courses/{id}/images` | Lấy danh sách ảnh của khóa học |
| GET | `/course/api/v1/courses/{id}/images/primary/view` | `/api/v1/courses/{id}/images/primary/view` | Xem ảnh chính của khóa học |

## Course Service - Course Category

| Method | Gateway API | Raw service API | Chức năng |
|---|---|---|---|
| GET | `/course/api/v1/course-categories` | `/api/v1/course-categories` | Danh sách danh mục khóa học |
| GET | `/course/api/v1/course-categories/{id}` | `/api/v1/course-categories/{id}` | Chi tiết danh mục |
| POST | `/course/api/v1/course-categories` | `/api/v1/course-categories` | Tạo danh mục |
| POST | `/course/api/v1/course-categories/{id}` | `/api/v1/course-categories/{id}` | Cập nhật danh mục |
| DELETE | `/course/api/v1/course-categories/{id}` | `/api/v1/course-categories/{id}` | Xóa mềm danh mục |
| DELETE | `/course/api/v1/course-categories` | `/api/v1/course-categories` | Xóa mềm nhiều danh mục |

## Course Service - Lesson

| Method | Gateway API | Raw service API | Chức năng |
|---|---|---|---|
| GET | `/course/api/v1/lessons` | `/api/v1/lessons` | Danh sách bài học |
| GET | `/course/api/v1/lessons/{id}` | `/api/v1/lessons/{id}` | Chi tiết bài học |
| POST | `/course/api/v1/lessons` | `/api/v1/lessons` | Tạo bài học |
| POST | `/course/api/v1/lessons/{id}` | `/api/v1/lessons/{id}` | Cập nhật bài học |
| DELETE | `/course/api/v1/lessons/{id}` | `/api/v1/lessons/{id}` | Xóa mềm bài học |
| DELETE | `/course/api/v1/lessons` | `/api/v1/lessons` | Xóa mềm nhiều bài học |
| GET | `/course/api/v1/courses/{courseId}/lessons` | `/api/v1/courses/{courseId}/lessons` | Lấy danh sách bài học theo khóa học |
| POST | `/course/api/v1/courses/{courseId}/lessons` | `/api/v1/courses/{courseId}/lessons` | Tạo bài học trong khóa học |
| POST | `/course/api/v1/lessons/{id}/resources` | `/api/v1/lessons/{id}/resources` | Upload tài nguyên cho bài học |

## Course Service - Lesson Resource

| Method | Gateway API | Raw service API | Chức năng |
|---|---|---|---|
| GET | `/course/api/v1/lesson-resources` | `/api/v1/lesson-resources` | Danh sách tài nguyên bài học |
| GET | `/course/api/v1/lesson-resources/{id}` | `/api/v1/lesson-resources/{id}` | Chi tiết tài nguyên |
| POST | `/course/api/v1/lesson-resources` | `/api/v1/lesson-resources` | Tạo metadata tài nguyên |
| POST | `/course/api/v1/lesson-resources/{id}` | `/api/v1/lesson-resources/{id}` | Cập nhật tài nguyên |
| DELETE | `/course/api/v1/lesson-resources/{id}` | `/api/v1/lesson-resources/{id}` | Xóa mềm tài nguyên |
| DELETE | `/course/api/v1/lesson-resources` | `/api/v1/lesson-resources` | Xóa mềm nhiều tài nguyên |
| GET | `/course/api/v1/lesson-resources/{id}/view` | `/api/v1/lesson-resources/{id}/view` | Xem nội dung tài nguyên |
| GET | `/course/api/v1/lesson-resources/{id}/download` | `/api/v1/lesson-resources/{id}/download` | Tải tài nguyên |

## Course Service - Image

| Method | Gateway API | Raw service API | Chức năng |
|---|---|---|---|
| GET | `/course/api/v1/images` | `/api/v1/images` | Danh sách ảnh/metadata ảnh |
| GET | `/course/api/v1/images/{id}` | `/api/v1/images/{id}` | Chi tiết ảnh |
| POST | `/course/api/v1/images` | `/api/v1/images` | Tạo metadata ảnh |
| POST | `/course/api/v1/images/{id}` | `/api/v1/images/{id}` | Cập nhật metadata ảnh |
| DELETE | `/course/api/v1/images/{id}` | `/api/v1/images/{id}` | Xóa mềm ảnh |
| DELETE | `/course/api/v1/images` | `/api/v1/images` | Xóa mềm nhiều ảnh |
| GET | `/course/api/v1/images/{id}/view` | `/api/v1/images/{id}/view` | Xem ảnh inline theo imageId |
| GET | `/course/api/v1/images/{id}/download` | `/api/v1/images/{id}/download` | Tải ảnh theo imageId |

### Course Internal API

| Method | Internal API | Chức năng |
|---|---|---|
| POST | `/internal/v1/courses/{id}/check` | Kiểm tra khóa học tồn tại |
| GET | `/internal/v1/courses/{id}/published` | Kiểm tra khóa học tồn tại và đang `PUBLISHED` |
| POST | `/internal/v1/lesson/{id}/check` | Kiểm tra bài học tồn tại |
| GET | `/internal/v1/lesson/{id}/course` | Tìm courseId theo lessonId |
| GET | `/internal/v1/lesson/course/{id}` | Lấy danh sách lesson theo courseId |
| GET | `/internal/v1/lesson/count/{id}` | Đếm số bài học trong khóa học |

## Learning Service - Enrollment

Gateway prefix: `/learning`.

| Method | Gateway API | Raw service API | Chức năng |
|---|---|---|---|
| POST | `/learning/api/v1/courses/{courseId}/enroll` | `/api/v1/courses/{courseId}/enroll` | User hiện tại đăng ký khóa học |
| GET | `/learning/api/v1/my-courses` | `/api/v1/my-courses` | Lấy danh sách khóa học đã đăng ký của user hiện tại |
| POST | `/learning/api/v1/courses/{courseId}/complete` | `/api/v1/courses/{courseId}/complete` | Hoàn thành khóa học, kiểm tra điều kiện và sinh chứng chỉ |

## Learning Service - Learning Progress

| Method | Gateway API | Raw service API | Chức năng |
|---|---|---|---|
| POST | `/learning/api/v1/lessons/{lessonId}/start` | `/api/v1/lessons/{lessonId}/start` | Bắt đầu bài học, progress chuyển sang `IN_PROGRESS` |
| POST | `/learning/api/v1/lessons/{lessonId}/complete` | `/api/v1/lessons/{lessonId}/complete` | Hoàn thành bài học, progress chuyển sang `COMPLETED` |

## Learning Service - Certificate

| Method | Gateway API | Raw service API | Chức năng |
|---|---|---|---|
| GET | `/learning/api/v1/my-certificates` | `/api/v1/my-certificates` | Lấy chứng chỉ của user hiện tại |
| GET | `/learning/api/v1/certificates` | `/api/v1/certificates` | Admin lấy toàn bộ chứng chỉ trong hệ thống |
| GET | `/learning/api/v1/certificates/{code}` | `/api/v1/certificates/{code}` | Tra cứu/xác minh chứng chỉ theo certificate code |

### Learning Internal API

| Method | Internal API | Chức năng |
|---|---|---|
| GET | `/internal/v1/enrollment/{courseId}` | Quiz-service kiểm tra user hiện tại đã enroll course chưa |

## Quiz Service - Quiz

Gateway prefix: `/quiz`.

| Method | Gateway API | Raw service API | Chức năng |
|---|---|---|---|
| GET | `/quiz/api/v1/quiz` | `/api/v1/quiz` | Danh sách quiz |
| GET | `/quiz/api/v1/quiz/{id}` | `/api/v1/quiz/{id}` | Chi tiết quiz |
| POST | `/quiz/api/v1/quiz` | `/api/v1/quiz` | Tạo quiz |
| POST | `/quiz/api/v1/quiz/{id}` | `/api/v1/quiz/{id}` | Cập nhật quiz |
| DELETE | `/quiz/api/v1/quiz/{id}` | `/api/v1/quiz/{id}` | Xóa mềm quiz |
| DELETE | `/quiz/api/v1/quiz` | `/api/v1/quiz` | Xóa mềm nhiều quiz |

## Quiz Service - Question

| Method | Gateway API | Raw service API | Chức năng |
|---|---|---|---|
| GET | `/quiz/api/v1/questions` | `/api/v1/questions` | Danh sách câu hỏi |
| GET | `/quiz/api/v1/questions/{id}` | `/api/v1/questions/{id}` | Chi tiết câu hỏi |
| POST | `/quiz/api/v1/questions` | `/api/v1/questions` | Tạo câu hỏi |
| POST | `/quiz/api/v1/questions/quizzes/{id}` | `/api/v1/questions/quizzes/{id}` | Tạo câu hỏi cho quiz |
| POST | `/quiz/api/v1/questions/{id}` | `/api/v1/questions/{id}` | Cập nhật câu hỏi |
| DELETE | `/quiz/api/v1/questions/{id}` | `/api/v1/questions/{id}` | Xóa mềm câu hỏi |
| DELETE | `/quiz/api/v1/questions` | `/api/v1/questions` | Xóa mềm nhiều câu hỏi |

## Quiz Service - Answer

| Method | Gateway API | Raw service API | Chức năng |
|---|---|---|---|
| GET | `/quiz/api/v1/answers` | `/api/v1/answers` | Danh sách đáp án |
| GET | `/quiz/api/v1/answers/{id}` | `/api/v1/answers/{id}` | Chi tiết đáp án |
| POST | `/quiz/api/v1/answers` | `/api/v1/answers` | Tạo đáp án |
| POST | `/quiz/api/v1/answers/questions/{id}` | `/api/v1/answers/questions/{id}` | Tạo đáp án cho câu hỏi |
| POST | `/quiz/api/v1/answers/{id}` | `/api/v1/answers/{id}` | Cập nhật đáp án |
| DELETE | `/quiz/api/v1/answers/{id}` | `/api/v1/answers/{id}` | Xóa mềm đáp án |
| DELETE | `/quiz/api/v1/answers` | `/api/v1/answers` | Xóa mềm nhiều đáp án |

## Quiz Service - Quiz Attempt

| Method | Gateway API | Raw service API | Chức năng |
|---|---|---|---|
| POST | `/quiz/api/v1/quizzes/{id}/attempts` | `/api/v1/quizzes/{id}/attempts` | User hiện tại bắt đầu làm quiz |
| POST | `/quiz/api/v1/quiz-attempts/{id}/submit` | `/api/v1/quiz-attempts/{id}/submit` | User hiện tại nộp bài quiz |
| GET | `/quiz/api/v1/quizzes/{id}/attempts/me` | `/api/v1/quizzes/{id}/attempts/me` | User hiện tại xem lịch sử điểm từng lần làm quiz |

### Quiz Internal API

| Method | Internal API | Chức năng |
|---|---|---|
| GET | `/internal/v1/quizzes/course/{courseId}/required-result` | Learning-service kiểm tra user hiện tại đã pass toàn bộ quiz bắt buộc của course chưa |

## Notice Service

Gateway prefix: `/notice`.

## Notice Service - Device

| Method | Gateway API | Raw service API | Chức năng |
|---|---|---|---|
| POST | `/notice/api/v1/devices/register` | `/api/v1/devices/register` | User hiện tại đăng ký Firebase Installation ID (`installationId`; vẫn nhận alias cũ `token`) |
| POST | `/notice/api/v1/devices/deactivate` | `/api/v1/devices/deactivate` | User hiện tại tắt Firebase Installation ID của thiết bị |

## Notice Service - User Notice

| Method | Gateway API | Raw service API | Chức năng |
|---|---|---|---|
| GET | `/notice/api/v1/notices/me` | `/api/v1/notices/me` | Lấy danh sách thông báo của user hiện tại |
| GET | `/notice/api/v1/notices/me/unread-count` | `/api/v1/notices/me/unread-count` | Đếm thông báo chưa đọc của user hiện tại |
| POST | `/notice/api/v1/notices/{recipientId}/read` | `/api/v1/notices/{recipientId}/read` | Đánh dấu một thông báo đã đọc |
| POST | `/notice/api/v1/notices/read-all` | `/api/v1/notices/read-all` | Đánh dấu toàn bộ thông báo đã đọc |

### Notice Internal API

| Method | Internal API | Chức năng |
|---|---|---|
| GET | `/internal/v1/devices/users/{userId}/active` | Lấy danh sách thiết bị active của một user |
| POST | `/internal/v1/notices/send-user` | Gửi thông báo cho một user |
| POST | `/internal/v1/notices/send-users` | Gửi thông báo cho nhiều user |
| POST | `/internal/v1/notices/send-role` | Gửi thông báo cho các user thuộc một role |
| POST | `/internal/v1/notices/send-all` | Gửi thông báo cho tất cả user đang có thiết bị active |
