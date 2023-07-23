package com.example.hello_there.user;

import lombok.Getter;

@Getter
public enum UserStatus {
    ACTIVE, // 활성화 계정
    INACTIVE // 비활성화(영구정지) 계정
}
