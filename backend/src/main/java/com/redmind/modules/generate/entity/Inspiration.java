package com.redmind.modules.generate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("inspirations")
public class Inspiration {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long historyId;
    private String customTags;
    private LocalDateTime createdAt;
}
