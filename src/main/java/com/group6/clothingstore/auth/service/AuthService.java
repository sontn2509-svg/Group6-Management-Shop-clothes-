package com.group6.clothingstore.auth.service;

import com.group6.clothingstore.auth.dto.RegisterDto;
import com.group6.clothingstore.shared.enums.Role;
import com.group6.clothingstore.shared.enums.UserStatus;
import com.group6.clothingstore.user.entity.User;
import com.group6.clothingstore.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void register(RegisterDto dto) {
        // 1. Kiểm tra email đã tồn tại chưa (ERR-01)
        if (userRepository.existsByEmail(dto.getEmail().trim())) {
            throw new IllegalArgumentException("Email này đã được sử dụng. Vui lòng chọn một email khác!");
        }

        // 2. Kiểm tra mật khẩu xác nhận (ERR-02)
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không trùng khớp!");
        }

        // 3. Tạo User mới và băm mật khẩu bằng BCrypt (NFR02)
        User user = User.builder()
                .fullName(dto.getFullName().trim())
                .email(dto.getEmail().trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .phone(dto.getPhone() != null && !dto.getPhone().isBlank() ? dto.getPhone().trim() : null)
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(user);
        log.info("Đăng ký thành công tài khoản: {}", user.getEmail());
    }
}