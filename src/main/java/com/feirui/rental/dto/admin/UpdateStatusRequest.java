package com.feirui.rental.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 管理員後台 - 更新狀態請求 DTO
 * 用於更新訂單狀態或付款狀態
 */
@Getter
@Setter
public class UpdateStatusRequest {

    @NotBlank(message = "狀態不能為空")
    private String status;
}
