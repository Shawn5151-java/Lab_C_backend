package com.feirui.rental.controller;

import com.feirui.rental.dto.auth.ChangePasswordRequest;
import com.feirui.rental.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 會員操作 Controller
 * 負責會員自身資料的修改（更改密碼等）
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;

    /**
     * 更改密碼
     * PUT /api/users/password
     *
     * 需要登入（JWT Token），從 Principal 取出 email
     *
     * 前端呼叫範例：
     *   PUT http://localhost:8080/api/users/password
     *   Header: Authorization: Bearer eyJhbGci...
     *   Body: { "currentPassword": "oldPass", "newPassword": "newPass123" }
     */
    @PutMapping("/password")
    public ResponseEntity<Map<String, String>> changePassword(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody ChangePasswordRequest request) {

        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "請先登入"));
        }

        authService.changePassword(email, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "密碼更改成功"));
    }
}
