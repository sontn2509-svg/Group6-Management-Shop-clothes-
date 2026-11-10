-- ==========================================================================================
-- FILE: seed_db.sql
-- DỰ ÁN: Web Bán Quần Áo - Nhóm 6 (Group 6 Clothing Store)
-- MỤC ĐÍCH: Dữ liệu mẫu (Seed Data) phong phú dùng để kiểm thử toàn diện 25 chức năng (FR01-FR25)
-- HỆ THỐNG CƠ SỞ DỮ LIỆU: Microsoft SQL Server (MSSQL)
-- GHI CHÚ TÀI KHOẢN MẪU:
--   Mật khẩu cho TẤT CẢ các tài khoản dưới đây là: 123456
--   Tài khoản Admin:
--     1. admin@clothingstore.com   (Trịnh Ngọc Sơn - Quyền Quản trị viên)
--     2. admin2@clothingstore.com  (Lý Quang Minh - Quyền Quản trị viên)
--   Tài khoản Khách hàng (Customer):
--     3. customer1@gmail.com (Nguyễn Văn An - Đã mua nhiều đơn DELIVERED)
--     4. customer2@gmail.com (Trần Thị Mai - Có đơn DELIVERED, giỏ hàng mẫu)
--     5. customer3@gmail.com (Lê Hoàng Nam - Có đơn PENDING để test hủy đơn)
--     6. customer4@gmail.com (Phạm Thu Hà - Có đơn CONFIRMED để test duyệt đơn)
--     7. customer5@gmail.com (Đỗ Quốc Bảo - Có đơn SHIPPING)
--     8. locked_user@gmail.com (Vũ Đức Khóa - Tài khoản bị LOCKED để test ERR-04)
-- ==========================================================================================

USE [ClothingStoreDB];
GO

-- 1. TẮT TẠM THỜI TẤT CẢ CÁC RÀNG BUỘC KHÓA NGOẠI (FOREIGN KEYS) ĐỂ DỌN VÀ CHÈN DỮ LIỆU
ALTER TABLE [CartItems] NOCHECK CONSTRAINT ALL;
ALTER TABLE [OrderDetails] NOCHECK CONSTRAINT ALL;
ALTER TABLE [Orders] NOCHECK CONSTRAINT ALL;
ALTER TABLE [Reviews] NOCHECK CONSTRAINT ALL;
ALTER TABLE [Products] NOCHECK CONSTRAINT ALL;
ALTER TABLE [Users] NOCHECK CONSTRAINT ALL;
ALTER TABLE [Categories] NOCHECK CONSTRAINT ALL;
GO

-- 2. XÓA DỮ LIỆU CŨ NẾU CÓ
DELETE FROM [CartItems];
DELETE FROM [OrderDetails];
DELETE FROM [Orders];
DELETE FROM [Reviews];
DELETE FROM [Products];
DELETE FROM [Categories];
DELETE FROM [Users];
GO

-- 3. RESET LẠI GIÁ TRỊ TỰ TĂNG (IDENTITY) VỀ 0
DBCC CHECKIDENT ('CartItems', RESEED, 0);
DBCC CHECKIDENT ('OrderDetails', RESEED, 0);
DBCC CHECKIDENT ('Orders', RESEED, 0);
DBCC CHECKIDENT ('Reviews', RESEED, 0);
DBCC CHECKIDENT ('Products', RESEED, 0);
DBCC CHECKIDENT ('Categories', RESEED, 0);
DBCC CHECKIDENT ('Users', RESEED, 0);
GO

-- 4. BẬT LẠI CÁC RÀNG BUỘC KHÓA NGOẠI
ALTER TABLE [CartItems] CHECK CONSTRAINT ALL;
ALTER TABLE [OrderDetails] CHECK CONSTRAINT ALL;
ALTER TABLE [Orders] CHECK CONSTRAINT ALL;
ALTER TABLE [Reviews] CHECK CONSTRAINT ALL;
ALTER TABLE [Products] CHECK CONSTRAINT ALL;
ALTER TABLE [Users] CHECK CONSTRAINT ALL;
ALTER TABLE [Categories] CHECK CONSTRAINT ALL;
GO

PRINT N'>> Đã xóa sạch dữ liệu cũ và reset Identity thành công.';
GO

