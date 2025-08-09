package com.mengnankk.auth.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Token刷新请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "Token刷新请求", description = "刷新Token请求参数")
public class RefreshTokenRequest {

    @ApiModelProperty(value = "刷新令牌", required = true)
    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;
}
