# LMS Mini - Kế Hoạch Triển Khai Backend

## 1. Tổng Quan Đề Tài

Tên đề tài: **LMS Mini - Hệ thống quản lý khóa học mini**

Mục tiêu: xây dựng backend microservice cho hệ thống học trực tuyến mini, ưu tiên web app. Firebase được dùng cho **Web Push Notification** trên trình duyệt; mobile có thể mở rộng sau.

Hệ thống tập trung vào các nghiệp vụ:

- Quản lý người dùng và vai trò
- Quản lý khóa học
- Quản lý bài học
- Quản lý tài liệu bài học
- Đăng ký khóa học
- Theo dõi tiến độ học
- Làm quiz
- Cấp chứng chỉ
- Gửi thông báo trong hệ thống và Web Push Notification qua Firebase
- Upload ảnh/tài liệu qua MinIO

Không làm trong MVP:

- Video streaming phức tạp
- Payment thật
- Chat realtime
- Certificate PDF đẹp
- Recommendation/AI

## 2. Định Hướng Kiến Trúc

Hệ thống sử dụng kiến trúc microservice dựa trên base project hiện có.

Các service nên có:

- `api-gateway`
- `base-service`
- `authn-service`
- `author-service`
- `course-service`
- `learning-service`
- `quiz-service`
- `certificate-service`
- `notice-service`

Có thể gộp bớt trong MVP:

- `certificate-service` có thể nằm trong `learning-service`
- Upload image/resource có thể nằm trong `course-service`
- `notice-service` nên tách riêng vì có Firebase Web Push và in-app notification

## 3. Các Thành Phần Dùng Từ Base Project

Từ `base-service`:

- `BaseEntity`: audit fields và soft delete
- `BaseDto`: DTO base
- `BaseResponse`: response format chung
- `BaseController`: CRUD cơ bản
- `BaseService`: CRUD service cơ bản
- `BaseRepository`: repository chung
- `BaseSpecification`: filter/search bằng query params
- `GlobalExceptionHandler`: xử lý exception
- `i18n`: message đa ngôn ngữ
- `MinIOService`: upload/download file
- `SwaggerConfig`: Swagger UI
- `FeignConfig`: gọi service khác

Từ `api-gateway`:

- Route request sang từng service
- Validate JWT tập trung
- Gắn header `X-User`, `X-Role-Code`, `X-User-Info`
- Swagger UI gateway
- Consul discovery

## 4. Quy Tắc Database Chung

Tất cả bảng nghiệp vụ nên có các trường base:

```sql
id VARCHAR(36) PRIMARY KEY,
version BIGINT NOT NULL DEFAULT 0,
created_by VARCHAR(50),
created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
last_modified_by VARCHAR(50),
last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP,
deleted_at DATETIME NULL
```

Lưu ý:

- Không dùng `updated_by` nếu base đang dùng `last_modified_by`.
- Xóa dữ liệu bằng soft delete qua `deleted_at`.
- Các bảng master data nên có `code` và `status`.
- Các status nên thống nhất bằng enum/constant.

## 5. Danh Sách Role

Hệ thống có 3 role chính:

- `ADMIN`: quản trị hệ thống, duyệt khóa học, quản lý user/role
- `INSTRUCTOR`: tạo khóa học, bài học, quiz
- `STUDENT`: đăng ký học, học bài, làm quiz, nhận chứng chỉ


## 6. Service Nào Chứa Những Bảng Nào

Mỗi service nên quản lý trực tiếp database riêng của nó. Service khác nếu cần dữ liệu thì gọi qua API/Feign, không đọc thẳng DB của nhau.

| Service | Database đề xuất | Bảng quản lý | Ý nghĩa chính |
|---|---|---|---|
| `authn-service` | `lms_authn` | `tbl_users`, `tbl_refresh_tokens` | Quản lý tài khoản đăng nhập, mật khẩu, access token, refresh token, khóa tài khoản |
| `author-service` | `lms_author` | `tbl_roles`, `tbl_user_roles` | Quản lý vai trò và gán vai trò cho user |
| `course-service` | `lms_course` | `tbl_course_categories`, `tbl_courses`, `tbl_lessons`, `tbl_lesson_resources`, `tbl_images` | Quản lý danh mục khóa học, khóa học, bài học, tài liệu, ảnh/file |
| `learning-service` | `lms_learning` | `tbl_enrollments`, `tbl_learning_progress`, `tbl_certificates` | Quản lý đăng ký học, tiến độ học, chứng chỉ |
| `quiz-service` | `lms_quiz` | `tbl_quizzes`, `tbl_questions`, `tbl_answers`, `tbl_quiz_attempts`, `tbl_quiz_attempt_answers` | Quản lý quiz, câu hỏi, đáp án, lượt làm bài và kết quả |
| `notice-service` | `lms_notice` | `tbl_user_devices`, `tbl_notices`, `tbl_notice_recipients`, `tbl_notice_delivery_logs` | Quản lý thông báo trong web, token Firebase, lịch sử gửi thông báo |
| `api-gateway` | Không cần DB trong MVP | Không quản lý bảng | Nhận request từ frontend, validate JWT, route sang service đúng |
| `base-service` | Không cần DB riêng | Không quản lý bảng nghiệp vụ | Chứa class dùng chung như `BaseEntity`, `BaseResponse`, exception, i18n, MinIO, config |

Ghi chú quan trọng:

- `user_id`, `course_id`, `lesson_id`, `quiz_id` là ID tham chiếu sang dữ liệu của service khác hoặc bảng khác.
- Trong microservice, các ID này không nhất thiết phải tạo foreign key vật lý giữa database khác nhau.
- Ví dụ `learning-service.tbl_enrollments.course_id` lưu ID khóa học, nhưng thông tin chi tiết khóa học nên lấy bằng cách gọi `course-service` qua Feign.
- `certificate-service` chưa cần tách trong MVP. Bảng `tbl_certificates` để trong `learning-service` là hợp lý hơn.

## 7. Giải Thích Tên Trường Tiếng Anh Trong Database

Phần này giải thích các tên cột tiếng Anh thường gặp. Khi đọc entity hoặc SQL, có thể hiểu theo bảng dưới đây.

### 7.1. Nhóm trường dùng chung từ `BaseEntity`

