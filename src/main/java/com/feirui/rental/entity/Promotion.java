package com.feirui.rental.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 優惠活動 Entity，對應資料庫 promotions 表
 * 對應前台的「優惠活動」頁面（Promotions.html）和活動詳情頁（Promotion_Detail.html）
 */
@Getter
@Setter
@Entity
@Table(name = "promotions")
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * slug 是給 URL 使用的短字串，例如 "2025-summer-sale"
     * 前台網址會長這樣：/promotions/2025-summer-sale
     * unique = true 確保每個活動的 URL 不重複
     */
    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(length = 50)
    private String tag; // 標籤，例如「限時特惠」、「連假活動」，顯示在活動卡片上

    @Column(nullable = false, length = 200)
    private String title; // 活動標題

    @Column(columnDefinition = "TEXT")
    private String intro; // 活動簡介（顯示在列表頁的卡片描述）

    @Column(columnDefinition = "LONGTEXT")
    private String content; // 活動完整內容（顯示在詳情頁，可存放 HTML）

    @Column(name = "image_path", length = 255)
    private String imagePath; // 活動封面圖片路徑

    @Column(name = "period_start")
    private LocalDate periodStart; // 活動開始日期

    @Column(name = "period_end")
    private LocalDate periodEnd; // 活動結束日期

    @Column(name = "is_active")
    private Boolean isActive = true; // 是否上架（false = 下架，不顯示給客人）

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // 建立時間
}