-- ==========================================================================================
-- PHẦN I: CHÈN BẢNG USERS (TÀI KHOẢN NGƯỜI DÙNG)
-- BCrypt Hash của mật khẩu "123456": $2a$10$ioy8iImZiLIUnYC5Ra.85OHbPE4YsPorYURh3PhjNOvOgRBuEcn3G
-- ==========================================================================================
SET IDENTITY_INSERT [Users] ON;

INSERT INTO [Users] ([user_id], [full_name], [email], [password_hash], [phone], [address], [role], [status], [created_at])
VALUES 
-- Quản trị viên (ADMIN)
(1, N'Trịnh Ngọc Sơn (Admin)', 'admin@clothingstore.com', '$2a$10$ioy8iImZiLIUnYC5Ra.85OHbPE4YsPorYURh3PhjNOvOgRBuEcn3G', '0981112233', N'Số 1 Đại Cồ Việt, Hai Bà Trưng, Hà Nội', 'ADMIN', 'ACTIVE', DATEADD(DAY, -60, GETDATE())),
(2, N'Lý Quang Minh (Admin)', 'admin2@clothingstore.com', '$2a$10$ioy8iImZiLIUnYC5Ra.85OHbPE4YsPorYURh3PhjNOvOgRBuEcn3G', '0982223344', N'Số 144 Xuân Thủy, Cầu Giấy, Hà Nội', 'ADMIN', 'ACTIVE', DATEADD(DAY, -55, GETDATE())),

-- Khách hàng thông thường (CUSTOMER - ACTIVE)
(3, N'Nguyễn Văn An', 'customer1@gmail.com', '$2a$10$ioy8iImZiLIUnYC5Ra.85OHbPE4YsPorYURh3PhjNOvOgRBuEcn3G', '0912345678', N'Số 12 Chùa Bộc, Đống Đa, Hà Nội', 'CUSTOMER', 'ACTIVE', DATEADD(DAY, -50, GETDATE())),
(4, N'Trần Thị Mai', 'customer2@gmail.com', '$2a$10$ioy8iImZiLIUnYC5Ra.85OHbPE4YsPorYURh3PhjNOvOgRBuEcn3G', '0987654321', N'227 Nguyễn Văn Cừ, Quận 5, TP. Hồ Chí Minh', 'CUSTOMER', 'ACTIVE', DATEADD(DAY, -45, GETDATE())),
(5, N'Lê Hoàng Nam', 'customer3@gmail.com', '$2a$10$ioy8iImZiLIUnYC5Ra.85OHbPE4YsPorYURh3PhjNOvOgRBuEcn3G', '0905123456', N'54 Nguyễn Lương Bằng, Liên Chiểu, Đà Nẵng', 'CUSTOMER', 'ACTIVE', DATEADD(DAY, -30, GETDATE())),
(6, N'Phạm Thu Hà', 'customer4@gmail.com', '$2a$10$ioy8iImZiLIUnYC5Ra.85OHbPE4YsPorYURh3PhjNOvOgRBuEcn3G', '0938999888', N'88 Lạch Tray, Ngô Quyền, Hải Phòng', 'CUSTOMER', 'ACTIVE', DATEADD(DAY, -20, GETDATE())),
(7, N'Đỗ Quốc Bảo', 'customer5@gmail.com', '$2a$10$ioy8iImZiLIUnYC5Ra.85OHbPE4YsPorYURh3PhjNOvOgRBuEcn3G', '0977112233', N'Đường 3/2, Ninh Kiều, Cần Thơ', 'CUSTOMER', 'ACTIVE', DATEADD(DAY, -10, GETDATE())),

-- Khách hàng bị khóa (CUSTOMER - LOCKED dùng để test quy tắc nghiệp vụ BR08 / ERR-04)
(8, N'Vũ Đức Khóa (Bị khóa)', 'locked_user@gmail.com', '$2a$10$ioy8iImZiLIUnYC5Ra.85OHbPE4YsPorYURh3PhjNOvOgRBuEcn3G', '0911223344', N'Số 99 Lê Duẩn, Hoàn Kiếm, Hà Nội', 'CUSTOMER', 'LOCKED', DATEADD(DAY, -5, GETDATE()));

