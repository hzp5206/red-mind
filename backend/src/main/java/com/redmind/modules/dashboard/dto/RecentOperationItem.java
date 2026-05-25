package com.redmind.modules.dashboard.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecentOperationItem {

    private Long id;
    private String operatorNickname;
    private String moduleName;
    private String actionName;
    private String detailText;
    private String createdAt;
}
