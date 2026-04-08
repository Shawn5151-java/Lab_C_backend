package com.feirui.rental.repository;

import com.feirui.rental.entity.CarCategory;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 車款分類 Repository
 *
 * JpaRepository<CarCategory, Integer> 的意思：
 *   - 第一個參數 CarCategory = 這個 Repository 操作的 Entity 類別
 *   - 第二個參數 Integer     = 主鍵（id）的資料型別
 *
 * 繼承 JpaRepository 後，以下方法就免費自動擁有，不需要自己寫：
 *   - save(entity)        → 新增或更新
 *   - findById(id)        → 依 id 查詢單筆
 *   - findAll()           → 查詢全部
 *   - deleteById(id)      → 依 id 刪除
 *   - count()             → 計算總筆數
 *   - existsById(id)      → 確認某筆資料是否存在
 */
public interface CarCategoryRepository extends JpaRepository<CarCategory, Integer> {
    // 目前車款分類不需要額外的查詢方法，基本 CRUD 已足夠
}
