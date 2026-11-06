package com.group6.clothingstore.user.service;

import com.group6.clothingstore.shared.enums.UserStatus;
import com.group6.clothingstore.user.dto.ChangePasswordDto;
import com.group6.clothingstore.user.dto.ProfileUpdateDto;
import com.group6.clothingstore.user.entity.User;
import com.group6.clothingstore.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * FR03: Xem hồ sơ cá nhân theo Email đăng nhập
     */
    @Transactional(readOnly = true)
    public User getProfileByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin tài khoản!"));
    }

    /**
     * FR04: Cập nhật thông tin cá nhân (Họ tên, SĐT, Địa chỉ)
     */
    @Transactional
    public void updateProfile(String email, ProfileUpdateDto dto) {
        User user = getProfileByEmail(email);
        user.setFullName(dto.getFullName().trim());
        user.setPhone(dto.getPhone().trim());
        user.setAddress(dto.getAddress().trim());
        userRepository.save(user);
        log.info("Cập nhật thông tin thành công cho: {}", email);
    }

    /**
     * FR04: Đổi mật khẩu cá nhân
     */
    @Transactional
    public void changePassword(String email, ChangePasswordDto dto) {
        User user = getProfileByEmail(email);

        // 1. Kiểm tra mật khẩu cũ
        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không chính xác!");
        }

        // 2. Kiểm tra mật khẩu mới và xác nhận mật khẩu (ERR-02)
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không trùng khớp!");
        }

        // 3. Cập nhật mật khẩu băm mới
        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
        log.info("Đổi mật khẩu thành công cho: {}", email);
    }

    /**
     * FR05: Admin xem danh sách, tìm kiếm và phân trang khách hàng
     */
    @Transactional(readOnly = true)
    public Page<User> getUsers(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.isBlank()) {
            String trimmed = keyword.trim();
            return userRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(trimmed, trimmed, pageable);
        }
        return userRepository.findAll(pageable);
    }

    /**
     * FR05 & BR08: Khóa / Mở khóa tài khoản người dùng
     * Quy tắc BR08: Admin không thể tự khóa tài khoản của chính mình.
     */
    @Transactional
    public void toggleUserStatus(Long targetUserId, String currentAdminEmail) {
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại!"));

        // Kiểm tra quy tắc BR08
        if (targetUser.getEmail().equalsIgnoreCase(currentAdminEmail)) {
            throw new IllegalStateException("Bạn không thể tự khóa tài khoản của chính mình!");
        }

        // Đảo trạng thái: ACTIVE <-> LOCKED
        if (targetUser.getStatus() == UserStatus.ACTIVE) {
            targetUser.setStatus(UserStatus.LOCKED);
            log.info("Admin {} đã KHÓA tài khoản {}", currentAdminEmail, targetUser.getEmail());
        } else {
            targetUser.setStatus(UserStatus.ACTIVE);
            log.info("Admin {} đã MỞ KHÓA tài khoản {}", currentAdminEmail, targetUser.getEmail());
        }

        userRepository.save(targetUser);
    }
}