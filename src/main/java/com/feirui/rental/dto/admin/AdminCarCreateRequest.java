package com.feirui.rental.dto.admin;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 管理員新增車輛的請求 DTO
 */
@Getter
@Setter
public class AdminCarCreateRequest {

    @NotNull(message = "分類 ID 不可為空")
    private Integer categoryId;

    @NotBlank(message = "車款名稱不可為空")
    private String name;

    @NotBlank(message = "品牌不可為空")
    private String brand;

    @NotNull(message = "日租金不可為空")
    @DecimalMin(value = "0.01", message = "日租金必須大於 0")
    private BigDecimal pricePerDay;

    @NotNull(message = "座位數不可為空")
    @Min(value = 1, message = "座位數至少 1 位")
    private Integer seats;

    private String luggageDesc;

    private String description;
}
