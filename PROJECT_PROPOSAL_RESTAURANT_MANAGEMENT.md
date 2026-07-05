# De Xuat De Tai Va Thiet Ke He Thong

## 1. De Tai De Xuat

De tai: **He thong quan ly nha hang va mon an**

Muc tieu cua project la xay dung backend Java/Spring Boot theo huong base project hien co, tap trung vao cac nghiep vu:

- Quan ly nguoi dung
- Quan ly vai tro
- Quan ly nha hang
- Quan ly danh muc nha hang
- Quan ly mon an
- Quan ly danh muc mon an
- Quan ly anh tap trung
- Quan ly dang nhap, token, lich su login
- Chuan bi mo rong phan quyen, OTP, notice, promotion

Project nay phu hop voi base service hien tai vi co the tan dung:

- `BaseEntity`: cac truong audit va soft delete
- `BaseController`: CRUD co ban
- `BaseService`: logic create, update, delete, get detail, get all
- `BaseRepository`: soft delete va query chung
- `BaseSpecification`: filter/search theo query params
- `BaseResponse`: response format chung
- `GlobalExceptionHandler`: xu ly exception chung
- `i18n`: da ngon ngu message
- `MinIOService`: upload va quan ly anh
- `SecurityConfig`: cau hinh security
- `SwaggerConfig`: tai lieu API

## 2. Nguyen Tac Thiet Ke Chung

Tat ca cac bang nghiep vu nen co cac truong chung theo `BaseEntity`:

- `id`
- `version`
- `created_by`
- `created_date`
- `last_modified_by`
- `last_modified_date`
- `deleted_at`

Luu y:

- Khong dung `updated_by` neu code base dang dung `last_modified_by`.
- Cac chuc nang xoa nen la soft delete bang cach cap nhat `deleted_at`.
- Cac bang master data nen co `code` va `status` de de filter, search va tich hop.
- Cac gia tri nhu status nen thong nhat enum hoac constant.

## 3. Thiet Ke Database

### 3.1. Bang `tbl_users`

Muc dich: luu thong tin nguoi dung, trang thai dang nhap, ngon ngu, bao mat tai khoan.

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

    access_token TEXT,
    failed_login_attempts INT DEFAULT 0,
    account_locked_until DATETIME NULL,
    last_login_at DATETIME NULL,
    password_change_at DATETIME NULL,
    reset_password_key VARCHAR(255),
    reset_password_expired_at DATETIME NULL
);
```

Nghiep vu:

- Tao nguoi dung
- Cap nhat thong tin nguoi dung
- Khoa/mo khoa nguoi dung
- Luu ngon ngu uu tien cua nguoi dung
- Cap nhat `access_token` khi co token moi
- Xoa `access_token` khi logout neu thiet ke luu token hien tai
- Tang `failed_login_attempts` khi nhap sai password
- Khoa tai khoan tam thoi bang `account_locked_until`
- Cap nhat `last_login_at` khi dang nhap thanh cong
- Cap nhat `password_change_at` khi doi mat khau

### 3.2. Bang `tbl_roles`

Muc dich: luu vai tro nguoi dung.

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

Nghiep vu:

- Tao role
- Cap nhat role
- Xoa mem role
- Tim kiem theo `name`, `code`, `status`
- Khong cho trung `code`

### 3.3. Bang `tbl_user_roles`

Muc dich: gan nhieu role cho mot user.

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
    role_id VARCHAR(36) NOT NULL,

    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES tbl_users(id),
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES tbl_roles(id)
);
```

Nghiep vu:

- Gan role cho user
- Go role khoi user
- Lay danh sach role cua user
- Khong gan trung mot role cho cung mot user

### 3.4. Bang `tbl_images`

Muc dich: luu anh tap trung thay vi moi bang tu luu `image_url` rieng le.

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

`object_type` goi y:

- `USER`
- `RESTAURANT`
- `FOOD`
- `NOTICE`
- `PROMOTION`

