package com.mengnankk.auth.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "登录请求", description = "用户登录请求参数")
public class LoginRequest {

    @ApiModelProperty(value = "用户名或邮箱", required = true, example = "admin")
    @NotBlank(message = "用户名不能为空")
    private String username;

    @ApiModelProperty(value = "密码", required = true, example = "123456")
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度不能少于6个字符")
    private String password;

    @ApiModelProperty(value = "验证码", example = "1234")
    private String captcha;

    @ApiModelProperty(value = "验证码Key", example = "captcha_key_123")
    private String captchaKey;

    @ApiModelProperty(value = "记住我", example = "true")
    private Boolean rememberMe = false;

    @ApiModelProperty(value = "登录设备", example = "web")
    private String device = "web";
}

