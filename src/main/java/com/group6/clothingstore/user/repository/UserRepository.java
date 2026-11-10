package com.group6.clothingstore.user.repository;

import com.group6.clothingstore.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Tìm người dùng dựa theo địa chỉ email.
     */
    public Optional<User> findByEmail(String email);
    /**
     * Kiểm tra xem email đã tồn tại trong hệ thống hay chưa
     */
    public boolean existsByEmail(String email);
    /**
     * Dùng cho Admin Quản lý tài khoản (FR05 - SCR-13).
     * Tìm kiếm người dùng theo tên hoặc email (không phân biệt hoa/thường) kèm phân trang Pageable.
     */
    public Page<User> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String fullName,
            String email,
            Pageable pageable
    );

    long countByRole(com.group6.clothingstore.shared.enums.Role role);
}
