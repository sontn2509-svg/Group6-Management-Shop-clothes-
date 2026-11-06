package com.group6.clothingstore.auth.service;

import com.group6.clothingstore.shared.enums.UserStatus;
import com.group6.clothingstore.user.entity.User;
import com.group6.clothingstore.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email));

        log.info("Đang xác thực người dùng: {}, Vai trò: {}", user.getEmail(), user.getRole());

        // Gán Role (Spring Security quy ước prefix ROLE_)
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );

        // Kiểm tra trạng thái tài khoản theo quy tắc BR08 & mã lỗi ERR-04
        boolean active = (user.getStatus() == UserStatus.ACTIVE);
        boolean accountNotLocked = (user.getStatus() != UserStatus.LOCKED);

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                active,             // enabled
                true,               // accountNonExpired
                true,               // credentialsNonExpired
                accountNotLocked,   // accountNonLocked (nếu false -> Spring Security tự ném LockedException)
                authorities
        );
    }
}
