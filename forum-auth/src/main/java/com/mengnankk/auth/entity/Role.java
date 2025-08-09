package com.mengnankk.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;


import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("roles")
@ApiModel(value = "Role对象", description = "角色信息")
public class Role {

    @ApiModelProperty(value = "角色ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "角色名称", required = true)
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称长度不能超过50个字符")
    @TableField("role_name")
    private String roleName;

    @ApiModelProperty(value = "角色编码", required = true)
    @NotBlank(message = "角色编码不能为空")
    @Size(max = 50, message = "角色编码长度不能超过50个字符")
    @TableField("role_code")
    private String roleCode;

    @ApiModelProperty(value = "角色描述")
    @TableField("description")
    private String description;

    @ApiModelProperty(value = "状态: 0-禁用, 1-启用")
    @TableField("status")
    private Integer status;

    @ApiModelProperty(value = "排序")
    @TableField("sort_order")
    private Integer sortOrder;

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

    @ApiModelProperty(value = "权限列表")
    @TableField(exist = false)
    private List<Permission> permissions;

    // 常量定义
    public static final int STATUS_DISABLED = 0;
    public static final int STATUS_ENABLED = 1;

    // 预定义角色
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_USER = "USER";
    public static final String ROLE_MODERATOR = "MODERATOR";

    /**
     * 检查角色是否启用
     */
    public int isEnabled() {
        return STATUS_ENABLED;
    }
}