SET IDENTITY_INSERT [Users] OFF;
GO
PRINT N'>> Đã chèn 8 tài khoản Users mẫu.';
GO

-- ==========================================================================================
-- PHẦN II: CHÈN BẢNG CATEGORIES (DANH MỤC THỜI TRANG)
-- ==========================================================================================
SET IDENTITY_INSERT [Categories] ON;

INSERT INTO [Categories] ([category_id], [category_name], [description], [status])
VALUES
(1, N'Áo thun (T-Shirt)', N'Các mẫu áo thun cotton, polo, streetwear năng động và thoáng mát', 'ACTIVE'),
(2, N'Áo sơ mi (Shirt)', N'Áo sơ mi nam nữ công sở, sơ mi Oxford, sơ mi dạo phố cao cấp', 'ACTIVE'),
(3, N'Áo khoác & Hoodie', N'Áo khoác bomber, áo gió 2 lớp, hoodie nỉ ấm áp phong cách mùa thu đông', 'ACTIVE'),
(4, N'Quần Jeans & Denim', N'Quần jeans ống suông, ống loe, slim-fit chất liệu denim bền đẹp', 'ACTIVE'),
(5, N'Quần Tây & Kaki', N'Quần âu xếp ly, quần kaki co giãn lịch lãm cho công sở và đi chơi', 'ACTIVE'),
(6, N'Phụ kiện thời trang', N'Nón lưỡi trai, thắt lưng da, vớ cổ cao và phụ kiện trang phục', 'ACTIVE'),
(7, N'Bộ sưu tập cũ (Ngừng kinh doanh)', N'Danh mục hàng vụ hè năm ngoái hiện đang ẩn khỏi khách hàng', 'INACTIVE'),
(8, N'Danh mục thử nghiệm (Rỗng)', N'Danh mục trống dùng để test xóa thành công không bị ràng buộc BR05', 'ACTIVE');

SET IDENTITY_INSERT [Categories] OFF;
GO
PRINT N'>> Đã chèn 8 Categories mẫu.';
GO

-- ==========================================================================================
-- PHẦN III: CHÈN BẢNG PRODUCTS (SẢN PHẨM THỜI TRANG)
-- ==========================================================================================
SET IDENTITY_INSERT [Products] ON;

INSERT INTO [Products] (
    [product_id], [category_id], [product_name], [price], [stock_quantity], 
    [available_sizes], [color], [material], [gender], [description], [image_url], [status], [created_at]
)
VALUES
-- 1. Áo thun nam Cotton Compact
(1, 1, N'Áo Thun Nam Cotton Compact 100%', 199000.00, 120, 'S,M,L,XL', N'Đen', N'Cotton 100%', 'MEN', 
 N'Áo thun nam sợi bông tự nhiên mềm mịn, thoáng mát, thấm hút mồ hôi vượt trội, co giãn 4 chiều.', 
 'https://images.unsplash.com/photo-1521572267360-ee0c2909d518?w=500&auto=format&fit=crop&q=80', 'ACTIVE', DATEADD(DAY, -45, GETDATE())),

-- 2. Áo thun Unisex Oversize Streetwear
(2, 1, N'Áo Thun Unisex Oversize Streetwear Typography', 250000.00, 85, 'M,L,XL', N'Trắng', N'Cotton 2 chiều 250gsm', 'UNISEX', 
 N'Form áo thụng rộng rãi phong cách đường phố, hình in typo bền màu giặt máy không nứt vỡ.', 
 'https://images.unsplash.com/photo-1583743814966-8936f5b7be1a?w=500&auto=format&fit=crop&q=80', 'ACTIVE', DATEADD(DAY, -40, GETDATE())),

-- 3. Áo thun nữ Baby Tee dáng ngắn
(3, 1, N'Áo Thun Nữ Baby Tee Dáng Ngắn Ôm Body', 179000.00, 60, 'S,M', N'Hồng Pastel', N'Thun Bozip Hàn Quốc', 'WOMEN', 
 N'Thiết kế áo lửng tôn eo thon, chất vải co giãn nhẹ ôm dáng mềm mại, trẻ trung.', 
 'https://images.unsplash.com/photo-1503342217505-b0a15ec3261c?w=500&auto=format&fit=crop&q=80', 'ACTIVE', DATEADD(DAY, -35, GETDATE())),

