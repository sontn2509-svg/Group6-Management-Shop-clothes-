# Tài liệu mô tả Luồng nghiệp vụ — Group 6 Clothing Store

Tài liệu này mô tả **cách hệ thống hoạt động**, **luồng xử lý code** cho từng nhóm chức năng,
và liệt kê chi tiết **HTTP method, URL, tham số truyền vào** để bạn chạy demo / test API.

- **Base URL:** `http://localhost:9999`
- **Chạy app:** `mvn spring-boot:run` (cần `JAVA_HOME` trỏ tới JDK 21)
- **Tài khoản demo** (password chung: `123456`):
  - Admin: `admin@clothingstore.com`
  - Customer: `customer1@gmail.com` … `customer5@gmail.com`
  - Locked: `locked_user@gmail.com` (để test tài khoản bị khóa)

---

## 1. Kiến trúc tổng quan (luồng request)

```
Browser (Thymeleaf form)
   → Controller (@GetMapping / @PostMapping)      // nhận @RequestParam / @ModelAttribute / @PathVariable
   → Service (@Transactional business logic)      // kiểm tra quy tắc nghiệp vụ (BR), throw IllegalArgumentException
   → Repository (JpaRepository + @Query)          // truy vấn SQL Server
   → Entity  ⇄  Database ClothingStoreDB
   → Controller trả tên view → Thymeleaf render → HTML Bootstrap trả về browser
```

- **Validation** chạy qua `@Valid @ModelAttribute` + `BindingResult` (Jakarta Bean Validation trên DTO).
- **Bảo mật** bởi `SecurityConfig`: `/admin/**` bắt buộc `ROLE_ADMIN`; `/cart/**`, `/checkout/**`, `/my-orders/**`, `/profile/**` cần đăng nhập.
- **Lỗi nghiệp vụ** được ném ra dạng `IllegalArgumentException`, controller bắt và đẩy `errorMessage` qua `RedirectAttributes` → hiển thị alert đỏ trên view.
- **Lỗi hệ thống / not found:** `ShopController` ném `ResponseStatusException` → Spring Boot render `templates/error/404.html` / `error.html`.

---

## 2. Xác thực & Tài khoản (FR01 – FR05)

### FR01 — Đăng ký
| | |
|---|---|
| URL | `GET /register` → hiển thị form; `POST /register` → xử lý |
| Controller | `AuthController#processRegister` |
| Tham số (form body) | `fullName`, `email`, `password`, `confirmPassword`, `phone` — bind vào `RegisterDto` (`@Valid`) |
| Service | `AuthService.register(dto)` |

**Luồng nghiệp vụ:**
1. `AuthService` kiểm tra `userRepository.existsByEmail(email)` — nếu trùng → throw lỗi "Email đã được sử dụng" (ERR-01).
2. So sánh `password` vs `confirmPassword` — không khớp → "Mật khẩu xác nhận không trùng khớp" (ERR-02).
3. Băm `password` bằng `BCryptPasswordEncoder`, tạo `User` với `role=CUSTOMER`, `status=ACTIVE` → `userRepository.save`.
4. Thành công → `redirect:/login?registered` (alert xanh trên trang login).

### FR02 — Đăng nhập / Phân quyền
| | |
|---|---|
| URL | `GET /login` (form), `POST /login` (Spring Security xử lý, không có controller method) |
| Tham số | `email`, `password`, `rememberMe` (checkbox) |
| Cấu hình | `SecurityConfig.securityFilterChain` — `formLogin().loginPage("/login").loginProcessingUrl("/login")` |

**Luồng nghiệp vụ:**
- `UserDetailsServiceImpl.loadUserByUsername(email)` load user → so khớp `password_hash` bằng BCrypt.
- **Sai mật khẩu** → `failureHandler` redirect `/login?error` (ERR-03).
- **Tài khoản `LOCKED`** (`locked_user@gmail.com`) → `LockedException` → redirect `/login?locked` (ERR-04 / BR08).
- **Thành công** → `successHandler` đọc authorities: `ROLE_ADMIN` → redirect `/admin/dashboard`; `ROLE_CUSTOMER` → `/`.
- **Remember me** 14 ngày (cookie `remember-me`); logout → `POST /logout` → `/login?logout`.

