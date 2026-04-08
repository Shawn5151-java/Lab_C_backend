package com.feirui.rental.controller;

import com.feirui.rental.dto.contact.ContactRequest;
import com.feirui.rental.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 聯絡表單 Controller
 * 負責接收前台 Contact_us.html 送出的聯絡表單
 *
 * SecurityConfig 中已設定 POST /api/contacts 為公開路徑，不需要登入即可送出
 */
@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    /**
     * 送出聯絡表單
     * POST /api/contacts
     *
     * 前端呼叫範例：
     *   POST http://localhost:8080/api/contacts
     *   Body: {
     *     "name": "王小明",
     *     "phone": "0912345678",
     *     "email": "user@test.com",
     *     "subject": "租車詢問",
     *     "message": "請問有沒有七人座車款？"
     *   }
     *
     * 成功回傳 HTTP 201 Created + 訊息字串
     */
    @PostMapping
    public ResponseEntity<String> submitContact(@Valid @RequestBody ContactRequest request) {
        contactService.submitContact(request);
        // 回傳 201 + 成功訊息（前端顯示「已成功送出」）
        return ResponseEntity.status(HttpStatus.CREATED).body("訊息已成功送出，我們將盡快與您聯繫");
    }
}
