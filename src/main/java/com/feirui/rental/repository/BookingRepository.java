package com.feirui.rental.repository;

import com.feirui.rental.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 訂單 Repository
 */
public interface BookingRepository extends JpaRepository<Booking, Integer> {

    /**
     * 查詢某位會員的所有訂單
     * 用於「我的訂單」頁面顯示該會員的歷史訂單
     */
    List<Booking> findByUserId(Integer userId);

    /**
     * 將超過付款期限的訂單自動設為 EXPIRED（過期）
     *
     * @Modifying   → 告訴 Spring 這個查詢會修改資料（UPDATE），不是 SELECT
     * @Transactional → 確保這個操作在交易中執行，若失敗會自動 rollback
     *
     * 為什麼要用完整 enum 路徑？
     *   Booking.status 欄位是 Java enum 型別（BookingStatus），
     *   JPQL 是操作 Java 物件的查詢語言，不是 SQL。
     *   若直接寫 'EXPIRED'（字串），JPQL 解析器無法確定要對應哪個 enum 值，
     *   可能在嚴格的 JPA 環境下拋出例外或產生預期外的行為。
     *   正確做法是使用完整的 enum 類別路徑，讓 JPA 知道要比對的是 Java enum 常數。
     *
     * @param now 當下時間，由排程器傳入 LocalDateTime.now()
     */
    @Modifying
    @Transactional
    @Query("""
        UPDATE Booking b
        SET b.status = com.feirui.rental.enums.BookingStatus.EXPIRED
        WHERE b.status = com.feirui.rental.enums.BookingStatus.PENDING_PAYMENT
        AND b.paymentDeadline < :now
    """)
    void expireOverdueBookings(@Param("now") LocalDateTime now);
}
