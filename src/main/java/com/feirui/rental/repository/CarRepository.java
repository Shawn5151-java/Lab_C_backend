package com.feirui.rental.repository;

import com.feirui.rental.entity.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 車輛 Repository
 * 除了基本 CRUD，還需要自訂查詢方法來篩選可用車輛
 */
public interface CarRepository extends JpaRepository<Car, Integer> {

    /**
     * 查詢所有上架中的車輛（isAvailable = true）
     * Spring Data JPA 的「命名查詢」功能：方法名稱就是查詢條件，不需要寫 SQL
     * findBy + IsAvailable + True → WHERE is_available = 1
     */
    List<Car> findByIsAvailableTrue();

    /**
     * 依分類 ID 查詢車輛
     * findBy + Category + Id → WHERE category_id = ?
     */
    List<Car> findByCategoryId(Integer categoryId);

    /**
     * 查詢在指定日期區間內「可以被租用」的車輛
     *
     * 判斷邏輯：一台車「不可以租」的條件是：
     *   1. 已有狀態為 CONFIRMED 或 ACTIVE 的訂單，且日期有重疊
     *   2. 已有狀態為 PENDING_PAYMENT 的訂單、付款期限還沒到，且日期有重疊
     *      （30分鐘內鎖定，超過就解鎖）
     *
     * 日期「有重疊」的判斷（NOT 取反）：
     *   NOT (已有訂單還車日 <= 新取車日  →  代表舊訂單已在新訂單之前結束
     *     OR 已有訂單取車日 >= 新還車日) →  代表舊訂單在新訂單之後才開始
     *   上面兩個條件成立代表「不衝突」，取 NOT 就是「有衝突」
     *
     * 為什麼使用完整 enum 路徑？
     *   同 BookingRepository 的說明：JPQL 操作的是 Java 物件，
     *   enum 欄位必須用 Java enum 常數比對，不能用字串字面值，
     *   否則在嚴格的 JPA 環境下可能解析失敗。
     *
     * @param pickupDate 客人想要的取車日期
     * @param returnDate 客人想要的還車日期
     * @param now        當下時間（用來判斷 30 分鐘鎖是否還在）
     */
    @Query("""
        SELECT c FROM Car c
        WHERE c.isAvailable = true
        AND c.id NOT IN (
            SELECT b.car.id FROM Booking b
            WHERE b.status IN (
                com.feirui.rental.enums.BookingStatus.CONFIRMED,
                com.feirui.rental.enums.BookingStatus.ACTIVE
            )
            AND NOT (b.returnDate <= :pickupDate OR b.pickupDate >= :returnDate)
        )
        AND c.id NOT IN (
            SELECT b.car.id FROM Booking b
            WHERE b.status = com.feirui.rental.enums.BookingStatus.PENDING_PAYMENT
            AND b.paymentDeadline > :now
            AND NOT (b.returnDate <= :pickupDate OR b.pickupDate >= :returnDate)
        )
    """)
    List<Car> findAvailableCars(
            @Param("pickupDate") LocalDate pickupDate,
            @Param("returnDate") LocalDate returnDate,
            @Param("now") LocalDateTime now
    );
}
