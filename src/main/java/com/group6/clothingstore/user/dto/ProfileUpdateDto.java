package com.group6.clothingstore.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileUpdateDto {
    @NotBlank(message = "Họ và tên không được để trống!")
    @Size(max = 100, message = "Họ và tên tối đa 100 ký tự!")
    private String fullName;

    @NotBlank(message = "Số điện thoại không được để trống!")
    @Pattern(regexp = "^[0-9]{10}$", message = "Số điện thoại phải gồm đúng 10 chữ số!")
    private String phone;

    @NotBlank(message = "Địa chỉ không được để trống!")
    @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự!")
    private String address;
}
