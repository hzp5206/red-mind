package com.redmind.modules.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("operation_logs")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long operatorId;
    private String operatorNickname;
    private String moduleName;
    private String actionName;
    private String targetType;
    private Long targetId;
    private String detailText;
    private LocalDateTime createdAt;
}
