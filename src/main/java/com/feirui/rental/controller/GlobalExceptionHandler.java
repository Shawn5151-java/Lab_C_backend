package com.feirui.rental.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全域例外處理器
 *
 * 如果沒有這個類別，當 Service 或 Controller 拋出例外時，
 * Spring 會回傳一個 HTML 格式的錯誤頁面，前端 JS 無法解析。
 *
 * 有了這個類別，所有例外都會被攔截，並統一回傳 JSON 格式的錯誤訊息。
 *
 * @RestControllerAdvice → 攔截所有 @RestController 拋出的例外
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 處理表單驗證失敗（@Valid 驗證不通過）
     * 例如：Email 格式錯誤、密碼太短、必填欄位為空
     *
     * 回傳格式：
     * {
     *   "email": "Email 格式不正確",
     *   "password": "密碼至少 6 個字元"
     * }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        // 逐一取出每個欄位的驗證錯誤訊息
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField(); // 哪個欄位出錯
            String message = error.getDefaultMessage();          // 錯誤訊息
            errors.put(fieldName, message);
        });

        return ResponseEntity.badRequest().body(errors); // HTTP 400
    }

    /**
     * 處理帳號密碼錯誤（登入失敗）
     *
     * 回傳格式：
     * { "error": "帳號或密碼錯誤" }
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED) // HTTP 401
                .body(Map.of("error", "帳號或密碼錯誤"));
    }

    /**
     * 處理所有其他的 RuntimeException
     * 例如：找不到車輛、Email 已被使用、日期格式錯誤等
     *
     * 回傳格式：
     * { "error": "此 Email 已被註冊：user@test.com" }
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity
                .badRequest() // HTTP 400
                .body(Map.of("error", ex.getMessage()));
    }
}
