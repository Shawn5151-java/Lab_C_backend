package com.feirui.rental.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 登入請求 DTO（Data Transfer Object）
 *
 * DTO 是什麼？
 *   專門用來接收前端傳來的資料，或回傳給前端的資料。
 *   不能直接用 Entity（如 User），因為 Entity 裡有密碼等敏感欄位，
 *   DTO 可以只包含「這次操作需要的欄位」，更安全也更清楚。
 *
 * 前端 POST /api/auth/login 時，Request Body 格式：
 * {
 *   "email": "user@example.com",
 *   "password": "123456"
 * }
 */
@Getter
@Setter
public class LoginRequest {

    /**
     * @NotBlank → 不可為 null、不可為空字串、不可只有空白
     * @Email    → 必須符合 Email 格式（例如 xxx@xxx.xxx）
     * 驗證失敗時，Controller 的 @Valid 會自動回傳 400 錯誤
     */
    @NotBlank(message = "Email 不可為空")
    @Email(message = "Email 格式不正確")
    private String email;

    @NotBlank(message = "密碼不可為空")
    private String password;
}
