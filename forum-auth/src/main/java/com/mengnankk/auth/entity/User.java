package com.mengnankk.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户实体类
 *
 * @author Forum Auth Team
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("users")
@ApiModel(value = "User对象", description = "用户信息")
public class User {

    @ApiModelProperty(value = "用户ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "用户名", required = true)
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    @TableField("username")
    private String username;

    @ApiModelProperty(value = "邮箱", required = true)
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @TableField("email")
    private String email;

    @ApiModelProperty(value = "密码", required = true)
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度不能少于6个字符")
    @JsonIgnore
    @TableField("password")
    private String password;

    @ApiModelProperty(value = "昵称")
    @TableField("nickname")
    private String nickname;

    @ApiModelProperty(value = "头像URL")
    @TableField("avatar")
    private String avatar;

    @ApiModelProperty(value = "手机号")
    @TableField("phone")
    private String phone;

    @ApiModelProperty(value = "状态: 0-禁用, 1-启用")
    @TableField("status")
    private Integer status;

    @ApiModelProperty(value = "邮箱是否验证: 0-未验证, 1-已验证")
    @TableField("email_verified")
    private Integer emailVerified;

    @ApiModelProperty(value = "手机是否验证: 0-未验证, 1-已验证")
    @TableField("phone_verified")
    private Integer phoneVerified;

    @ApiModelProperty(value = "最后登录时间")
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

    @ApiModelProperty(value = "最后登录IP")
    @TableField("last_login_ip")
    private String lastLoginIp;

    @ApiModelProperty(value = "登录次数")
    @TableField("login_count")
    private Integer loginCount;

    @ApiModelProperty(value = "创建时间")
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @ApiModelProperty(value = "更新时间")
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    @ApiModelProperty(value = "逻辑删除: 0-未删除, 1-已删除")
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    // 非数据库字段
    @ApiModelProperty(value = "用户角色列表")
    @TableField(exist = false)
    private List<Role> roles;

    @ApiModelProperty(value = "用户权限列表")
    @TableField(exist = false)
    private List<Permission> permissions;

    // 常量定义
    public static final int STATUS_DISABLED = 0;
    public static final int STATUS_ENABLED = 1;

    public static final int EMAIL_NOT_VERIFIED = 0;
    public static final int EMAIL_VERIFIED = 1;

    public static final int PHONE_NOT_VERIFIED = 0;
    public static final int PHONE_VERIFIED = 1;

    /**
     * 检查用户是否启用
     */
    public boolean isEnabled() {
        return Integer.valueOf(STATUS_ENABLED).equals(this.status);
    }

    /**
     * 检查邮箱是否验证
     */
    public boolean isEmailVerified() {
        return Integer.valueOf(EMAIL_VERIFIED).equals(this.emailVerified);
    }

    /**
     * 检查手机是否验证
     */
    public boolean isPhoneVerified() {
        return Integer.valueOf(PHONE_VERIFIED).equals(this.phoneVerified);
    }

    /**
     * 更新登录信息
     */
    public void updateLoginInfo(String loginIp) {
        this.lastLoginTime = LocalDateTime.now();
        this.lastLoginIp = loginIp;
        this.loginCount = (this.loginCount == null ? 0 : this.loginCount) + 1;
    }
}