package com.feirui.rental.repository;

import com.feirui.rental.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 付款紀錄 Repository
 */
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    /**
     * 依訂單 ID 查詢付款紀錄
     * 因為一筆訂單只有一筆付款，所以回傳 Optional<Payment>
     * 用於查詢某筆訂單的付款狀態
     */
    Optional<Payment> findByBookingId(Integer bookingId);
}
