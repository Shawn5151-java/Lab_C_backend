package com.feirui.rental.dto.booking;

import com.feirui.rental.enums.BookingStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 訂單資訊回傳 DTO
 *
 * 後端回傳給前端的 JSON 格式範例：
 * {
 *   "id": 101,
 *   "carName": "Porsche 911 GTS",
 *   "carMainImage": "/img/cars/porsche_911.jpg",
 *   "pickupDate": "2025-08-01",
 *   "returnDate": "2025-08-03",
 *   "totalDays": 2,
 *   "pickupLocation": "台中旗艦店",
 *   "basePrice": 16000.00,
 *   "addonTotal": 700.00,
 *   "totalPrice": 16700.00,
 *   "status": "PENDING_PAYMENT",
 *   "paymentDeadline": "2025-07-20T14:30:00",
 *   "addonNames": ["兒童安全座椅", "超安心全險"]
 * }
 */
@Getter
@Setter
public class BookingResponse {

    private Integer id;                      // 訂單編號

    private String carName;                  // 車型名稱（方便前端直接顯示）

    private String carMainImage;             // 車輛主圖路徑

    private LocalDate pickupDate;            // 取車日期

    private LocalDate returnDate;            // 還車日期

    private Integer totalDays;               // 租車天數

    private String pickupLocation;           // 取車地點

    private BigDecimal basePrice;            // 車輛基本費用快照

    private BigDecimal addonTotal;           // 加購費用小計

    private BigDecimal totalPrice;           // 總金額

    private BookingStatus status;            // 訂單狀態

    private LocalDateTime paymentDeadline;   // 付款期限（給前端做倒數計時用）

    private LocalDateTime createdAt;         // 訂單建立時間

    private List<String> addonNames;         // 加購項目名稱列表（方便前端顯示）
}