### FR03–FR04 — Hồ sơ & đổi mật khẩu
| | |
|---|---|
| URL | `GET /profile`; `POST /profile/update`; `POST /profile/change-password` |
| Controller | `ProfileController` |
| Tham số update | `fullName`, `phone`, `address` → `ProfileUpdateDto` |
| Tham số đổi mật khẩu | `currentPassword`, `newPassword`, `confirmPassword` → `ChangePasswordDto` |

**Luồng:** `UserService.updateProfile` cập nhật 3 trường liên hệ; `changePassword` kiểm tra `passwordEncoder.matches(currentPassword, user.passwordHash)` trước khi set hash mới — sai pass hiện tại → lỗi.

### FR05 — Quản lý khách hàng (Admin)
| | |
|---|---|
| URL | `GET /admin/users?keyword=&page=0&size=10`; `POST /admin/users/{id}/toggle-status` |
| Controller | `AdminUserController` |

**Luồng:** `UserService.getUsers(keyword, PageRequest.of(page,size,Sort.by(DESC,"createdAt")))` trả `Page<User>` → view phân trang. `toggleUserStatus(id, principal.getName())` đổi `ACTIVE↔LOCKED` (BR08) — **không cho phép admin tự khóa chính mình**.

---

## 3. Danh mục & Kho hàng — Admin (FR06 – FR10)

### FR06 — Xem kho
| | |
|---|---|
| URL | `GET /admin/products?categoryId=&status=&keyword=` |
| Controller | `AdminProductController#listProducts` |
| Tham số | `categoryId` (Integer, optional), `status` (ProductStatus: `ACTIVE`/`OUT_OF_STOCK`/`INACTIVE`), `keyword` |

`ProductManageService.getAllProducts` lọc theo 3 điều kiện kết hợp.

### FR07–FR08 — Thêm / Sửa sản phẩm
| | |
|---|---|
| URL | `GET /admin/products/add`; `POST /admin/products/add`<br>`GET /admin/products/edit/{id}`; `POST /admin/products/edit/{id}` |
| Tham số (form) | `productName`, `categoryId`, `price`, `stockQuantity`, `sizes` (List checkbox), `color`, `material`, `gender` (`UNISEX/WOMEN/MEN`), `description`, `imageUrl`, `status` → `ProductFormDto` (`@Valid`) |

**Luồng:** `product-form.html` dùng `th:field="*{...}"` binding. `sizes` (checkbox list) được `ProductManageService` join thành chuỗi `"S,M,L,XL"` lưu vào `available_sizes`. Validate: `price>0`, `stockQuantity>=0`, `productName`/`color`/`categoryId`/`gender` bắt buộc — lỗi trả lại form kèm `BindingResult`.

### FR09 — Ẩn / Ngừng kinh doanh
| | |
|---|---|
| URL | `POST /admin/products/{id}/toggle-status` |
| Luồng | `ProductManageService.toggleStatus` đổi `ACTIVE↔INACTIVE`. Sản phẩm `INACTIVE` không hiện ở shop (BR: `ShopService.search` chỉ query `status=ACTIVE`), không add được vào giỏ (`CartService` check `status != ACTIVE` → throw). |

### FR10 — Quản lý danh mục
| | |
|---|---|
| URL | `GET /admin/categories`; `POST /admin/categories/add`; `POST /admin/categories/edit/{id}`; `POST /admin/categories/{id}/delete` |
| Tham số | `categoryName`, `description`, `status` → `CategoryFormDto` |

**Luồng / BR05:** `CategoryService.deleteCategory` chỉ xóa được danh mục **không chứa sản phẩm** — nếu có product → throw lỗi (seed có "Danh mục thử nghiệm (Rỗng)" id=8 để test xóa thành công).

---

## 4. Mua sắm — Customer (FR11 – FR20)

