package com.feirui.rental.dto.car;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 車輛資訊回傳 DTO
 *
 * 為什麼不直接回傳 Car Entity？
 *   1. Car Entity 有 @ManyToOne 關聯物件（CarCategory），
 *      直接序列化（轉成 JSON）可能引發無限遞迴或 Lazy Load 例外
 *   2. DTO 可以控制只回傳前端需要的欄位，例如不需要把 createdAt 給前端
 *
 * 後端回傳給前端的 JSON 格式範例：
 * {
 *   "id": 1,
 *   "name": "Porsche 911 GTS",
 *   "brand": "Porsche",
 *   "categoryName": "尊榮車型",
 *   "pricePerDay": 8000.00,
 *   "seats": 4,
 *   "luggageDesc": "可放 2 個 26 吋行李箱",
 *   "description": "...",
 *   "mainImage": "/img/cars/porsche_911.jpg",
 *   "images": ["/img/cars/porsche_911.jpg", "/img/cars/porsche_911_2.jpg"]
 * }
 */
@Getter
@Setter
public class CarResponse {

    private Integer id;

    private String name;         // 車型名稱

    private String brand;        // 品牌

    private String categoryName; // 分類名稱（從關聯的 CarCategory.name 取出）

    private BigDecimal pricePerDay; // 每日租金

    private Integer seats;       // 座位數

    private String luggageDesc;  // 行李箱描述

    private String description;  // 詳細介紹

    private String mainImage;    // 主圖路徑（sort_order = 0 的那張）

    private List<String> images; // 所有圖片路徑列表
}