-- 4. Áo sơ mi nam Oxford
(4, 2, N'Áo Sơ Mi Nam Oxford Dài Tay Chống Nhăn', 350000.00, 50, 'M,L,XL', N'Xanh Nhạt', N'Vải Oxford dệt tổ ong', 'MEN', 
 N'Sơ mi cổ bẻ đứng form, vải dệt sợi Oxford hạn chế tối đa nhăn nhàu, chuẩn phong cách thanh lịch.', 
 'https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=500&auto=format&fit=crop&q=80', 'ACTIVE', DATEADD(DAY, -30, GETDATE())),

-- 5. Áo sơ mi lụa công sở nữ
(5, 2, N'Áo Sơ Mi Lụa Tay Phồng Công Sở Nữ', 320000.00, 40, 'S,M,L', N'Be Sữa', N'Lụa Mango mềm rủ', 'WOMEN', 
 N'Thiết kế cổ nơ thắt nhẹ nhàng, tay phồng duyên dáng, thích hợp môi trường văn phòng và dự tiệc.', 
 'https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=500&auto=format&fit=crop&q=80', 'ACTIVE', DATEADD(DAY, -28, GETDATE())),

-- 6. Áo sơ mi Hawaii dạo biển
(6, 2, N'Áo Sơ Mi Cổ Cuba Họa Tiết Hawaii Dạo Biển', 280000.00, 45, 'M,L,XL', N'Xanh Họa Tiết', N'Rayon lụa mát rượi', 'UNISEX', 
 N'Họa tiết hoa lá nhiệt đới bắt mắt, form rộng phóng khoáng lý tưởng cho các chuyến du lịch mùa hè.', 
 'https://images.unsplash.com/photo-1607345366928-199ea26cfe3e?w=500&auto=format&fit=crop&q=80', 'ACTIVE', DATEADD(DAY, -25, GETDATE())),

-- 7. Áo khoác Bomber gió 2 lớp
(7, 3, N'Áo Khoác Bomber Gió 2 Lớp Chống Thấm Nước', 450000.00, 30, 'M,L,XL', N'Xanh Rêu', N'Vải dù tráng PU + lót dù', 'UNISEX', 
 N'Cản gió và giữ ấm hiệu quả, bo dệt co giãn ở cổ và gấu áo, phong cách cá tính năng động.', 
 'https://images.unsplash.com/photo-1544441893-675973e31985?w=500&auto=format&fit=crop&q=80', 'ACTIVE', DATEADD(DAY, -22, GETDATE())),

-- 8. Áo Hoodie nỉ bông ấm áp
(8, 3, N'Áo Hoodie Nỉ Bông Dày Dặn Có Mũ Trùm Túi Bụng', 390000.00, 65, 'M,L,XL', N'Xám Chì', N'Nỉ cua lót bông 350gsm', 'UNISEX', 
 N'Mũ trùm đầu 2 lớp đứng form, túi kangaroo tiện lợi, giữ nhiệt cực tốt trong thời tiết se lạnh.', 
 'https://images.unsplash.com/photo-1556905055-8f358a7a47b2?w=500&auto=format&fit=crop&q=80', 'ACTIVE', DATEADD(DAY, -20, GETDATE())),

-- 9. Áo khoác Denim rách cá tính
(9, 3, N'Áo Khoác Bò Denim Vintage Wash Cào Xước', 520000.00, 25, 'M,L', N'Xanh Bò Cổ Điển', N'Jean 100% Cotton 14oz', 'MEN', 
 N'Màu wash tự nhiên kiểu cổ điển bụi bặm, cúc đồng đúc chắc chắn, bền đẹp theo năm tháng.', 
 'https://images.unsplash.com/photo-1576871337632-b9aef4c17ab9?w=500&auto=format&fit=crop&q=80', 'ACTIVE', DATEADD(DAY, -18, GETDATE())),

-- 10. Quần Jeans nam ống suông Straight Fit
(10, 4, N'Quần Jeans Nam Ống Suông Straight Fit Basic', 390000.00, 75, 'S,M,L,XL', N'Xanh Đậm', N'Denim co giãn nhẹ 2%', 'MEN', 
 N'Ống suông thẳng đứng tôn dáng chân dài, đường may kép chắc chắn, thích hợp phối với mọi loại giày.', 
 'https://images.unsplash.com/photo-1542272604-787c3835535d?w=500&auto=format&fit=crop&q=80', 'ACTIVE', DATEADD(DAY, -15, GETDATE())),

