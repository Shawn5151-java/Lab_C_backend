package com.feirui.rental.dto.admin;

import lombok.Getter;
import lombok.Setter;

/**
 * 管理員後台 - 聯絡表單列表回應 DTO
 */
@Getter
@Setter
public class AdminContactResponse {

    private Integer id;
    private String name;
    private String phone;
    private String email;
    private String subject;
    private String message;
    private Boolean isRead;
    private String createdAt;  // LocalDateTime formatted as "yyyy-MM-dd HH:mm"
}
