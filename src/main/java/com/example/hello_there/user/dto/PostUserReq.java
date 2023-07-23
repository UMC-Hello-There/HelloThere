package com.example.hello_there.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostUserReq {
    private String nickName;
    private String email;
    private String password;
    private String passwordChk; // 비밀번호 확인
}