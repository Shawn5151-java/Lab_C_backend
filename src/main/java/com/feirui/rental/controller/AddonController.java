package com.feirui.rental.controller;

import com.feirui.rental.entity.Addon;
import com.feirui.rental.repository.AddonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 加購項目公開 API
 * GET /api/addons — 供前台預約頁面動態讀取啟用中的加購項目
 * SecurityConfig 已設定此路徑為公開，不需要登入
 */
@RestController
@RequestMapping("/api/addons")
@RequiredArgsConstructor
public class AddonController {

    private final AddonRepository addonRepository;

    /**
     * 查詢所有啟用中的加購項目（isActive = true）
     * GET /api/addons
     */
    @GetMapping
    public ResponseEntity<List<Addon>> getActiveAddons() {
        return ResponseEntity.ok(addonRepository.findByIsActiveTrue());
    }
}
