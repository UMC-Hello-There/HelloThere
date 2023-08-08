package com.example.hello_there.sqs.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PostEmailReq {
    private Long receiverId; // 수신자 ID
    private int reportCount; // 누적 신고 횟수
    private String prohibition; // 제재 사항
}
