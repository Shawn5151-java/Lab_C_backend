package com.feirui.rental;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 飛瑞租車系統 - 主啟動類別
 *
 * @SpringBootApplication 包含三個功能：
 *   1. @SpringBootConfiguration → 標示這是 Spring Boot 設定類別
 *   2. @EnableAutoConfiguration → 自動載入 Spring Boot 預設設定（資料庫連線、Security 等）
 *   3. @ComponentScan           → 自動掃描同一套件下所有的 @Component、@Service、@Repository 等
 *
 * @EnableScheduling → 啟用排程功能，讓 @Scheduled 標注的方法可以定時執行
 *   → 用於 BookingScheduler，每分鐘自動將超時未付款的訂單設為 EXPIRED
 */
@SpringBootApplication
@EnableScheduling
public class FeiruiCarRentalApplication {

    public static void main(String[] args) {
        SpringApplication.run(FeiruiCarRentalApplication.class, args);
    }
}
