package com.redmind.modules.trending.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TrendingTaskSaveRequest {

    private Long id;

    @NotBlank(message = "任务名称不能为空")
    private String taskName;

    @NotBlank(message = "平台不能为空")
    private String platformCode;

    @NotBlank(message = "关键词不能为空")
    private String keywords;

    @NotNull(message = "采集数量不能为空")
    @Min(value = 1, message = "采集数量不能少于1")
    @Max(value = 50, message = "采集数量不能超过50")
    private Integer fetchLimit;

    @NotBlank(message = "Cron 表达式不能为空")
    private String cronExpr;

    @NotBlank(message = "Provider 不能为空")
    private String providerCode;

    @NotNull(message = "启用状态不能为空")
    private Boolean enabled;
}
