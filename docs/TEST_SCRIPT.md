# Kịch bản test — Clothing Store (Group 6)

Tài liệu này dành cho người **chưa biết gì về project** — chỉ cần làm theo từng bước để chạy và kiểm tra toàn bộ nghiệp vụ + validate.

## 0. Chuẩn bị (1 lần duy nhất)

Mở **PowerShell**, dán nguyên khối lệnh sau:

```powershell
cd "C:\Users\khanh\Documents\GitHub\Group6-Management-Shop-clothes-"
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.12"

# Nạp dữ liệu mẫu vào DB (chỉ cần nếu DB chưa có / bị lỗi dữ liệu)
sqlcmd -S localhost -U sa -P sa -C -d ClothingStoreDB -i database\seed_db.sql

# Chạy app
mvn spring-boot:run
```

Chờ đến khi thấy dòng: `Tomcat started on port 9999` và `Started ClothingStoreApplication` là xong.

**Mở trình duyệt:** `http://localhost:9999`

## Tài khoản có sẵn để test

| Tài khoản | Mật khẩu | Vai trò | Dùng để test |
|-----------|----------|---------|--------------|
| `admin@clothingstore.com` | `123456` | ADMIN | Toàn bộ trang quản trị |
| `admin2@clothingstore.com` | `123456` | ADMIN | Admin phụ |
| `customer1@gmail.com` | `123456` | CUSTOMER | Mua hàng, giỏ, đặt hàng |
| `customer2@gmail.com` | `123456` | CUSTOMER | Có đơn DELIVERED (test review) |
| `locked_user@gmail.com` | `123456` | CUSTOMER (bị khóa) | Test đăng nhập bị khóa |

## Dữ liệu test đã seed sẵn

| Loại | Giá trị | Dùng để test |
|------|---------|--------------|
| Product #16 | `OUT_OF_STOCK` | Xem được nhưng không cho thêm giỏ/mua |
| Product #17 | `INACTIVE` | Bị ẩn, vào link trả 404 |
| Order #8 | `PENDING` | Admin duyệt / khách hủy |
| Order #2 | `DELIVERED` (customer2, có product 7) | customer2 được review product 7 |
| Review #6 | `is_visible=0` | Admin thấy review ẩn |
| Category #8 | rỗng (không có product) | Admin được phép xóa |

---

# PHẦN A — NGHIỆP VỤ KHÁCH (PUBLIC / CUSTOMER)

## A1. Đăng ký tài khoản — FR01

**URL:** `http://localhost:9999/register`

| # | Nhập gì | Kỳ vọng | Rule đang test |
|---|---------|---------|----------------|
| 1 | Để trống Họ tên → Submit | "Họ và tên không được để trống!" | `@NotBlank` |
| 2 | Email `abc` (sai định dạng) → Submit | "Địa chỉ email không đúng định dạng!" | `@Email` |
| 3 | Email `customer1@gmail.com` (đã tồn tại) | "Email này đã được sử dụng..." | ERR-01 |
| 4 | Mật khẩu `123` (<6 ký tự) | "Mật khẩu phải có từ 6 đến 32 ký tự!" | `@Size 6..32` |
| 5 | Pass `123456`, nhập lại `654321` | "Mật khẩu xác nhận không trùng khớp!" | ERR-02 |
| 6 | SĐT `0123` (không đủ 10 số) | "Số điện thoại phải gồm 10 chữ số!" | `@Pattern [0-9]{10}` |
| 7 | Điền đúng: Tên `Test User`, Email `test@gmail.com`, Pass `123456`, nhập lại `123456`, SĐT `0905123456` | Redirect về `/login?registerSuccess`, vào `/login` đăng nhập được | FR01 thành công |

## A2. Đăng nhập — FR02

**URL:** `http://localhost:9999/login`

| # | Nhập gì | Kỳ vọng | Rule |
|---|---------|---------|------|
| 1 | Để trống Email → Submit | "Email không được để trống!" | `@NotBlank` |
| 2 | Email `xyz`, Pass `123456` | "Địa chỉ email không đúng định dạng!" | `@Email` |
| 3 | Email `admin@clothingstore.com`, Pass `sai` | Redirect `/login?error` (Sai email/mật khẩu) | BCrypt so khớp |
| 4 | `locked_user@gmail.com` / `123456` | Redirect `/login?error` (tài khoản bị khóa) | BR08 / ERR-04 — `accountNonLocked=false` → LockedException |
| 5 | `admin@clothingstore.com` / `123456` | Redirect `/admin/dashboard` | ADMIN → trang quản trị |
| 6 | `customer1@gmail.com` / `123456` | Redirect `/` (trang chủ) | CUSTOMER → trang chủ |

