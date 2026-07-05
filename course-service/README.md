# course-service

LMS Mini course-service.

- Java 17
- Spring Boot 3.5.7
- Build: `mvn clean package`

## Run locally

1. Create databases:

   ```bash
   mysql -uroot -proot < ../database/init-databases.sql
   ```

2. Start dependencies:

   - MySQL: `127.0.0.1:3306`, username `root`, password `root`
   - Consul: `localhost:8500`

3. Run course-service:

   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=local
   ```

4. Open Swagger UI:

   ```text
   http://localhost:8083/swagger-ui.html
   ```

In IntelliJ, set `Active profiles` to `local` when running
`CourseServiceApplication`.

## Run with Docker Compose

From the repository root:

```bash
docker compose up -d --build
```

By default, Compose uses MySQL running on your host machine:

```text
host.docker.internal:3306
username: root
password: root
database: lms_course_service
```

If you also want Docker to run MySQL, use:

```bash
docker compose --profile mysql up -d --build
```

Useful URLs:

```text
Course Swagger: http://localhost:8083/swagger-ui.html
Consul UI:      http://localhost:8500
MinIO Console: http://localhost:9001
```

MinIO login:

```text
username: minioadmin
password: minioadmin
```

## Course CRUD samples

CRUD resources in this service all extend `BaseController` and use service
implementations that extend `BaseService`.

```text
CourseCategoryServiceImpl
CourseServiceImpl
LessonServiceImpl
LessonResourceServiceImpl
ImageServiceImpl
```

Request/response wrappers are also defined under:

```text
src/main/java/vn/com/atomi/charge/course/model/request
src/main/java/vn/com/atomi/charge/course/model/response
```

Endpoints:

```text
GET    /{resource}
GET    /{resource}/{id}
POST   /{resource}
POST   /{resource}/{id}
DELETE /{resource}/{id}
DELETE /{resource}
```

Resources:

```text
/course-categories
/courses
/lessons
/lesson-resources
/images
```

Create request body:

```json
{
  "data": {
    "name": "Backend",
    "code": "BACKEND",
    "description": "Courses about backend development",
    "status": "ACTIVE"
  }
}
```

## Custom business API sample

Example function not provided by `BaseService`:

```text
POST /courses/{id}/submit-review
```

This API is documented in `LMS_MINI_PROJECT_PLAN.md` under the course workflow:
Instructor submits a draft course for admin review.

Implementation files:

```text
CourseController.submitReview
CourseService.submitReview
CourseServiceImpl.submitReview
```

Business rule:

- Only courses with status `DRAFT` or `REJECTED` can be submitted.
- When submitted, status becomes `PENDING_REVIEW`.
- `rejectReason` is cleared because the course is waiting for review again.

This is the pattern for custom functions:

1. Add method to the service interface.
2. Implement business logic in the service implementation.
3. Add a controller endpoint that calls the service method.
4. Keep `BaseService` unchanged because it should only contain generic CRUD.

Course-specific messages follow the same i18n style as `BaseService`:

```java
return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("course.not_found"));
```

Message keys are defined under `src/main/resources/i18n/messages_*.yml`.

## Enum pattern

Course service keeps enum values under:

```text
src/main/java/vn/com/atomi/charge/course/model/enums
```

DTO fields use enum types directly, so request validation and Swagger can show
valid values clearly:

```java
@NotNull(groups = Create.class)
@Schema(example = "DRAFT", allowableValues = {"DRAFT", "PENDING_REVIEW", "PUBLISHED", "REJECTED", "ARCHIVED"})
private CourseStatus status;
```

Entity fields also use enum types and persist them as text:

```java
@Column(name = "status", nullable = false)
@Enumerated(EnumType.STRING)
private CourseStatus status;
```

With this pattern, the database still stores `VARCHAR` values like `DRAFT` or
`ACTIVE`, but Java code cannot accidentally save random text such as `abc`.
