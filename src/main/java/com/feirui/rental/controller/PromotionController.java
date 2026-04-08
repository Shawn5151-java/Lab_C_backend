package com.feirui.rental.controller;

import com.feirui.rental.entity.Promotion;
import com.feirui.rental.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 優惠活動 Controller
 * 負責優惠活動查詢的 REST API
 *
 * SecurityConfig 中已設定 GET /api/promotions/** 為公開路徑，不需要登入
 */
@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    /**
     * 查詢所有上架中的活動（依建立時間倒序）
     * GET /api/promotions
     *
     * 對應前台 Promotions.html 的活動列表
     *
     * 前端呼叫範例：
     *   GET http://localhost:8080/api/promotions
     */
    @GetMapping
    public ResponseEntity<List<Promotion>> getAllActivePromotions() {
        return ResponseEntity.ok(promotionService.getAllActivePromotions());
    }

    /**
     * 依 slug 查詢活動詳情
     * GET /api/promotions/{slug}
     *
     * 對應前台 Promotion_Detail.html 的活動詳情頁
     *
     * 前端呼叫範例：
     *   GET http://localhost:8080/api/promotions/2025-summer-sale
     */
    @GetMapping("/{slug}")
    public ResponseEntity<Promotion> getPromotionBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(promotionService.getPromotionBySlug(slug));
    }
}
