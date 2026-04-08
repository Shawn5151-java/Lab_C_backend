package com.feirui.rental.entity;

import com.feirui.rental.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 付款紀錄 Entity，對應資料庫 payments 表
 * 每一筆訂單只會有一筆付款紀錄（一對一關係）
 * 記錄付款金額、方式、狀態
 */
@Getter
@Setter
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 一對一關係：一筆付款對應一筆訂單
     * unique = true 確保同一筆訂單不會有兩筆付款紀錄
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount; // 付款金額（應等於訂單的 totalPrice）

    @Column(name = "payment_method", length = 50)
    private String paymentMethod; // 付款方式，例如 CREDIT_CARD / ATM / LINE_PAY

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private PaymentStatus status = PaymentStatus.PENDING; // 付款狀態，預設為等待付款

    @Column(name = "paid_at")
    private LocalDateTime paidAt; // 付款成功的時間，未付款前為 NULL

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // 付款紀錄建立時間
}
