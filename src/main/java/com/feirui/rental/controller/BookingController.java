package com.feirui.rental.controller;

import com.feirui.rental.dto.booking.BookingResponse;
import com.feirui.rental.dto.booking.CreateBookingRequest;
import com.feirui.rental.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 訂單 Controller
 * 負責租車訂單建立與查詢的 REST API
 */
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    /**
     * 建立新訂單
     * POST /api/bookings
     *
     * 訪客和已登入用戶都可以建立訂單（SecurityConfig 已設定此路徑為公開）
     *
     * @AuthenticationPrincipal UserDetails userDetails
     *   → Spring Security 自動注入當前登入用戶的資訊
     *   → 若是訪客（未登入）則為 null
     *   → 已登入則包含 email（username）和角色
     *
     * 流程：
     *   1. 從 userDetails 取出 email（若有登入）
     *   2. 呼叫 BookingService.createBooking()
     *   3. 回傳 HTTP 201 Created + 訂單資訊
     *
     * 前端呼叫範例（已登入時帶 JWT Token）：
     *   POST http://localhost:8080/api/bookings
     *   Header: Authorization: Bearer eyJhbGci...
     *   Body: { "carId": 1, "pickupDate": "2025-08-01", "returnDate": "2025-08-03",
     *           "renterName": "王小明", "renterPhone": "0912345678",
     *           "renterIdNumber": "A123456789", "addonIds": [1, 2] }
     */
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            @AuthenticationPrincipal String email) {

        // JWT Filter 將 email(String) 存為 principal，未登入時為 null（訪客租車）
        BookingResponse response = bookingService.createBooking(request, email);

        // 回傳 HTTP 201 Created，表示資源建立成功
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 查詢當前登入會員的所有訂單（我的訂單頁面）
     * GET /api/bookings/my
     *
     * 需要登入，從 JWT Token 取出 email，再查出 userId
     *
     * 前端呼叫範例：
     *   GET http://localhost:8080/api/bookings/my
     *   Header: Authorization: Bearer eyJhbGci...
     */
    @GetMapping("/my")
    public ResponseEntity<List<BookingResponse>> getMyBookings(
            @AuthenticationPrincipal String email) {

        // JWT Filter 將 email(String) 存為 principal
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(bookingService.getUserBookingsByEmail(email));
    }


    /**
     * 查詢單筆訂單詳情
     * GET /api/bookings/{id}
     *
     * 需要登入（SecurityConfig 的 anyRequest().authenticated() 規則）
     *
     * 前端呼叫範例：
     *   GET http://localhost:8080/api/bookings/101
     *   Header: Authorization: Bearer eyJhbGci...
     */
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBookingById(
            @PathVariable Integer id,
            @AuthenticationPrincipal String email) {
        BookingResponse booking = bookingService.getBookingById(id);
        // 驗證訂單屬於當前登入用戶（防止 IDOR）
        if (email != null && !bookingService.isOwner(id, email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(booking);
    }
}

