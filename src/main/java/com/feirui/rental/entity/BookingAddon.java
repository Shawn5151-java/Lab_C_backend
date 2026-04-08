package com.feirui.rental.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 訂單加購明細 Entity，對應資料庫 booking_addons 表
 * 這是「訂單」與「加購項目」之間的中間表（多對多關係的橋梁）
 *
 * 為什麼需要這張表？
 * 一筆訂單可以選多個加購項目，一個加購項目也可以被多筆訂單選取，
 * 這種「多對多」關係需要一張中間表來記錄。
 * 同時這裡也儲存了「加購當下的單價快照」，避免日後改價影響舊訂單。
 */
@Getter
@Setter
@Entity
@Table(name = "booking_addons")
public class BookingAddon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 多對一：多筆加購明細屬於同一筆訂單
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    /**
     * 多對一：多筆加購明細可以指向同一個加購項目
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "addon_id", nullable = false)
    private Addon addon;

    @Column(name = "price_snapshot", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceSnapshot; // 加購時的每日單價快照（防止日後調價影響舊單）
}
