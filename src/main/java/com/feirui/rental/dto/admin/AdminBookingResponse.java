package com.feirui.rental.dto.admin;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 管理員後台 - 訂單列表回應 DTO
 */
@Getter
@Setter
public class AdminBookingResponse {

    private Integer id;
    private Integer carId;
    private String carName;
    private String carMainImage;
    private String userName;
    private String userEmail;
    private String renterName;
    private String renterPhone;
    private String pickupDate;       // LocalDate formatted as "yyyy-MM-dd"
    private String returnDate;       // LocalDate formatted as "yyyy-MM-dd"
    private Integer totalDays;
    private String pickupLocation;
    private BigDecimal totalPrice;
    private String status;
    private String paymentDeadline;  // LocalDateTime formatted as "yyyy-MM-dd HH:mm"
    private String createdAt;        // LocalDateTime formatted as "yyyy-MM-dd HH:mm"
    private List<String> addonNames;
}
