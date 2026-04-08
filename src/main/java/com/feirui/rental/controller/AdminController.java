package com.feirui.rental.controller;

import com.feirui.rental.dto.admin.*;
import com.feirui.rental.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理員後台 API 控制器
 *
 * 所有路徑前綴 /api/admin/** 已在 SecurityConfig 設定為需要 ADMIN 角色，
 * 因此這裡不需要再加 @PreAuthorize 注解。
 *
 * 各功能模組：
 *   - 儀表板統計
 *   - 會員管理
 *   - 訂單管理
 *   - 車輛管理
 *   - 加購項目管理
 *   - 優惠活動管理
 *   - 聯絡表單管理
 *   - 付款紀錄管理
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // ─────────────────────────────────────────────
    // 儀表板
    // ─────────────────────────────────────────────

    /**
     * GET /api/admin/dashboard
     * 取得後台儀表板統計數據
     */
    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> getDashboard() {
        return ResponseEntity.ok(adminService.getDashboard());
    }

    // ─────────────────────────────────────────────
    // 會員管理
    // ─────────────────────────────────────────────

    /**
     * GET /api/admin/users
     * 取得所有會員列表
     */
    @GetMapping("/users")
    public ResponseEntity<List<AdminUserResponse>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    // ─────────────────────────────────────────────
    // 訂單管理
    // ─────────────────────────────────────────────

    /**
     * GET /api/admin/bookings
     * 取得所有訂單列表，依建立時間倒序排列
     */
    @GetMapping("/bookings")
    public ResponseEntity<List<AdminBookingResponse>> getAllBookings() {
        return ResponseEntity.ok(adminService.getAllBookings());
    }

    /**
     * PATCH /api/admin/bookings/{id}/status
     * 更新訂單狀態
     *
     * 請求體範例：{ "status": "CONFIRMED" }
     * 有效值：PENDING_PAYMENT, CONFIRMED, ACTIVE, COMPLETED, CANCELLED, EXPIRED
     */
    @PatchMapping("/bookings/{id}/status")
    public ResponseEntity<AdminBookingResponse> updateBookingStatus(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateStatusRequest request) {
        AdminBookingResponse updated = adminService.updateBookingStatus(id, request.getStatus());
        return ResponseEntity.ok(updated);
    }

    // ─────────────────────────────────────────────
    // 車輛管理
    // ─────────────────────────────────────────────

    /**
     * GET /api/admin/cars
     * 取得所有車輛列表（含下架車輛）
     */
    @GetMapping("/cars")
    public ResponseEntity<List<AdminCarResponse>> getAllCars() {
        return ResponseEntity.ok(adminService.getAllCars());
    }

    /**
     * PATCH /api/admin/cars/{id}/availability
     * 切換車輛上架/下架狀態
     */
    @PatchMapping("/cars/{id}/availability")
    public ResponseEntity<AdminCarResponse> toggleCarAvailability(@PathVariable Integer id) {
        AdminCarResponse updated = adminService.toggleCarAvailability(id);
        return ResponseEntity.ok(updated);
    }

    // ─────────────────────────────────────────────
    // 加購項目管理
    // ─────────────────────────────────────────────

    /**
     * GET /api/admin/addons
     * 取得所有加購項目列表（含停用項目）
     */
    @GetMapping("/addons")
    public ResponseEntity<List<AdminAddonResponse>> getAllAddons() {
        return ResponseEntity.ok(adminService.getAllAddons());
    }

    /**
     * PATCH /api/admin/addons/{id}/active
     * 切換加購項目啟用/停用狀態
     */
    @PatchMapping("/addons/{id}/active")
    public ResponseEntity<AdminAddonResponse> toggleAddonActive(@PathVariable Integer id) {
        AdminAddonResponse updated = adminService.toggleAddonActive(id);
        return ResponseEntity.ok(updated);
    }

    // ─────────────────────────────────────────────
    // 優惠活動管理
    // ─────────────────────────────────────────────

    /**
     * GET /api/admin/promotions
     * 取得所有優惠活動列表（含下架活動）
     */
    @GetMapping("/promotions")
    public ResponseEntity<List<AdminPromotionResponse>> getAllPromotions() {
        return ResponseEntity.ok(adminService.getAllPromotions());
    }

    /**
     * PATCH /api/admin/promotions/{id}/active
     * 切換優惠活動上架/下架狀態
     */
    @PatchMapping("/promotions/{id}/active")
    public ResponseEntity<AdminPromotionResponse> togglePromotionActive(@PathVariable Integer id) {
        AdminPromotionResponse updated = adminService.togglePromotionActive(id);
        return ResponseEntity.ok(updated);
    }

    // ─────────────────────────────────────────────
    // 聯絡表單管理
    // ─────────────────────────────────────────────

    /**
     * GET /api/admin/contacts
     * 取得所有聯絡表單，依建立時間倒序排列
     */
    @GetMapping("/contacts")
    public ResponseEntity<List<AdminContactResponse>> getAllContacts() {
        return ResponseEntity.ok(adminService.getAllContacts());
    }

    /**
     * PATCH /api/admin/contacts/{id}/read
     * 將聯絡表單標記為已讀
     */
    @PatchMapping("/contacts/{id}/read")
    public ResponseEntity<AdminContactResponse> markContactRead(@PathVariable Integer id) {
        AdminContactResponse updated = adminService.markContactRead(id);
        return ResponseEntity.ok(updated);
    }

    // ─────────────────────────────────────────────
    // 付款紀錄管理
    // ─────────────────────────────────────────────

    /**
     * GET /api/admin/payments
     * 取得所有付款紀錄，依建立時間倒序排列
     */
    @GetMapping("/payments")
    public ResponseEntity<List<AdminPaymentResponse>> getAllPayments() {
        return ResponseEntity.ok(adminService.getAllPayments());
    }

    /**
     * PATCH /api/admin/payments/{id}/status
     * 更新付款狀態
     *
     * 請求體範例：{ "status": "SUCCESS" }
     * 有效值：PENDING, SUCCESS, FAILED, REFUNDED
     */
    @PatchMapping("/payments/{id}/status")
    public ResponseEntity<AdminPaymentResponse> updatePaymentStatus(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateStatusRequest request) {
        AdminPaymentResponse updated = adminService.updatePaymentStatus(id, request.getStatus());
        return ResponseEntity.ok(updated);
    }
}
