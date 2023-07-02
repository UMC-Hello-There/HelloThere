package com.example.hello_there.config;

import lombok.Getter;

@Getter
public enum UserStatus { // 비활성화 계정 또는 신고 계정 처리
    ACTIVE, // 활성화 계정
    INACTIVE, // 비활성화 계정
    DISABLE_TO_CHAT, // 3일 채팅 금지
    DISABLE_TO_UPLOAD, // 3일 게시글 업로드 금지
}