> **Check "Remember me"** khi login → đóng trình duyệt mở lại vẫn đăng nhập (FR02).

## A3. Xem danh sách & lọc sản phẩm — FR03–FR05

**URL:** `http://localhost:9999/shop`

| # | Thao tác | Kỳ vọng |
|---|---------|---------|
| 1 | Vào `/shop` | Thấy lưới sản phẩm, mỗi card có tên + giá + rating |
| 2 | Gõ từ khóa `áo` vào ô search | Chỉ hiện sản phẩm có "áo" trong tên |
| 3 | Chọn Danh mục "Áo thun" | Chỉ hiện sản phẩm thuộc danh mục đó |
| 4 | Kéo/nhập giá `minPrice=200000&maxPrice=500000` | Chỉ hiện SP trong khoảng giá |
| 5 | Chọn Size `M`, Giới tính `MEN` | Lọc kết hợp nhiều tiêu chí |
| 6 | Sắp xếp "Giá tăng dần" | Sản phẩm xếp theo giá tăng |
| 7 | Qua trang 2 (`?page=1`) | Phân trang hoạt động |

**Xem chi tiết:** click vào 1 sản phẩm → `/shop/products/{id}`
- Thấy ảnh, giá, size còn bán, mô tả, đánh giá.
- `/shop/products/17` (INACTIVE) → **404** (bị ẩn).
- `/shop/products/16` (OUT_OF_STOCK) → mở được nhưng **không có nút Thêm giỏ**.

## A4. Giỏ hàng — FR12–FR14 (phải đăng nhập CUSTOMER)

Login `customer1@gmail.com / 123456` trước.

| # | Thao tác | Kỳ vọng | Rule |
|---|---------|---------|------|
| 1 | Vào sản phẩm, chọn Size `M`, SL `2` → "Thêm vào giỏ" | Redirect `/cart`, thấy món hàng | FR12 |
| 2 | Thêm lại cùng SP + Size | Số lượng cộng dồn (không tạo dòng mới) | cùng key user+product+size |
| 3 | Nhập SL `999` > tồn kho | Lỗi "Số lượng yêu cầu vượt quá... trong kho (Còn lại: X)!" | chặn vượt stock |
| 4 | Vào `/cart` → đổi SL `0` | Lỗi "Số lượng phải lớn hơn 0!" | `quantity<1` |
| 5 | Đổi Size `L`, SL `3` → Cập nhật | Giỏ cập nhật size+SL | FR13 |
| 6 | Bấm "Xóa" 1 món | Món biến khỏi giỏ | FR14 |
| 7 | Thử thêm product 16 (OUT_OF_STOCK) | Lỗi "Sản phẩm này hiện không còn kinh doanh!" | BR — chỉ ACTIVE mới thêm được |

## A5. Đặt hàng (Checkout) — FR15

Vào `/cart` → bấm "Thanh toán" → `/checkout`

| # | Nhập gì | Kỳ vọng | Rule |
|---|---------|---------|------|
| 1 | Giỏ trống (không có món) | "Giỏ hàng của bạn đang trống..." | chặn checkout giỏ rỗng |
| 2 | Để trống "Họ tên người nhận" | "Họ và tên người nhận không được để trống!" | `@NotBlank` |
| 3 | SĐT `0123` | "Số điện thoại phải gồm đúng 10 chữ số!" | `@Pattern` |
| 4 | Để trống Địa chỉ | "Địa chỉ nhận hàng không được để trống!" | `@NotBlank` |
| 5 | Không chọn thanh toán | "Vui lòng chọn phương thức thanh toán!" | `@NotNull` |
| 6 | Điền đúng + chọn `COD` → "Đặt hàng" | Redirect `/my-orders`, đơn mới `PENDING`, giỏ bị xóa, tồn kho giảm | FR15 |

**Kiểm tra trừ kho:** sau khi đặt, vào lại sản phẩm → `stock_quantity` giảm đúng số đã mua.

## A6. Đơn hàng của tôi — FR16

**URL:** `http://localhost:9999/my-orders`

- Thấy danh sách đơn đã đặt, mới nhất lên đầu.
- Click đơn → `/my-orders/{id}` xem chi tiết (người nhận, địa chỉ, các món, tổng tiền).
- Đơn `PENDING` → có nút **"Hủy đơn"** → hủy xong tồn kho được **cộng trả lại**.
- Đơn `CONFIRMED/SHIPPED/DELIVERED/CANCELLED` → **không** có nút hủy.
- Vào `/my-orders/{id đơn của người khác}` → lỗi "Bạn không có quyền xem đơn hàng này!" (bảo mật).

