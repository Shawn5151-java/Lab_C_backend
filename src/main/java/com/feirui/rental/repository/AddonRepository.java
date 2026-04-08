package com.feirui.rental.repository;

import com.feirui.rental.entity.Addon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 加購項目 Repository
 */
public interface AddonRepository extends JpaRepository<Addon, Integer> {

    /**
     * 查詢所有啟用中的加購項目（isActive = true）
     * 用於租車步驟 2 的加購選項顯示
     * 下架的項目（isActive = false）不會出現在前台
     */
    List<Addon> findByIsActiveTrue();
}
