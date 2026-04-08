package com.feirui.rental.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 車輛圖片 Entity，對應資料庫 car_images 表
 * 一台車可以有多張圖片，用 sort_order 控制顯示順序
 */
@Getter
@Setter
@Entity
@Table(name = "car_images")
public class CarImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 多對一關係：多張圖片屬於同一台車
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id", nullable = false) // 外鍵 car_id
    private Car car;

    @Column(name = "image_path", nullable = false, length = 255)
    private String imagePath; // 圖片路徑，例如 /img/cars/porsche_911.jpg

    @Column(name = "sort_order")
    private Integer sortOrder = 0; // 排序數字，數字越小越靠前，0 = 第一張（主圖）
}
