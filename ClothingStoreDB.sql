USE [master]
GO

/****** Object:  Database [ClothingStoreDB] ******/
IF DB_ID(N'ClothingStoreDB') IS NOT NULL
BEGIN
    ALTER DATABASE [ClothingStoreDB] SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE [ClothingStoreDB];
END
GO

CREATE DATABASE [ClothingStoreDB];
GO

USE [ClothingStoreDB];
GO

/****** Object:  Table [dbo].[Users] ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Users](
	[user_id] [bigint] IDENTITY(1,1) NOT NULL,
	[full_name] [nvarchar](100) NOT NULL,
	[email] [varchar](100) NOT NULL,
	[password_hash] [varchar](255) NOT NULL,
	[phone] [varchar](15) NULL,
	[address] [nvarchar](255) NULL,
	[role] [varchar](20) NOT NULL DEFAULT ('CUSTOMER'),
	[status] [varchar](20) NOT NULL DEFAULT ('ACTIVE'),
	[created_at] [datetime] NULL DEFAULT (getdate()),
PRIMARY KEY CLUSTERED 
(
	[user_id] ASC
),
UNIQUE NONCLUSTERED 
(
	[email] ASC
)
) ON [PRIMARY]
GO

/****** Object:  Table [dbo].[Categories] ******/
CREATE TABLE [dbo].[Categories](
	[category_id] [int] IDENTITY(1,1) NOT NULL,
	[category_name] [nvarchar](100) NOT NULL,
	[description] [nvarchar](255) NULL,
	[status] [varchar](20) NOT NULL DEFAULT ('ACTIVE'),
PRIMARY KEY CLUSTERED 
(
	[category_id] ASC
),
UNIQUE NONCLUSTERED 
(
	[category_name] ASC
)
) ON [PRIMARY]
GO

/****** Object:  Table [dbo].[Products] ******/
CREATE TABLE [dbo].[Products](
	[product_id] [bigint] IDENTITY(1,1) NOT NULL,
	[category_id] [int] NOT NULL,
	[product_name] [nvarchar](200) NOT NULL,
	[price] [decimal](18, 2) NOT NULL,
	[stock_quantity] [int] NOT NULL DEFAULT ((0)),
	[available_sizes] [varchar](50) NOT NULL,
	[color] [nvarchar](50) NOT NULL,
	[material] [nvarchar](100) NULL,
	[gender] [varchar](20) NOT NULL DEFAULT ('UNISEX'),
	[description] [nvarchar](max) NULL,
	[image_url] [varchar](500) NULL,
	[status] [varchar](20) NOT NULL DEFAULT ('ACTIVE'),
	[created_at] [datetime] NULL DEFAULT (getdate()),
PRIMARY KEY CLUSTERED 
(
	[product_id] ASC
)
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO

/****** Object:  Table [dbo].[CartItems] ******/
CREATE TABLE [dbo].[CartItems](
	[cart_item_id] [bigint] IDENTITY(1,1) NOT NULL,
	[user_id] [bigint] NOT NULL,
	[product_id] [bigint] NOT NULL,
	[selected_size] [varchar](10) NOT NULL,
	[quantity] [int] NOT NULL DEFAULT ((1)),
PRIMARY KEY CLUSTERED 
(
	[cart_item_id] ASC
)
) ON [PRIMARY]
GO

/****** Object:  Table [dbo].[Orders] ******/
CREATE TABLE [dbo].[Orders](
	[order_id] [bigint] IDENTITY(1,1) NOT NULL,
	[user_id] [bigint] NOT NULL,
	[receiver_name] [nvarchar](100) NOT NULL,
	[receiver_phone] [varchar](15) NOT NULL,
	[shipping_address] [nvarchar](255) NOT NULL,
	[note] [nvarchar](255) NULL,
	[total_amount] [decimal](18, 2) NOT NULL,
	[payment_method] [varchar](30) NOT NULL DEFAULT ('COD'),
	[order_status] [varchar](20) NOT NULL DEFAULT ('PENDING'),
	[order_date] [datetime] NULL DEFAULT (getdate()),
PRIMARY KEY CLUSTERED 
(
	[order_id] ASC
)
) ON [PRIMARY]
GO

/****** Object:  Table [dbo].[OrderDetails] ******/
CREATE TABLE [dbo].[OrderDetails](
	[order_detail_id] [bigint] IDENTITY(1,1) NOT NULL,
	[order_id] [bigint] NOT NULL,
	[product_id] [bigint] NOT NULL,
	[selected_size] [varchar](10) NOT NULL,
	[quantity] [int] NOT NULL,
	[unit_price] [decimal](18, 2) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[order_detail_id] ASC
)
) ON [PRIMARY]
GO