-- 11. Quần Jeans nữ cạp cao ống loe
(11, 4, N'Quần Jeans Nữ Cạp Cao Ống Loe Hack Dáng', 380000.00, 50, 'S,M,L', N'Xanh Nhạt Vintage', N'Denim mềm co giãn 4 chiều', 'WOMEN', 
 N'Cạp cao che khuyết điểm vòng 2, ống loe nhẹ từ đầu gối tạo hiệu ứng thon dài quyến rũ.', 
 'https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=500&auto=format&fit=crop&q=80', 'ACTIVE', DATEADD(DAY, -14, GETDATE())),

-- 12. Quần Kaki nam Slim Fit
(12, 5, N'Quần Kaki Nam Slim Fit Co Giãn Thoải Mái', 340000.00, 60, 'S,M,L,XL', N'Vàng Cát (Khaki)', N'Kaki cotton spandex 98/2', 'MEN', 
 N'Form ôm vừa vặn, không gò bó khi cử động, phù hợp cả đi làm văn phòng và dạo phố.', 
 'https://images.unsplash.com/photo-1624378439575-d8705ad7ae80?w=500&auto=format&fit=crop&q=80', 'ACTIVE', DATEADD(DAY, -12, GETDATE())),

-- 13. Quần Tây nam xếp ly Hàn Quốc
(13, 5, N'Quần Tây Nam Xếp Ly Phong Cách Hàn Quốc', 420000.00, 40, 'M,L,XL', N'Đen Tuyền', N'Vải tuyết mưa cao cấp', 'MEN', 
 N'Thiết kế xếp ly thân trước thanh lịch, ống rộng rủ nhẹ bay bổng, khóa kéo ẩn mượt mà.', 
 'https://images.unsplash.com/photo-1473966968600-fa801b869a1a?w=500&auto=format&fit=crop&q=80', 'ACTIVE', DATEADD(DAY, -10, GETDATE())),

-- 14. Quần Short Kaki unisex
(14, 5, N'Quần Short Kaki Túi Hộp Dạo Phố Năng Động', 210000.00, 70, 'S,M,L,XL', N'Xanh Rêu', N'Kaki thô 100% cotton', 'UNISEX', 
 N'Chiều dài trên gối mát mẻ, cạp chun có dây rút linh hoạt kích cỡ, phối cùng áo thun cực đẹp.', 
 'https://images.unsplash.com/photo-1591195853828-11db59a44f6b?w=500&auto=format&fit=crop&q=80', 'ACTIVE', DATEADD(DAY, -8, GETDATE())),

-- 15. Nón lưỡi trai Classic
(15, 6, N'Mũ Lưỡi Trai Classic Thêu Chữ Nổi Minimalist', 120000.00, 95, 'S,M,L', N'Đen', N'Vải Kaki dệt dày', 'UNISEX', 
 N'Khóa cài kim loại phía sau dễ dàng điều chỉnh chu vi đầu, vành mũ cứng cáp che nắng tốt.', 
 'https://images.unsplash.com/photo-1588850561407-ed78c282e89b?w=500&auto=format&fit=crop&q=80', 'ACTIVE', DATEADD(DAY, -6, GETDATE())),

-- 16. Mẫu Áo Polo hết hàng (stock_quantity = 0, OUT_OF_STOCK) để test hiển thị nhãn hết hàng và chặn đặt mua
(16, 1, N'Áo Polo Nam Cổ Bẻ Thể Thao (Tạm Hết Hàng)', 230000.00, 0, 'S,M,L', N'Đỏ Đô', N'Vải cá sấu poly co giãn', 'MEN', 
 N'Mẫu áo polo bán rất chạy trong đợt lễ vừa qua, hiện tại đã hết sạch tồn kho.', 
 'https://images.unsplash.com/photo-1586363104862-3a5e2ab60d99?w=500&auto=format&fit=crop&q=80', 'OUT_OF_STOCK', DATEADD(DAY, -4, GETDATE())),

