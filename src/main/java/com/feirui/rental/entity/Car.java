package com.feirui.rental.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 車輛 Entity，對應資料庫 cars 表
 * 一台車屬於一個分類（CarCategory），並可擁有多張圖片（CarImage）
 */
@Getter
@Setter
@Entity
@Table(name = "cars")
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 多對一關係：多台車屬於同一個分類
     * fetch = LAZY 代表「延遲載入」，查詢車輛時不會自動把分類資料也一起撈出來，
     * 只有在你真正呼叫 car.getCategory() 時才去資料庫查，可以提升效能
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false) // 對應資料庫的外鍵欄位 category_id
    private CarCategory category;

    @Column(nullable = false, length = 100)
    private String name; // 車型名稱，例如「Porsche 911 GTS」

    @Column(nullable = false, length = 50)
    private String brand; // 品牌，例如「Porsche」

    /**
     * BigDecimal 用於金額計算，比 double 精確，不會有浮點數誤差
     * precision = 10 代表總共最多 10 位數字
     * scale = 2 代表小數點後最多 2 位，例如 1500.00
     */
    @Column(name = "price_per_day", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerDay; // 每日租金

    @Column(nullable = false)
    private Integer seats; // 座位數

    @Column(name = "luggage_desc", length = 100)
    private String luggageDesc; // 行李箱描述，例如「可放 2 個 26 吋行李箱」

    @Column(columnDefinition = "TEXT") // TEXT 類型，可存放較長的文字
    private String description; // 車輛詳細介紹

    @Column(name = "is_available")
    private Boolean isAvailable = true; // 是否上架（false = 下架，不顯示給客人）

    /**
     * @CreationTimestamp：插入資料時自動填入當下時間，之後不會再改變
     * updatable = false：禁止 JPA 更新這個欄位
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // 建立時間

    /**
     * 一對多關係：一台車可以有多張圖片
     * mappedBy = "car" 代表關係由 CarImage 的 car 欄位來維護
     * cascade = ALL 代表對 Car 做任何操作（刪除、更新），都會同步套用到圖片
     */
    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CarImage> images;
}
