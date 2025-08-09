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
 * 权限实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("permissions")
@ApiModel(value = "Permission对象", description = "权限信息")
public class Permission {

    @ApiModelProperty(value = "权限ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "权限名称", required = true)
    @NotBlank(message = "权限名称不能为空")
    @Size(max = 100, message = "权限名称长度不能超过100个字符")
    @TableField("permission_name")
    private String permissionName;

    @ApiModelProperty(value = "权限编码", required = true)
    @NotBlank(message = "权限编码不能为空")
    @Size(max = 100, message = "权限编码长度不能超过100个字符")
    @TableField("permission_code")
    private String permissionCode;

    @ApiModelProperty(value = "资源类型: menu-菜单, button-按钮, api-接口")
    @TableField("resource_type")
    private String resourceType;

    @ApiModelProperty(value = "资源路径")
    @TableField("resource_path")
    private String resourcePath;

    @ApiModelProperty(value = "父权限ID")
    @TableField("parent_id")
    private Long parentId;

    @ApiModelProperty(value = "权限描述")
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

    @ApiModelProperty(value = "子权限列表")
    @TableField(exist = false)
    private List<Permission> children;

    // 常量定义
    public static final int STATUS_DISABLED = 0;
    public static final int STATUS_ENABLED = 1;

    public static final String RESOURCE_TYPE_MENU = "menu";
    public static final String RESOURCE_TYPE_BUTTON = "button";
    public static final String RESOURCE_TYPE_API = "api";

    /**
     * 检查权限是否启用
     */
    public int isEnabled() {
        return STATUS_ENABLED;
    }

    /**
     * 检查是否为根权限
     */
    public boolean isRootPermission() {
        return parentId == null || parentId.equals(0L);
    }
}