-- 17. Mẫu Quần Ẩn / Ngừng bán (INACTIVE) để test chức năng ẩn kinh doanh FR09 & lọc Admin
(17, 4, N'Quần Jeans Mẫu Cũ Mùa Đông (Ngừng Bán)', 490000.00, 15, 'M,L', N'Nâu Đất', N'Denim thô', 'MEN', 
 N'Sản phẩm tồn kho mẫu cũ chuyển sang ngừng kinh doanh để nhường chỗ cho bộ sưu tập mới.', 
 'https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=500&auto=format&fit=crop&q=80', 'INACTIVE', DATEADD(DAY, -2, GETDATE()));

SET IDENTITY_INSERT [Products] OFF;
GO
PRINT N'>> Đã chèn 17 Products mẫu.';
GO

-- ==========================================================================================
-- PHẦN IV: CHÈN BẢNG CARTITEMS (GIỎ HÀNG THỬ NGHIỆM CHO KHÁCH)
-- ==========================================================================================
SET IDENTITY_INSERT [CartItems] ON;

INSERT INTO [CartItems] ([cart_item_id], [user_id], [product_id], [selected_size], [quantity])
VALUES
-- Giỏ hàng của Khách hàng 3 (Nguyễn Văn An)
(1, 3, 1, 'L', 2),  -- 2 chiếc Áo thun nam Cotton Compact size L
(2, 3, 4, 'XL', 1), -- 1 chiếc Áo sơ mi Oxford size XL

-- Giỏ hàng của Khách hàng 4 (Trần Thị Mai)
(3, 4, 3, 'S', 1),  -- 1 chiếc Áo thun Baby Tee size S
(4, 4, 11, 'M', 1); -- 1 chiếc Quần Jeans nữ cạp cao size M

SET IDENTITY_INSERT [CartItems] OFF;
GO
PRINT N'>> Đã chèn 4 món vào giỏ hàng CartItems.';
GO

-- ==========================================================================================
-- PHẦN V: CHÈN BẢNG ORDERS & ORDERDETAILS (ĐƠN HÀNG VÀ CHI TIẾT ĐƠN)
-- Dữ liệu trải dài từ Tháng 9, Tháng 10 đến Tháng 11/2026 với nhiều trạng thái để test:
--   - Báo cáo doanh thu tháng FR23 (chỉ tính DELIVERED)
--   - Top 5 bán chạy FR24 (tính từ DELIVERED)
--   - Đơn cần đóng gói FR25 (PENDING & CONFIRMED)
--   - Khách hủy đơn PENDING FR19
--   - Admin cập nhật đơn FR20
-- ==========================================================================================
SET IDENTITY_INSERT [Orders] ON;

INSERT INTO [Orders] (
    [order_id], [user_id], [receiver_name], [receiver_phone], [shipping_address], 
    [note], [total_amount], [payment_method], [order_status], [order_date]
)
VALUES
-- Đơn 1: Tháng 9/2026 - Đã giao thành công (DELIVERED)
(1, 3, N'Nguyễn Văn An', '0912345678', N'Số 12 Chùa Bộc, Đống Đa, Hà Nội', 
 N'Giao giờ hành chính giúp mình', 738000.00, 'COD', 'DELIVERED', '2026-09-18 10:30:00'),

-- Đơn 2: Tháng 10/2026 - Đã giao thành công (DELIVERED)
(2, 4, N'Trần Thị Mai', '0987654321', N'227 Nguyễn Văn Cừ, Quận 5, TP.HCM', 
 N'Gọi trước khi giao 15 phút', 939000.00, 'BANK_TRANSFER', 'DELIVERED', '2026-10-12 14:15:00'),

-- Đơn 3: Tháng 10/2026 - Đã giao thành công (DELIVERED)
(3, 5, N'Lê Hoàng Nam', '0905123456', N'54 Nguyễn Lương Bằng, Liên Chiểu, Đà Nẵng', 
 NULL, 1340000.00, 'COD', 'DELIVERED', '2026-10-25 09:00:00'),

-- Đơn 4: Tháng 11/2026 - Đã giao thành công (DELIVERED)
(4, 6, N'Phạm Thu Hà', '0938999888', N'88 Lạch Tray, Ngô Quyền, Hải Phòng', 
 N'Giao tận tay', 1080000.00, 'BANK_TRANSFER', 'DELIVERED', '2026-11-02 11:20:00'),

