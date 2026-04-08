package com.feirui.rental.repository;

import com.feirui.rental.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 聯絡表單 Repository
 * 基本 CRUD 已足夠使用：
 *   - save()    → 儲存客人送出的表單
 *   - findAll() → 管理員查看所有詢問
 */
public interface ContactRepository extends JpaRepository<Contact, Integer> {

    /**
     * 統計未讀聯絡表單數量
     * 用於後台儀表板顯示未讀訊息數
     */
    long countByIsReadFalse();

    /**
     * 查詢所有聯絡表單，依建立時間倒序排列（最新的在最前面）
     * 用於管理員後台的聯絡表單列表
     */
    List<Contact> findAllByOrderByCreatedAtDesc();
}
