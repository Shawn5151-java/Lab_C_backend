package com.feirui.rental.service;

import com.feirui.rental.entity.Promotion;
import com.feirui.rental.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 優惠活動服務
 * 負責查詢優惠活動（對應前台 Promotions.html 和 Promotion_Detail.html）
 *
 * 這個服務直接回傳 Entity，因為 Promotion 沒有敏感欄位，
 * 也沒有複雜的關聯物件，不需要特別建立 DTO。
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 這個 Service 全部都是讀取，設定為 readOnly 提升效能
public class PromotionService {

    private final PromotionRepository promotionRepository;

    /**
     * 查詢所有上架中的活動
     * 依建立時間倒序（最新的在最前面）
     *
     * @return 活動列表
     */
    public List<Promotion> getAllActivePromotions() {
        return promotionRepository.findByIsActiveTrueOrderByCreatedAtDesc();
    }

    /**
     * 依 slug 查詢活動詳情
     * 用於活動詳情頁，例如網址 /promotions/2025-summer-sale
     *
     * @param slug URL 用的短字串，例如 "2025-summer-sale"
     * @return 活動詳情
     */
    public Promotion getPromotionBySlug(String slug) {
        return promotionRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("找不到活動：" + slug));
    }
}
