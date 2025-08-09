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
 * 密码修改请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "密码修改请求", description = "用户密码修改请求参数")
public class ChangePasswordRequest {

    @ApiModelProperty(value = "原密码", required = true)
    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    @ApiModelProperty(value = "新密码", required = true)
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, message = "密码长度不能少于6个字符")
    private String newPassword;

    @ApiModelProperty(value = "确认新密码", required = true)
    @NotBlank(message = "确认新密码不能为空")
    private String confirmNewPassword;
}
