package com.mengnankk.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 角色权限关联实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("role_permissions")
@ApiModel(value = "RolePermission对象", description = "角色权限关联")
class RolePermission {

    @ApiModelProperty(value = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "角色ID")
    @TableField("role_id")
    private Long roleId;

    @ApiModelProperty(value = "权限ID")
    @TableField("permission_id")
    private Long permissionId;

    @ApiModelProperty(value = "创建时间")
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @ApiModelProperty(value = "创建人")
    @TableField("created_by")
    private Long createdBy;
}