| Trường | Nghĩa tiếng Việt | Dùng để làm gì |
|---|---|---|
| `id` | Mã định danh | Khóa chính của bản ghi, thường là UUID |
| `version` | Phiên bản bản ghi | Dùng cho optimistic locking, tránh 2 người sửa cùng lúc làm mất dữ liệu |
| `created_by` | Người tạo | Lưu username/userId của người tạo bản ghi |
| `created_date` | Ngày tạo | Thời điểm tạo bản ghi |
| `last_modified_by` | Người cập nhật gần nhất | Lưu username/userId của người sửa bản ghi gần nhất |
| `last_modified_date` | Ngày cập nhật gần nhất | Thời điểm sửa bản ghi gần nhất |
| `deleted_at` | Thời điểm xóa mềm | Có giá trị nghĩa là bản ghi đã bị xóa mềm, không xóa thật khỏi DB |

### 7.2. Nhóm trường tài khoản và bảo mật

| Trường | Nghĩa tiếng Việt | Dùng để làm gì |
|---|---|---|
| `username` | Tên đăng nhập | Người dùng nhập khi login |
| `password_hash` | Mật khẩu đã mã hóa | Không lưu mật khẩu thật, chỉ lưu hash |
| `email` | Email | Email của user |
| `phone` | Số điện thoại | Số điện thoại của user |
| `full_name` | Họ tên đầy đủ | Tên hiển thị của user |
| `language` | Ngôn ngữ | Ví dụ `vi`, `en`; dùng để trả message theo ngôn ngữ user |
| `status` | Trạng thái | Trạng thái bản ghi, ví dụ `ACTIVE`, `INACTIVE`, `LOCKED` |
| `current_access_token_id` | Mã access token hiện tại | Lưu `jti` của access token mới nhất, dùng khi muốn vô hiệu hóa token cũ |
| `access_token_expired_at` | Thời điểm access token hết hạn | Biết token hiện tại hết hạn lúc nào |
| `failed_login_attempts` | Số lần đăng nhập sai | Tăng lên khi nhập sai mật khẩu |
| `account_locked_until` | Khóa tài khoản đến thời điểm nào | Nếu user nhập sai nhiều lần thì khóa tạm đến thời điểm này |
| `last_login_at` | Lần đăng nhập gần nhất | Ghi lại thời điểm user login thành công gần nhất |
| `password_change_at` | Lần đổi mật khẩu gần nhất | Dùng để kiểm tra token cũ có còn hợp lệ sau khi đổi mật khẩu không |
| `token_id` | Mã định danh token | ID riêng của refresh token, không phải chuỗi token thật |
| `refresh_token_hash` | Refresh token đã mã hóa/hash | Lưu hash để nếu DB lộ thì không lộ token thật |
| `device_id` | Mã thiết bị/trình duyệt | Phân biệt các phiên đăng nhập trên từng thiết bị |
| `user_agent` | Thông tin trình duyệt/thiết bị | Ví dụ Chrome, Safari, hệ điều hành; dùng để audit bảo mật |
| `ip_address` | Địa chỉ IP | IP lúc login/refresh token |
| `issued_at` | Thời điểm phát hành | Token được tạo lúc nào |
| `expired_at` | Thời điểm hết hạn | Token hết hạn lúc nào |
| `revoked_at` | Thời điểm thu hồi | Token bị logout/thu hồi lúc nào |
| `replaced_by_token_id` | Token mới thay thế token cũ | Dùng trong refresh token rotation |

### 7.3. Nhóm trường role và phân quyền

| Trường | Nghĩa tiếng Việt | Dùng để làm gì |
|---|---|---|
| `name` | Tên | Tên hiển thị, ví dụ `Quản trị viên` |
| `code` | Mã code | Mã duy nhất để xử lý logic, ví dụ `ADMIN`, `STUDENT` |
| `description` | Mô tả | Mô tả thêm về role/danh mục/khóa học |
| `user_id` | ID người dùng | Tham chiếu tới user |
| `role_id` | ID vai trò | Tham chiếu tới role |

### 7.4. Nhóm trường khóa học và bài học

| Trường | Nghĩa tiếng Việt | Dùng để làm gì |
|---|---|---|
| `category_id` | ID danh mục | Khóa học thuộc danh mục nào |
| `instructor_id` | ID giảng viên | User tạo/giảng dạy khóa học |
| `title` | Tiêu đề | Tên bài học, tên thông báo, tên tài liệu tùy bảng |
| `content` | Nội dung | Nội dung bài học, câu hỏi hoặc thông báo |
| `level` | Cấp độ | Ví dụ `BEGINNER`, `INTERMEDIATE`, `ADVANCED` |
| `duration_minutes` | Thời lượng phút | Tổng số phút học hoặc thời lượng bài học |
| `price` | Giá | Giá khóa học, MVP có thể để 0 nếu chưa làm thanh toán |
| `reject_reason` | Lý do từ chối | Admin nhập khi từ chối duyệt khóa học |
| `published_at` | Thời điểm xuất bản | Khóa học được public lúc nào |
| `course_id` | ID khóa học | Tham chiếu tới khóa học |
| `lesson_id` | ID bài học | Tham chiếu tới bài học |
| `video_url` | Link video | Chỉ lưu URL video, không lưu file video trực tiếp trong MVP |
| `order_index` | Thứ tự sắp xếp | Dùng để sắp xếp bài học/câu hỏi/đáp án |
| `resource_type` | Loại tài nguyên | Ví dụ `PDF`, `DOCX`, `LINK` |
| `file_path` | Đường dẫn file | Đường dẫn file trong MinIO/storage |
| `external_url` | Link ngoài | Link tài liệu/video bên ngoài hệ thống |

### 7.5. Nhóm trường học tập và chứng chỉ

| Trường | Nghĩa tiếng Việt | Dùng để làm gì |
|---|---|---|
| `enrolled_at` | Thời điểm đăng ký học | Student đăng ký khóa học lúc nào |
| `completed_at` | Thời điểm hoàn thành | Hoàn thành khóa học/bài học lúc nào |
| `progress_percent` | Phần trăm tiến độ | Tiến độ học, ví dụ `65.50` nghĩa là 65.5% |
| `started_at` | Thời điểm bắt đầu | Bắt đầu học hoặc bắt đầu làm quiz lúc nào |
| `certificate_code` | Mã chứng chỉ | Mã dùng để tra cứu/xác minh chứng chỉ |
| `issued_at` | Thời điểm cấp | Chứng chỉ được cấp lúc nào |

