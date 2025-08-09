package com.mengnankk.user.entry;


import com.baomidou.mybatisplus.annotation.TableName;
import com.mengnankk.common.entry.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseEntity {
    private String username;
    private String password;
    private String email;
    private String phone;
    private String nickname;
    private String avatar;
    private Integer gender;
    private LocalDate birthday;
    private String signature;
    private Integer status;
    private LocalDateTime lastLoginTime;
}
