package com.redmind.modules.trending.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("trending_copy_tasks")
public class TrendingCopyTask {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String taskName;
    private String platformCode;
    private String keywords;
    private Integer fetchLimit;
    private String cronExpr;
    private Boolean enabled;
    private String providerCode;
    private LocalDateTime lastRunAt;
    private Integer lastFetchedCount;
    private String lastStatus;
    private String lastMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