/****** Object:  Table [dbo].[Reviews] ******/
CREATE TABLE [dbo].[Reviews](
	[review_id] [bigint] IDENTITY(1,1) NOT NULL,
	[user_id] [bigint] NOT NULL,
	[product_id] [bigint] NOT NULL,
	[rating] [int] NOT NULL,
	[comment] [nvarchar](500) NULL,
	[is_visible] [bit] NOT NULL DEFAULT ((1)),
	[created_at] [datetime] NULL DEFAULT (getdate()),
PRIMARY KEY CLUSTERED 
(
	[review_id] ASC
)
) ON [PRIMARY]
GO

-- KHÓA NGOẠI (FOREIGN KEYS)
ALTER TABLE [dbo].[Products] WITH CHECK ADD CONSTRAINT [FK_Products_Categories] FOREIGN KEY([category_id])
REFERENCES [dbo].[Categories] ([category_id])
GO
ALTER TABLE [dbo].[Products] CHECK CONSTRAINT [FK_Products_Categories]
GO

ALTER TABLE [dbo].[CartItems] WITH CHECK ADD CONSTRAINT [FK_CartItems_Users] FOREIGN KEY([user_id])
REFERENCES [dbo].[Users] ([user_id]) ON DELETE CASCADE
GO
ALTER TABLE [dbo].[CartItems] CHECK CONSTRAINT [FK_CartItems_Users]
GO

ALTER TABLE [dbo].[CartItems] WITH CHECK ADD CONSTRAINT [FK_CartItems_Products] FOREIGN KEY([product_id])
REFERENCES [dbo].[Products] ([product_id])
GO
ALTER TABLE [dbo].[CartItems] CHECK CONSTRAINT [FK_CartItems_Products]
GO

ALTER TABLE [dbo].[Orders] WITH CHECK ADD CONSTRAINT [FK_Orders_Users] FOREIGN KEY([user_id])
REFERENCES [dbo].[Users] ([user_id])
GO
ALTER TABLE [dbo].[Orders] CHECK CONSTRAINT [FK_Orders_Users]
GO

ALTER TABLE [dbo].[OrderDetails] WITH CHECK ADD CONSTRAINT [FK_OrderDetails_Orders] FOREIGN KEY([order_id])
REFERENCES [dbo].[Orders] ([order_id]) ON DELETE CASCADE
GO
ALTER TABLE [dbo].[OrderDetails] CHECK CONSTRAINT [FK_OrderDetails_Orders]
GO

ALTER TABLE [dbo].[OrderDetails] WITH CHECK ADD CONSTRAINT [FK_OrderDetails_Products] FOREIGN KEY([product_id])
REFERENCES [dbo].[Products] ([product_id])
GO
ALTER TABLE [dbo].[OrderDetails] CHECK CONSTRAINT [FK_OrderDetails_Products]
GO

ALTER TABLE [dbo].[Reviews] WITH CHECK ADD CONSTRAINT [FK_Reviews_Users] FOREIGN KEY([user_id])
REFERENCES [dbo].[Users] ([user_id])
GO
ALTER TABLE [dbo].[Reviews] CHECK CONSTRAINT [FK_Reviews_Users]
GO

ALTER TABLE [dbo].[Reviews] WITH CHECK ADD CONSTRAINT [FK_Reviews_Products] FOREIGN KEY([product_id])
REFERENCES [dbo].[Products] ([product_id])
GO
ALTER TABLE [dbo].[Reviews] CHECK CONSTRAINT [FK_Reviews_Products]
GO

-- RÀNG BUỘC KIỂM TRA (CHECK CONSTRAINTS)
ALTER TABLE [dbo].[Users] WITH CHECK ADD CONSTRAINT [CHK_User_Role] CHECK (([role]='ADMIN' OR [role]='CUSTOMER'))
GO
ALTER TABLE [dbo].[Users] CHECK CONSTRAINT [CHK_User_Role]
GO

