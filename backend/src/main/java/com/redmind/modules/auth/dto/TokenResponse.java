package com.redmind.modules.auth.dto;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenResponse {

    private Long userId;
    private String token;
    private String nickname;
    private String role;
    private String roleCode;
    private Set<String> permissions;
}
