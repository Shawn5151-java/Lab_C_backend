package com.feirui.rental.entity;

import com.feirui.rental.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 訂單 Entity，對應資料庫 bookings 表
 * 這是整個系統最核心的資料表，記錄每一筆租車訂單的完整資訊
 */
@Getter
@Setter
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 多對一關係：一個會員可以有多筆訂單
     * nullable = true（預設），允許 user_id 為 NULL，表示這是訪客租車（未登入）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * 多對一關係：一台車可以被租很多次（不同時間）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    @Column(name = "renter_name", nullable = false, length = 50)
    private String renterName; // 承租人姓名（填表單時輸入）

    @Column(name = "renter_phone", nullable = false, length = 20)
    private String renterPhone; // 承租人手機號碼

    /**
     * 承租人身分證號碼，使用 AES-256 加密後存入
     * 和 User.idNumber 一樣，解密方式相同
     */
    @Column(name = "renter_id_number", nullable = false, length = 255)
    private String renterIdNumber;

    @Column(name = "pickup_date", nullable = false)
    private LocalDate pickupDate; // 取車日期

    @Column(name = "return_date", nullable = false)
    private LocalDate returnDate; // 還車日期

    @Column(name = "total_days", nullable = false)
    private Integer totalDays; // 租車天數（= returnDate - pickupDate）

    @Column(name = "pickup_location", length = 100)
    private String pickupLocation; // 取車地點，例如「台中旗艦店」

    /**
     * 價格快照（Price Snapshot）
     * 在建立訂單的當下，把車輛的每日租金複製一份存在這裡
     * 這樣即使之後管理員修改了車輛售價，舊訂單的金額也不會改變
     */
    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice; // 車輛基本租金（快照）

    @Column(name = "addon_total", precision = 10, scale = 2)
    private BigDecimal addonTotal = BigDecimal.ZERO; // 所有加購項目的總費用

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice; // 最終總金額 = basePrice + addonTotal

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private BookingStatus status = BookingStatus.PENDING_PAYMENT; // 訂單狀態，預設為等待付款

    /**
     * 付款期限 = 建立訂單時間 + 30 分鐘
     * 由 BookingService 在建立訂單時計算並寫入
     * BookingScheduler 每分鐘會掃描一次，超過此時間且未付款的訂單自動設為 EXPIRED
     */
    @Column(name = "payment_deadline")
    private LocalDateTime paymentDeadline;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // 訂單建立時間

    /**
     * @UpdateTimestamp：每次這筆資料被更新時，自動更新此欄位為當下時間
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 最後更新時間

    /**
     * 一對多關係：一筆訂單可以有多個加購項目
     */
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BookingAddon> bookingAddons;
}