-- Đơn 5: Tháng 11/2026 - Đã giao thành công (DELIVERED)
(5, 3, N'Nguyễn Văn An', '0912345678', N'Số 12 Chùa Bộc, Đống Đa, Hà Nội', 
 NULL, 780000.00, 'COD', 'DELIVERED', '2026-11-05 16:45:00'),

-- Đơn 6: Đang vận chuyển (SHIPPING)
(6, 7, N'Đỗ Quốc Bảo', '0977112233', N'Đường 3/2, Ninh Kiều, Cần Thơ', 
 N'Hàng dễ vỡ xin nhẹ tay', 520000.00, 'COD', 'SHIPPING', '2026-11-08 08:30:00'),

-- Đơn 7: Đã xác nhận / Chờ đóng gói (CONFIRMED) -> Tính vào KPI Dashboard
(7, 4, N'Trần Thị Mai', '0987654321', N'227 Nguyễn Văn Cừ, Quận 5, TP.HCM', 
 NULL, 420000.00, 'BANK_TRANSFER', 'CONFIRMED', '2026-11-09 13:00:00'),

-- Đơn 8: Đơn mới chờ duyệt (PENDING) -> Dùng để test Khách tự hủy đơn FR19 hoặc Admin duyệt FR20
(8, 5, N'Lê Hoàng Nam', '0905123456', N'54 Nguyễn Lương Bằng, Liên Chiểu, Đà Nẵng', 
 N'Đơn mới vừa đặt cần test', 250000.00, 'COD', 'PENDING', GETDATE()),

-- Đơn 9: Đơn đã hủy (CANCELLED) -> Đã hoàn kho thành công theo quy tắc BR03
(9, 6, N'Phạm Thu Hà', '0938999888', N'88 Lạch Tray, Ngô Quyền, Hải Phòng', 
 N'Khách đổi ý muốn đổi size', 350000.00, 'COD', 'CANCELLED', DATEADD(DAY, -3, GETDATE()));

SET IDENTITY_INSERT [Orders] OFF;
GO
PRINT N'>> Đã chèn 9 Orders mẫu.';
GO

-- CHÈN CHI TIẾT TỪNG ĐƠN HÀNG (ORDERDETAILS)
SET IDENTITY_INSERT [OrderDetails] ON;

INSERT INTO [OrderDetails] ([order_detail_id], [order_id], [product_id], [selected_size], [quantity], [unit_price])
VALUES
-- Đơn 1: Tổng 738.000 đ
(1, 1, 1, 'L', 2, 199000.00),   -- 2 x Áo thun nam = 398.000
(2, 1, 12, 'L', 1, 340000.00),  -- 1 x Quần Kaki nam = 340.000

-- Đơn 2: Tổng 939.000 đ
(3, 2, 7, 'M', 1, 450000.00),   -- 1 x Áo Bomber = 450.000
(4, 2, 3, 'S', 1, 179000.00),   -- 1 x Áo Baby Tee = 179.000
(5, 2, 15, 'M', 1, 120000.00),  -- 1 x Mũ lưỡi trai = 120.000
(6, 2, 14, 'L', 1, 210000.00),  -- 1 x Quần Short = 210.000 (Tổng = 959k -> sale nhẹ 939k)

-- Đơn 3: Tổng 1.340.000 đ
(7, 3, 4, 'XL', 2, 350000.00),  -- 2 x Áo sơ mi Oxford = 700.000
(8, 3, 10, 'L', 1, 390000.00),  -- 1 x Quần Jeans nam = 390.000
(9, 3, 2, 'XL', 1, 250000.00),  -- 1 x Áo thun Oversize = 250.000

-- Đơn 4: Tổng 1.080.000 đ
(10, 4, 11, 'M', 2, 380000.00), -- 2 x Quần Jeans nữ cạp cao = 760.000
(11, 4, 5, 'S', 1, 320000.00),  -- 1 x Áo sơ mi lụa nữ = 320.000

-- Đơn 5: Tổng 780.000 đ
(12, 5, 8, 'L', 2, 390000.00),  -- 2 x Áo Hoodie nỉ = 780.000

-- Đơn 6: Tổng 520.000 đ (SHIPPING)
(13, 6, 9, 'M', 1, 520000.00),  -- 1 x Áo khoác bò Denim

