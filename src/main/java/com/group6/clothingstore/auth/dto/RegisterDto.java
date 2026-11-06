

package com.group6.clothingstore.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterDto {
    @NotBlank(message = "Họ và tên không được để trống!")
    @Size(max = 100, message = "Họ và tên tối đa 100 ký tự!")
    private String fullName;

    @NotBlank(message = "Email không được để trống!")
    @Email(message = "Địa chỉ email không đúng định dạng!")
    @Size(max = 100, message = "Email tối đa 100 ký tự!")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống!")
    @Size(min = 6, max = 32, message = "Mật khẩu phải có từ 6 đến 32 ký tự!")
    private String password;

    @NotBlank(message = "Vui lòng nhập lại mật khẩu xác nhận!")
    private String confirmPassword;

    @Pattern(regexp = "^$|^[0-9]{10}$", message = "Số điện thoại phải gồm 10 chữ số!")
    private String phone;
}
