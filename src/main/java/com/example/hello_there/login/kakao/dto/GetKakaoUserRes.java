package com.example.hello_there.login.kakao.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class GetKakaoUserRes {
    private String email;
    private String nickName;
}
