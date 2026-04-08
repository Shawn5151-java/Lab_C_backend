package com.feirui.rental.dto.contact;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 聯絡表單請求 DTO
 *
 * 對應前台 Contact_us.html 表單送出，
 * 前端 POST /api/contacts 時的 Request Body 格式：
 * {
 *   "name": "王小明",
 *   "phone": "0912345678",
 *   "email": "user@example.com",
 *   "subject": "租車詢問",
 *   "message": "請問有沒有7人座的車款？"
 * }
 */
@Getter
@Setter
public class ContactRequest {

    @NotBlank(message = "姓名不可為空")
    private String name;

    @NotBlank(message = "電話不可為空")
    private String phone;

    // Email 為選填，有填才驗證格式
    @Email(message = "Email 格式不正確")
    private String email;

    private String subject;  // 詢問主題（選填）

    @NotBlank(message = "詢問內容不可為空")
    private String message;
}
