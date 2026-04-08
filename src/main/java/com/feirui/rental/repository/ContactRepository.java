package com.feirui.rental.repository;

import com.feirui.rental.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 聯絡表單 Repository
 * 基本 CRUD 已足夠使用：
 *   - save()    → 儲存客人送出的表單
 *   - findAll() → 管理員查看所有詢問
 */
public interface ContactRepository extends JpaRepository<Contact, Integer> {
}
