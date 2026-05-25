package com.redmind.modules.trending.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("trending_copy_items")
public class TrendingCopyItem {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private String platformCode;
    private String sourceId;
    private String keyword;
    private String title;
    private String contentText;
    private String authorName;
    private String noteUrl;
    private Integer likesCount;
    private Integer collectsCount;
    private Integer commentsCount;
    private Integer heatScore;
    private String tagsJson;
    private String snapshotJson;
    private String analysisJson;
    private LocalDateTime publishedAt;
    private LocalDateTime fetchedAt;
}
