package com.mengnankk.auth.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 注册请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "注册请求", description = "用户注册请求参数")
public class RegisterRequest {

    @ApiModelProperty(value = "用户名", required = true, example = "newuser")
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    private String username;

    @ApiModelProperty(value = "邮箱", required = true, example = "user@example.com")
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @ApiModelProperty(value = "密码", required = true, example = "123456")
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度不能少于6个字符")
    private String password;

    @ApiModelProperty(value = "确认密码", required = true, example = "123456")
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    @ApiModelProperty(value = "昵称", example = "新用户")
    private String nickname;

    @ApiModelProperty(value = "手机号", example = "13800138000")
    private String phone;

    @ApiModelProperty(value = "验证码", example = "1234")
    private String captcha;

    @ApiModelProperty(value = "验证码Key", example = "captcha_key_123")
    private String captchaKey;
}