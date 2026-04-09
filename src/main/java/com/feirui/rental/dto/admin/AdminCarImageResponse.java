package com.feirui.rental.dto.admin;

import lombok.Getter;
import lombok.Setter;

/**
 * 車輛圖片回應 DTO
 */
@Getter
@Setter
public class AdminCarImageResponse {
    private Integer id;
    private String imagePath;   // 圖片路徑（/uploads/cars/xxx.jpg 或原始路徑）
    private Integer sortOrder;
}
