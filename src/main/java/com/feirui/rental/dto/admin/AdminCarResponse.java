package com.feirui.rental.dto.admin;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 管理員後台 - 車輛列表回應 DTO
 */
@Getter
@Setter
public class AdminCarResponse {

    private Integer id;
    private String name;
    private String brand;
    private String categoryName;
    private BigDecimal pricePerDay;
    private Integer seats;
    private Boolean isAvailable;
    private String mainImage;
    private Integer totalBookings = 0;
}
