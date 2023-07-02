package com.example.hello_there.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetUserRes {
    private Long memberId;
    private String nickName;
    private String email;
    private String password;
}