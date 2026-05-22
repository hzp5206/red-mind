package com.redmind.modules.generate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("generation_histories")
public class GenerationHistory {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String coreInput;
    private String style;
    private String persona;
    private Integer wordCount;
    private String results;
    private Boolean isCollected;
    private LocalDateTime createdAt;
}
