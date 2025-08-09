package com.mengnankk.auth.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "分页响应", description = "分页查询响应结果")
public class PageResponse<T> {

    @ApiModelProperty(value = "数据列表")
    private List<T> records;

    @ApiModelProperty(value = "当前页码")
    private Long current;

    @ApiModelProperty(value = "每页大小")
    private Long size;

    @ApiModelProperty(value = "总记录数")
    private Long total;

    @ApiModelProperty(value = "总页数")
    private Long pages;

    @ApiModelProperty(value = "是否有上一页")
    private Boolean hasPrevious;

    @ApiModelProperty(value = "是否有下一页")
    private Boolean hasNext;
}
