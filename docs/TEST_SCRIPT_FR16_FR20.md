# Kịch bản test phần của tôi — FR16 đến FR20

Phần này là **luồng mua hàng & đơn hàng** — từ checkout đến khi admin duyệt đơn.
Làm theo đúng thứ tự để thấy đủ validate và nghiệp vụ.

> **Chuẩn bị:** app đang chạy ở `http://localhost:9999` (xem mục 0 trong `TEST_SCRIPT.md`).
> Đăng nhập `customer1@gmail.com / 123456` cho các bước khách hàng.

---

## FR16 — Checkout / Đặt hàng

**URL:** `GET /checkout` (form) · `POST /checkout` (xác nhận)

**Trước khi test:** vào 1 sản phẩm ACTIVE (vd `/shop/products/1`), chọn Size + số lượng → **"Thêm vào giỏ"** để giỏ có hàng.

### Form hiển thị (GET /checkout)
- Form **tự điền sẵn** Họ tên / SĐT / Địa chỉ lấy từ hồ sơ của bạn (nếu đã cập nhật).
- Bên cạnh/bên dưới hiển thị **giỏ hàng + tổng tiền** để bạn kiểm tra trước khi đặt.

### Bảng test validate + nghiệp vụ

| # | Nhập / làm gì | Kết quả mong đợi | Rule đang test |
|---|---------------|-------------------|----------------|
| 1 | Giỏ **trống** → bấm "Đặt hàng" | Quay lại form, lỗi **"Giỏ hàng của bạn đang trống. Vui lòng chọn sản phẩm trước khi thanh toán!"** | Chặn checkout giỏ rỗng |
| 2 | Xóa trắng "Họ tên người nhận" → Đặt | **"Họ và tên người nhận không được để trống!"** | `@NotBlank` |
| 3 | SĐT `0123` (thiếu số) | **"Số điện thoại phải gồm đúng 10 chữ số!"** | `@Pattern ^[0-9]{10}$` |
| 4 | SĐT để trống | **"Số điện thoại nhận hàng không được để trống!"** | `@NotBlank` |
| 5 | Xóa trắng "Địa chỉ nhận hàng" | **"Địa chỉ nhận hàng không được để trống!"** | `@NotBlank` |
| 6 | **Không chọn** phương thức thanh toán | **"Vui lòng chọn phương thức thanh toán!"** | `@NotNull` |
| 7 | Ghi chú >255 ký tự | **"Ghi chú tối đa 255 ký tự!"** | `@Size(max=255)` |
| 8 | Điền đúng + chọn **COD** → "Đặt hàng" | Redirect `/my-orders`, hiện **"Đặt hàng thành công! Đơn hàng đang chờ Admin duyệt."** | FR16 thành công |
| 9 | Điền đúng + chọn **Chuyển khoản (BANK_TRANSFER)** → Đặt | Redirect sang **trang thanh toán VNPay** (không về `/my-orders`) | VNPay gateway |

### Kiểm tra nghiệp vụ sau khi đặt COD (rất quan trọng)

| Kiểm tra | Cách xem | Kỳ vọng |
|----------|----------|---------|
| Giỏ bị dọn | Vào `/cart` | Giỏ trống |
| Đơn mới tạo | Vào `/my-orders` | Đơn đầu tiên, trạng thái **PENDING** |
| Tồn kho giảm | Xem lại sản phẩm vừa mua | `stock` giảm đúng số đã mua |
| Về 0 → OUT_OF_STOCK | Đặt hết số còn lại của 1 SP | SP đó chuyển `Hết hàng`, không thêm giỏ được |
| Giá snapshot | Xem chi tiết đơn → cột đơn giá | Giá = giá lúc mua; sau này đổi giá SP đơn cũ **không đổi** |

### Case thất bại (rollback toàn bộ)
- Nếu **1 món trong giỏ** vừa hết/không đủ số lượng tại lúc đặt → lỗi **"Sản phẩm '...' vừa hết hàng hoặc không đủ số lượng..."**, **toàn bộ đơn không được tạo** (transaction rollback).

---

## FR17 — Xem danh sách đơn của tôi

**URL:** `GET /my-orders`

