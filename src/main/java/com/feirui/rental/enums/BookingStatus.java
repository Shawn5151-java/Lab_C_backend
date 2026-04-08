package com.feirui.rental.enums;

/**
 * 訂單狀態列舉
 *
 * 狀態流程：
 * PENDING_PAYMENT → 建立訂單後等待付款（30 分鐘內必須付款）
 * CONFIRMED       → 付款成功，訂單已確認
 * ACTIVE          → 取車中（租用期間）
 * COMPLETED       → 已還車，訂單結束
 * CANCELLED       → 客戶或管理員取消訂單
 * EXPIRED         → 超過 30 分鐘未付款，系統自動設為此狀態並解鎖車輛
 */
public enum BookingStatus {
    PENDING_PAYMENT,
    CONFIRMED,
    ACTIVE,
    COMPLETED,
    CANCELLED,
    EXPIRED
}
