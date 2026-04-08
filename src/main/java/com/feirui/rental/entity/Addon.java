package com.feirui.rental.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 加購項目 Entity，對應資料庫 addons 表
 * 例如：兒童安全座椅、GPS、超安心全險、行車紀錄器、Wi-Fi
 * 每個加購項目都有每日費用，租幾天就乘以幾天
 */
@Getter
@Setter
@Entity
@Table(name = "addons")
public class Addon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name; // 加購項目名稱，例如「GPS 導航」

    @Column(name = "price_per_day", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerDay; // 每日加購費用，例如 200.00

    @Column(length = 255)
    private String description; // 項目說明

    @Column(name = "is_active")
    private Boolean isActive = true; // 是否啟用（false = 下架，不顯示給客人）
}