| # | Thao tác | Kỳ vọng |
|---|---------|---------|
| 1 | Đặt xong ở FR16 → vào `/my-orders` | Đơn vừa đặt hiện **đầu tiên**, trạng thái `PENDING` |
| 2 | Có nhiều đơn | Đơn **mới nhất lên đầu** (sắp xếp theo ngày đặt giảm dần) |
| 3 | Xem mỗi dòng | Thấy: mã đơn, ngày đặt, tổng tiền, trạng thái, phương thức thanh toán |
| 4 | Chưa login → vào `/my-orders` | Redirect `/login` (bắt buộc đăng nhập) |
| 5 | Tài khoản chưa mua gì → vào | Hiện "Bạn chưa có đơn hàng nào" / bảng trống |

---

## FR18 — Xem chi tiết đơn của tôi

**URL:** `GET /my-orders/{id}`

| # | Thao tác | Kỳ vọng | Rule |
|---|---------|---------|------|
| 1 | Click 1 đơn của mình | Xem đầy đủ: người nhận, SĐT, địa chỉ, ghi chú, danh sách món (tên SP, size, SL, đơn giá), tổng tiền | FR18 |
| 2 | Vào `/my-orders/{id của người khác}` | Redirect `/my-orders` + lỗi **"Bạn không có quyền xem đơn hàng này!"** | Bảo mật — check `order.user == currentUser` |
| 3 | Vào `/my-orders/99999` (không tồn tại) | Redirect `/my-orders` + lỗi **"Đơn hàng không tồn tại!"** | `findById` fail |
| 4 | Đơn `PENDING` | Hiện nút **"Hủy đơn hàng"** | Chỉ PENDING mới hủy được (FR19) |
| 5 | Đơn `CONFIRMED/SHIPPING/DELIVERED/CANCELLED` | **Không** có nút hủy | BR03 |

---

## FR19 — Khách tự hủy đơn (+ Hoàn tiền)

**URL:** `POST /my-orders/{id}/cancel` — hủy được đến khi đơn `SHIPPING` · `POST /my-orders/{id}/request-refund` — đơn VNPAY đã trả

| # | Thao tác | Kỳ vọng | Rule |
|---|---------|---------|------|
| 1 | Đơn **COD** `PENDING`/`CONFIRMED` → bấm "Hủy đơn" | Đơn `CANCELLED`, tồn kho cộng trả | FR19 — COD hủy ngay |
| 2 | Đơn **VNPAY** `PENDING` (chưa trả) → "Hủy đơn" | `CANCELLED`, tồn kho trả | VNPAY chưa trả = như COD |
| 3 | Đơn **VNPAY** `CONFIRMED` (đã trả) → "Yêu cầu hủy/Hoàn tiền" | Đơn `REFUND_REQUESTED`, chờ admin | Không hủy trực tiếp, phải duyệt hoàn tiền |
| 4 | Kiểm tra tồn kho sau hủy | Xem lại sản phẩm | `stock` cộng trả; `OUT_OF_STOCK`→`ACTIVE` | `restoreStock` |
| 5 | Hủy đơn `SHIPPING`/`DELIVERED` | Lỗi "đã được bàn giao cho đơn vị vận chuyển..." | BR03 — đã ship thì khóa hủy |
| 6 | Hủy đơn của người khác | Lỗi "Bạn không có quyền xem đơn hàng này!" | Bảo mật |
| 7 | Thử bấm "Hủy" trên đơn VNPAY `CONFIRMED` | Lỗi: "Đơn hàng đã thanh toán qua VNPAY. Vui lòng gửi 'Yêu cầu hủy/hoàn tiền'..." | Chặn hủy trực tiếp đơn đã trả |

---

## FR20 — Admin duyệt đơn

**URL:** `GET /admin/orders` · `POST /admin/orders/{id}/status`

**Đăng xuất → đăng nhập `admin@clothingstore.com / 123456` → vào `/admin/orders`.**

