package com.feirui.rental.dto.booking;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * 建立訂單請求 DTO
 *
 * 對應前台 booking.html 的步驟三（填寫承租人資料）送出後，
 * 前端 POST /api/bookings 時的 Request Body 格式：
 * {
 *   "carId": 1,
 *   "pickupDate": "2025-08-01",
 *   "returnDate": "2025-08-03",
 *   "pickupLocation": "台中旗艦店",
 *   "renterName": "王小明",
 *   "renterPhone": "0912345678",
 *   "renterIdNumber": "A123456789",
 *   "addonIds": [1, 3]
 * }
 */
@Getter
@Setter
public class CreateBookingRequest {

    @NotNull(message = "請選擇車輛")
    private Integer carId;              // 要租的車輛 ID

    @NotNull(message = "請選擇取車日期")
    private LocalDate pickupDate;       // 取車日期

    @NotNull(message = "請選擇還車日期")
    private LocalDate returnDate;       // 還車日期

    private String pickupLocation;      // 取車地點（選填）

    @NotBlank(message = "承租人姓名不可為空")
    private String renterName;          // 承租人姓名

    @NotBlank(message = "承租人電話不可為空")
    private String renterPhone;         // 承租人手機號碼

    @NotBlank(message = "承租人身分證不可為空")
    private String renterIdNumber;      // 承租人身分證號碼（後端會加密後存入）

    // 選擇的加購項目 ID 列表（可為空，表示沒有加購）
    // 例如：[1, 3] 代表選了 id=1 的兒童安全座椅和 id=3 的超安心全險
    private List<Integer> addonIds;
}
