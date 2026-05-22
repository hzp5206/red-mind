package com.redmind.modules.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("users")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String email;
    private String passwordHash;
    private String nickname;
    private String avatarUrl;
    private String role;
    private String roleCode;
    private String memberType;
    private LocalDateTime memberExpireAt;
    private Integer dailyGenCount;
    private LocalDate lastGenDate;
    private LocalDateTime createdAt;
}