### FR11 — Trang chủ
| | |
|---|---|
| URL | `GET /` |
| Controller | `AuthController#home` → attributes `newestProducts`, `bestSellerProducts` |
| Luồng | `ShopService.newest()` = `ProductRepository` top-8 `status=ACTIVE order by created_at desc`; `bestSellers()` = query `OrderDetails` join `Orders` where `order_status='DELIVERED'` group by `product_id` order by `SUM(quantity) desc` top 8. |

### FR12 — Tìm kiếm & lọc
| | |
|---|---|
| URL | `GET /shop?keyword=&categoryId=&gender=&size=&minPrice=&maxPrice=&page=` |
| Tham số | `keyword` (theo tên/mô tả), `categoryId` (Integer), `gender` (`MEN/WOMEN/UNISEX`), `size` (chuỗi, vd `L`), `minPrice`/`maxPrice` (BigDecimal), `page` (int ≥0) |

**Luồng / validate (BR):** `page<0`, giá âm, hoặc `minPrice>maxPrice` → `ResponseStatusException(400)` → `error/404`-style error page. `ShopService.search` chỉ trả `status=ACTIVE` (khách không thấy hàng ẩn/hết hàng còn `OUT_OF_STOCK` vẫn hiển thị nhưng chặn mua).

### FR13 — Chi tiết sản phẩm
| | |
|---|---|
| URL | `GET /shop/products/{id}` |
| PathVariable | `id` (long) — không tồn tại → `404` qua `ResponseStatusException(NOT_FOUND)` |
| Model | `product`, `availableSizesList` (split từ `available_sizes` bằng `,`), `reviews`, `averageRating`, `reviewCount`, `reviewDto` (form trống), `canReview` (boolean BR06) |

### FR14 — Thêm giỏ hàng
| | |
|---|---|
| URL | `POST /cart/add` |
| Tham số | `productId` (Long), `selectedSize` (String, bắt buộc — lấy từ radio/select size trên product-detail), `quantity` (Integer, default 1) |
| Auth | Bắt buộc login — chưa login bị `SecurityConfig` redirect `/login?required` |

**Luồng (CartService.addToCart):**
1. `productRepository.findById(productId)` — không có → throw.
2. `product.status != ACTIVE` → "Sản phẩm không còn kinh doanh" (chặn mua hàng ẩn/hết hàng).
3. Tìm `CartItem` cũ theo `(userId, productId, selectedSize)` — cùng size thì **cộng dồn quantity**, khác size thì tạo dòng mới.
4. `newQuantity > stockQuantity` → "vượt quá tồn kho (Còn lại: N)" (BR01).
5. Save → redirect `/cart` kèm `successMessage`.

### FR15 — Cập nhật / Xóa giỏ
| | |
|---|---|
| `POST /cart/update` | `cartItemId`, `selectedSize`, `quantity` — đổi size + số lượng của 1 dòng |
| `POST /cart/delete` | `cartItemId` — xóa dòng khỏi giỏ |
| `GET /cart` | hiển thị giỏ: `cartItems`, `cartTotal` (`CartService.calculateTotal` = Σ price×quantity) |

**Luồng (updateItem):** kiểm tra `item.user.userId == currentUserId` (chống sửa giỏ người khác), `quantity>=1`, `quantity<=stockQuantity` (BR01) → cập nhật `selectedSize`, `quantity`.

### FR16 — Checkout / Đặt hàng
| | |
|---|---|
| URL | `GET /checkout` (form, pre-fill từ profile); `POST /checkout` |
| Tham số | `receiverName`, `receiverPhone` (regex `^[0-9]{10}$`), `shippingAddress`, `note`, `paymentMethod` (`COD`/`BANK_TRANSFER`) → `CheckoutDto` (`@Valid`) |

