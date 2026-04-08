package com.feirui.rental.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 車款分類 Entity，對應資料庫 car_categories 表
 *
 * 例如：
 *   name = "一般車型"，code = "ECONOMY"
 *   name = "尊榮車型"，code = "PREMIUM"
 *   name = "休旅車型"，code = "SUV"
 *   name = "電動車型"，code = "EV"
 */
@Getter  // Lombok：自動產生所有欄位的 getter 方法
@Setter  // Lombok：自動產生所有欄位的 setter 方法
@Entity  // 告訴 JPA 這個 class 對應資料庫的一張表
@Table(name = "car_categories") // 指定對應的資料表名稱
public class CarCategory {

    @Id // 這個欄位是主鍵
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 主鍵由資料庫自動遞增（AUTO_INCREMENT）
    private Integer id;

    @Column(nullable = false, length = 50) // 不可為 NULL，最大長度 50
    private String name; // 分類顯示名稱，例如「一般車型」

    @Column(nullable = false, unique = true, length = 20) // 不可為 NULL，且值必須唯一
    private String code; // 分類代碼，例如 ECONOMY / PREMIUM / SUV / EV
}
