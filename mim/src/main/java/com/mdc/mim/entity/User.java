package com.mdc.mim.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String uid;
    private String passwordDigest; // 密码摘要，基于MD5
    private String nickname;
}
