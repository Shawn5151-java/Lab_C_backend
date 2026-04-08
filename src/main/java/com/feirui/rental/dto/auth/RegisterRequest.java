package com.feirui.rental.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 註冊請求 DTO
 *
 * 前端 POST /api/auth/register 時，Request Body 格式：
 * {
 *   "email": "user@example.com",
 *   "password": "123456",
 *   "name": "王小明",
 *   "phone": "0912345678"
 * }
 */
@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Email 不可為空")
    @Email(message = "Email 格式不正確")
    private String email;

    /**
     * @Size(min = 6) → 密碼最少 6 個字元
     */
    @NotBlank(message = "密碼不可為空")
    @Size(min = 6, message = "密碼至少 6 個字元")
    private String password;

    @NotBlank(message = "姓名不可為空")
    private String name;

    // 手機號碼為選填（沒有 @NotBlank）
    private String phone;
}
