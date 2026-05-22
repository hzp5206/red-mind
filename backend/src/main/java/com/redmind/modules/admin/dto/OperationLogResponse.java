package com.redmind.modules.admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OperationLogResponse {

    private Long id;
    private Long operatorId;
    private String operatorNickname;
    private String moduleName;
    private String actionName;
    private String targetType;
    private Long targetId;
    private String detailText;
    private String createdAt;
}
