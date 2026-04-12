package com.feirui.rental.dto.admin;

import lombok.Getter;
import lombok.Setter;

/**
 * 管理員後台 - 優惠活動列表回應 DTO
 */
@Getter
@Setter
public class AdminPromotionResponse {

    private Integer id;
    private String slug;
    private String title;
    private String tag;
    private String intro;
    private String periodStart;  // LocalDate formatted as "yyyy-MM-dd"
    private String periodEnd;    // LocalDate formatted as "yyyy-MM-dd"
    private Boolean isActive;
    private String imagePath;
    private String content;
    private String createdAt;    // LocalDateTime formatted as "yyyy-MM-dd HH:mm"
}
