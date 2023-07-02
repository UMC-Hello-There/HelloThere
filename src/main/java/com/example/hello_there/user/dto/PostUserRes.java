package com.example.hello_there.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PostUserRes {
    private Long userId;
    private String nickName;
}