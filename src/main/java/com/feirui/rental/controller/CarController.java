package com.feirui.rental.controller;

import com.feirui.rental.dto.car.CarResponse;
import com.feirui.rental.service.CarService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 車輛 Controller
 * 負責車輛查詢相關的 REST API
 *
 * SecurityConfig 中已設定 GET /api/cars/** 為公開路徑，不需要登入
 */
@RestController
@RequestMapping("/api/cars")
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;

    /**
     * 查詢所有上架中的車輛
     * GET /api/cars
     *
     * 前端呼叫範例：
     *   GET http://localhost:8080/api/cars
     */
    @GetMapping
    public ResponseEntity<List<CarResponse>> getAllCars() {
        return ResponseEntity.ok(carService.getAllCars());
    }

    /**
     * 依 ID 查詢單台車輛詳情
     * GET /api/cars/{id}
     *
     * @PathVariable → 從 URL 路徑中取出變數
     *   例如 GET /api/cars/3 → id = 3
     *
     * 前端呼叫範例：
     *   GET http://localhost:8080/api/cars/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<CarResponse> getCarById(@PathVariable Integer id) {
        return ResponseEntity.ok(carService.getCarById(id));
    }

    /**
     * 查詢指定日期區間內可用的車輛
     * GET /api/cars/available?pickupDate=2025-08-01&returnDate=2025-08-03
     *
     * @RequestParam → 從 URL 的查詢參數（?key=value）取出值
     * @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) → 指定日期格式為 ISO 8601（yyyy-MM-dd）
     *   讓 Spring 將字串 "2025-08-01" 自動轉換為 LocalDate 物件
     *
     * 前端呼叫範例：
     *   GET http://localhost:8080/api/cars/available?pickupDate=2025-08-01&returnDate=2025-08-03
     */
    @GetMapping("/available")
    public ResponseEntity<List<CarResponse>> getAvailableCars(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate pickupDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnDate) {
        return ResponseEntity.ok(carService.getAvailableCars(pickupDate, returnDate));
    }
}
