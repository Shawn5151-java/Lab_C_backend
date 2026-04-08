package com.feirui.rental.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 更改密碼請求 DTO
 * PUT /api/users/password
 *
 * 範例 JSON：
 * {
 *   "currentPassword": "oldPassword123",
 *   "newPassword": "newPassword456"
 * }
 */
@Getter
@Setter
public class ChangePasswordRequest {

    @NotBlank(message = "目前密碼不可為空")
    private String currentPassword;

    @NotBlank(message = "新密碼不可為空")
    @Size(min = 6, message = "新密碼至少需要 6 個字元")
    private String newPassword;
}
