package com.feirui.rental.dto.admin;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 管理員後台 - 儀表板統計數據回應 DTO
 */
@Getter
@Setter
public class AdminDashboardResponse {

    private Long totalUsers;
    private Long totalBookings;
    private BigDecimal totalRevenue;
    private Long pendingBookings;
    private Long todayBookings;
    private Long unreadContacts;
}