### 7.6. Nhóm trường quiz

| Trường | Nghĩa tiếng Việt | Dùng để làm gì |
|---|---|---|
| `quiz_id` | ID quiz | Tham chiếu tới quiz |
| `question_id` | ID câu hỏi | Tham chiếu tới câu hỏi |
| `answer_id` | ID đáp án | Tham chiếu tới đáp án user chọn |
| `question_type` | Loại câu hỏi | Ví dụ một đáp án đúng hoặc nhiều đáp án đúng |
| `score` | Điểm | Điểm câu hỏi hoặc điểm bài làm |
| `pass_score` | Điểm đạt | Điểm tối thiểu để pass quiz |
| `max_attempts` | Số lần làm tối đa | Giới hạn số lần student được làm quiz |
| `required_to_complete` | Bắt buộc để hoàn thành | Nếu `true`, phải pass quiz mới hoàn thành khóa học |
| `attempt_id` | ID lượt làm bài | Tham chiếu tới một lần làm quiz |
| `passed` | Đã đạt hay chưa | `true` là đạt, `false` là chưa đạt |
| `submitted_at` | Thời điểm nộp bài | Student nộp quiz lúc nào |
| `is_correct` | Có đúng không | Đáp án đúng hay lựa chọn của user có đúng không |

### 7.7. Nhóm trường ảnh, file và MinIO

| Trường | Nghĩa tiếng Việt | Dùng để làm gì |
|---|---|---|
| `object_type` | Loại đối tượng | File/ảnh đang gắn với cái gì, ví dụ `COURSE`, `USER` |
| `object_id` | ID đối tượng | ID của course/user/lesson tương ứng |
| `file_name` | Tên file | Tên file gốc hoặc tên hiển thị |
| `file_url` | URL file | Link truy cập file nếu có |
| `content_type` | Kiểu nội dung file | Ví dụ `image/png`, `application/pdf` |
| `file_size` | Dung lượng file | Kích thước file tính bằng byte |
| `is_primary` | Có phải ảnh chính không | Ví dụ ảnh đại diện chính của khóa học |

### 7.8. Nhóm trường thông báo và Firebase

| Trường | Nghĩa tiếng Việt | Dùng để làm gì |
|---|---|---|
| `fcm_token` | Token Firebase Cloud Messaging | Token browser/device để Firebase gửi push notification |
| `app_version` | Phiên bản ứng dụng | Dùng khi có mobile app hoặc version web client |
| `last_active_at` | Lần hoạt động gần nhất | Thiết bị/user active lần cuối lúc nào |
| `notice_type` | Loại thông báo | Ví dụ khóa học được duyệt, chứng chỉ được cấp |
| `target_type` | Kiểu đối tượng nhận | Gửi cho một user, nhiều user, theo role hoặc tất cả |
| `data` | Dữ liệu phụ | JSON/text chứa metadata, ví dụ `courseId` để frontend điều hướng |
| `sent_at` | Thời điểm gửi | Thông báo được gửi lúc nào |
| `notice_id` | ID thông báo | Tham chiếu tới thông báo gốc |
| `recipient_id` | ID người nhận trong bảng recipient | Tham chiếu tới bản ghi nhận thông báo |
| `delivery_status` | Trạng thái gửi | `PENDING`, `SENT`, `FAILED` |
| `read_status` | Trạng thái đọc | `UNREAD`, `READ` |
| `read_at` | Thời điểm đọc | User đọc thông báo lúc nào |
| `failure_reason` | Lý do gửi thất bại | Mô tả lỗi khi gửi notification |
| `provider` | Nhà cung cấp gửi | Ví dụ `FIREBASE` |
| `provider_message_id` | ID message từ nhà cung cấp | Firebase trả về ID sau khi gửi thành công |
| `error_code` | Mã lỗi | Mã lỗi từ Firebase/backend |
| `error_message` | Nội dung lỗi | Chi tiết lỗi gửi notification |

## 7A. Danh Sách Enum Cho Các Trường Dữ Liệu

Quy ước:

- Các field enum trong database vẫn lưu dạng `VARCHAR`, nhưng trong Java nên tạo `enum` hoặc `constant` để tránh nhập sai text.
- API request nên validate enum ở DTO/service trước khi lưu DB.
- Giá trị enum nên dùng chữ in hoa và dấu gạch dưới, ví dụ `PENDING_REVIEW`.

### 7A.1. Enum dùng chung

| Enum | Giá trị | Dùng cho | Ghi chú |
|---|---|---|---|
| `CommonStatus` | `ACTIVE`, `INACTIVE` | Các bảng master data đơn giản | Dùng cho role, category, lesson resource, image nếu chưa cần trạng thái phức tạp |
| `Language` | `VI`, `EN`, `LO` | `tbl_users.language` | Đang có trong `base-service` |
| `RoleCode` | `ADMIN`, `INSTRUCTOR`, `STUDENT` | `tbl_roles.code` | Đây là role nghiệp vụ chính của hệ thống |
| `MinIOSupportType` | `IMAGE`, `FILE` | Luồng upload file/ảnh | `IMAGE` hỗ trợ `.jpg`, `.jpeg`, `.png`; `FILE` hỗ trợ `.pdf`, `.docx` |

### 7A.2. `authn-service`

| Bảng | Trường | Enum | Giá trị đề xuất | Giá trị mặc định |
|---|---|---|---|---|
| `tbl_users` | `language` | `Language` | `VI`, `EN`, `LO` | `VI` |
| `tbl_users` | `status` | `UserStatus` | `ACTIVE`, `INACTIVE`, `LOCKED`, `DELETED` | `ACTIVE` |
| `tbl_refresh_tokens` | `status` | `RefreshTokenStatus` | `ACTIVE`, `REVOKED`, `EXPIRED`, `ROTATED` | `ACTIVE` |

Ý nghĩa nhanh:

- `ACTIVE`: user/token đang dùng được.
- `INACTIVE`: user bị tạm ngưng nhưng chưa xóa.
- `LOCKED`: user bị khóa do nghiệp vụ hoặc đăng nhập sai nhiều lần.
- `DELETED`: user đã xóa mềm.
- `REVOKED`: refresh token bị thu hồi, thường do logout.
- `EXPIRED`: refresh token đã hết hạn.
- `ROTATED`: refresh token cũ đã được thay bằng token mới.

