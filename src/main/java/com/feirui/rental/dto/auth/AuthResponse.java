package com.feirui.rental.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 登入 / 註冊成功後的回傳 DTO
 *
 * 後端回傳給前端的格式：
 * {
 *   "token": "eyJhbGciOiJ...",
 *   "name": "王小明",
 *   "email": "user@example.com",
 *   "role": "USER"
 * }
 *
 * 前端收到後：
 *   1. 將 token 存入 localStorage：localStorage.setItem('token', response.token)
 *   2. 之後每次呼叫需要登入的 API，在 Header 帶上：
 *      Authorization: Bearer <token>
 *
 * @AllArgsConstructor → Lombok：自動產生「包含所有欄位」的建構子
 *   讓我們可以用 new AuthResponse(token, name, email, role) 快速建立物件
 */
@Getter
@AllArgsConstructor
public class AuthResponse {

    private String token;  // JWT Token，前端需要保存起來

    private String name;   // 顯示在前端頁面的歡迎訊息用

    private String email;  // 用戶的 Email

    private String role;   // USER 或 ADMIN（前端用來判斷是否顯示管理員功能）
}
