package com.feirui.rental.scheduler;

import com.feirui.rental.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 訂單排程器
 *
 * 負責定時執行「自動過期未付款訂單」的任務：
 *   當客人建立訂單後，系統給予 30 分鐘的付款期限。
 *   若客人超過 30 分鐘沒有完成付款，訂單狀態自動從
 *   PENDING_PAYMENT 改為 EXPIRED，並釋放車輛佔用（解鎖）。
 *
 * 此功能需要在 FeiruiCarRentalApplication 加上 @EnableScheduling 才會生效。
 *
 * @Component → Spring 管理此類別，排程才能被偵測到
 * @Slf4j     → Lombok：自動產生 log 變數，可以用 log.info()、log.error() 記錄日誌
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookingScheduler {

    private final BookingRepository bookingRepository;

    /**
     * 自動過期未付款訂單
     *
     * @Scheduled(fixedDelay = 60_000)：
     *   - fixedDelay = 上一次執行「完成後」，等待 60,000 毫秒（60 秒）再執行下一次
     *   - 與 fixedRate 的差異：fixedRate 是固定間隔啟動，不管上次有沒有跑完；
     *                          fixedDelay 是上次跑完後才開始計時，比較安全
     *
     * 執行邏輯：
     *   1. 取得當下時間 now
     *   2. 呼叫 Repository 的批次更新 SQL：
     *      UPDATE bookings SET status = 'EXPIRED'
     *      WHERE status = 'PENDING_PAYMENT'
     *      AND payment_deadline < now
     *   3. 所有超過 30 分鐘未付款的訂單都會被設為 EXPIRED
     */
    @Scheduled(fixedDelay = 60_000)
    public void expireUnpaidBookings() {
        LocalDateTime now = LocalDateTime.now();

        log.info("[排程] 開始執行未付款訂單過期檢查，當下時間：{}", now);

        // 執行批次更新，回傳受影響的筆數
        bookingRepository.expireOverdueBookings(now);

        log.info("[排程] 未付款訂單過期檢查完成");
    }
}