### 7A.3. `author-service`

| Bảng | Trường | Enum | Giá trị đề xuất | Giá trị mặc định |
|---|---|---|---|---|
| `tbl_roles` | `code` | `RoleCode` | `ADMIN`, `INSTRUCTOR`, `STUDENT` | Không có |
| `tbl_roles` | `status` | `RoleStatus` | `ACTIVE`, `INACTIVE` | `ACTIVE` |

Ý nghĩa nhanh:

- `ADMIN`: quản trị hệ thống, duyệt khóa học, quản lý user/role.
- `INSTRUCTOR`: tạo khóa học, bài học, quiz.
- `STUDENT`: đăng ký học, học bài, làm quiz, nhận chứng chỉ.

### 7A.4. `course-service`

| Bảng | Trường | Enum | Giá trị đề xuất | Giá trị mặc định |
|---|---|---|---|---|
| `tbl_course_categories` | `status` | `CourseCategoryStatus` | `ACTIVE`, `INACTIVE` | `ACTIVE` |
| `tbl_courses` | `level` | `CourseLevel` | `BEGINNER`, `INTERMEDIATE`, `ADVANCED` | `BEGINNER` |
| `tbl_courses` | `status` | `CourseStatus` | `DRAFT`, `PENDING_REVIEW`, `PUBLISHED`, `REJECTED`, `ARCHIVED` | `DRAFT` |
| `tbl_lessons` | `status` | `LessonStatus` | `DRAFT`, `ACTIVE`, `INACTIVE`, `ARCHIVED` | `DRAFT` |
| `tbl_lesson_resources` | `resource_type` | `LessonResourceType` | `PDF`, `DOCX`, `LINK`, `VIDEO`, `IMAGE` | Không có |
| `tbl_lesson_resources` | `status` | `LessonResourceStatus` | `ACTIVE`, `INACTIVE` | `ACTIVE` |
| `tbl_images` | `object_type` | `ImageObjectType` | `COURSE`, `LESSON`, `USER`, `CERTIFICATE` | Không có |
| `tbl_images` | `status` | `ImageStatus` | `ACTIVE`, `INACTIVE` | `ACTIVE` |

Ý nghĩa nhanh:

- `DRAFT`: bản nháp, instructor còn chỉnh sửa.
- `PENDING_REVIEW`: instructor gửi admin duyệt.
- `PUBLISHED`: đã được duyệt và student có thể xem/enroll.
- `REJECTED`: admin từ chối, cần lưu `reject_reason`.
- `ARCHIVED`: ngừng hiển thị nhưng vẫn giữ lịch sử.
- `content_type` của `tbl_images` không nên khóa enum cứng vì đây là MIME type, ví dụ `image/png`, `application/pdf`.

### 7A.5. `learning-service`

| Bảng | Trường | Enum | Giá trị đề xuất | Giá trị mặc định |
|---|---|---|---|---|
| `tbl_enrollments` | `status` | `EnrollmentStatus` | `ACTIVE`, `COMPLETED`, `CANCELLED` | `ACTIVE` |
| `tbl_learning_progress` | `status` | `LearningProgressStatus` | `NOT_STARTED`, `IN_PROGRESS`, `COMPLETED` | `NOT_STARTED` |
| `tbl_certificates` | `status` | `CertificateStatus` | `ISSUED`, `REVOKED` | `ISSUED` |

Ý nghĩa nhanh:

- `ACTIVE`: student đang học course.
- `COMPLETED`: student đã hoàn thành course.
- `CANCELLED`: student hủy hoặc bị hủy enrollment.
- `NOT_STARTED`: lesson chưa bắt đầu.
- `IN_PROGRESS`: lesson đang học.
- `ISSUED`: certificate đã được cấp.
- `REVOKED`: certificate bị thu hồi.

### 7A.6. `quiz-service`

| Bảng | Trường | Enum | Giá trị đề xuất | Giá trị mặc định |
|---|---|---|---|---|
| `tbl_quizzes` | `status` | `QuizStatus` | `DRAFT`, `ACTIVE`, `INACTIVE`, `ARCHIVED` | `DRAFT` |
| `tbl_questions` | `question_type` | `QuestionType` | `SINGLE_CHOICE`, `MULTIPLE_CHOICE` | `SINGLE_CHOICE` |
| `tbl_quiz_attempts` | `status` | `QuizAttemptStatus` | `IN_PROGRESS`, `SUBMITTED`, `CANCELLED` | `IN_PROGRESS` |

Ý nghĩa nhanh:

- `DRAFT`: quiz đang soạn.
- `ACTIVE`: quiz có thể làm.
- `INACTIVE`: quiz tạm ẩn.
- `ARCHIVED`: quiz ngừng dùng nhưng giữ lịch sử.
- `SINGLE_CHOICE`: một câu hỏi chỉ có một đáp án đúng.
- `MULTIPLE_CHOICE`: một câu hỏi có thể có nhiều đáp án đúng.
- `SUBMITTED`: student đã nộp bài, điểm được tính theo `score` và `passed`.

### 7A.7. `notice-service`

| Bảng | Trường | Enum | Giá trị đề xuất | Giá trị mặc định |
|---|---|---|---|---|
| `tbl_user_devices` | `device_type` | `DeviceType` | `WEB`, `ANDROID`, `IOS` | `WEB` |
| `tbl_user_devices` | `status` | `UserDeviceStatus` | `ACTIVE`, `INACTIVE`, `INVALID` | `ACTIVE` |
| `tbl_notices` | `notice_type` | `NoticeType` | `COURSE_SUBMITTED`, `COURSE_APPROVED`, `COURSE_REJECTED`, `COURSE_PUBLISHED`, `ENROLLMENT_SUCCESS`, `COURSE_COMPLETED`, `CERTIFICATE_ISSUED`, `SYSTEM` | `SYSTEM` |
| `tbl_notices` | `target_type` | `NoticeTargetType` | `USER`, `USERS`, `ROLE`, `ALL` | Không có |
| `tbl_notices` | `status` | `NoticeStatus` | `DRAFT`, `SENDING`, `SENT`, `FAILED`, `CANCELLED` | `DRAFT` |
| `tbl_notice_recipients` | `delivery_status` | `NoticeDeliveryStatus` | `PENDING`, `SENT`, `FAILED` | `PENDING` |
| `tbl_notice_recipients` | `read_status` | `NoticeReadStatus` | `UNREAD`, `READ` | `UNREAD` |
| `tbl_notice_delivery_logs` | `provider` | `NoticeProvider` | `FIREBASE` | `FIREBASE` |
| `tbl_notice_delivery_logs` | `status` | `NoticeDeliveryLogStatus` | `PENDING`, `SENT`, `FAILED` | `PENDING` |

