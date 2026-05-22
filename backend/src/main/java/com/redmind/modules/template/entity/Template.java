package com.redmind.modules.template.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("templates")
public class Template {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String category;
    private String title;
    private String contentExample;
    private String tags;
    private String style;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