Nghiep vu:

- Upload anh
- Gan anh voi mot object bat ky
- Danh dau anh dai dien bang `is_primary`
- Lay danh sach anh cua mot object
- Soft delete anh
- Dung `MinIOService` de upload file len MinIO

### 3.5. Bang `tbl_restaurant_categories`

Muc dich: luu danh muc nha hang, vi du: do an nhanh, lau nuong, ca phe, nha hang gia dinh.

```sql
CREATE TABLE tbl_restaurant_categories (
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

Nghiep vu:

- CRUD danh muc nha hang
- Tim kiem theo `name`, `code`, `status`
- Khong cho trung `code`
- Khong xoa neu dang co nha hang lien ket, hoac chi soft delete

### 3.6. Bang `tbl_restaurants`

Muc dich: luu thong tin nha hang.

```sql
CREATE TABLE tbl_restaurants (
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
    phone VARCHAR(30),
    email VARCHAR(255),
    address VARCHAR(500),
    opening_time VARCHAR(20),
    closing_time VARCHAR(20),
    status VARCHAR(30) NOT NULL
);
```

Nghiep vu:

- Tao nha hang
- Cap nhat thong tin nha hang
- Xoa mem nha hang
- Tim kiem theo `name`, `code`, `status`, `phone`
- Upload anh nha hang qua bang `tbl_images`
- Khong cho trung `code`

### 3.7. Bang `tbl_restaurant_category_mappings`

Muc dich: mot nha hang co the thuoc nhieu danh muc.

```sql
CREATE TABLE tbl_restaurant_category_mappings (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    restaurant_id VARCHAR(36) NOT NULL,
    category_id VARCHAR(36) NOT NULL,

    CONSTRAINT fk_restaurant_mapping_restaurant FOREIGN KEY (restaurant_id) REFERENCES tbl_restaurants(id),
    CONSTRAINT fk_restaurant_mapping_category FOREIGN KEY (category_id) REFERENCES tbl_restaurant_categories(id)
);
```

Nghiep vu:

- Gan nha hang vao nhieu category
- Go category khoi nha hang
- Lay danh sach category cua nha hang
- Lay danh sach nha hang theo category
- Khong cho trung cap `restaurant_id` va `category_id`

### 3.8. Bang `tbl_food_categories`

Muc dich: luu danh muc mon an, vi du: mon chinh, do uong, trang mieng, combo.

```sql
CREATE TABLE tbl_food_categories (
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

Nghiep vu:

- CRUD danh muc mon an
- Tim kiem theo `name`, `code`, `status`
- Khong cho trung `code`
- Co the de chung module voi Food, nhung nen giu service rieng neu nghiep vu category phat trien

### 3.9. Bang `tbl_foods`

Muc dich: luu thong tin mon an.

```sql
CREATE TABLE tbl_foods (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    restaurant_id VARCHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(18,2) NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL,

    CONSTRAINT fk_food_restaurant FOREIGN KEY (restaurant_id) REFERENCES tbl_restaurants(id)
);
```

Nghiep vu:

- Tao mon an
- Cap nhat mon an
- Xoa mem mon an
- Tim kiem theo `name`, `code`, `status`, `restaurantId`
- Upload anh mon an qua bang `tbl_images`
- Khong cho trung `code` trong cung mot nha hang
- Gia mon an phai lon hon hoac bang 0

### 3.10. Bang `tbl_food_category_mappings`

Muc dich: mot mon an co the thuoc nhieu danh muc.

```sql
CREATE TABLE tbl_food_category_mappings (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    food_id VARCHAR(36) NOT NULL,
    category_id VARCHAR(36) NOT NULL,

    CONSTRAINT fk_food_mapping_food FOREIGN KEY (food_id) REFERENCES tbl_foods(id),
    CONSTRAINT fk_food_mapping_category FOREIGN KEY (category_id) REFERENCES tbl_food_categories(id)
);
```

Nghiep vu:

- Gan mon an vao nhieu category
- Go category khoi mon an
- Lay danh sach category cua mon an
- Lay danh sach mon an theo category
- Khong cho trung cap `food_id` va `category_id`

### 3.11. Bang `tbl_login_history`

Muc dich: luu lich su dang nhap.

```sql
CREATE TABLE tbl_login_history (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    user_id VARCHAR(36),
    username VARCHAR(100),
    login_time DATETIME NOT NULL,
    ip_address VARCHAR(100),
    user_agent VARCHAR(1000),
    status VARCHAR(30) NOT NULL,
    failure_reason TEXT,

    CONSTRAINT fk_login_history_user FOREIGN KEY (user_id) REFERENCES tbl_users(id)
);
```

Nghiep vu:

- Ghi lich su login thanh cong
- Ghi lich su login that bai
- Luu ly do that bai neu co
- Dung de audit va dieu tra bao mat

### 3.12. Bang `tbl_otp_history`

Muc dich: luu lich su OTP.

```sql
CREATE TABLE tbl_otp_history (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    user_id VARCHAR(36),
    receiver VARCHAR(255) NOT NULL,
    otp_type VARCHAR(50) NOT NULL,
    otp_hash VARCHAR(255) NOT NULL,
    expired_at DATETIME NOT NULL,
    verified_at DATETIME NULL,
    status VARCHAR(30) NOT NULL,

    CONSTRAINT fk_otp_history_user FOREIGN KEY (user_id) REFERENCES tbl_users(id)
);
```

Nghiep vu:

- Tao OTP
- Gui OTP qua email/SMS neu co
- Verify OTP
- Khong luu OTP plain text, chi nen luu hash
- OTP het han theo `expired_at`

### 3.13. Bang `tbl_permissions`

Muc dich: luu permission cho authorization service sau nay.

```sql
CREATE TABLE tbl_permissions (
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

### 3.14. Bang `tbl_role_permissions`

Muc dich: gan permission cho role.

```sql
CREATE TABLE tbl_role_permissions (
    id VARCHAR(36) PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by VARCHAR(50),
    last_modified_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,

    role_id VARCHAR(36) NOT NULL,
    permission_id VARCHAR(36) NOT NULL,

    CONSTRAINT fk_role_permission_role FOREIGN KEY (role_id) REFERENCES tbl_roles(id),
    CONSTRAINT fk_role_permission_permission FOREIGN KEY (permission_id) REFERENCES tbl_permissions(id)
);
```

### 3.15. Bang `tbl_notices`

Muc dich: luu thong bao.

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
    target_type VARCHAR(50),
    status VARCHAR(30) NOT NULL,
    publish_at DATETIME NULL
);
```

### 3.16. Bang `tbl_promotions`

Muc dich: luu khuyen mai.

```sql
CREATE TABLE tbl_promotions (
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
    discount_type VARCHAR(50) NOT NULL,
    discount_value DECIMAL(18,2) NOT NULL,
    start_at DATETIME NOT NULL,
    end_at DATETIME NOT NULL,
    status VARCHAR(30) NOT NULL
);
```

## 4. Cac Service Can Thiet Ke

### 4.1. `authn-service`

Muc dich: xu ly xac thuc nguoi dung.

Nghiep vu:

- Dang ky user neu can
- Dang nhap
- Dang xuat
- Refresh token
- Doi mat khau
- Quen mat khau
- Cap nhat `access_token`
- Cap nhat `failed_login_attempts`
- Khoa tai khoan tam thoi khi nhap sai nhieu lan
- Ghi `login_history`
- Tao va verify OTP neu lam them

Bang chinh:

- `tbl_users`
- `tbl_login_history`
- `tbl_otp_history`

API goi y:

- `POST /api/v1/auth/login`
- `POST /api/v1/auth/logout`
- `POST /api/v1/auth/refresh-token`
- `POST /api/v1/auth/change-password`
- `POST /api/v1/auth/forgot-password`
- `POST /api/v1/auth/verify-otp`

### 4.2. `authorization-service` hoac `authz-service`

Luu y: nen dung ten `authorization-service` hoac `authz-service`, khong nen dung `author-service` neu y nghia la phan quyen.

Muc dich: quan ly role va permission.

Nghiep vu:

- CRUD role
- CRUD permission
- Gan permission cho role
- Gan role cho user
- Lay danh sach permission cua user
- Kiem tra user co quyen nao do hay khong

Bang chinh:

- `tbl_roles`
- `tbl_permissions`
- `tbl_role_permissions`
- `tbl_user_roles`

API goi y:

- `GET /api/v1/roles`
- `POST /api/v1/roles`
- `POST /api/v1/roles/{id}`
- `DELETE /api/v1/roles/{id}`
- `GET /api/v1/permissions`
- `POST /api/v1/roles/{id}/permissions`
- `POST /api/v1/users/{id}/roles`

### 4.3. `restaurant-service`

Muc dich: quan ly nha hang va danh muc nha hang.

Nghiep vu:

- CRUD nha hang
- CRUD danh muc nha hang
- Gan nha hang vao nhieu category
- Tim kiem/filter nha hang
- Upload anh nha hang
- Lay danh sach nha hang theo category

Bang chinh:

- `tbl_restaurants`
- `tbl_restaurant_categories`
- `tbl_restaurant_category_mappings`
- `tbl_images`

API goi y:

- `GET /api/v1/restaurants`
- `POST /api/v1/restaurants`
- `POST /api/v1/restaurants/{id}`
- `DELETE /api/v1/restaurants/{id}`
- `POST /api/v1/restaurants/{id}/categories`
- `POST /api/v1/restaurants/{id}/images`
- `GET /api/v1/restaurant-categories`

### 4.4. `food-service`

Muc dich: quan ly mon an va danh muc mon an.

Nghiep vu:

- CRUD mon an
- CRUD danh muc mon an
- Gan mon an vao nhieu category
- Tim kiem/filter mon an
- Upload anh mon an
- Lay danh sach mon an theo nha hang
- Lay danh sach mon an theo category

Luu y ve viec gop `FoodCategoryService` va `FoodService`:

- Co the dat chung trong cung `food-service`.
- Khong nen gop tat ca logic vao mot class qua lon.
- Nen giu `FoodService` xu ly mon an.
- Nen giu `FoodCategoryService` xu ly danh muc mon an.
- Neu category chi CRUD rat don gian thi co the cung module, nhung van nen tach class de de bao tri.

Bang chinh:

- `tbl_foods`
- `tbl_food_categories`
- `tbl_food_category_mappings`
- `tbl_images`

API goi y:

- `GET /api/v1/foods`
- `POST /api/v1/foods`
- `POST /api/v1/foods/{id}`
- `DELETE /api/v1/foods/{id}`
- `POST /api/v1/foods/{id}/categories`
- `POST /api/v1/foods/{id}/images`
- `GET /api/v1/food-categories`

### 4.5. `media-service` hoac Image module

Neu he thong chua can tach service rieng, co the de `ImageService` dung chung trong service hien tai.

Muc dich: quan ly upload anh tap trung.

Nghiep vu:

- Upload anh len MinIO
- Luu metadata vao `tbl_images`
- Gan anh voi object
- Danh dau anh chinh
- Xoa mem anh
- Lay danh sach anh cua object

Bang chinh:

- `tbl_images`

API goi y:

- `POST /api/v1/images`
- `GET /api/v1/images?objectType=FOOD&objectId=...`
- `POST /api/v1/images/{id}/primary`
- `DELETE /api/v1/images/{id}`

### 4.6. `notice-service` sau nay

Muc dich: quan ly thong bao.

Nghiep vu:

- Tao thong bao
- Cap nhat thong bao
- Publish thong bao
- Lay danh sach thong bao

Bang chinh:

- `tbl_notices`

### 4.7. `promotion-service` sau nay

Muc dich: quan ly khuyen mai.

Nghiep vu:

- Tao khuyen mai
- Cap nhat khuyen mai
- Kich hoat/huy kich hoat khuyen mai
- Validate thoi gian khuyen mai
- Validate gia tri giam gia

Bang chinh:

- `tbl_promotions`

## 5. Quy Tac Status Goi Y

Dung chung cho master data:

- `ACTIVE`
- `INACTIVE`
- `DELETED`

Dung cho login history:

- `SUCCESS`
- `FAILED`

Dung cho OTP:

- `PENDING`
- `VERIFIED`
- `EXPIRED`
- `FAILED`

Dung cho notice/promotion:

- `DRAFT`
- `ACTIVE`
- `INACTIVE`
- `EXPIRED`

## 6. Cac API Filter Nen Ho Tro

Nho `BaseSpecification`, cac API list co the filter theo query param.

Vi du:

```http
GET /api/v1/restaurants?name=pho&status=ACTIVE
GET /api/v1/restaurants?code=RES001
GET /api/v1/foods?name=ga&status=ACTIVE&restaurantId=...
GET /api/v1/food-categories?name=do&status=ACTIVE
GET /api/v1/roles?code=ADMIN&status=ACTIVE
```

Quy tac cua `BaseSpecification`:

- Field chua `name` hoac `code`: dung `LIKE`
- Field chua `status`: ho tro nhieu gia tri bang dau `|`
- Field chua `id`: neu value dai thi equal, ngan thi like
- Mac dinh chi lay ban ghi `deleted_at IS NULL`

## 7. Scope Lam Trong 1 Thang

### Tuan 1

- Chot database
- Tao project service dau tien
- Import `base-service`
- Lam CRUD:
  - Role
  - User
  - RestaurantCategory
  - FoodCategory

### Tuan 2

- Lam CRUD:
  - Restaurant
  - Food
- Them mapping:
  - Restaurant - RestaurantCategory
  - Food - FoodCategory
- Them filter/search bang `BaseSpecification`

### Tuan 3

- Lam Image module:
  - Upload anh
  - Luu metadata vao `tbl_images`
  - Gan anh voi restaurant/food/user
- Lam auth co ban:
  - Login
  - Logout
  - Luu access token
  - Ghi login history

### Tuan 4

- Hoan thien validation
- Hoan thien i18n message
- Hoan thien Swagger
- Test API
- Viet README
- Chuan bi demo

## 8. Scope Uu Tien De Khong Bi Qua Tai

Neu thoi gian gap, uu tien lam:

1. `tbl_users`
2. `tbl_roles`
3. `tbl_restaurants`
4. `tbl_restaurant_categories`
5. `tbl_foods`
6. `tbl_food_categories`
7. `tbl_images`
8. Mapping restaurant-category
9. Mapping food-category
10. Login/logout co ban

Cac phan de mo rong sau:

- `permissions`
- `role_permissions`
- `otp_history`
- `login_history` nang cao
- `notices`
- `promotions`

## 9. Ket Luan

De tai **He thong quan ly nha hang va mon an** phu hop voi base project hien tai vi:

- Co nhieu bang CRUD de ap dung `BaseController`, `BaseService`, `BaseRepository`
- Co search/filter de ap dung `BaseSpecification`
- Co soft delete de dung `BaseEntity`
- Co anh de dung `MinIOService`
- Co user/role de mo rong security va authorization
- Co i18n de chuan hoa message
- Co huong mo rong ro rang: permission, OTP, notice, promotion

Day la scope vua du cho 1 thang: khong qua lon nhu ecommerce day du, nhung du thuc te de chung minh kha nang thiet ke database, service va trien khai theo base project.
