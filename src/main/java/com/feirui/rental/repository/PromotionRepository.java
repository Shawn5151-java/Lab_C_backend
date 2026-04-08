package com.feirui.rental.repository;

import com.feirui.rental.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 優惠活動 Repository
 */
public interface PromotionRepository extends JpaRepository<Promotion, Integer> {

    /**
     * 查詢所有上架中的活動，並依建立時間倒序排列（最新的在最前面）
     * 對應方法名稱分解：
     *   findBy + IsActive + True → WHERE is_active = 1
     *   OrderBy + CreatedAt + Desc → ORDER BY created_at DESC
     */
    List<Promotion> findByIsActiveTrueOrderByCreatedAtDesc();

    /**
     * 依 slug 查詢單一活動
     * 用於活動詳情頁，例如 URL /promotions/2025-summer-sale 就傳入 "2025-summer-sale"
     */
    Optional<Promotion> findBySlug(String slug);
}
