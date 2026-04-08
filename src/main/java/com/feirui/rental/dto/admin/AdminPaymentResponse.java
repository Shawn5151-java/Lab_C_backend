package com.feirui.rental.dto.admin;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 管理員後台 - 付款紀錄列表回應 DTO
 */
@Getter
@Setter
public class AdminPaymentResponse {

    private Integer id;
    private Integer bookingId;
    private String carName;
    private String renterName;
    private BigDecimal amount;
    private String paymentMethod;
    private String status;
    private String paidAt;      // LocalDateTime formatted as "yyyy-MM-dd HH:mm", nullable
    private String createdAt;   // LocalDateTime formatted as "yyyy-MM-dd HH:mm"
}