## A7. Đánh giá sản phẩm — FR21 (điều kiện: đã mua & nhận hàng)

Đăng nhập `customer2@gmail.com / 123456` (customer2 có đơn DELIVERED chứa product 7).

| # | Thao tác | Kỳ vọng | Rule |
|---|---------|---------|------|
| 1 | Vào `/shop/products/7` → viết review 5 sao + comment → Gửi | Review xuất hiện ngay, rating trung bình cập nhật | BR06 — đã DELIVERED nên được |
| 2 | Vào `/shop/products/10` (customer2 chưa mua) | **Không có form** review, hoặc gửi bị lỗi "Bạn chỉ có thể đánh giá sau khi đã mua..." | ERR-12 |
| 3 | Gửi review nhưng không chọn sao | "Vui lòng chọn số sao đánh giá (1-5 sao)!" | `@NotNull` |
| 4 | Comment để trống | "Vui lòng nhập nhận xét về sản phẩm!" | `@NotBlank` |

## A8. Hồ sơ cá nhân — FR17–FR19

**URL:** `http://localhost:9999/profile`

- **Cập nhật thông tin:** sửa Họ tên / SĐT / Địa chỉ → Lưu. Validate: SĐT phải 10 số, không được trống.
- **Đổi mật khẩu:**
  - Nhập sai "Mật khẩu hiện tại" → lỗi "Mật khẩu hiện tại không đúng!"
  - Pass mới `123` → "Mật khẩu mới phải từ 6 đến 32 ký tự!"
  - Pass mới & nhập lại không khớp → lỗi xác nhận
  - Đúng → đổi thành công, logout → login lại bằng pass mới.

---

# PHẦN B — NGHIỆP VỤ ADMIN

Login `admin@clothingstore.com / 123456` → tự vào `/admin/dashboard`.

## B1. Dashboard — FR23

`/admin/dashboard` → thấy:
- KPI: tổng doanh thu, số đơn, số sản phẩm, số khách.
- Biểu đồ doanh thu theo tháng.
- Top sản phẩm bán chạy.

## B2. Quản lý sản phẩm — FR08–FR10

`/admin/products`

| # | Thao tác | Kỳ vọng | Rule |
|---|---------|---------|------|
| 1 | "Thêm sản phẩm" → để trống Tên | "Tên sản phẩm không được để trống!" | `@NotBlank` |
| 2 | Không chọn Danh mục / Giới tính | "Vui lòng chọn danh mục!" / "chọn giới tính!" | `@NotNull` |
| 3 | Giá / Tồn kho để trống | "Giá bán không được để trống!" / "Số lượng tồn kho..." | `@NotNull` |
| 4 | Tồn kho `-5` | "Số lượng tồn kho không được là số âm!" | `@Min(0)` |
| 5 | Điền đầy đủ hợp lệ → Lưu | Sản phẩm mới xuất hiện trong shop | FR08 |
| 6 | "Sửa" 1 sản phẩm → đổi giá/tồn kho | Cập nhật thành công | FR09 |
| 7 | Bấm "Ẩn/Hiện" (toggle status) | ACTIVE ↔ INACTIVE; sản phẩm INACTIVE biến khỏi shop (404) | FR09 |

## B3. Quản lý danh mục — FR06, FR07

`/admin/categories`

- Thêm danh mục: tên trống → "Tên danh mục không được để trống!"
- Thêm tên mới → xuất hiện trong dropdown lọc.
- **Xóa:** category #8 (rỗng) → xóa được. Category có sản phẩm → lỗi "không thể xóa danh mục đang có sản phẩm" (bảo vệ dữ liệu).

## B4. Quản lý đơn hàng — FR24

`/admin/orders`

- Lọc theo trạng thái (`PENDING`, `CONFIRMED`, `SHIPPED`, `DELIVERED`, `CANCELLED`).
- Tìm theo **mã đơn / tên người nhận / SĐT** trong ô search.
- Phân trang (5/trang) — "Trang 1/2...".
- Vào `/admin/orders/8` (PENDING) → đổi `CONFIRMED` → trạng thái cập nhật.
- Đổi `CANCELLED` → đơn hủy, **tồn kho được cộng trả lại**.
- Xem chi tiết đơn → các món + đơn giá (giá tại thời điểm mua, không đổi theo giá SP sau này).

## B5. Quản lý người dùng — FR11

`/admin/users`