Ý nghĩa nhanh:

- `WEB`, `ANDROID`, `IOS`: loại thiết bị nhận push notification.
- `INVALID`: FCM token không còn hợp lệ, không gửi tiếp cho token đó.
- `USER`: gửi cho một user.
- `USERS`: gửi cho danh sách user.
- `ROLE`: gửi cho tất cả user thuộc một role.
- `ALL`: gửi toàn hệ thống.
- `SENDING`: notice đang được xử lý gửi.
- `SENT`: gửi xong ở mức notice hoặc recipient/log.
- `FAILED`: gửi lỗi, xem thêm `failure_reason`, `error_code`, `error_message`.
- `UNREAD`: user chưa đọc notice.
- `READ`: user đã đọc notice.

## 8. Thiết Kế Database

### 8.1. Bảng `tbl_users`

Mục đích: lưu thông tin người dùng, trạng thái đăng nhập, ngôn ngữ và bảo mật tài khoản.

```sql
CREATE TABLE tbl_users (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    username VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(30),
    full_name VARCHAR(255),
    language VARCHAR(10) DEFAULT 'vi',
    status VARCHAR(30) NOT NULL,

    current_access_token_id VARCHAR(100),
    access_token_expired_at DATETIME NULL,
    failed_login_attempts INT DEFAULT 0,
    account_locked_until DATETIME NULL,
    last_login_at DATETIME NULL,
    password_change_at DATETIME NULL
);
```

Nghiệp vụ:

- Đăng nhập thành công thì cấp access token, refresh token, cập nhật `current_access_token_id`, `access_token_expired_at`, `last_login_at`.
- Đăng nhập sai thì tăng `failed_login_attempts`.
- Sai quá giới hạn thì khóa tạm bằng `account_locked_until`.
- Logout thì revoke refresh token và có thể set `current_access_token_id = null` nếu muốn access token hết hiệu lực ngay.
- User có ngôn ngữ riêng qua `language`.

Ghi chú:

- Không nên lưu full access token trong DB nếu không cần thiết.
- Access token nên sống ngắn, ví dụ 15-30 phút.
- `current_access_token_id` lưu `jti` của access token hiện tại, dùng khi muốn kiểm tra/revoke access token ngay lúc logout.

### 8.2. Bảng `tbl_refresh_tokens`

Mục đích: lưu refresh token để cấp access token mới khi access token hết hạn.

```sql
CREATE TABLE tbl_refresh_tokens (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    user_id VARCHAR(36) NOT NULL,
    token_id VARCHAR(100) NOT NULL,
    refresh_token_hash VARCHAR(255) NOT NULL,
    device_id VARCHAR(255),
    user_agent VARCHAR(1000),
    ip_address VARCHAR(100),
    issued_at DATETIME NOT NULL,
    expired_at DATETIME NOT NULL,
    revoked_at DATETIME NULL,
    replaced_by_token_id VARCHAR(100),
    status VARCHAR(30) NOT NULL
);
```

Status:

- `ACTIVE`
- `REVOKED`
- `EXPIRED`
- `ROTATED`

Nghiệp vụ:

- Không lưu refresh token plain text, chỉ lưu hash.
- Login tạo một refresh token mới, có `token_id` riêng.
- Refresh token hợp lệ thì cấp access token mới.
- Nên dùng refresh token rotation: mỗi lần refresh thành công thì cấp refresh token mới và chuyển refresh token cũ sang `ROTATED`.
- Logout thì cập nhật `revoked_at` và `status = REVOKED`.
- Nếu muốn cho phép nhiều thiết bị đăng nhập cùng lúc, mỗi thiết bị có một refresh token riêng.
- Nếu chỉ cho phép một phiên đăng nhập, login mới sẽ revoke các refresh token cũ của user.
- Nếu refresh token đã `ROTATED` hoặc `REVOKED` nhưng vẫn bị dùng lại, coi đó là dấu hiệu token bị lộ và có thể revoke toàn bộ phiên của user/device.

### 8.3. Bảng `tbl_roles`

```sql
CREATE TABLE tbl_roles (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    name VARCHAR(255) NOT NULL,
    code VARCHAR(100) NOT NULL,
    description TEXT,
    status VARCHAR(30) NOT NULL
);
```

Nghiệp vụ:

- Tạo/sửa/xóa mềm role.
- Không cho trùng `code`.
- Filter theo `name`, `code`, `status`.

### 8.4. Bảng `tbl_user_roles`

```sql
CREATE TABLE tbl_user_roles (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    user_id VARCHAR(36) NOT NULL,
    role_id VARCHAR(36) NOT NULL
);
```

Nghiệp vụ:

- Gán role cho user.
- Gỡ role khỏi user.
- Không gán trùng role cho cùng user.

### 8.5. Bảng `tbl_course_categories`

```sql
CREATE TABLE tbl_course_categories (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    name VARCHAR(255) NOT NULL,
    code VARCHAR(100) NOT NULL,
    description TEXT,
    status VARCHAR(30) NOT NULL
);
```

Nghiệp vụ:

- CRUD danh mục khóa học.
- Không cho trùng `code`.
- Filter/search theo `name`, `code`, `status`.

### 8.6. Bảng `tbl_courses`

```sql
CREATE TABLE tbl_courses (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    category_id VARCHAR(36) NOT NULL,
    instructor_id VARCHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(100) NOT NULL,
    description TEXT,
    level VARCHAR(30),
    duration_minutes INT DEFAULT 0,
    price DECIMAL(18,2) DEFAULT 0,
    status VARCHAR(30) NOT NULL,
    reject_reason TEXT,
    published_at DATETIME NULL
);
```

Status khóa học:

- `DRAFT`
- `PENDING_REVIEW`
- `PUBLISHED`
- `REJECTED`
- `ARCHIVED`

Nghiệp vụ:

