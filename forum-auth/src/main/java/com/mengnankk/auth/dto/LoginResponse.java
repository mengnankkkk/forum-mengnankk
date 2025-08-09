package com.mengnankk.auth.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 登录响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "登录响应", description = "用户登录响应结果")
public class LoginResponse {

    @ApiModelProperty(value = "访问令牌")
    private String accessToken;

    @ApiModelProperty(value = "刷新令牌")
    private String refreshToken;

    @ApiModelProperty(value = "令牌类型", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    @ApiModelProperty(value = "过期时间(秒)", example = "86400")
    private Long expiresIn;

    @ApiModelProperty(value = "用户信息")
    private UserInfo userInfo;

    @ApiModelProperty(value = "权限列表")
    private List<String> permissions;

    @ApiModelProperty(value = "角色列表")
    private List<String> roles;
}