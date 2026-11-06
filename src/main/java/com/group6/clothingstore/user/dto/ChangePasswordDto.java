package com.group6.clothingstore.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangePasswordDto {

    @NotBlank(message = "Mật khẩu hiện tại không được để trống!")
    private String currentPassword;

    @NotBlank(message = "Mật khẩu mới không được để trống!")
    @Size(min = 6, max = 32, message = "Mật khẩu mới phải từ 6 đến 32 ký tự!")
    private String newPassword;

    @NotBlank(message = "Vui lòng nhập lại mật khẩu mới!")
    private String confirmPassword;
}