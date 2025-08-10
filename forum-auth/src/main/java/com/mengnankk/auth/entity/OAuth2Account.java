package com.mengnankk.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * OAuth2第三方账号实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("oauth2_accounts")
@ApiModel(value = "OAuth2Account对象", description = "OAuth2第三方账号")
public class OAuth2Account {

    @ApiModelProperty(value = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "用户ID")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty(value = "第三方平台")
    @TableField("provider")
    private String provider;

    @ApiModelProperty(value = "第三方平台用户ID")
    @TableField("provider_user_id")
    private String providerUserId;

    @ApiModelProperty(value = "第三方平台用户名")
    @TableField("provider_username")
    private String providerUsername;

    @ApiModelProperty(value = "第三方平台邮箱")
    @TableField("provider_email")
    private String providerEmail;

    @ApiModelProperty(value = "第三方平台头像")
    @TableField("provider_avatar")
    private String providerAvatar;

    @ApiModelProperty(value = "Access Token")
    @TableField("access_token")
    private String accessToken;

    @ApiModelProperty(value = "Refresh Token")
    @TableField("refresh_token")
    private String refreshToken;

    @ApiModelProperty(value = "Token过期时间")
    @TableField("expires_at")
    private LocalDateTime expiresAt;

    @ApiModelProperty(value = "创建时间")
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @ApiModelProperty(value = "更新时间")
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    // 第三方平台常量
    public static final String PROVIDER_GITHUB = "github";
    public static final String PROVIDER_GOOGLE = "google";
    public static final String PROVIDER_WECHAT = "wechat";
    public static final String PROVIDER_QQ = "qq";
}