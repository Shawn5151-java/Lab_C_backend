package com.feirui.rental.dto.admin;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 管理員後台 - 加購項目列表回應 DTO
 */
@Getter
@Setter
public class AdminAddonResponse {

    private Integer id;
    private String name;
    private BigDecimal pricePerDay;
    private String description;
    private Boolean isActive;
}