- Instructor tạo khóa học ở `DRAFT`.
- Instructor submit khóa học sang `PENDING_REVIEW`.
- Admin duyệt thành `PUBLISHED`.
- Admin từ chối thành `REJECTED` và nhập `reject_reason`.
- Student chỉ thấy/đăng ký khóa học `PUBLISHED`.
- Course đã publish không xóa cứng, chỉ archived hoặc soft delete.

### 8.7. Bảng `tbl_lessons`

```sql
CREATE TABLE tbl_lessons (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    course_id VARCHAR(36) NOT NULL,
    title VARCHAR(255) NOT NULL,
    code VARCHAR(100),
    content TEXT,
    video_url VARCHAR(1000),
    order_index INT NOT NULL,
    duration_minutes INT DEFAULT 0,
    status VARCHAR(30) NOT NULL
);
```

Nghiệp vụ:

- Instructor tạo/sửa/xóa bài học.
- Bài học thuộc một khóa học.
- `order_index` dùng để sắp xếp bài học.
- `video_url` chỉ lưu link video, không làm streaming trong MVP.

### 8.8. Bảng `tbl_lesson_resources`

```sql
CREATE TABLE tbl_lesson_resources (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    lesson_id VARCHAR(36) NOT NULL,
    title VARCHAR(255) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    file_path VARCHAR(1000),
    external_url VARCHAR(1000),
    status VARCHAR(30) NOT NULL
);
```

Nghiệp vụ:

- Upload tài liệu bài học lên MinIO.
- Hỗ trợ PDF/DOCX hoặc link ngoài.
- Lưu metadata tài liệu.

### 8.9. Bảng `tbl_enrollments`

```sql
CREATE TABLE tbl_enrollments (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    user_id VARCHAR(36) NOT NULL,
    course_id VARCHAR(36) NOT NULL,
    enrolled_at DATETIME NOT NULL,
    completed_at DATETIME NULL,
    progress_percent DECIMAL(5,2) DEFAULT 0,
    status VARCHAR(30) NOT NULL
);
```

Status enrollment:

- `ACTIVE`
- `COMPLETED`
- `CANCELLED`

Nghiệp vụ:

- Student enroll khóa học đã publish.
- Không cho enroll trùng khóa học đang active/completed.
- Cập nhật tiến độ học.
- Hoàn thành khóa học khi xong lesson và pass quiz bắt buộc.

### 8.10. Bảng `tbl_learning_progress`

```sql
CREATE TABLE tbl_learning_progress (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    enrollment_id VARCHAR(36) NOT NULL,
    lesson_id VARCHAR(36) NOT NULL,
    started_at DATETIME NULL,
    completed_at DATETIME NULL,
    status VARCHAR(30) NOT NULL
);
```

Status progress:

- `NOT_STARTED`
- `IN_PROGRESS`
- `COMPLETED`

Nghiệp vụ:

- Tạo progress khi student bắt đầu học.
- Mark lesson completed.
- Tính lại `progress_percent` trong enrollment.

### 8.11. Bảng `tbl_quizzes`

```sql
CREATE TABLE tbl_quizzes (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    course_id VARCHAR(36),
    lesson_id VARCHAR(36),
    title VARCHAR(255) NOT NULL,
    pass_score DECIMAL(5,2) NOT NULL,
    max_attempts INT DEFAULT 3,
    required_to_complete BOOLEAN DEFAULT TRUE,
    status VARCHAR(30) NOT NULL
);
```

Nghiệp vụ:

- Quiz có thể gắn với course hoặc lesson.
- `pass_score` là điểm tối thiểu để pass.
- `max_attempts` giới hạn số lần làm.
- `required_to_complete` quyết định quiz có bắt buộc để hoàn thành course không.

### 8.12. Bảng `tbl_questions`

```sql
CREATE TABLE tbl_questions (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    quiz_id VARCHAR(36) NOT NULL,
    content TEXT NOT NULL,
    question_type VARCHAR(50) NOT NULL,
    score DECIMAL(5,2) NOT NULL,
    order_index INT NOT NULL
);
```

Question type:

- `SINGLE_CHOICE`
- `MULTIPLE_CHOICE`

### 8.13. Bảng `tbl_answers`

```sql
CREATE TABLE tbl_answers (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    question_id VARCHAR(36) NOT NULL,
    content TEXT NOT NULL,
    is_correct BOOLEAN DEFAULT FALSE,
    order_index INT NOT NULL
);
```

Nghiệp vụ:

- Instructor tạo câu hỏi và đáp án.
- Mỗi câu hỏi phải có ít nhất một đáp án đúng.

### 8.14. Bảng `tbl_quiz_attempts`

```sql
CREATE TABLE tbl_quiz_attempts (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    quiz_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    enrollment_id VARCHAR(36) NOT NULL,
    score DECIMAL(5,2) DEFAULT 0,
    passed BOOLEAN DEFAULT FALSE,
    started_at DATETIME NOT NULL,
    submitted_at DATETIME NULL,
    status VARCHAR(30) NOT NULL
);
```

Status attempt:

- `IN_PROGRESS`
- `SUBMITTED`
- `CANCELLED`

### 8.15. Bảng `tbl_quiz_attempt_answers`

```sql
CREATE TABLE tbl_quiz_attempt_answers (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    attempt_id VARCHAR(36) NOT NULL,
    question_id VARCHAR(36) NOT NULL,
    answer_id VARCHAR(36),
    is_correct BOOLEAN DEFAULT FALSE,
    score DECIMAL(5,2) DEFAULT 0
);
```

Nghiệp vụ:

- Lưu đáp án student đã chọn.
- Submit quiz thì tính điểm.
- Nếu score >= pass_score thì `passed = true`.

### 8.16. Bảng `tbl_certificates`

```sql
CREATE TABLE tbl_certificates (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    user_id VARCHAR(36) NOT NULL,
    course_id VARCHAR(36) NOT NULL,
    enrollment_id VARCHAR(36) NOT NULL,
    certificate_code VARCHAR(100) NOT NULL,
    issued_at DATETIME NOT NULL,
    status VARCHAR(30) NOT NULL
);
```

Nghiệp vụ:

- Enrollment completed thì sinh certificate.
- Không sinh trùng certificate cho cùng user/course.
- Có API tra cứu certificate theo `certificate_code`.

