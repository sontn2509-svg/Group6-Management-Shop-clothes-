package com.group6.clothingstore.order.dto;

import com.group6.clothingstore.shared.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutDto {

    @NotBlank(message = "Họ và tên người nhận không được để trống!")
    @Size(max = 100, message = "Họ và tên tối đa 100 ký tự!")
    private String receiverName;

    @NotBlank(message = "Số điện thoại nhận hàng không được để trống!")
    @Pattern(regexp = "^[0-9]{10}$", message = "Số điện thoại phải gồm đúng 10 chữ số!")
    private String receiverPhone;

    @NotBlank(message = "Địa chỉ nhận hàng không được để trống!")
    @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự!")
    private String shippingAddress;

    @Size(max = 255, message = "Ghi chú tối đa 255 ký tự!")
    private String note;

    @NotNull(message = "Vui lòng chọn phương thức thanh toán!")
    private PaymentMethod paymentMethod;
}