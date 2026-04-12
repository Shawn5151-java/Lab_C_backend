package com.feirui.rental.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 管理員新增優惠活動的請求 DTO
 */
@Getter
@Setter
public class AdminPromotionCreateRequest {

    @NotBlank(message = "標題不可為空")
    private String title;

    @NotBlank(message = "Slug 不可為空")
    private String slug;

    private String tag;
    private String intro;
    private String content;
    private String periodStart; // yyyy-MM-dd
    private String periodEnd;   // yyyy-MM-dd
}