### Danh sách (GET /admin/orders)
| # | Thao tác | Kỳ vọng |
|---|---------|---------|
| 1 | Mở `/admin/orders` | Thấy **tất cả** đơn toàn shop, mới nhất lên đầu |
| 2 | Lọc `status=PENDING` | Chỉ hiện đơn chờ duyệt |
| 3 | Ô search nhập **mã đơn / tên người nhận / SĐT** | Lọc theo từ khóa |
| 4 | Có >10 đơn | **Phân trang** "Trang 1/2...", bấm trang chuyển được |

### Cập nhật trạng thái (POST /admin/orders/{id}/status)
Đổi trạng thái qua dropdown/nút trên từng đơn:

| # | Đổi sang | Kỳ vọng | Rule |
|---|---------|---------|------|
| 1 | `PENDING` → `CONFIRMED` | "Cập nhật trạng thái đơn hàng thành công!", đơn chuyển Đã xác nhận | Admin duyệt |
| 2 | `CONFIRMED` → `SHIPPING` | Trạng thái Đang giao | Tiến độ |
| 3 | `SHIPPING` → `DELIVERED` | Trạng thái Đã giao | Hoàn tất → khách được review SP |
| 4 | Bất kỳ → `CANCELLED` | Đơn hủy, **tồn kho cộng trả lại** (giống khách hủy FR19) | `restoreStock` |
| 5 | Đổi sang trạng thái hiện tại | Không làm gì (return sớm, không lỗi) | Guard trùng trạng thái |

### Duyệt yêu cầu hoàn tiền (POST /admin/orders/{id}/resolve-refund)
Khi đơn ở `REFUND_REQUESTED` (VNPAY đã trả, khách yêu cầu hủy):

| # | Nút | Kỳ vọng |
|---|-----|---------|
| 1 | ✓ **Duyệt** (`approve=true`) | Đơn `REFUNDED`, tồn kho cộng trả — "đã duyệt hoàn tiền" |
| 2 | ✗ **Từ chối** (`approve=false`) | Đơn quay lại `CONFIRMED` — "đã từ chối hoàn tiền" |

---

## Luồng demo trọn vẹn (quay video / thuyết trình)

1. **Khách** `customer1` thêm SP vào giỏ → `/checkout` → điền form → `COD` → đặt.
   → Thấy đơn `PENDING` trong `/my-orders`, tồn kho giảm.
2. **Admin** `admin` vào `/admin/orders` → thấy đơn mới `PENDING` → đổi `CONFIRMED` → `SHIPPING` → `DELIVERED`.
   → Trạng thái cập nhật từng bước.
3. **Khách** `customer1` vào `/my-orders` → đơn giờ `DELIVERED` → vào sản phẩm → **được review** (BR06).
4. **Khách** `customer1` đặt đơn `PENDING` khác → tự **Hủy** → đơn `CANCELLED`, tồn kho cộng lại.
   → Demo được cả khách-hủy và admin-hoàn-kho.

## Các nghiệp vụ (BR) cần nhớ khi trình bày

| BR | Ý nghĩa | Chỗ code |
|----|---------|----------|
| BR02 | Re-check `status=ACTIVE` & `stock>=qty` tại **lúc đặt** — không tin số hiển thị ở giỏ | `OrderService.checkout` |
| — | `unit_price` = giá **tại thời điểm mua**, không đổi theo giá SP sau | `OrderDetail.unitPrice` |
| — | Đặt thành công → **dọn giỏ** + trừ kho; về 0 → `OUT_OF_STOCK` | `checkout` |
| BR03 | Chỉ `PENDING` mới hủy được (đã giao vận chuyển thì khóa) | `cancelOrder` |
| — | Hủy (khách/admin) → **cộng trả kho**, `OUT_OF_STOCK`→`ACTIVE` | `restoreStock` |
| — | Xem/hủy đơn: check `order.user == currentUser` (chống xem đơn người khác) | `getMyOrderDetail` |
| — | 2 cổng thanh toán: `COD` → về `/my-orders`; `BANK_TRANSFER` → VNPay | `doCheckout` |
| — | **Hoàn tiền:** VNPAY `CONFIRMED` (đã trả, chưa ship) → khách `requestRefund` → `REFUND_REQUESTED` → admin `resolveRefund` → `REFUNDED`+hoàn kho, hoặc từ chối → `CONFIRMED` | `requestRefund`, `resolveRefund` |
