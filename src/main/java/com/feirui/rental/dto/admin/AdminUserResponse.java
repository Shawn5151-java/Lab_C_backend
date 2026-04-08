package com.feirui.rental.dto.admin;

import lombok.Getter;
import lombok.Setter;

/**
 * 管理員後台 - 會員列表回應 DTO
 */
@Getter
@Setter
public class AdminUserResponse {

    private Integer id;
    private String name;
    private String email;
    private String phone;
    private String role;
    private String createdAt;  // LocalDateTime formatted as "yyyy-MM-dd HH:mm"
}