-- Đơn 7: Tổng 420.000 đ (CONFIRMED)
(14, 7, 13, 'L', 1, 420000.00), -- 1 x Quần Tây nam xếp ly

-- Đơn 8: Tổng 250.000 đ (PENDING)
(15, 8, 2, 'M', 1, 250000.00),  -- 1 x Áo thun Oversize

-- Đơn 9: Tổng 350.000 đ (CANCELLED)
(16, 9, 4, 'L', 1, 350000.00);  -- 1 x Áo sơ mi Oxford

SET IDENTITY_INSERT [OrderDetails] OFF;
GO
PRINT N'>> Đã chèn 16 dòng OrderDetails mẫu.';
GO

-- ==========================================================================================
-- PHẦN VI: CHÈN BẢNG REVIEWS (ĐÁNH GIÁ CHẤT LƯỢNG ÁO / QUẦN)
-- Tuân thủ quy tắc BR06: Khách hàng (User 3, 4, 5, 6) đều đã có đơn hàng DELIVERED chứa các sản phẩm này!
-- Có sẵn 1 review bị ẩn (is_visible = 0) để Admin test kiểm duyệt FR22.
-- ==========================================================================================
SET IDENTITY_INSERT [Reviews] ON;

INSERT INTO [Reviews] ([review_id], [user_id], [product_id], [rating], [comment], [is_visible], [created_at])
VALUES
-- Đánh giá sản phẩm 1 (Áo thun nam Cotton) bởi User 3 (Nguyễn Văn An)
(1, 3, 1, 5, N'Chất vải cotton 100% cực kỳ mát và mềm mại, giặt máy nhiều lần vẫn giữ form tốt không bị xù lông. Rất đáng tiền!', 1, '2026-09-25 15:00:00'),

-- Đánh giá sản phẩm 7 (Áo khoác Bomber) bởi User 4 (Trần Thị Mai)
(2, 4, 7, 5, N'Áo bomber mặc lên dáng rất chuẩn và tôn dáng. Đường kim mũi chỉ tỉ mỉ, khả năng cản gió chống thấm tốt khi đi xe máy.', 1, '2026-10-18 20:30:00'),

-- Đánh giá sản phẩm 4 (Áo sơ mi Oxford) bởi User 5 (Lê Hoàng Nam)
(3, 5, 4, 4, N'Sơ mi đứng form lịch sự, cổ áo dệt cứng cáp chuẩn phong cách công sở. Vải dệt tổ ong hơi dày nhẹ nhưng thấm hút tốt.', 1, '2026-10-28 08:45:00'),

-- Đánh giá sản phẩm 11 (Quần Jeans nữ) bởi User 6 (Phạm Thu Hà)
(4, 6, 11, 5, N'Quần cạp cao hack chân dài miên man, ống loe nhẹ che khuyết điểm bắp chân to cực kỳ ưng ý. Sẽ tiếp tục ủng hộ shop!', 1, '2026-11-04 18:20:00'),

-- Đánh giá sản phẩm 8 (Áo Hoodie nỉ bông) bởi User 3 (Nguyễn Văn An)
(5, 3, 8, 5, N'Áo nỉ bông ấm áp, form rộng rãi thoải mái mặc đi chơi mùa lạnh là hết nước chấm. Mũ trùm to dày dặn.', 1, '2026-11-07 21:10:00'),

-- Đánh giá spam/tiêu cực bị ẩn (is_visible = 0) bởi User 4
(6, 4, 3, 1, N'[Bình luận vi phạm] Mời tham gia hội nhóm nhận quà miễn phí tại đường link abcxyz...', 0, '2026-10-20 12:00:00');

SET IDENTITY_INSERT [Reviews] OFF;
GO
PRINT N'>> Đã chèn 6 Reviews mẫu (trong đó có 1 review vi phạm bị ẩn để test kiểm duyệt).';
GO

PRINT N'==========================================================================================';
PRINT N'>> CHÚC MỪNG: ĐÃ NẠP TOÀN BỘ DỮ LIỆU SEED_DB THÀNH CÔNG VÀO CƠ SỞ DỮ LIỆU CLOTHINGSTOREDB!';
PRINT N'==========================================================================================';
