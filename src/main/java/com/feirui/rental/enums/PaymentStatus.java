package com.feirui.rental.enums;

/**
 * 付款狀態列舉
 *
 * PENDING  → 等待付款中
 * SUCCESS  → 付款成功
 * FAILED   → 付款失敗（例如信用卡被拒）
 * REFUNDED → 已退款
 */
public enum PaymentStatus {
    PENDING,
    SUCCESS,
    FAILED,
    REFUNDED
}