**Luồng (OrderService.checkout — `@Transactional`, rollback toàn bộ nếu lỗi):**
1. `cartRepository.findByUserUserId(userId)` — giỏ rỗng → throw "giỏ hàng trống".
2. Tạo `Order` `orderStatus=PENDING`, `totalAmount=0`.
3. **Với mỗi CartItem:** kiểm tra `product.status==ACTIVE` và `stockQuantity >= quantity` (re-check tại thời điểm đặt — BR02); trừ `stockQuantity`; nếu về 0 → `status=OUT_OF_STOCK`; thêm `OrderDetail` với `unitPrice = product.price` **tại thời điểm đặt** (giá không đổi về sau); cộng vào `total`.
4. `orderRepository.save` → `cartRepository.deleteByUserUserId` (dọn giỏ).
5. Redirect `/my-orders` — "Đặt hàng thành công, chờ Admin duyệt".

### FR17–FR19 — Đơn của tôi
| | |
|---|---|
| `GET /my-orders` | danh sách đơn của user, mới nhất trước (`findByUserUserIdOrderByOrderDateDesc`) |
| `GET /my-orders/{id}` | chi tiết — `getMyOrderDetail` check `order.user.userId == currentUserId`, xem đơn người khác → throw |
| `POST /my-orders/{id}/cancel` | hủy đơn |

**Luồng hủy (cancelOrder — BR03):** chỉ được hủy khi `orderStatus==PENDING` (đã giao cho đơn vị vận chuyển thì không hủy). Hủy thành công → `restoreStock`: cộng trả `quantity` vào `stockQuantity` từng sản phẩm, và nếu sản phẩm đang `OUT_OF_STOCK` → chuyển lại `ACTIVE`. Đơn chuyển `CANCELLED`.

### FR20 — Admin duyệt đơn
| | |
|---|---|
| `GET /admin/orders?status=&keyword=&page=0&size=10` | danh sách + phân trang (mới thêm) |
| `POST /admin/orders/{id}/status` | `status` (enum OrderStatus) — cập nhật tiến độ |

**Luồng:** `updateStatus` nếu chuyển sang `CANCELLED` → cũng `restoreStock` giống khách hủy. `getOrdersForAdmin` lọc theo `status` + `keyword` (mã đơn / tên / SĐT người nhận), hỗ trợ `Pageable`.

---

## 5. Đánh giá & Báo cáo (FR21 – FR25)

### FR21 — Gửi đánh giá
| | |
|---|---|
| URL | `POST /shop/products/{id}/reviews` |
| Tham số | `rating` (int 1–5), `comment` → `ReviewDto` (`@Valid`); `{id}` = productId |
| BR06 / ERR-12 | `ReviewService.canUserReview(userId, productId)` → `OrderDetailRepository.hasUserPurchasedAndDelivered` — **chỉ được review khi đã có đơn `DELIVERED` chứa sản phẩm đó**, không thì throw "chỉ có thể đánh giá sau khi mua và nhận hàng" |

Review mới tạo với `isVisible=true` mặc định; trang chi tiết chỉ load `isVisible=true`.

### FR22 — Kiểm duyệt đánh giá (Admin)
| | |
|---|---|
| `GET /admin/reviews?productId=&isVisible=` | danh sách, lọc theo sản phẩm / trạng thái hiển thị |
| `POST /admin/reviews/{id}/toggle-visibility` | ẩn/hiện review |
| `POST /admin/reviews/{id}/delete` | xóa review spam |

### FR23–FR25 — Dashboard (Admin)
| | |
|---|---|
| `GET /admin/dashboard` | `AdminDashboardController` → `dashboard.html` |

**Nguồn số liệu (`ReportService`):**
- `getKpi()` → `DashboardKpiDto`: tổng doanh thu (`SUM(totalAmount)` where `DELIVERED` — **BR07 chỉ tính đơn đã giao**), số đơn cần xử lý (`PENDING`+`CONFIRMED`), tổng khách, tổng sản phẩm.
- `getMonthlyRevenue()` → `MonthlyRevenueDto[]`: group `YEAR(order_date), MONTH(order_date)` trên đơn `DELIVERED` (vẽ biểu đồ cột ở dashboard).
- `getTopSellingProducts()` → `TopSellingProductDto[]`: TOP 5 theo `SUM(OrderDetails.quantity)` trên đơn `DELIVERED`.

