package com.feirui.rental.controller;

import com.feirui.rental.dto.auth.AuthResponse;
import com.feirui.rental.dto.auth.LoginRequest;
import com.feirui.rental.dto.auth.RegisterRequest;
import com.feirui.rental.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 認證 Controller
 * 負責處理登入與註冊的 REST API
 *
 * @RestController → 等於 @Controller + @ResponseBody
 *   所有方法的回傳值都會自動轉成 JSON 格式回傳給前端
 *
 * @RequestMapping("/api/auth") → 這個 Controller 所有 API 路徑都以 /api/auth 開頭
 *
 * SecurityConfig 中已設定 /api/auth/** 為公開路徑，不需要登入即可存取
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 會員註冊
     * POST /api/auth/register
     *
     * @Valid → 觸發 RegisterRequest 內的欄位驗證（@NotBlank、@Email 等）
     *          若驗證失敗，Spring 自動回傳 400 Bad Request
     *
     * @RequestBody → 從 HTTP Request Body 解析 JSON 並對應到 RegisterRequest 物件
     *
     * ResponseEntity → 可以自訂 HTTP 狀態碼的回傳物件
     *   ResponseEntity.ok() → HTTP 200
     *   ResponseEntity.badRequest() → HTTP 400
     *
     * 前端呼叫範例：
     *   POST http://localhost:8080/api/auth/register
     *   Body: { "email": "user@test.com", "password": "123456", "name": "王小明" }
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 會員登入
     * POST /api/auth/login
     *
     * 前端呼叫範例：
     *   POST http://localhost:8080/api/auth/login
     *   Body: { "email": "user@test.com", "password": "123456" }
     *
     * 回傳：
     *   { "token": "eyJhbGci...", "name": "王小明", "email": "user@test.com", "role": "USER" }
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
