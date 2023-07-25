package com.example.hello_there.login.naver.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class GetNaverUserRes {
    private String email;
    private String nickName;
}