---

## 6. Tóm tắt bảng tham số (cheat-sheet test)

| Chức năng | Method | URL | Tham số chính |
|---|---|---|---|
| Đăng ký | POST | `/register` | `fullName, email, password, confirmPassword, phone` |
| Đăng nhập | POST | `/login` | `email, password, rememberMe` |
| Cửa hàng | GET | `/shop` | `keyword, categoryId, gender, size, minPrice, maxPrice, page` |
| Chi tiết SP | GET | `/shop/products/{id}` | `id` (path) |
| Thêm giỏ | POST | `/cart/add` | `productId, selectedSize, quantity` |
| Sửa giỏ | POST | `/cart/update` | `cartItemId, selectedSize, quantity` |
| Xóa giỏ | POST | `/cart/delete` | `cartItemId` |
| Thanh toán | POST | `/checkout` | `receiverName, receiverPhone(10 số), shippingAddress, note, paymentMethod` |
| Hủy đơn | POST | `/my-orders/{id}/cancel` | `id` (path) |
| Gửi review | POST | `/shop/products/{id}/reviews` | `id` (path), `rating, comment` |
| Admin đơn | GET | `/admin/orders` | `status, keyword, page, size` |
| Admin đổi trạng thái đơn | POST | `/admin/orders/{id}/status` | `id` (path), `status` |
| Admin sản phẩm | GET | `/admin/products` | `categoryId, status, keyword` |
| Admin thêm SP | POST | `/admin/products/add` | `productName, categoryId, price, stockQuantity, sizes[], color, material, gender, description, imageUrl, status` |
| Admin ẩn SP | POST | `/admin/products/{id}/toggle-status` | `id` (path) |
| Admin danh mục | POST | `/admin/categories/{id}/delete` | `id` (path) — chỉ xóa khi rỗng |
| Admin users | GET | `/admin/users` | `keyword, page, size` |
| Admin khóa user | POST | `/admin/users/{id}/toggle-status` | `id` (path) |
| Admin review | POST | `/admin/reviews/{id}/toggle-visibility` `/admin/reviews/{id}/delete` | `id` (path) |

---

## 7. Cách chạy demo nhanh (kịch bản gợi ý)

```bash
# 1. Nạp dữ liệu (một lần)
sqlcmd -S localhost -U sa -P sa -C -d ClothingStoreDB -i database\seed_db.sql

# 2. Chạy app
set JAVA_HOME=C:\Program Files\Java\jdk-21.0.12   # PowerShell: $env:JAVA_HOME="..."
mvn spring-boot:run
```

**Kịch bản demo mua hàng end-to-end:**
1. `customer1@gmail.com / 123456` login → `/shop` → mở sản phẩm → chọn size `L`, qty `2` → **Add to cart** (`POST /cart/add`).
2. Vào `/cart` → sửa qty hoặc đổi size (`POST /cart/update`).
3. `/checkout` → điền `receiverPhone` = `0912345678` → chọn `COD` → đặt hàng → `/my-orders` thấy đơn `PENDING`.
4. Hủy đơn `PENDING` (`POST /my-orders/{id}/cancel`) → tồn kho được cộng lại.
5. Đăng xuất → login `admin@clothingstore.com / 123456` → `/admin/dashboard` xem KPI → `/admin/orders` duyệt đơn `PENDING` → `CONFIRMED` → `SHIPPING` → `DELIVERED`.
6. Khi đơn `DELIVERED`, khách quay lại `/shop/products/{id}` sẽ thấy form đánh giá (BR06 mở khóa) → `POST /shop/products/{id}/reviews`.

**Test chặn nghiệp vụ:**
- Add vào giỏ sản phẩm 16 (`OUT_OF_STOCK`) → báo "không còn kinh doanh".
- Review sản phẩm chưa mua → báo lỗi ERR-12.
- Login `locked_user@gmail.com` → trang `?locked` (BR08).
- Xóa danh mục 1 (còn sản phẩm) → báo lỗi; xóa danh mục 8 (rỗng) → thành công.
