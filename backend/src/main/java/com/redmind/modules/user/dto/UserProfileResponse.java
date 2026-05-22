package com.redmind.modules.user.dto;

import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileResponse {

    private Long id;
    private String email;
    private String nickname;
    private String role;
    private String roleCode;
    private String memberType;
    private Integer dailyGenCount;
    private Integer dailyLimit;
    private Integer remainingCount;
    private Boolean pro;
    private Set<String> permissions;
}