### 8.17. Bảng `tbl_images`

```sql
CREATE TABLE tbl_images (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    object_type VARCHAR(50) NOT NULL,
    object_id VARCHAR(36) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(1000) NOT NULL,
    file_url VARCHAR(1000),
    content_type VARCHAR(100),
    file_size BIGINT,
    is_primary BOOLEAN DEFAULT FALSE,
    status VARCHAR(30) NOT NULL
);
```

Object type:

- `COURSE`
- `LESSON`
- `USER`
- `CERTIFICATE`

Nghiệp vụ:

- Lưu ảnh đại diện course.
- Lưu avatar user nếu cần.
- Lưu metadata file trên MinIO.

### 8.18. Bảng `tbl_user_devices`

Mục đích: lưu FCM token của browser/device để gửi push notification qua Firebase.

Trong MVP, hệ thống ưu tiên `device_type = WEB`. Android/iOS để mở rộng sau nếu có mobile app.

```sql
CREATE TABLE tbl_user_devices (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    user_id VARCHAR(36) NOT NULL,
    device_id VARCHAR(255),
    device_type VARCHAR(30),
    fcm_token TEXT NOT NULL,
    app_version VARCHAR(50),
    status VARCHAR(30) NOT NULL,
    last_active_at DATETIME NULL
);
```

Device type:

- `WEB`
- `ANDROID`
- `IOS`

Status:

- `ACTIVE`
- `INACTIVE`
- `INVALID`

Nghiệp vụ:

- Web app xin quyền notification trên browser.
- Frontend đăng ký Firebase Messaging và service worker.
- Frontend lấy FCM registration token từ Firebase.
- Client gửi token về backend qua API register device.
- Backend lưu token theo user/device.
- Firebase báo token invalid thì cập nhật `INVALID`.

Lưu ý cho web:

- Web Push cần HTTPS, trừ local `localhost`.
- Browser phải được user cho phép hiện notification.
- Frontend cần service worker Firebase Messaging.
- Backend chỉ lưu token và gửi push qua Firebase Admin SDK.

### 8.19. Bảng `tbl_notices`

```sql
CREATE TABLE tbl_notices (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    notice_type VARCHAR(50) NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    data TEXT,
    status VARCHAR(30) NOT NULL,
    sent_at DATETIME NULL
);
```

Notice type:

- `COURSE_SUBMITTED`
- `COURSE_APPROVED`
- `COURSE_REJECTED`
- `COURSE_PUBLISHED`
- `ENROLLMENT_SUCCESS`
- `COURSE_COMPLETED`
- `CERTIFICATE_ISSUED`
- `SYSTEM`

Target type:

- `USER`
- `USERS`
- `ROLE`
- `ALL`

### 8.20. Bảng `tbl_notice_recipients`

```sql
CREATE TABLE tbl_notice_recipients (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    notice_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    delivery_status VARCHAR(30) NOT NULL,
    read_status VARCHAR(30) NOT NULL,
    sent_at DATETIME NULL,
    read_at DATETIME NULL,
    failure_reason TEXT
);
```

Delivery status:

- `PENDING`
- `SENT`
- `FAILED`

Read status:

- `UNREAD`
- `READ`

### 8.21. Bảng `tbl_notice_delivery_logs`

```sql
CREATE TABLE tbl_notice_delivery_logs (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    notice_id VARCHAR(36) NOT NULL,
    recipient_id VARCHAR(36),
    user_id VARCHAR(36),
    device_id VARCHAR(255),
    fcm_token TEXT,
    provider VARCHAR(50) NOT NULL,
    provider_message_id VARCHAR(255),
    status VARCHAR(30) NOT NULL,
    error_code VARCHAR(100),
    error_message TEXT
);
```

Nghiệp vụ:

- Mỗi lần gửi Firebase thì ghi log.
- Thành công lưu `provider_message_id`.
- Lỗi thì lưu `error_code`, `error_message`.

## 9. Service Và Trách Nhiệm

### 9.1. `authn-service`

Quản lý:

- Đăng nhập
- Đăng xuất
- JWT access token
- Refresh token
- Đổi mật khẩu
- Validate token
- Get user info

API public:

- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh-token`
- `POST /api/v1/auth/logout`
- `POST /api/v1/auth/change-password`

API nội bộ cho gateway:

- `GET /internal/v1/verify/validate-token`
- `GET /internal/v1/verify/user-info`
- `POST /internal/v1/verify/signature`

Luồng token:

```text
Login thành công
-> authn-service trả access_token và refresh_token
-> access_token dùng để gọi API qua gateway
-> refresh_token dùng để xin access_token mới khi access_token hết hạn
-> mỗi lần refresh thành công thì rotate refresh_token
-> logout thì revoke refresh_token hiện tại
```

Chi tiết login:

```text
1. Verify username/password
2. Kiểm tra user status và account_locked_until
3. Tạo access_token sống ngắn, có claim jti
4. Tạo refresh_token sống dài, có token_id
5. Lưu hash của refresh_token vào tbl_refresh_tokens
6. Cập nhật current_access_token_id và last_login_at trong tbl_users
7. Trả access_token, refresh_token, expires_in cho client
```

Chi tiết refresh token:

```text
1. Client gửi refresh_token
2. Authn-service hash refresh_token
3. Tìm bản ghi ACTIVE trong tbl_refresh_tokens
4. Kiểm tra expired_at, revoked_at, status
5. Nếu hợp lệ thì tạo access_token mới
6. Tạo refresh_token mới
7. Chuyển refresh_token cũ sang ROTATED
8. Lưu refresh_token mới ở trạng thái ACTIVE
9. Trả access_token mới và refresh_token mới cho client
```

Chi tiết logout:

```text
1. Client gửi refresh_token hoặc session token hiện tại
2. Authn-service tìm refresh token tương ứng
3. Set revoked_at và status = REVOKED
4. Có thể set current_access_token_id = null nếu muốn access token hết hiệu lực ngay
```

### 9.2. `author-service`

Quản lý:

- Role
- User role
- Permission nếu mở rộng

API:

- CRUD role
- Gán role cho user
- Lấy danh sách user theo role để notice-service gửi thông báo theo role

### 9.3. `course-service`

Quản lý:

- Course category
- Course
- Lesson
- Lesson resource
- Course image/resource upload qua MinIO

Nghiệp vụ chính:

- Instructor tạo course
- Instructor submit review
- Admin approve/reject
- Publish course
- Student xem course published

### 9.4. `learning-service`

Quản lý:

- Enrollment
- Learning progress
- Completion rule
- Certificate nếu chưa tách service

Nghiệp vụ chính:

- Student enroll course
- Student complete lesson
- Tính progress
- Kiểm tra điều kiện hoàn thành course

### 9.5. `quiz-service`

Quản lý:

- Quiz
- Question
- Answer
- Attempt
- Attempt answer

Nghiệp vụ chính:

- Instructor tạo quiz
- Student start attempt
- Student submit attempt
- Tính điểm và pass/fail

### 9.6. Certificate trong MVP

Trong MVP chưa cần tách `certificate-service`. Phần chứng chỉ nên để trong `learning-service` để giảm độ phức tạp.

`learning-service` quản lý thêm:

- Certificate
- Certificate code
- Verify certificate

Nghiệp vụ chính:

- Sinh certificate khi course completed
- Tra cứu certificate theo code

### 9.7. `notice-service`

Quản lý:

- In-app notification
- Firebase Web Push notification
- User device token
- Notice recipient
- Delivery log

Nghiệp vụ chính:

- Register FCM token
- Gửi notice cho user
- Gửi notice cho nhiều user
- Gửi notice theo role
- Lưu lịch sử notice
- Đánh dấu đã đọc
- Ghi log Firebase delivery

## 10. Firebase Trong Notice Service

Firebase được dùng để gửi Web Push Notification cho người dùng web.

Trong hệ thống này có 2 kênh thông báo:

- In-app notification: lưu trong DB, hiển thị trong màn hình thông báo/biểu tượng chuông trên web
- Firebase Web Push: đẩy notification ra browser khi user đã cho phép notification

Firebase không thay thế `tbl_notices` và `tbl_notice_recipients`. Backend vẫn phải lưu lịch sử thông báo để user xem lại trong web.

Client flow:

```text
Web app xin quyền notification trên browser
Frontend đăng ký Firebase Messaging service worker
Frontend lấy FCM registration token từ Firebase
Client gọi API register device
Backend lưu token vào tbl_user_devices
```

Backend flow:

```text
Service khác phát sinh sự kiện
Service đó gọi notice-service qua Feign
notice-service tạo notice
notice-service tạo recipients
notice-service lấy fcm_token của recipients
notice-service gọi Firebase Admin SDK
notice-service cập nhật delivery status và delivery logs
```

Nếu user không cho phép browser notification:

```text
notice-service vẫn tạo in-app notification
Firebase push bỏ qua vì không có active fcm_token
User vẫn xem được thông báo trong web khi đăng nhập
```

Event nên gửi Firebase:

- Course submitted review -> gửi ADMIN
- Course approved -> gửi INSTRUCTOR
- Course rejected -> gửi INSTRUCTOR
- New course published -> gửi STUDENT
- Enrollment success -> gửi STUDENT
- Course completed -> gửi STUDENT
- Certificate issued -> gửi STUDENT

API notice:

- `POST /api/v1/devices/register`
- `POST /api/v1/devices/deactivate`
- `GET /api/v1/notices/me`
- `GET /api/v1/notices/me/unread-count`
- `POST /api/v1/notices/{id}/read`
- `POST /api/v1/notices/read-all`
- `POST /internal/v1/notices/send-user`
- `POST /internal/v1/notices/send-role`
- `POST /internal/v1/notices/send-users`

## 11. API Chính Theo Nghiệp Vụ

Auth:

- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh-token`
- `POST /api/v1/auth/logout`
- `POST /api/v1/auth/change-password`

Course:

- `GET /api/v1/courses`
- `POST /api/v1/courses`
- `GET /api/v1/courses/{id}`
- `POST /api/v1/courses/{id}`
- `DELETE /api/v1/courses/{id}`
- `POST /api/v1/courses/{id}/submit-review`
- `POST /api/v1/courses/{id}/approve`
- `POST /api/v1/courses/{id}/reject`

Lesson:

- `GET /api/v1/courses/{courseId}/lessons`
- `POST /api/v1/courses/{courseId}/lessons`
- `POST /api/v1/lessons/{id}`
- `DELETE /api/v1/lessons/{id}`

Enrollment:

- `POST /api/v1/courses/{courseId}/enroll`
- `GET /api/v1/my-courses`
- `POST /api/v1/lessons/{lessonId}/complete`

Quiz:

- `GET /api/v1/quizzes/{id}`
- `POST /api/v1/quizzes/{id}/attempts`
- `POST /api/v1/quiz-attempts/{id}/submit`

Certificate:

- `GET /api/v1/certificates/{code}`
- `GET /api/v1/my-certificates`

Upload:

- `POST /api/v1/courses/{id}/images`
- `POST /api/v1/lessons/{id}/resources`

Notice:

- `POST /api/v1/devices/register`
- `GET /api/v1/notices/me`
- `POST /api/v1/notices/{id}/read`

## 12. Scope MVP 1 Tháng

Ưu tiên làm:

1. Auth/login/access token/refresh token rotation/logout cơ bản
2. Role ADMIN/INSTRUCTOR/STUDENT
3. CourseCategory CRUD
4. Course CRUD và workflow approve/reject
5. Lesson CRUD
6. Enrollment
7. Learning progress
8. Quiz cơ bản
9. Certificate cơ bản
10. Notice-service + Firebase Web Push cơ bản
11. Upload course image/lesson resource qua MinIO
12. Swagger UI test API

Chưa làm:

- Payment
- Video streaming
- Chat
- Certificate PDF đẹp
- Advanced permission
- Retry queue cho notification

## 13. Lý Do Đề Tài Phù Hợp

Đề tài này không quá đơn giản vì có:

- Nhiều service rõ boundary
- Auth/JWT/role
- Workflow duyệt khóa học
- Đăng ký học và tiến độ học
- Quiz và tính điểm
- Certificate
- Firebase Web Push notification
- Upload file qua MinIO
- Filter/search bằng `BaseSpecification`

Đề tài này không quá lớn vì:

- Không làm video streaming
- Không làm payment thật
- Không làm chat realtime
- Không cần frontend quá phức tạp ngay từ đầu

Đây là scope phù hợp để bắt đầu backend trước và áp dụng tốt base project hiện có.
