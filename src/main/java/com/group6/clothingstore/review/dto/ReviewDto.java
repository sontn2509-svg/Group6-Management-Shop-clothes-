package com.group6.clothingstore.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDto {

    @NotNull(message = "Vui lòng chọn số sao đánh giá (1-5 sao)!")
    @Min(value = 1, message = "Đánh giá tối thiểu 1 sao!")
    @Max(value = 5, message = "Đánh giá tối đa 5 sao!")
    private Integer rating;

    @NotBlank(message = "Vui lòng nhập nhận xét về sản phẩm!")
    @Size(max = 500, message = "Nội dung nhận xét tối đa 500 ký tự!")
    private String comment;
}