ALTER TABLE [dbo].[Users] WITH CHECK ADD CONSTRAINT [CHK_User_Status] CHECK (([status]='LOCKED' OR [status]='ACTIVE'))
GO
ALTER TABLE [dbo].[Users] CHECK CONSTRAINT [CHK_User_Status]
GO

ALTER TABLE [dbo].[Categories] WITH CHECK ADD CONSTRAINT [CHK_Category_Status] CHECK (([status]='INACTIVE' OR [status]='ACTIVE'))
GO
ALTER TABLE [dbo].[Categories] CHECK CONSTRAINT [CHK_Category_Status]
GO

ALTER TABLE [dbo].[Products] WITH CHECK ADD CONSTRAINT [CHK_Product_Gender] CHECK (([gender]='UNISEX' OR [gender]='WOMEN' OR [gender]='MEN'))
GO
ALTER TABLE [dbo].[Products] CHECK CONSTRAINT [CHK_Product_Gender]
GO

ALTER TABLE [dbo].[Products] WITH CHECK ADD CONSTRAINT [CHK_Product_Price] CHECK (([price]>(0)))
GO
ALTER TABLE [dbo].[Products] CHECK CONSTRAINT [CHK_Product_Price]
GO

ALTER TABLE [dbo].[Products] WITH CHECK ADD CONSTRAINT [CHK_Product_Status] CHECK (([status]='INACTIVE' OR [status]='OUT_OF_STOCK' OR [status]='ACTIVE'))
GO
ALTER TABLE [dbo].[Products] CHECK CONSTRAINT [CHK_Product_Status]
GO

ALTER TABLE [dbo].[Products] WITH CHECK ADD CONSTRAINT [CHK_Product_Stock] CHECK (([stock_quantity]>=(0)))
GO
ALTER TABLE [dbo].[Products] CHECK CONSTRAINT [CHK_Product_Stock]
GO

ALTER TABLE [dbo].[CartItems] WITH CHECK ADD CONSTRAINT [CHK_CartItem_Quantity] CHECK (([quantity]>(0)))
GO
ALTER TABLE [dbo].[CartItems] CHECK CONSTRAINT [CHK_CartItem_Quantity]
GO

ALTER TABLE [dbo].[Orders] WITH CHECK ADD CONSTRAINT [CHK_Order_Payment] CHECK (([payment_method]='BANK_TRANSFER' OR [payment_method]='COD'))
GO
ALTER TABLE [dbo].[Orders] CHECK CONSTRAINT [CHK_Order_Payment]
GO

ALTER TABLE [dbo].[Orders] WITH CHECK ADD CONSTRAINT [CHK_Order_Status] CHECK (([order_status]='CANCELLED' OR [order_status]='DELIVERED' OR [order_status]='SHIPPING' OR [order_status]='CONFIRMED' OR [order_status]='PENDING'))
GO
ALTER TABLE [dbo].[Orders] CHECK CONSTRAINT [CHK_Order_Status]
GO

ALTER TABLE [dbo].[Orders] WITH CHECK ADD CONSTRAINT [CHK_Order_Total] CHECK (([total_amount]>=(0)))
GO
ALTER TABLE [dbo].[Orders] CHECK CONSTRAINT [CHK_Order_Total]
GO

ALTER TABLE [dbo].[OrderDetails] WITH CHECK ADD CONSTRAINT [CHK_OrderDetail_Quantity] CHECK (([quantity]>(0)))
GO
ALTER TABLE [dbo].[OrderDetails] CHECK CONSTRAINT [CHK_OrderDetail_Quantity]
GO

ALTER TABLE [dbo].[OrderDetails] WITH CHECK ADD CONSTRAINT [CHK_OrderDetail_UnitPrice] CHECK (([unit_price]>(0)))
GO
ALTER TABLE [dbo].[OrderDetails] CHECK CONSTRAINT [CHK_OrderDetail_UnitPrice]
GO

ALTER TABLE [dbo].[Reviews] WITH CHECK ADD CONSTRAINT [CHK_Review_Rating] CHECK (([rating]>=(1) AND [rating]<=(5)))
GO
ALTER TABLE [dbo].[Reviews] CHECK CONSTRAINT [CHK_Review_Rating]
GO

PRINT N'>> ĐÃ TẠO DATABASE ClothingStoreDB VÀ CÁC BẢNG THÀNH CÔNG!';
GO
