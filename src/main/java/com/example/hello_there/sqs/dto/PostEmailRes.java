package com.example.hello_there.sqs.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PostEmailRes {
    private String receiverEmail;
    private int duration; // 제재 기간
    private String prohibition; // 제재 사항
}