- Danh sách user + role + status.
- Bấm "Khóa/Mở" 1 user → status ACTIVE ↔ LOCKED.
- User bị LOCKED → không đăng nhập được (test ở A2#4).
- Admin **không thể tự khóa chính mình** (nút bị chặn).

## B6. Quản lý đánh giá — FR22

`/admin/reviews`

- Thấy toàn bộ review kể cả `is_visible=0` (review #6).
- Bấm "Ẩn/Hiện" → review biến khỏi trang sản phẩm (hoặc hiện lại).
- Bấm "Xóa" → xóa vĩnh viễn review spam.

## B7. Báo cáo doanh thu — FR25

`/admin/reports` (hoặc trong dashboard) → biểu đồ doanh thu theo tháng + top sản phẩm bán chạy.

---

# PHẦN C — BẢO MẬT / PHÂN QUYỀN

| # | Thao tác | Kỳ vọng |
|---|---------|---------|
| 1 | Chưa login, vào `/admin/dashboard` | Redirect về `/login` |
| 2 | Login CUSTOMER rồi vào `/admin/products` | Trang 403 "Không có quyền truy cập" |
| 3 | Chưa login, vào `/cart` hoặc `/checkout` | Redirect `/login` |
| 4 | Vào URL không tồn tại `/abcxyz` | Trang 404 custom |
| 5 | Đăng xuất → Back → vào trang cần login | Redirect `/login` (session đã xóa) |

---

# TẮT APP

Khi test xong, quay lại PowerShell chạy `mvn spring-boot:run` bấm **`Ctrl + C`** → `Y` để dừng.

Hoặc nếu chạy nền:

```powershell
Get-NetTCPConnection -LocalPort 9999 -State Listen |
  ForEach-Object { Stop-Process -Id $_.OwningProcess -Force }
```

# Bảng tổng hợp nhanh — tất cả validate

| Form | Trường | Rule | Message lỗi |
|------|--------|------|-------------|
| Đăng ký | Họ tên | `@NotBlank`, `≤100` | "Họ và tên không được để trống!" |
| Đăng ký | Email | `@NotBlank`, `@Email`, `≤100`, unique | "Email này đã được sử dụng..." |
| Đăng ký | Mật khẩu | `@NotBlank`, `6..32` | "Mật khẩu phải có từ 6 đến 32 ký tự!" |
| Đăng ký | Nhập lại pass | khớp pass | "Mật khẩu xác nhận không trùng khớp!" |
| Đăng ký | SĐT | `^[0-9]{10}$` (cho phép rỗng) | "Số điện thoại phải gồm 10 chữ số!" |
| Đăng nhập | Email / Pass | `@NotBlank` | "không được để trống!" |
| Checkout | Người nhận | `@NotBlank`, `≤100` | "Họ và tên người nhận không được để trống!" |
| Checkout | SĐT | `@NotBlank`, `^[0-9]{10}$` | "Số điện thoại phải gồm đúng 10 chữ số!" |
| Checkout | Địa chỉ | `@NotBlank`, `≤255` | "Địa chỉ nhận hàng không được để trống!" |
| Checkout | Thanh toán | `@NotNull` | "Vui lòng chọn phương thức thanh toán!" |
| Giỏ | SL thêm/sửa | `1..stock` | "vượt quá số lượng hàng còn trong kho" |
| Review | Sao | `1..5`, `@NotNull` | "Vui lòng chọn số sao đánh giá" |
| Review | Comment | `@NotBlank`, `≤500` | "Vui lòng nhập nhận xét" |
| Profile | SĐT | `@NotBlank`, `^[0-9]{10}$` | "Số điện thoại phải gồm đúng 10 chữ số" |
| Đổi pass | Pass mới | `6..32` | "Mật khẩu mới phải từ 6 đến 32 ký tự!" |
| Product | Giá/Tồn kho | `@NotNull`, `≥0` | "không được là số âm!" |

# Nghiệp vụ đặc biệt cần demo

1. **Trừ kho khi đặt hàng** — đặt xong `stock_quantity` giảm; về 0 → SP tự `OUT_OF_STOCK`.
2. **Hoàn kho khi hủy** — khách hủy (PENDING) hoặc admin hủy (CANCELLED) → `stock` cộng lại, `OUT_OF_STOCK` → `ACTIVE`.
3. **Chỉ review khi đã nhận hàng** — `canUserReview` = có đơn `DELIVERED` chứa SP đó.
4. **Giá snapshot** — `order_detail.unit_price` lưu giá lúc mua; đổi giá SP sau không ảnh hưởng đơn cũ.
5. **Giỏ cộng dồn** — thêm cùng `user+product+size` → tăng SL, không tạo dòng mới.
