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
 * 重置密码请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "重置密码请求", description = "用户重置密码请求参数")
public class ResetPasswordRequest {

    @ApiModelProperty(value = "邮箱", required = true)
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @ApiModelProperty(value = "重置令牌", required = true)
    @NotBlank(message = "重置令牌不能为空")
    private String resetToken;

    @ApiModelProperty(value = "新密码", required = true)
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, message = "密码长度不能少于6个字符")
    private String newPassword;

    @ApiModelProperty(value = "确认新密码", required = true)
    @NotBlank(message = "确认新密码不能为空")
    private String confirmNewPassword;
}
